/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.CommonsLogLogChute;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.FactoryConfiguration;
import org.apache.velocity.tools.config.ToolConfiguration;
import org.apache.velocity.tools.config.ToolboxConfiguration;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.OpenmrsCharacterEscapes;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.*;
import org.openmrs.web.WebConstants;
import org.openmrs.web.filter.initialization.InitializationFilter;
import org.openmrs.web.filter.update.UpdateFilter;
import org.openmrs.web.filter.util.FilterUtil;
import org.openmrs.web.filter.util.LocalizationTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class used when a small wizard is needed before Spring, jsp, etc has been started up.
 *
 * @see UpdateFilter
 * @see InitializationFilter
 */
public abstract class StartupFilter implements Filter {
	
	private static final Logger log = LoggerFactory.getLogger(StartupFilter.class);
	
	protected static VelocityEngine velocityEngine = null;
	
	public static final String AUTO_RUN_OPENMRS = "auto_run_openmrs";
	
	/**
	 * Set by the {@link #init(FilterConfig)} method so that we have access to the current
	 * {@link ServletContext}
	 */
	protected FilterConfig filterConfig = null;
	
	/**
	 * Records errors that will be displayed to the user
	 */
	protected Map<String, Object[]> errors = new HashMap<>();
	
	/**
	 * Messages that will be displayed to the user
	 */
	protected Map<String, Object[]> msgs = new HashMap<>();
	
	/**
	 * Used for configuring tools within velocity toolbox
	 */
	private ToolContext toolContext = null;
	
	/**
	 * The web.xml file sets this {@link StartupFilter} to be the first filter for all requests.
	 *
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
	 *      javax.servlet.FilterChain)
	 */
	@Override
	public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	        throws IOException, ServletException {
		if (skipFilter((HttpServletRequest) request)) {
			chain.doFilter(request, response);
		} else {
			
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			
			String servletPath = httpRequest.getServletPath();
			// for all /images and /initfilter/scripts files, write the path
			// (the "/initfilter" part is needed so that the openmrs_static_context-servlet.xml file doesn't
			//  get instantiated early, before the locale messages are all set up)
			if (servletPath.startsWith("/images") || servletPath.startsWith("/initfilter/scripts")) {
				servletPath = servletPath.replaceFirst("/initfilter", "/WEB-INF/view"); // strip out the /initfilter part
				// writes the actual image file path to the response
				File file = new File(filterConfig.getServletContext().getRealPath(servletPath));
				if (httpRequest.getPathInfo() != null) {
					file = new File(file, httpRequest.getPathInfo());
				}
				
				InputStream imageFileInputStream = null;
				try {
					imageFileInputStream = new FileInputStream(file);
					OpenmrsUtil.copyFile(imageFileInputStream, httpResponse.getOutputStream());
				}
				catch (FileNotFoundException e) {
					log.error("Unable to find file: " + file.getAbsolutePath());
				}
				finally {
					if (imageFileInputStream != null) {
						try {
							imageFileInputStream.close();
						}
						catch (IOException io) {
							log.warn("Couldn't close imageFileInputStream: " + io);
						}
					}
				}
			} else if (servletPath.startsWith("/scripts")) {
				log.error(
				    "Calling /scripts during the initializationfilter pages will cause the openmrs_static_context-servlet.xml to initialize too early and cause errors after startup.  Use '/initfilter"
				            + servletPath + "' instead.");
			}
			// for anything but /initialsetup
			else if (!httpRequest.getServletPath().equals("/" + WebConstants.SETUP_PAGE_URL)
			        && !httpRequest.getServletPath().equals("/" + AUTO_RUN_OPENMRS)) {
				// send the user to the setup page
				httpResponse.sendRedirect("/" + WebConstants.WEBAPP_NAME + "/" + WebConstants.SETUP_PAGE_URL);
			} else {
				
				if ("GET".equals(httpRequest.getMethod())) {
					doGet(httpRequest, httpResponse);
				} else if ("POST".equals(httpRequest.getMethod())) {
					// only clear errors before POSTS so that redirects can show errors too.
					errors.clear();
					msgs.clear();
					doPost(httpRequest, httpResponse);
				}
			}
			// Don't continue down the filter chain otherwise Spring complains
			// that it hasn't been set up yet.
			// The jsp and servlet filter are also on this chain, so writing to
			// the response directly here is the only option
		}
	}
	
	/**
	 * Convenience method to set up the velocity context properly
	 */
	private void initializeVelocity() {
		if (velocityEngine == null) {
			velocityEngine = new VelocityEngine();
			
			Properties props = new Properties();
			props.setProperty(RuntimeConstants.RUNTIME_LOG, "startup_wizard_vel.log");
			// Linux requires setting logging properties to initialize Velocity Context.
			props.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
			    "org.apache.velocity.runtime.log.CommonsLogLogChute");
			props.setProperty(CommonsLogLogChute.LOGCHUTE_COMMONS_LOG_NAME, "initial_wizard_velocity");
			
			// so the vm pages can import the header/footer
			props.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
			props.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
			props.setProperty("class.resource.loader.class",
			    "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			
			try {
				velocityEngine.init(props);
			}
			catch (Exception e) {
				log.error("velocity init failed, because: " + e);
			}
		}
	}
	
	/**
	 * Called by {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} on GET requests
	 *
	 * @param httpRequest
	 * @param httpResponse
	 */
	protected abstract void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
	        throws IOException, ServletException;
	
	/**
	 * Called by {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} on POST requests
	 *
	 * @param httpRequest
	 * @param httpResponse
	 */
	protected abstract void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
	        throws IOException, ServletException;
	
	/**
	 * All private attributes on this class are returned to the template via the velocity context and
	 * reflection
	 *
	 * @param templateName the name of the velocity file to render. This name is prepended with
	 *            {@link #getTemplatePrefix()}
	 * @param referenceMap
	 * @param httpResponse
	 */
	protected void renderTemplate(String templateName, Map<String, Object> referenceMap, HttpServletResponse httpResponse)
	        throws IOException {
		// first we should get velocity tools context for current client request (within
		// his http session) and merge that tools context with basic velocity context
		if (referenceMap == null) {
			return;
		}
		
		Object locale = referenceMap.get(FilterUtil.LOCALE_ATTRIBUTE);
		ToolContext velocityToolContext = getToolContext(
		    locale != null ? locale.toString() : Context.getLocale().toString());
		VelocityContext velocityContext = new VelocityContext(velocityToolContext);
		
		for (Map.Entry<String, Object> entry : referenceMap.entrySet()) {
			velocityContext.put(entry.getKey(), entry.getValue());
		}
		
		Object model = getUpdateFilterModel();
		
		// put each of the private varibles into the template for convenience
		for (Field field : model.getClass().getDeclaredFields()) {
			try {
				field.setAccessible(true);
				velocityContext.put(field.getName(), field.get(model));
			}
			catch (IllegalArgumentException | IllegalAccessException e) {
				log.error("Error generated while getting field value: " + field.getName(), e);
			}
		}
		
		String fullTemplatePath = getTemplatePrefix() + templateName;
		InputStream templateInputStream = getClass().getClassLoader().getResourceAsStream(fullTemplatePath);
		if (templateInputStream == null) {
			throw new IOException("Unable to find " + fullTemplatePath);
		}
		
		velocityContext.put("errors", errors);
		velocityContext.put("msgs", msgs);
		
		// explicitly set the content type for the response because some servlet containers are assuming text/plain
		httpResponse.setContentType("text/html");
		
		try {
			velocityEngine.evaluate(velocityContext, httpResponse.getWriter(), this.getClass().getName(),
			    new InputStreamReader(templateInputStream, StandardCharsets.UTF_8));
		}
		catch (Exception e) {
			throw new APIException("Unable to process template: " + fullTemplatePath, e);
		}
	}
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		initializeVelocity();
	}
	
	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
	}
	
	/**
	 * This string is prepended to all templateNames passed to
	 * {@link #renderTemplate(String, Map, HttpServletResponse)}
	 *
	 * @return string to prepend as the path for the templates
	 */
	protected String getTemplatePrefix() {
		return "org/openmrs/web/filter/";
	}
	
	/**
	 * The model that is used as the backer for all pages in this startup wizard. Should never return
	 * null.
	 *
	 * @return the stored formbacking/model object
	 */
	protected abstract Object getUpdateFilterModel();
	
	/**
	 * If this returns true, this filter fails early and quickly. All logic is skipped and startup and
	 * usage continue normally.
	 *
	 * @return true if this filter can be skipped
	 */
	public abstract boolean skipFilter(HttpServletRequest request);

	/**
	 * Convenience method to read the last 5 log lines from the MemoryAppender
	 * 
	 * The log lines will be added to the "logLines" key
	 * 
	 * @param result A map to be returned as a JSON document
	 */
	protected void addLogLinesToResponse(Map<String, Object> result) {
		MemoryAppender appender = OpenmrsUtil.getMemoryAppender();
		if (appender != null) {
			List<String> logLines = appender.getLogLines();
			
			// truncate the list to the last five so we don't overwhelm jquery
			if (logLines.size() > 5) {
				logLines = logLines.subList(logLines.size() - 5, logLines.size());
			}
			
			result.put("logLines", logLines);
		} else {
			result.put("logLines", Collections.emptyList());
		}
	}
	
	/**
	 * Convenience method to convert the given object to a JSON string. Supports Maps, Lists, Strings,
	 * Boolean, Double
	 *
	 * @param object object to convert to json
	 * @return JSON string to be eval'd in javascript
	 */
	protected String toJSONString(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.getJsonFactory().setCharacterEscapes(new OpenmrsCharacterEscapes());
		try {
			return mapper.writeValueAsString(object);
		}
		catch (IOException e) {
			log.error("Failed to convert object to JSON");
			throw new APIException(e);
		}
	}
	
	/**
	 * Gets tool context for specified locale parameter. If context does not exists, it creates new
	 * context, configured for that locale. Otherwise, it changes locale property of
	 * {@link LocalizationTool} object, that is being contained in tools context
	 *
	 * @param locale the string with locale parameter for configuring tools context
	 * @return the tool context object
	 */
	public ToolContext getToolContext(String locale) {
		Locale systemLocale = LocaleUtility.fromSpecification(locale);
		//Defaults to en if systemLocale is null or invalid e.g en_GBs
		if (systemLocale == null || !ArrayUtils.contains(Locale.getAvailableLocales(), systemLocale)) {
			systemLocale = Locale.ENGLISH;
		}
		// If tool context has not been configured yet
		if (toolContext == null) {
			// first we are creating manager for tools, factory for configuring tools 
			// and empty configuration object for velocity tool box
			ToolManager velocityToolManager = new ToolManager();
			FactoryConfiguration factoryConfig = new FactoryConfiguration();
			// since we are using one tool box for all request within wizard
			// we should propagate toolbox's scope on all application 
			ToolboxConfiguration toolbox = new ToolboxConfiguration();
			toolbox.setScope(Scope.APPLICATION);
			// next we are directly configuring custom localization tool by
			// setting its class name, locale property etc.
			ToolConfiguration localizationTool = new ToolConfiguration();
			localizationTool.setClassname(LocalizationTool.class.getName());
			localizationTool.setProperty(ToolContext.LOCALE_KEY, systemLocale);
			localizationTool.setProperty(LocalizationTool.BUNDLES_KEY, "messages");
			// and finally we are adding just configured tool into toolbox
			// and creating tool context for this toolbox
			toolbox.addTool(localizationTool);
			factoryConfig.addToolbox(toolbox);
			velocityToolManager.configure(factoryConfig);
			toolContext = velocityToolManager.createContext();
			toolContext.setUserCanOverwriteTools(true);
		} else {
			// if it already has been configured, we just pull out our custom localization tool 
			// from tool context, then changing its locale property and putting this tool back to the context
			// First, we need to obtain the value of default key annotation of our localization tool
			// class using reflection
			Annotation annotation = LocalizationTool.class.getAnnotation(DefaultKey.class);
			DefaultKey defaultKeyAnnotation = (DefaultKey) annotation;
			String key = defaultKeyAnnotation.value();
			//
			LocalizationTool localizationTool = (LocalizationTool) toolContext.get(key);
			localizationTool.setLocale(systemLocale);
			toolContext.put(key, localizationTool);
		}
		return toolContext;
	}
}

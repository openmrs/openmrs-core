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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.openmrs.OpenmrsCharacterEscapes;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.logging.MemoryAppender;
import org.openmrs.logging.OpenmrsLoggingUtil;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.Listener;
import org.openmrs.web.WebConstants;
import org.openmrs.web.filter.initialization.InitializationFilter;
import org.openmrs.web.filter.update.UpdateFilter;
import org.openmrs.web.filter.util.FilterUtil;
import org.openmrs.web.filter.util.LocalizationTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

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
	 * Exposed to templates for localizing messages
	 */
	private LocalizationTool localizationTool = null;

	/**
	 * The web.xml file sets this {@link StartupFilter} to be the first filter for all requests.
	 *
	 * @see jakarta.servlet.Filter#doFilter(jakarta.servlet.ServletRequest,
	 *      jakarta.servlet.ServletResponse, jakarta.servlet.FilterChain)
	 */
	@Override
	public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	        throws IOException, ServletException {
		if (((HttpServletRequest) request).getServletPath().equals("/health/started")) {
			((HttpServletResponse) response).setStatus(
			    Listener.isOpenmrsStarted() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		} else if (((HttpServletRequest) request).getServletPath().equals("/health/alive")) {
			if (Listener.isSetupNeeded() && !InitializationFilter.isInstallationStarted()) {
				triggerSetup((HttpServletRequest) request);
			}
			boolean isOpenmrsAlive = Listener.isOpenmrsStarted() || Listener.isSetupNeeded()
			        || InitializationFilter.isInstallationStarted();
			((HttpServletResponse) response)
			        .setStatus(isOpenmrsAlive ? HttpServletResponse.SC_OK : HttpServletResponse.SC_SERVICE_UNAVAILABLE);

		} else if (skipFilter((HttpServletRequest) request)) {
			chain.doFilter(request, response);
		} else {

			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			String servletPath = httpRequest.getServletPath();
			// for all /images and /initfilter/scripts files, write the path
			// (the "/initfilter" part is needed so that the openmrs_static_context-servlet.xml file doesn't
			//  get instantiated early, before the locale messages are all set up)
			if (servletPath.startsWith("/images") || servletPath.startsWith("/initfilter/scripts")) {
				// strip out the /initfilter part
				servletPath = servletPath.replaceFirst("/initfilter", "/WEB-INF/view");
				// writes the actual file path to the response
				Path filePath = Paths.get(filterConfig.getServletContext().getRealPath(servletPath)).normalize();
				Path fullFilePath = filePath;

				if (httpRequest.getPathInfo() != null) {
					fullFilePath = fullFilePath.resolve(httpRequest.getPathInfo());
					if (!(fullFilePath.normalize().startsWith(filePath))) {
						log.warn("Detected attempted directory traversal in request for {}", httpRequest.getPathInfo());
						return;
					}
				}

				String contentType = httpRequest.getServletContext().getMimeType(fullFilePath.toString());
				if (contentType == null || contentType.isEmpty()) {
					try {
						contentType = Files.probeContentType(fullFilePath);
					} catch (IOException ignored) {}
				}

				MediaType mediaType;
				if (contentType != null && !contentType.isEmpty()) {
					mediaType = MediaType.parseMediaType(contentType);
				} else {
					mediaType = MediaType.APPLICATION_OCTET_STREAM;
				}

				response.setContentType(mediaType.toString());

				try (InputStream fis = new FileInputStream(fullFilePath.normalize().toFile())) {
					OpenmrsUtil.copyFile(fis, httpResponse.getOutputStream());
				} catch (FileNotFoundException e) {
					log.error("Unable to find file: {}", filePath, e);
				} catch (IOException e) {
					log.warn("An error occurred while handling file {}", filePath, e);
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
			// so the vm pages can import the header/footer
			props.setProperty(RuntimeConstants.RESOURCE_LOADER, "class");
			props.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
			props.setProperty("class.resource.loader.class",
			    "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

			try {
				velocityEngine.init(props);
			} catch (Exception e) {
				log.error("velocity init failed, because: {}", e, e);
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
		if (referenceMap == null) {
			return;
		}

		Object locale = referenceMap.get(FilterUtil.LOCALE_ATTRIBUTE);
		VelocityContext velocityContext = new VelocityContext();
		velocityContext.put(LocalizationTool.KEY,
		    getLocalizationTool(locale != null ? locale.toString() : Context.getLocale().toString()));

		for (Map.Entry<String, Object> entry : referenceMap.entrySet()) {
			velocityContext.put(entry.getKey(), entry.getValue());
		}

		Object model = getUpdateFilterModel();

		// put each of the private varibles into the template for convenience
		for (Field field : model.getClass().getDeclaredFields()) {
			try {
				field.setAccessible(true);
				velocityContext.put(field.getName(), field.get(model));
			} catch (IllegalArgumentException | IllegalAccessException e) {
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
		} catch (Exception e) {
			throw new APIException("Unable to process template: " + fullTemplatePath, e);
		}
	}

	/**
	 * Makes a request to the root of the application to trigger setup.
	 *
	 * @param request the incoming request
	 */
	private void triggerSetup(HttpServletRequest request) {
		try {
			String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			        + request.getContextPath() + "/";
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(2000); // 2 seconds
			con.setReadTimeout(2000);
			con.setInstanceFollowRedirects(true);

			con.getResponseCode();
		} catch (IOException e) {
			log.error("Health check probe to root path failed", e);
		}
	}

	/**
	 * @see jakarta.servlet.Filter#init(jakarta.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		initializeVelocity();
	}

	/**
	 * @see jakarta.servlet.Filter#destroy()
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
	 * Convenience method to read the last 5 log lines from the MemoryAppender The log lines will be
	 * added to the "logLines" key
	 *
	 * @param result A map to be returned as a JSON document
	 */
	protected void addLogLinesToResponse(Map<String, Object> result) {
		MemoryAppender appender = OpenmrsLoggingUtil.getMemoryAppender();
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
		mapper.getFactory().setCharacterEscapes(new OpenmrsCharacterEscapes());
		try {
			return mapper.writeValueAsString(object);
		} catch (IOException e) {
			log.error("Failed to convert object to JSON");
			throw new APIException(e);
		}
	}

	/**
	 * Gets the localization tool for the specified locale parameter. If the tool does not exist yet, it
	 * is created for that locale. Otherwise, its locale is changed.
	 *
	 * @param locale the string with locale parameter for configuring the localization tool
	 * @return the localization tool
	 */
	public LocalizationTool getLocalizationTool(String locale) {
		Locale systemLocale = LocaleUtility.fromSpecification(locale);
		//Defaults to en if systemLocale is null or invalid e.g en_GBs
		if (systemLocale == null || !ArrayUtils.contains(Locale.getAvailableLocales(), systemLocale)) {
			systemLocale = Locale.ENGLISH;
		}
		if (localizationTool == null) {
			localizationTool = new LocalizationTool(systemLocale);
		} else {
			localizationTool.setLocale(systemLocale);
		}
		return localizationTool;
	}
}

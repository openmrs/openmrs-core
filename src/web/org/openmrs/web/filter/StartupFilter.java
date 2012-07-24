/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.CommonsLogLogChute;
import org.directwebremoting.util.JavascriptUtil;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.WebConstants;
import org.openmrs.web.filter.initialization.InitializationFilter;
import org.openmrs.web.filter.update.UpdateFilter;

/**
 * Abstract class used when a small wizard is needed before Spring, jsp, etc has been started up.
 * 
 * @see UpdateFilter
 * @see InitializationFilter
 */
public abstract class StartupFilter implements Filter {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	protected static VelocityEngine velocityEngine = null;
	
	/**
	 * Set by the {@link #init(FilterConfig)} method so that we have access to the current
	 * {@link ServletContext}
	 */
	protected FilterConfig filterConfig = null;
	
	/**
	 * Records errors that will be displayed to the user
	 */
	protected List<String> errors = new ArrayList<String>();
	
	/**
	 * The web.xml file sets this {@link StartupFilter} to be the first filter for all requests.
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	                                                                                               ServletException {
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
				if (httpRequest.getPathInfo() != null)
					file = new File(file, httpRequest.getPathInfo());
				
				try {
					InputStream imageFileInputStream = new FileInputStream(file);
					OpenmrsUtil.copyFile(imageFileInputStream, httpResponse.getOutputStream());
					imageFileInputStream.close();
				}
				catch (FileNotFoundException e) {
					log.error("Unable to find file: " + file.getAbsolutePath());
				}
			} else if (servletPath.startsWith("/scripts")) {
				log
				        .error("Calling /scripts during the initializationfilter pages will cause the openmrs_static_context-servlet.xml to initialize too early and cause errors after startup.  Use '/initfilter"
				                + servletPath + "' instead.");
			}
			// for anything but /initialsetup
			else if (!httpRequest.getServletPath().equals("/" + WebConstants.SETUP_PAGE_URL)) {
				// send the user to the setup page 
				httpResponse.sendRedirect("/" + WebConstants.WEBAPP_NAME + "/" + WebConstants.SETUP_PAGE_URL);
			} else {
				
				if (httpRequest.getMethod().equals("GET")) {
					doGet(httpRequest, httpResponse);
				} else if (httpRequest.getMethod().equals("POST")) {
					// only clear errors before POSTS so that redirects can show errors too.
					errors.clear();
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
            props.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                "org.apache.velocity.runtime.log.CommonsLogLogChute" );
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
	protected abstract void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException,
	                                                                                               ServletException;
	
	/**
	 * Called by {@link #doFilter(ServletRequest, ServletResponse, FilterChain)} on POST requests
	 * 
	 * @param httpRequest
	 * @param httpResponse
	 * @throws Exception
	 */
	protected abstract void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException,
	                                                                                                ServletException;
	
	/**
	 * All private attributes on this class are returned to the template via the velocity context
	 * and reflection
	 * 
	 * @param templateName the name of the velocity file to render. This name is prepended with
	 *            {@link #getTemplatePrefix()}
	 * @param referenceMap
	 * @param writer
	 */
	protected void renderTemplate(String templateName, Map<String, Object> referenceMap, HttpServletResponse httpResponse)
	                                                                                                                      throws IOException {
		
		VelocityContext velocityContext = new VelocityContext();
		
		if (referenceMap != null) {
			for (Map.Entry<String, Object> entry : referenceMap.entrySet()) {
				velocityContext.put(entry.getKey(), entry.getValue());
			}
		}
		
		Object model = getModel();
		
		// put each of the private varibles into the template for convenience
		for (Field field : model.getClass().getDeclaredFields()) {
			try {
				velocityContext.put(field.getName(), field.get(model));
			}
			catch (IllegalArgumentException e) {
				log.error("Error generated while getting field value: " + field.getName(), e);
			}
			catch (IllegalAccessException e) {
				log.error("Error generated while getting field value: " + field.getName(), e);
			}
		}
		
		String fullTemplatePath = getTemplatePrefix() + templateName;
		InputStream templateInputStream = getClass().getClassLoader().getResourceAsStream(fullTemplatePath);
		if (templateInputStream == null) {
			throw new IOException("Unable to find " + fullTemplatePath);
		}
		
		velocityContext.put("errors", errors);
		
		// explicitly set the content type for the response because some servlet containers are assuming text/plain
		httpResponse.setContentType("text/html");
		
		try {
			velocityEngine.evaluate(velocityContext, httpResponse.getWriter(), this.getClass().getName(),
			    new InputStreamReader(templateInputStream));
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to process template: " + fullTemplatePath, e);
		}
	}
	
	/**
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
		initializeVelocity();
	}
	
	/**
	 * @see javax.servlet.Filter#destroy()
	 */
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
	 * The model that is used as the backer for all pages in this startup wizard. Should never
	 * return null.
	 * 
	 * @return the stored formbacking/model object
	 */
	protected abstract Object getModel();
	
	/**
	 * If this returns true, this filter fails early and quickly. All logic is skipped and startup
	 * and usage continue normally.
	 * 
	 * @return true if this filter can be skipped
	 */
	public abstract boolean skipFilter(HttpServletRequest request);
	
	/**
	 * Convert a map of strings to objects to json
	 * 
	 * @param map object to convert
	 * @param sb StringBuffer to append to
	 */
	private void toJSONString(Map<String, Object> map, StringBuffer sb) {
		boolean first = true;
		
		sb.append('{');
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (first)
				first = false;
			else
				sb.append(',');
			
			sb.append('"');
			if (entry.getKey() == null)
				sb.append("null");
			else
				sb.append(JavascriptUtil.escapeJavaScript(entry.getKey()));
			sb.append('"').append(':');
			
			sb.append(toJSONString(entry.getValue()));
			
		}
		sb.append('}');
	}
	
	/**
	 * Convert a list of objects to json
	 * 
	 * @param list object to convert
	 * @param sb StringBuffer to append to
	 */
	private void toJSONString(List<Object> list, StringBuffer sb) {
		boolean first = true;
		
		sb.append('[');
		for (Object listItem : list) {
			if (first)
				first = false;
			else
				sb.append(',');
			
			sb.append(toJSONString(listItem));
		}
		sb.append(']');
	}
	
	/**
	 * Convert all other objects to json
	 * 
	 * @param object object to convert
	 * @param sb StringBuffer to append to
	 */
	private void toJSONString(Object object, StringBuffer sb) {
		if (object == null)
			sb.append("null");
		else
			sb.append('"').append(JavascriptUtil.escapeJavaScript(object.toString())).append('"');
	}
	
	/**
	 * Convenience method to convert the given object to a JSON string. Supports Maps, Lists,
	 * Strings, Boolean, Double
	 * 
	 * @param object object to convert to json
	 * @return JSON string to be eval'd in javascript
	 */
	protected String toJSONString(Object object) {
		StringBuffer sb = new StringBuffer();
		
		if (object instanceof Map)
			toJSONString((Map<String, Object>) object, sb);
		else if (object instanceof List)
			toJSONString((List) object, sb);
		else if (object instanceof Boolean)
			sb.append(object.toString());
		else
			toJSONString(object, sb);
		
		return sb.toString();
	}
}

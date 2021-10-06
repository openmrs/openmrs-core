/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.jasper.Constants;
import org.apache.jasper.EmbeddedServletOptions;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.compiler.Localizer;
import org.apache.jasper.compiler.TldCache;
import org.apache.jasper.runtime.ExceptionUtils;
import org.apache.jasper.security.SecurityUtil;
import org.apache.jasper.servlet.JspServletWrapper;
import org.apache.jasper.servlet.TldScanner;
import org.apache.tomcat.PeriodicEventListener;
import org.apache.tomcat.util.security.Escape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

public class OpenmrsJspServlet extends HttpServlet implements PeriodicEventListener {
	
	private static final Logger log = LoggerFactory.getLogger(OpenmrsJspServlet.class);

	private transient ServletContext context;
	private transient ServletConfig config;
	private transient EmbeddedServletOptions options;
	private transient JspRuntimeContext rctxt;
	private String jspFile;
	
	private transient boolean tldScanComplete = false;

	/*
	 * Initializes this JspServlet.
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.config = config;
		this.context = config.getServletContext();
		this.options = new EmbeddedServletOptions(config, context);
		this.rctxt = new JspRuntimeContext(context, options);
		setJspFile(config.getServletContext());
	}
	
	protected void setJspFile(ServletContext context) throws ServletException {
		if (config.getInitParameter("jspFile") != null) {
			jspFile = config.getInitParameter("jspFile");
			try {
				if (null == context.getResource(jspFile)) {
					return;
				}
			} catch (MalformedURLException e) {
				throw new ServletException("cannot locate jsp file", e);
			}
			try {
				if (SecurityUtil.isPackageProtectionEnabled()){
					AccessController.doPrivileged(new PrivilegedExceptionAction<Object>(){
						@Override
						public Object run() throws IOException, ServletException {
							serviceJspFile(null, null, jspFile, true);
							return null;
						}
					});
				} else {
					serviceJspFile(null, null, jspFile, true);
				}
			} catch (IOException e) {
				throw new ServletException("Could not precompile jsp: " + jspFile, e);
			} catch (PrivilegedActionException e) {
				Throwable t = e.getCause();
				if (t instanceof ServletException) {
					throw (ServletException)t;
				}
				throw new ServletException("Could not precompile jsp: " + jspFile, e);
			}
		}
	}

	/**
	 * Returns the number of JSPs for which JspServletWrappers exist, i.e.,
	 * the number of JSPs that have been loaded into the webapp with which
	 * this JspServlet is associated.
	 *
	 * <p>This info may be used for monitoring purposes.
	 *
	 * @return The number of JSPs that have been loaded into the webapp with
	 * which this JspServlet is associated
	 */
	public int getJspCount() {
		return this.rctxt.getJspCount();
	}


	/**
	 * Resets the JSP reload counter.
	 *
	 * @param count Value to which to reset the JSP reload counter
	 */
	public void setJspReloadCount(int count) {
		this.rctxt.setJspReloadCount(count);
	}


	/**
	 * Gets the number of JSPs that have been reloaded.
	 *
	 * <p>This info may be used for monitoring purposes.
	 *
	 * @return The number of JSPs (in the webapp with which this JspServlet is
	 * associated) that have been reloaded
	 */
	public int getJspReloadCount() {
		return this.rctxt.getJspReloadCount();
	}


	/**
	 * Gets the number of JSPs that are in the JSP limiter queue
	 *
	 * <p>This info may be used for monitoring purposes.
	 *
	 * @return The number of JSPs (in the webapp with which this JspServlet is
	 * associated) that are in the JSP limiter queue
	 */
	public int getJspQueueLength() {
		return this.rctxt.getJspQueueLength();
	}


	/**
	 * Gets the number of JSPs that have been unloaded.
	 *
	 * <p>This info may be used for monitoring purposes.
	 *
	 * @return The number of JSPs (in the webapp with which this JspServlet is
	 * associated) that have been unloaded
	 */
	public int getJspUnloadCount() {
		return this.rctxt.getJspUnloadCount();
	}


	/**
	 * <p>Look for a <em>precompilation request</em> as described in
	 * Section 8.4.2 of the JSP 1.2 Specification.  <strong>WARNING</strong> -
	 * we cannot use <code>request.getParameter()</code> for this, because
	 * that will trigger parsing all of the request parameters, and not give
	 * a servlet the opportunity to call
	 * <code>request.setCharacterEncoding()</code> first.</p>
	 *
	 * @param request The servlet request we are processing
	 *
	 * @exception ServletException if an invalid parameter value for the
	 *  <code>jsp_precompile</code> parameter name is specified
	 */
	boolean preCompile(HttpServletRequest request) throws ServletException {
		String queryString = request.getQueryString();
		if (queryString == null) {
			return false;
		}
		int start = queryString.indexOf(Constants.PRECOMPILE);
		if (start < 0) {
			return false;
		}
		queryString =
			queryString.substring(start + Constants.PRECOMPILE.length());
		if (queryString.length() == 0) {
			return true;             // ?jsp_precompile
		}
		if (queryString.startsWith("&")) {
			return true;             // ?jsp_precompile&foo=bar...
		}
		if (!queryString.startsWith("=")) {
			return false;            // part of some other name or value
		}
		int limit = queryString.length();
		int ampersand = queryString.indexOf('&');
		if (ampersand > 0) {
			limit = ampersand;
		}
		String value = queryString.substring(1, limit);
		if (value.equals("true")) {
			return true;             // ?jsp_precompile=true
		} else if (value.equals("false")) {
			// Spec says if jsp_precompile=false, the request should not
			// be delivered to the JSP page; the easiest way to implement
			// this is to set the flag to true, and precompile the page anyway.
			// This still conforms to the spec, since it says the
			// precompilation request can be ignored.
			return true;             // ?jsp_precompile=false
		} else {
			throw new ServletException("Cannot have request parameter " +
				Constants.PRECOMPILE + " set to " +
				value);
		}
	}
	
	@Override
	public void service (HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		log.warn("OpenmrsJspServlet.service: " + request.getRequestURI());
		scanTldsIfNeeded();
		
		// jspFile may be configured as an init-param for this servlet instance
		String jspUri = jspFile;

		if (jspUri == null) {
			/*
			 * Check to see if the requested JSP has been the target of a
			 * RequestDispatcher.include()
			 */
			jspUri = (String) request.getAttribute(
				RequestDispatcher.INCLUDE_SERVLET_PATH);
			if (jspUri != null) {
				/*
				 * Requested JSP has been target of
				 * RequestDispatcher.include(). Its path is assembled from the
				 * relevant javax.servlet.include.* request attributes
				 */
				String pathInfo = (String) request.getAttribute(
					RequestDispatcher.INCLUDE_PATH_INFO);
				if (pathInfo != null) {
					jspUri += pathInfo;
				}
			} else {
				/*
				 * Requested JSP has not been the target of a
				 * RequestDispatcher.include(). Reconstruct its path from the
				 * request's getServletPath() and getPathInfo()
				 */
				jspUri = request.getServletPath();
				String pathInfo = request.getPathInfo();
				if (pathInfo != null) {
					jspUri += pathInfo;
				}
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("JspEngine --> " + jspUri);
			log.debug("\t     ServletPath: " + request.getServletPath());
			log.debug("\t        PathInfo: " + request.getPathInfo());
			log.debug("\t        RealPath: " + context.getRealPath(jspUri));
			log.debug("\t      RequestURI: " + request.getRequestURI());
			log.debug("\t     QueryString: " + request.getQueryString());
		}

		try {
			boolean precompile = preCompile(request);
			serviceJspFile(request, response, jspUri, precompile);
		} catch (RuntimeException | IOException | ServletException e) {
			throw e;
		} catch (Throwable e) {
			ExceptionUtils.handleThrowable(e);
			throw new ServletException(e);
		}

	}

	@Override
	public void destroy() {
		if (log.isDebugEnabled()) {
			log.debug("JspServlet.destroy()");
		}

		rctxt.destroy();
	}


	@Override
	public void periodicEvent() {
		rctxt.checkUnload();
		rctxt.checkCompile();
	}

	// -------------------------------------------------------- Private Methods

	private void serviceJspFile(HttpServletRequest request,
								HttpServletResponse response, String jspUri,
								boolean precompile)
		throws ServletException, IOException {

		JspServletWrapper wrapper = rctxt.getWrapper(jspUri);
		if (wrapper == null) {
			synchronized(this) {
				wrapper = rctxt.getWrapper(jspUri);
				if (wrapper == null) {
					// Check if the requested JSP page exists, to avoid
					// creating unnecessary directories and files.
					if (null == context.getResource(jspUri)) {
						handleMissingResource(request, response, jspUri);
						return;
					}
					wrapper = new JspServletWrapper(config, options, jspUri,
						rctxt);
					rctxt.addWrapper(jspUri,wrapper);
				}
			}
		}

		try {
			wrapper.service(request, response, precompile);
		} catch (FileNotFoundException fnfe) {
			handleMissingResource(request, response, jspUri);
		}

	}


	private void handleMissingResource(HttpServletRequest request,
									   HttpServletResponse response, String jspUri)
		throws ServletException, IOException {

		String includeRequestUri =
			(String)request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI);

		String msg = Localizer.getMessage("jsp.error.file.not.found",jspUri);
		if (includeRequestUri != null) {
			// This file was included. Throw an exception as
			// a response.sendError() will be ignored
			// Strictly, filtering this is an application
			// responsibility but just in case...
			throw new ServletException(Escape.htmlElementContent(msg));
		} else {
			try {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, msg);
			} catch (IllegalStateException ise) {
				log.error(msg);
			}
		}
	}

	protected void scanTldsIfNeeded() throws ServletException {
		if (!tldScanComplete) {
			log.warn("TLD Scanning has not yet been done via OpenmrsJasperServlet.  Initiating now.");
			boolean namespaceAware = true;
			boolean validate = getBooleanParameter(Constants.XML_VALIDATION_TLD_INIT_PARAM, false);
			boolean blockExternalString = getBooleanParameter(Constants.XML_BLOCK_EXTERNAL_INIT_PARAM, true);
			TldScanner scanner = new TldScanner(getServletContext(), namespaceAware, validate, blockExternalString);
			try {
				scanner.scan();
			}
			catch (IOException | SAXException e) {
				throw new ServletException(e);
			}
			// add any listeners defined in TLDs
			for (String listener : scanner.getListeners()) {
				getServletContext().addListener(listener);
			}

			TldCache tldCache = new TldCache(getServletContext(), scanner.getUriTldResourcePathMap(), scanner.getTldResourcePathTaglibXmlMap());
			getServletContext().setAttribute(TldCache.SERVLET_CONTEXT_ATTRIBUTE_NAME, tldCache);
			options.setTldCache(tldCache);

			log.warn("Scanning completed successfully");
			tldScanComplete = true;
		}
	}

	private boolean getBooleanParameter(String parameter, boolean defaultValue) {
		String val = getServletContext().getInitParameter(parameter);
		if (StringUtils.isNotBlank(val)) {
			return Boolean.parseBoolean(val);
		}
		return defaultValue;
	}

}

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
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.jasper.Constants;
import org.apache.jasper.EmbeddedServletOptions;
import org.apache.jasper.Options;
import org.apache.jasper.compiler.TldCache;
import org.apache.jasper.servlet.JspServlet;
import org.apache.jasper.servlet.TldScanner;
import org.openmrs.util.OpenmrsClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The purpose of this class is to provide a custom JspServlet implementation that accounts for TLD files introduced
 * by OpenMRS modules.  From Tomcat 8 onward, a change to the Tomcat TLD processing necessitates this step.
 * See:  https://issues.openmrs.org/browse/LUI-169
 */
public class OpenmrsJspServlet extends JspServlet {
	
	private static final Logger log = LoggerFactory.getLogger(OpenmrsJspServlet.class);
	
	public static final String OPENMRS_TLD_SCAN_NEEDED = "OPENMRS_TLD_SCAN_NEEDED";

	@Override
	public void init(ServletConfig config) throws ServletException {
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		super.init(config);
	}
	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		rescanTldsIfNeeded();
		super.service(request, response);
	}

	protected synchronized void rescanTldsIfNeeded() throws ServletException {
		if (getBooleanAttribute(OPENMRS_TLD_SCAN_NEEDED, true)) {
			log.warn("Rescanning TLDs");
			boolean namespaceAware = true;
			boolean validate = getBooleanParameter(Constants.XML_VALIDATION_TLD_INIT_PARAM, false);
			boolean blockExternalString = getBooleanParameter(Constants.XML_BLOCK_EXTERNAL_INIT_PARAM, true);
			try {
				TldScanner scanner = new TldScanner(getServletContext(), namespaceAware, validate, blockExternalString);
				try {
					scanner.scan();
				} catch (IOException | SAXException e) {
					throw new ServletException(e);
				}
				// add any listeners defined in TLDs
				for (String listener : scanner.getListeners()) {
					getServletContext().addListener(listener);
				}

				TldCache tldCache = new TldCache(getServletContext(), scanner.getUriTldResourcePathMap(), scanner.getTldResourcePathTaglibXmlMap());
				getServletContext().setAttribute(TldCache.SERVLET_CONTEXT_ATTRIBUTE_NAME, tldCache);
				log.info("TldCache updated on ServletContext");
				try {
					Options options = (Options) FieldUtils.readField(this, "options", true);
					if (options instanceof EmbeddedServletOptions) {
						EmbeddedServletOptions embeddedServletOptions = (EmbeddedServletOptions) options;
						embeddedServletOptions.setTldCache(tldCache);
						log.info("TldCache updated on JspServlet");
					}
				} catch (IllegalAccessException e) {
					throw new ServletException("Unable to set TldCache on JspServlet options", e);
				}
			} catch (NoClassDefFoundError e) {
				/*
					If we hit a NoClassDefFoundError, assume this means that we are operating in a Non-Tomcat
					environment, or we are in a version of Tomcat 7 or before, which does not require this additional
					TLD Scanning Steps.  Proceed with startup.
				 */
				log.debug("Got NoClassDefFound error, skipping additional TLD scanning step");
			} finally {
				log.info("Scanning completed successfully");
				getServletContext().setAttribute(OPENMRS_TLD_SCAN_NEEDED, false);
			}
		}
	}

	private boolean getBooleanParameter(String parameter, boolean defaultValue) {
		String val = getServletContext().getInitParameter(parameter);
		if (StringUtils.isNotBlank(val)) {
			return Boolean.parseBoolean(val);
		}
		return defaultValue;
	}

	private boolean getBooleanAttribute(String attribute, boolean defaultValue) {
		Boolean val = (Boolean)getServletContext().getAttribute(attribute);
		if (val != null) {
			return val;
		}
		return defaultValue;
	}
}

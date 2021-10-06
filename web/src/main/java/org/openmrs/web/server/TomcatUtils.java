/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.server;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;

import org.apache.jasper.compiler.TldCache;
import org.apache.jasper.servlet.TldScanner;
import org.xml.sax.SAXException;

public class TomcatUtils {
	
	public static boolean isTomcat(ServletContext servletContext) {
		return servletContext.getClassLoader().getClass().toString().startsWith("org.apache.catalina");
	}

	public static void loadModuleTlds(ServletContext servletContext) throws ServletException {
		TldScanner scanner = new TldScanner(servletContext, true, true, true);
		try {
			scanner.scan();
		} catch (IOException | SAXException e) {
			throw new ServletException(e);
		}

		servletContext.setAttribute(TldCache.SERVLET_CONTEXT_ATTRIBUTE_NAME,
			new TldCache(servletContext, scanner.getUriTldResourcePathMap(),
				scanner.getTldResourcePathTaglibXmlMap()));
	}
}

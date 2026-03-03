/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.dtd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class DtdTestValidator {
	
	private static final Logger log = LoggerFactory.getLogger(DtdTestValidator.class);
	
	private DtdTestValidator() {
	}
	
	public static boolean isValidConfigXml(InputStream xml) {
		try {
			DocumentBuilderFactory domFactory;
			DocumentBuilder builder;
			domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setValidating(true);
			builder = domFactory.newDocumentBuilder();
			final boolean[] isValidConfig = { true };
			
			builder.setErrorHandler(new ErrorHandler() {
				
				@Override
				public void warning(SAXParseException e) {
					isValidConfig[0] = true;
					
				}
				
				@Override
				public void error(SAXParseException e) {
					e.printStackTrace();
					isValidConfig[0] = false;
				}
				
				@Override
				public void fatalError(SAXParseException e) {
					e.printStackTrace();
					isValidConfig[0] = false;
				}
			});
			builder.parse(xml);
			return isValidConfig[0];
		}
		catch (SAXException | IOException | ParserConfigurationException e) {
			log.error("Failure reason: ", e);
			return false;
		}
	}
}

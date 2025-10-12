/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import org.openmrs.api.APIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringReader;

public final class XmlUtils {
	
	private static final Logger log = LoggerFactory.getLogger(XmlUtils.class);
	
	private XmlUtils() {
	}
	
	public static DocumentBuilder createDocumentBuilder() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			// Harden parser against XXE and DTD attacks
			// Basically see here: https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#jaxp-documentbuilderfactory-saxparserfactory-and-dom4j
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
			dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			dbf.setXIncludeAware(false);
			dbf.setExpandEntityReferences(false);

			DocumentBuilder db = dbf.newDocumentBuilder();
			
			// Disable resolution of external entities. See TRUNK-3942 
			db.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
			
			return db;
		} catch (ParserConfigurationException e) {
			log.error("ParserConfigurationException thrown while configuring DocumentBuilder", e);
			throw new APIException("ParserConfigurationException thrown while configuring DocumentBuilder", e);
		}
	}
}

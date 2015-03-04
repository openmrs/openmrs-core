/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class ReportObjectXMLDecoder {
	
	//private Log log = LogFactory.getLog(this.getClass());
	
	private String xmlToDecode;
	
	public ReportObjectXMLDecoder(String xmlToDecode) {
		this.xmlToDecode = xmlToDecode;
	}
	
	public AbstractReportObject toAbstractReportObject() {
		ExceptionListener exListener = new ReportObjectWrapperExceptionListener();
		XMLDecoder dec = new XMLDecoder(new BufferedInputStream(new ByteArrayInputStream(xmlToDecode.getBytes())), null,
		        exListener);
		AbstractReportObject o = (AbstractReportObject) dec.readObject();
		dec.close();
		
		return o;
	}
	
	/**
	 * @return Returns the xmlToDecode.
	 */
	public String getXmlToDecode() {
		return xmlToDecode;
	}
	
	/**
	 * @param xmlToDecode The xmlToDecode to set.
	 */
	public void setXmlToDecode(String xmlToDecode) {
		this.xmlToDecode = xmlToDecode;
	}
	
}

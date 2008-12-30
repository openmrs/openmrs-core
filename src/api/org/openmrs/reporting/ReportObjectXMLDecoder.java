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
package org.openmrs.reporting;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

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

package org.openmrs.reporting;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

public class ReportObjectXMLDecoder {

	private String xmlToDecode;
	
	public ReportObjectXMLDecoder ( String xmlToDecode ) {
		this.xmlToDecode = xmlToDecode;
	}
	
	public AbstractReportObject toAbstractReportObject() {
		XMLDecoder dec = new XMLDecoder(new BufferedInputStream(new ByteArrayInputStream(xmlToDecode.getBytes())));
	    return (AbstractReportObject)dec.readObject();
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

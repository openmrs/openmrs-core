package org.openmrs.reporting;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

public class ReportObjectXMLDecoder {

	//private Log log = LogFactory.getLog(this.getClass());
	
	private String xmlToDecode;
	
	public ReportObjectXMLDecoder ( String xmlToDecode ) {
		this.xmlToDecode = xmlToDecode;
	}
	
	public AbstractReportObject toAbstractReportObject() {
		ExceptionListener exListener = new ReportObjectWrapperExceptionListener();
		XMLDecoder dec = new XMLDecoder(new BufferedInputStream(new ByteArrayInputStream(xmlToDecode.getBytes())), null, exListener);
		AbstractReportObject o = (AbstractReportObject)dec.readObject();
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

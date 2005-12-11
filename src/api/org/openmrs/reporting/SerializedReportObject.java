package org.openmrs.reporting;

public class SerializedReportObject extends AbstractReportObject {

	private String xml;
	
	public SerializedReportObject() { }
	
	public void setXml(String xml) {
		this.xml = xml;
	}
	
	public String getXml() {
		return xml;
	}
	
}

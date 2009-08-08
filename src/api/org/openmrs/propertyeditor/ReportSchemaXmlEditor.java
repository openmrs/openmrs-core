package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.ReportService;
import org.openmrs.api.context.Context;
import org.openmrs.report.ReportSchemaXml;
import org.springframework.util.StringUtils;

public class ReportSchemaXmlEditor extends PropertyEditorSupport {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public ReportSchemaXmlEditor() {
	}
	
	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			try {
				ReportService rs = (ReportService) Context.getService(ReportService.class);
				setValue(rs.getReportSchemaXml(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("ReportSchemaXml not found: " + ex.getMessage());
			}
		} else {
			setValue(null);
		}
	}
	
	public String getAsText() {
		ReportSchemaXml rsx = (ReportSchemaXml) getValue();
		if (rsx == null || rsx.getReportSchemaId() == null) {
			return "";
		} else {
			return rsx.getReportSchemaId().toString();
		}
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.ReportService;
import org.openmrs.api.context.Context;
import org.openmrs.report.ReportSchemaXml;
import org.springframework.util.StringUtils;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
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

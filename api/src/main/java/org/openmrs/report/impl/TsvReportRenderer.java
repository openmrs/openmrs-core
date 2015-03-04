/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.report.impl;

import org.openmrs.report.ReportSchema;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class TsvReportRenderer extends DelimitedTextReportRenderer {
	
	public TsvReportRenderer() {
	}
	
	public String getLabel() {
		return "TSV";
	}
	
	public String getFilenameExtension() {
		return "tsv";
	}
	
	public String getBeforeColumnDelimiter() {
		return "\"";
	}
	
	public String getAfterColumnDelimiter() {
		return "\"\t";
	}
	
	public String getBeforeRowDelimiter() {
		return "";
	}
	
	public String getAfterRowDelimiter() {
		return "\n";
	}
	
	public String escape(String text) {
		if (text == null) {
			return null;
		} else {
			return text.replaceAll("\"", "\\\"");
		}
	}
	
	/**
	 * @see org.openmrs.report.ReportRenderer#getLabel()
	 */
	public String getLabel(ReportSchema model) {
		return "TSV";
	}
	
	/**
	 * @see org.openmrs.report.ReportRenderer#getRenderedContentType(ReportSchema, String)
	 */
	public String getRenderedContentType(ReportSchema model, String argument) {
		return "text/tsv";
	}
	
}

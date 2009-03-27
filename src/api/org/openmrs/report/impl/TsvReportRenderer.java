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
package org.openmrs.report.impl;

import org.openmrs.report.ReportSchema;

/**
 *
 */
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
		if (text == null)
			return null;
		else
			return text.replaceAll("\"", "\\\"");
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

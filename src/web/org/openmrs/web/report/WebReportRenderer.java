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
package org.openmrs.web.report;

import org.openmrs.report.ReportRenderer;
import org.openmrs.report.ReportSchema;

/**
 * Renders report schemas for the web. The web renderer can render using the render method, but will
 * most likely be used to redirect to another URL (which, in most cases, delegates to another
 * rendering engine).
 */
public interface WebReportRenderer extends ReportRenderer {
	
	/**
	 * If this method returns a value, then this renderer should be called by redirecting to that
	 * link, rather than with the render(ReportData, OutputStream) method. In this situation, the
	 * ReportData to be displayed will be passed to that page via the session attribute called
	 * WebConstants.OPENMRS_REPORT_DATA
	 * 
	 * @param schema
	 * @return
	 */
	public String getLinkUrl(ReportSchema schema);
	
}

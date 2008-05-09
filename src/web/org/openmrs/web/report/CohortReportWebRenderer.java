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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;

import org.openmrs.report.CohortDataSetDefinition;
import org.openmrs.report.RenderingMode;
import org.openmrs.report.ReportData;
import org.openmrs.report.ReportRenderingException;
import org.openmrs.report.ReportSchema;

/**
 * A ReportRenderer that provides a dynamic web view of a CohortDataSet.
 * This renderer can only handle reports with a single data set, that's a cohort data set.
 */
public class CohortReportWebRenderer implements WebReportRenderer {
	
	public CohortReportWebRenderer() { }

	/**
	 * @see org.openmrs.report.ReportRenderer#getRenderingModes(org.openmrs.report.ReportSchema)
	 */
	public Collection<RenderingMode> getRenderingModes(ReportSchema schema) {
	    if (schema.getDataSetDefinitions().size() == 1 &&
				schema.getDataSetDefinitions().get(0) instanceof CohortDataSetDefinition) {
	    	return Collections.singleton(new RenderingMode(this, this.getLabel(), null, 100));
	    } else {
	    	return null;
	    }
    }

	/**
	 * @see org.openmrs.report.ReportRenderer#getLabel(org.openmrs.report.ReportSchema)
	 */
	public String getLabel() {
		return "Cohort report web preview";
	}

	/**
	 * @see org.openmrs.report.ReportRenderer#getLinkUrl(org.openmrs.report.ReportSchema)
	 */
	public String getLinkUrl(ReportSchema schema) {
		return "admin/reports/reportData.form";
	}

	/**
	 * @see org.openmrs.report.ReportRenderer#getRenderedContentType(org.openmrs.report.ReportSchema)
	 */
	public String getRenderedContentType(ReportSchema schema, String argument) {
		return "text/html";
	}

	/**
	 * @see org.openmrs.report.ReportRenderer#getFilename(org.openmrs.report.ReportSchema)
	 */
	public String getFilename(ReportSchema schema, String argument) {
	    return null;
    }

	/**
	 * @see org.openmrs.report.ReportRenderer#render(org.openmrs.report.ReportData, java.io.OutputStream)
	 */
	public void render(ReportData reportData, String argument, OutputStream out)
	        throws ReportRenderingException {
		// Do nothing. This renderer returns a value from getLinkUrl() 
	}

	/**
     * @see org.openmrs.report.ReportRenderer#render(org.openmrs.report.ReportData, java.lang.String, java.io.Writer)
     */
    public void render(ReportData reportData, String argument, Writer writer)
            throws IOException, ReportRenderingException {
	    // Do nothing.  This renderer returns a value from getLinkUrl()
	    
    }
	
}

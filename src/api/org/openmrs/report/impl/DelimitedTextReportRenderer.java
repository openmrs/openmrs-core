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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openmrs.Cohort;
import org.openmrs.report.CohortDataSet;
import org.openmrs.report.DataSet;
import org.openmrs.report.RenderingException;
import org.openmrs.report.RenderingMode;
import org.openmrs.report.ReportData;
import org.openmrs.report.ReportRenderer;
import org.openmrs.report.ReportSchema;

/**
 *
 */
public abstract class DelimitedTextReportRenderer implements ReportRenderer {
	
	public abstract String getFilenameExtension();
	
	public abstract String getBeforeColumnDelimiter();
	
	public abstract String getAfterColumnDelimiter();
	
	public abstract String getBeforeRowDelimiter();
	
	public abstract String getAfterRowDelimiter();
	
	public abstract String escape(String text);
	
	/**
	 * @see org.openmrs.report.ReportRenderer#getLinkUrl(org.openmrs.report.ReportSchema)
	 */
	public String getLinkUrl(ReportSchema schema) {
		return null;
	}
	
	/**
	 * @see org.openmrs.report.ReportRenderer#getFilename(org.openmrs.report.ReportSchema)
	 */
	public String getFilename(ReportSchema schema, String argument) {
		return schema.getName() + "." + getFilenameExtension();
	}
	
	/**
	 * @see org.openmrs.report.ReportRenderer#getRenderingModes(org.openmrs.report.ReportSchema)
	 */
	public Collection<RenderingMode> getRenderingModes(ReportSchema schema) {
		if (schema.getDataSetDefinitions() == null || schema.getDataSetDefinitions().size() != 1)
			return null;
		else
			return Collections.singleton(new RenderingMode(this, this.getLabel(), null, Integer.MIN_VALUE));
	}
	
	/**
	 * @see org.openmrs.report.ReportRenderer#render(ReportData, String, OutputStream)
	 */
	public void render(ReportData results, String argument, OutputStream out) throws IOException, RenderingException {
		PrintWriter pw = new PrintWriter(out);
		render(results, argument, pw);
	}
	
	/**
	 * @see org.openmrs.report.ReportRenderer#render(ReportData, String, Writer)
	 */
	@SuppressWarnings("unchecked")
    public void render(ReportData results, String argument, Writer writer) throws IOException, RenderingException {
		DataSet dataset = results.getDataSets().values().iterator().next();
		List<String> colKeys = dataset.getDefinition().getColumnKeys();
		
		// header row
		writer.write(getBeforeRowDelimiter());
		for (String colKey : colKeys) {
			writer.write(getBeforeColumnDelimiter());
			writer.write(escape(colKey));
			writer.write(getAfterColumnDelimiter());
		}
		writer.write(getAfterRowDelimiter());
		
		// data rows
		for (Iterator<Map<String, Object>> i = dataset.iterator(); i.hasNext();) {
			writer.write(getBeforeRowDelimiter());
			Map<String, Object> map = i.next();
			for (String colKey : colKeys) {
				Object colValue = map.get(colKey);
				writer.write(getBeforeColumnDelimiter());
				if (colValue != null)
					if (dataset instanceof CohortDataSet) {
						writer.write(escape(Integer.toString(((Cohort) colValue).size())));
					} else {
						writer.write(escape(colValue.toString()));
					}
				writer.write(getAfterColumnDelimiter());
			}
			writer.write(getAfterRowDelimiter());
		}
		
		writer.flush();
	}
	
}

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
package org.openmrs.web.controller.analysis;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openmrs.Obs;
import org.openmrs.reporting.PatientDataSet;
import org.openmrs.reporting.PatientDataSetFormatter;

public class ChronologicalObsFormatterHtml implements PatientDataSetFormatter {

	private boolean vertical = true;
	private String seriesName;
	
	public ChronologicalObsFormatterHtml(String seriesName) {
		this.seriesName = seriesName;
	}
	
	public ChronologicalObsFormatterHtml() { }
	
	/**
	 * @return Returns the seriesName.
	 */
	public String getSeriesName() {
		return seriesName;
	}

	/**
	 * @param seriesName The seriesName to set.
	 */
	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}

	public Object format(PatientDataSet input, Locale locale) {
		Map<Integer, Object> series = input.getDataSeries(seriesName);
		
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		
		StringBuffer ret = new StringBuffer();
		ret.append("<table>");
		if (vertical) {
			for (Map.Entry<Integer, Object> e : series.entrySet()) {
				ret.append("<tr><td>" + e.getKey() + "</td>");
				List<Obs> obsList = (List<Obs>) e.getValue();
				if (obsList == null) {
					continue;
				}
				ret.append("<td>");
				for (Obs obs : obsList) {
					ret.append(df.format(obs.getObsDatetime()) + ":" + obs.getValueNumeric() + " ");
				}
				ret.append("</td></tr>");
			}
		}
		ret.append("</table>");
		return ret.toString();
	}

}

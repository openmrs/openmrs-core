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

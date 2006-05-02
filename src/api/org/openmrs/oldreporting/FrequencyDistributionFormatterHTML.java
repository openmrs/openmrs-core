package org.openmrs.oldreporting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.Format;

/**
 * @author djazayeri
 * Formats two columns of data as an HTML table showing a frequency distribution.
 */
public class FrequencyDistributionFormatterHTML implements DataTableFormatter {

	protected final Log log = LogFactory.getLog(getClass());
	
	private String frequencyColumn;
	private boolean includePercentage = true;
	private boolean showZeroFrequencies = false;
	
	public FrequencyDistributionFormatterHTML(String frequencyColumn) {
		this.frequencyColumn = frequencyColumn;
	}
	
	/**
	 * @return Returns the includePercentage.
	 */
	public boolean getIncludePercentage() {
		return includePercentage;
	}

	/**
	 * @param includePercentage The includePercentage to set.
	 */
	public void setIncludePercentage(boolean includePercentage) {
		this.includePercentage = includePercentage;
	}

	/* (non-Javadoc)
	 * @see org.openmrs.reporting.Formatter#getMimeType()
	 */
	public String getMimeType() {
		return "text/html";
	}

	/**
	 * If the input is a Map, the key is the label and the value is the numeric frequency.
	 * If the input is a List<Object[]>, the 0th element of each array is the label and the 1st element is the numeric frequency
	 */
	public String format(DataTable input) {
		double total = 0;
		if (includePercentage) {
			for (DataRow row : input.getRows()) {
				total += ((Number) row.get(frequencyColumn)).doubleValue();
			}
		}
		StringBuffer ret = new StringBuffer();
		ret.append("<table border=1>");
		ret.append("<tr>");
		for (String columnName : input.getColumnNames()) {
			ret.append("<td>" + columnName + "</td>");
		}
		if (includePercentage) {
			ret.append("<td>Percentage</td>");
		}
		ret.append("</tr>");
		for (DataRow row : input.getRows()) {
			double val = ((Number) row.get(frequencyColumn)).doubleValue();
			if (showZeroFrequencies || val != 0) {
				ret.append("<tr>");
				for (String columnName : input.getColumnNames()) {
					ret.append("<td>" + row.get(columnName) + "</td>");
				}
				if (includePercentage) {
					ret.append("<td>" + Format.formatPercentage(val / total) + "</td>");
				}
				ret.append("</tr>");
			}
		}
		ret.append("</table>");
		return ret.toString();
	}

}

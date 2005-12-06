package org.openmrs.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.Format;

/**
 * @author djazayeri
 * Formats two columns of data as an HTML table showing a frequency distribution.
 */
public class FrequencyDistributionFormatterHTML {

	protected final Log log = LogFactory.getLog(getClass());
	
	private boolean includePercentage = true;
	private boolean showZeroFrequencies = false;
	
	public FrequencyDistributionFormatterHTML() {}
	
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
	public Object format(Object input) {
		List<Object[]> data = new ArrayList<Object[]>();
		if (input instanceof List) {
			data = (List<Object[]>) input;
		} else if (input instanceof Map) {
			for (Map.Entry<Object, Number> e : ((Map<Object, Number>) input).entrySet()) {
				Object[] holder = { e.getKey(), e.getValue() };
				data.add(holder);
			}
		} else {
			throw new IllegalArgumentException(input.getClass() + " is not a legal input to FrequenceDistributionFormatterHTML");
		}
		double total = 0;
		if (includePercentage) {
			for (Object[] holder : data) {
				total += ((Number) holder[1]).doubleValue();
			}
		}
		StringBuffer ret = new StringBuffer();
		ret.append("<table border=1>");
		for (Object[] holder : data) {
			if (showZeroFrequencies || ((Number) holder[1]).doubleValue() != 0) {
				ret.append("<tr><td>" + holder[0] + "</td><td>" + holder[1] + "</td>");
				if (includePercentage) {
					ret.append("<td>" + Format.formatPercentage(((Number) holder[1]).doubleValue() / total) + "</td>");
				}
			}
		}
		ret.append("</table>");
		return ret.toString();
	}

}

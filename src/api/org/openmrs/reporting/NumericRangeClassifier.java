package org.openmrs.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.openmrs.util.DoubleRange;

public class NumericRangeClassifier implements DataRowClassifier {

	private String dataItemName;
	private List<DoubleRange> ranges;
	private boolean inclusiveOfLowBound;
	
	/**
	 * @param range	space- or comma-separated list of numbers
	 * @param inclusiveOfLowBound true if the low bound of each range component is inclusive, false if the high bound is
	 */
	public NumericRangeClassifier(String dataItemName, String range, boolean inclusiveOfLowBound) {
		this.dataItemName = dataItemName;
		this.inclusiveOfLowBound = inclusiveOfLowBound;
		ranges = new ArrayList<DoubleRange>();
		Double last = null; 
		for (StringTokenizer st = new StringTokenizer(range, ", "); st.hasMoreTokens(); ) {
			Double d = Double.parseDouble(st.nextToken());
			DoubleRange holder = new DoubleRange(last, d);
			ranges.add(holder);
			last = d;
		}
		DoubleRange holder = new DoubleRange(last, null);
		ranges.add(holder);
	}
	
	public DoubleRange classify(DataRow row) {
		Number val = (Number) row.get(dataItemName);
		DoubleRange foundKey = null;
		if (val != null) {
			for (DoubleRange range : ranges) {
				if (range.contains(val.doubleValue())) {
					foundKey = range;
				}
			}
		}
		return foundKey;
	}

}

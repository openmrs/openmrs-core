package org.openmrs.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.openmrs.util.DoubleRange;

/**
 * 
 * @author djazayeri
 *
 * @param <T> Type of DataSet<T> that this works with
 */
public class NumericRangeGrouper<T> implements DataGrouper<T, DoubleRange> {

	private String dataItemName;
	private List<DoubleRange> ranges;
	private boolean inclusiveOfLowBound;
	
	public NumericRangeGrouper() { }
	
	/**
	 * @param range	space- or comma-separated list of numbers
	 * @param inclusiveOfLowBound true if the low bound of each range component is inclusive, false if the high bound is
	 */
	public NumericRangeGrouper(String dataItemName, String range, boolean inclusiveOfLowBound) {
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

	
	public SortedMap<DoubleRange, DataSet<T>> groupDataSet(DataSet<T> input) {
		Map<DoubleRange, DataSet<T>> ret = new HashMap<DoubleRange, DataSet<T>>();
		for (T inputKey : input.getRowKeys()) {
			DataRow row = input.getRow(inputKey);
			Number val = (Number) row.get(dataItemName);
			DoubleRange foundKey = null;
			if (val != null) {
				for (DoubleRange range : ranges) {
					if (range.contains(val.doubleValue())) {
						foundKey = range;
					}
				}
			}
			if (foundKey == null) {
				foundKey = new DoubleRange();
			}
			DataSet<T> set = ret.get(foundKey);
			if (set == null) {
				set = new SimpleDataSet<T>();
				ret.put(foundKey, set);
			}
			set.setRow(inputKey, row);
		}
		return new TreeMap<DoubleRange, DataSet<T>>(ret);
	}

}

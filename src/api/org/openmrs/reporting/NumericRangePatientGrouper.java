package org.openmrs.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.openmrs.Patient;
import org.openmrs.util.DoubleRange;

/**
 * Splits a PatientDataSet by dividing based on a numeric column into  
 * @author djazayeri
 */
public class NumericRangePatientGrouper implements PatientDataGrouper {

	private String dataItemName;
	private List<DoubleRange> ranges;
	private boolean inclusiveOfLowBound;
	
	public NumericRangePatientGrouper() { }
	
	/**
	 * @param range	space- or comma-separated list of numbers
	 * @param inclusiveOfLowBound true if the low bound of each range component is inclusive, false if the high bound is
	 */
	public NumericRangePatientGrouper(String dataItemName, String range, boolean inclusiveOfLowBound) {
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
	
	/**
	 * If the data item is null, then the patient is put into the "" group 
	 */
	public SortedMap<Object, PatientDataSet> groupPatientData(PatientDataSet input) {
		Map<Object, PatientDataSet> ret = new HashMap<Object, PatientDataSet>();
		for (Iterator<Map.Entry<Patient, Map<String, Object>>> i = input.iterator(); i.hasNext(); ) {
			Map.Entry<Patient, Map<String, Object>> e = i.next();
			Patient p = e.getKey();
			Map<String, Object> row = e.getValue();
			Number val = (Number) row.get(dataItemName);
			Object foundKey = null;
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
			PatientDataSet pds = ret.get(foundKey);
			if (pds == null) {
				pds = new PatientDataSet();
				ret.put(foundKey, pds);
			}
			pds.add(p, row);
		}
		return new TreeMap<Object, PatientDataSet>(ret);
	}

}

package org.openmrs.reporting;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public class CountAggregator implements Aggregator, PatientDataSetAggregator {

	public Map<Object, Object> aggregate(Map<Object, Collection> input) {
		Map<Object, Object> ret = input instanceof SortedMap ? new TreeMap<Object, Object>() : new HashMap<Object, Object>();
		for (Iterator<Map.Entry<Object, Collection>> i = input.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<Object, Collection> e = i.next();
			ret.put(e.getKey(), e.getValue().size());
		}
		return ret;
	}

	public Map<Object, Object> aggregatePatientDataSets(Map<Object, PatientDataSet> input) {
		Map<Object, Object> ret = input instanceof SortedMap ? new TreeMap<Object, Object>() : new HashMap<Object, Object>();
		for (Iterator<Map.Entry<Object, PatientDataSet>> i = input.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<Object, PatientDataSet> e = i.next();
			ret.put(e.getKey(), e.getValue().size());
		}
		return ret;
	}
	
}

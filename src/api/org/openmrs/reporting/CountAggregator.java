package org.openmrs.reporting;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class CountAggregator<T, C> implements DataSetAggregator<T, C, Integer> {

	public Map<C, Integer> aggregateDataSets(Map<C, DataSet<T>> input) {
		Map<C, Integer> ret = input instanceof SortedMap ? new TreeMap<C, Integer>() : new HashMap<C, Integer>();
		for (Map.Entry<C, DataSet<T>> e : input.entrySet()) {
			ret.put(e.getKey(), e.getValue().getRowCount());
		}
		return ret;
	}

}

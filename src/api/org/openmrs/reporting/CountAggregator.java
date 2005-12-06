package org.openmrs.reporting;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class CountAggregator<T, C> implements DataSetAggregator<T, C, Integer> {

	public Map<C, Integer> aggregateDataSets(Map<? extends C, DataSet<? extends T>> input) {
		Map<C, Integer> ret = input instanceof SortedMap ? new TreeMap<C, Integer>() : new HashMap<C, Integer>();
		for (Map.Entry<? extends C, DataSet<? extends T>> e : input.entrySet()) {
			ret.put(e.getKey(), e.getValue().getRowCount());
		}
		return ret;
	}

}

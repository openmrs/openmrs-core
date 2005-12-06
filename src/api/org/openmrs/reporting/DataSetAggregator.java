package org.openmrs.reporting;

import java.util.Map;

public interface DataSetAggregator<T, C, K> {

	public Map<C, K> aggregateDataSets(Map<C, DataSet<T>> input);
	
}

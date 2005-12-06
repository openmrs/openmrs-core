package org.openmrs.reporting;

import java.util.Map;

/**
 * @author djazayeri
 *
 * @param <T> The type of the DataSet this aggregates
 * @param <C> The type of the key that the DataSet<T>s are grouped by
 * @param <K> Type that the DataSet<T> is aggregated into
 */
public interface DataSetAggregator<T, C, K> {

	public Map<C, K> aggregateDataSets(Map<? extends C, DataSet<? extends T>> input);
	
}

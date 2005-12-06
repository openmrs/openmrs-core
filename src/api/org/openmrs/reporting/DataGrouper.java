package org.openmrs.reporting;

import java.util.Map;

public interface DataGrouper<T, C> {

	Map<C, DataSet<T>> groupDataSet(DataSet<T> input);
	
}

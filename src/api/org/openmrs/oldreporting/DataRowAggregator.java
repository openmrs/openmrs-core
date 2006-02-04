package org.openmrs.oldreporting;

import java.util.Collection;

public interface DataRowAggregator {

	public DataRow aggregate(Collection<DataRow> rows, String columnNameForAggregation, Collection<String> columnsGroupedOn);
	
}

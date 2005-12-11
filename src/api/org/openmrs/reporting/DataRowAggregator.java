package org.openmrs.reporting;

import java.util.Collection;

public interface DataRowAggregator {

	public Object aggregate(Collection<DataRow> rows);
	
}

package org.openmrs.reporting;

import java.util.Collection;

public class CountAggregator implements DataRowAggregator {

	public CountAggregator() { }
	
	public Object aggregate(Collection<DataRow> rows) {
		return new Integer(rows.size());
	}

}

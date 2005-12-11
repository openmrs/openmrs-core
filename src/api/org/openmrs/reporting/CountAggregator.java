package org.openmrs.reporting;

import java.util.Collection;

public class CountAggregator implements DataRowAggregator {

	public CountAggregator() { }
	
	public DataRow aggregate(Collection<DataRow> rows, String columnNameForAggregation, Collection<String> columnsGroupedOn) {
		DataRow row = new SimpleDataRow();
		if (rows.size() == 0) {
			for (String columnName : columnsGroupedOn) {
				row.set(columnName, null);
			}
			row.set(columnNameForAggregation, new Integer(0));
		} else {
			DataRow r = rows.iterator().next();
			for (String columnName : columnsGroupedOn) {
				row.set(columnName, r.get(columnName));
			}
			row.set(columnNameForAggregation, new Integer(rows.size()));
		}
		return row;
	}

}

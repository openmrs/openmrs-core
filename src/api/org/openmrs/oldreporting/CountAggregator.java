package org.openmrs.oldreporting;

import java.util.Collection;


public class CountAggregator implements DataRowAggregator, DataRowAggregationFunction {

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
	
	public Integer aggregate(Collection<DataRow> rows) {
		return new Integer(rows.size());
	}

}

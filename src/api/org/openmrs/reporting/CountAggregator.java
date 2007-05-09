package org.openmrs.reporting;

public class CountAggregator implements TableRowAggregator {

	public CountAggregator() { }
	
	public Object aggregate(DataTable table) {
		return table.getRowCount();
	}

}

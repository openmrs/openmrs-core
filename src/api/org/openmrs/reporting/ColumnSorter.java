package org.openmrs.reporting;

import java.util.Comparator;

public class ColumnSorter implements Comparator<DataRow> {

	private String columnName;
	
	public ColumnSorter(String columnName) {
		this.columnName = columnName;
	}
	
	public int compare(DataRow left, DataRow right) {
		Comparable leftVal = (Comparable) left.get(columnName);
		Comparable rightVal = (Comparable) right.get(columnName);
		return leftVal.compareTo(rightVal);
	}
	
}

package org.openmrs.oldreporting;

import java.util.Comparator;

public class ColumnSorter implements Comparator<DataRow> {

	private String columnName;
	
	public ColumnSorter() { }
	
	public ColumnSorter(String columnName) {
		this.columnName = columnName;
	}
	
	/**
	 * @return Returns the columnName.
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @param columnName The columnName to set.
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public int compare(DataRow left, DataRow right) {
		Comparable leftVal = (Comparable) left.get(columnName);
		Comparable rightVal = (Comparable) right.get(columnName);
		return leftVal.compareTo(rightVal);
	}
	
}

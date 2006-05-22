package org.openmrs.reporting.export;

import java.io.Serializable;

public class CalculatedColumn extends SimpleColumn implements ExportColumn, Serializable {
	
	public static final long serialVersionUID = 987654324L;
	
	public CalculatedColumn() {
		super();
		setColumnType("calculated");
	}
	
	public CalculatedColumn(String columnName, String columnValue) {
		super(columnName, columnValue);
		setColumnType("calculated");
	}
	
}
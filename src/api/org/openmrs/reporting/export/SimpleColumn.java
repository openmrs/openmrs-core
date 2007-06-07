package org.openmrs.reporting.export;

import java.io.Serializable;

public class SimpleColumn implements ExportColumn, Serializable {
	
	public static final long serialVersionUID = 987654322L;
	
	private String columnType = "simple";
	private String columnName = "";
	private String returnValue = "";
	
	public SimpleColumn() { }
	
	public SimpleColumn(String columnName, String columnValue) {
		this.columnName = columnName;
		returnValue = columnValue;
	}
	
	public String toTemplateString() {
		return returnValue;
	}

	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public String getTemplateColumnName() {
		return columnName;
	}
	
	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(String returnValue) {
		this.returnValue = returnValue;
	}
	
}
package org.openmrs.reporting.export;

public interface ExportColumn {
	String columnType = "";
	String columnName = "";
	String toTemplateString();
	
	String getTemplateColumnName();
	
	String getColumnName();
	void setColumnName(String s);
}
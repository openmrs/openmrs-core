package org.openmrs.reporting;

public class SimpleColumnClassifier implements TableRowClassifier {

	private String columnName;
	private String valueIfNull;
	
	public SimpleColumnClassifier(String columnName, String valueIfNull) {
		this.columnName = columnName;
		this.valueIfNull = valueIfNull;
	}
	
	public String classify(TableRow row) {
		Object temp = row.get(columnName);
		return temp == null ? valueIfNull : temp.toString();
	}

}

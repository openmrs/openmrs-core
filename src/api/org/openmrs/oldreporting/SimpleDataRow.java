package org.openmrs.oldreporting;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class SimpleDataRow implements DataRow {

	private Map<String, Object> row;
	
	public SimpleDataRow() {
		row = new HashMap<String, Object>();
	}
	
	public Set<String> getColumnNames() {
		return row.keySet();
	}

	public Object get(String columnName) {
		return row.get(columnName);
	}

	public void set(String columnName, Object value) {
		row.put(columnName, value);
	}

	public String getString(String columnName) {
		return (String) row.get(columnName);
	}

	public Number getNumber(String columnName) {
		return (Number) row.get(columnName);
	}

}

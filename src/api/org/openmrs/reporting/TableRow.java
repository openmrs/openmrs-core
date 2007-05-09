package org.openmrs.reporting;

import java.util.HashMap;
import java.util.Set;

public class TableRow extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	public TableRow() {
		super();
	}
	
	public TableRow(String singleColumnName, Object singleValue) {
		super();
		put(singleColumnName, singleValue);
	}
	
	public Set<String> getColumnNames() {
		return keySet();
	}

}

package org.openmrs.oldreporting;

import java.util.Set;

public interface DataRow {

	Set<String> getColumnNames();
	Object get(String columnName);
	void set(String columnName, Object value);

	String getString(String columnName);
	Number getNumber(String columnName);
	// ...
	
}

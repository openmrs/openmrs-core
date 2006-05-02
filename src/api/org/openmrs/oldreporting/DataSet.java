package org.openmrs.oldreporting;

import java.util.Set;
import java.util.Collection;

public interface DataSet<T> {

	public int getRowCount();
	
	public Set<String> getColumnNames();
	public Set<T> getRowKeys();
	
	public DataRow getRow(T rowKey);
	public void setRow(T rowKey, DataRow row);
	
	//public Map<T, Object> getColumn(String columnName);
	
	Object getValue(T rowKey, String columnName);
	void setValue(T rowKey, String columnName, Object value);
	
	Collection<DataRow> rowValues();
	
}

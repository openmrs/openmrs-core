package org.openmrs.oldreporting;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public interface DataTable {

	public int getRowCount();
	
	public Set<String> getColumnNames();
	public List<DataRow> getRows();
	
	public DataRow getRow(int index);
	public void setRow(int index, DataRow row);
	
	public boolean addRow(DataRow row);
	public DataRow removeRow(int rowIndex);

	public List<Object> getColumn(String columnName);
	
	public void setValue(int rowIndex, String columnName, Object value);
	public Object getValue(int rowIndex, String columnName);

	public void sort(Comparator<DataRow> comparator);
	
}

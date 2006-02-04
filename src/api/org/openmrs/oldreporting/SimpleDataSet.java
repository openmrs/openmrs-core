package org.openmrs.oldreporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


// TODO: think about whether things that return rows.collectionView() should be unmodifiable
public class SimpleDataSet<T> implements DataSet<T> {

	Set<String> columnNames;
	Map<T, DataRow> rows;
	
	public SimpleDataSet() {
		rows = new HashMap<T, DataRow>();
		columnNames = new HashSet<String>();
	}
	
	/*
	public SimpleDataSet(Comparator<T> rowKeyComparator) {
		rows = new TreeMap<T, DataRow>(rowKeyComparator);
		columnNames = new HashSet<String>();
	}
	*/
	
	public SimpleDataSet(Collection<T> keys) {
		rows = new HashMap<T, DataRow>();
		columnNames = new HashSet<String>();
		for (T t : keys) {
			rows.put(t, new SimpleDataRow());
		}
	}
	
	public int getRowCount() {
		return rows.size();
	}
	
	public Set<String> getColumnNames() {
		return Collections.unmodifiableSet(columnNames);
	}

	public Set<T> getRowKeys() {
		return rows.keySet();
	}

	public DataRow getRow(T rowKey) {
		return rows.get(rowKey);
	}

	public Map<T, Object> getColumn(String columnName) {
		Map<T, Object> ret = new HashMap<T, Object>();
		for (Map.Entry<T, DataRow> e : rows.entrySet()) {
			ret.put(e.getKey(), e.getValue().get(columnName));
		}
		return ret;
	}

	public Object getValue(T rowKey, String columnName) {
		DataRow row = rows.get(rowKey);
		return row == null ? null : row.get(columnName);
	}

	public Collection<DataRow> rowValues() {
		return rows.values();
	}

	public void setRow(T rowKey, DataRow row) {
		rows.put(rowKey, row);
		columnNames.addAll(row.getColumnNames());
	}

	public void setValue(T rowKey, String columnName, Object value) {
		DataRow row = rows.get(rowKey);
		if (row == null) {
			row = new SimpleDataRow();
		}
		row.set(columnName, value);
		columnNames.add(columnName);
	}

	public String toString() {
		return toHtmlTable();
	}
	
	/**
	 * @return For debugging purposes, display this data set as an HTML table. 
	 */
	public String toHtmlTable() {
		List<String> columns = new ArrayList<String>(columnNames);
		Collections.sort(columns);
		StringBuffer sb = new StringBuffer();
		sb.append("<table border=1>");
		sb.append("<tr><td>Key</td>");
		for (String colName : columns) {
			sb.append("<td>" + colName + "</td>");
		}
		sb.append("</tr>");
		for (T key : rows.keySet()) {
			sb.append("<tr>");
			sb.append("<td>" + key + "</td>");
			for (String colName : columns) {
				sb.append("<td>" + getValue(key, colName) + "</td>");
			}
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}
	
}

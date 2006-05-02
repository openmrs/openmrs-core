package org.openmrs.oldreporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openmrs.Patient;

//TODO: think about whether this should really expose its underlying list...
public class SimpleDataTable implements DataTable {

	Set<String> columnNames;
	List<DataRow> rows;
	
	public SimpleDataTable() {
		rows = new ArrayList<DataRow>();
		columnNames = new HashSet<String>();
	}
		
	public SimpleDataTable(Collection<DataRow> other) {
		rows = new ArrayList<DataRow>();
		columnNames = new HashSet<String>();
		for (DataRow r : other) {
			rows.add(r);
			columnNames.addAll(r.getColumnNames());
		}
	}
	
	public SimpleDataTable(DataSet<Patient> patientDataSet) {
		rows = new ArrayList<DataRow>();
		columnNames = new HashSet<String>(patientDataSet.getColumnNames());
		for (Patient patient : patientDataSet.getRowKeys()) {
			DataRow row = patientDataSet.getRow(patient);
			row.set("patient", patient);
			rows.add(row);
		}
		columnNames.add("patient");
	}
	
	public static SimpleDataTable fromSeries(String title, Collection<DataSeries> series) {
		Set rowKeys = new HashSet();
		String keyTitle = null;
		for (DataSeries s : series) {
			rowKeys.addAll(s.getKeys());
			keyTitle = s.getKeyTitle();
		}
		try {
			rowKeys = new TreeSet(rowKeys);
		} catch (Exception ex) { }
		List<DataRow> rows = new ArrayList<DataRow>();
		for (Object o : rowKeys) {
			DataRow row = new SimpleDataRow();
			row.set(title, o);
			for (DataSeries s : series) {
				row.set(s.getValueTitle(), s.getValue(o));
			}
			rows.add(row);
		}
		return new SimpleDataTable(rows);
	}
	
	public int getRowCount() {
		return rows.size();
	}
	
	public Set<String> getColumnNames() {
		return Collections.unmodifiableSet(columnNames);
	}

	public void addColumnNames(Collection<String> names) {
		columnNames.addAll(names);
	}
	
	// TODO should this be modifiable?
	public List<DataRow> getRows() {
		return rows;
	}

	public DataRow getRow(int rowIndex) {
		return rows.get(rowIndex);
	}
	
	public void setRow(int rowIndex, DataRow row) {
		rows.set(rowIndex, row);
		columnNames.addAll(row.getColumnNames());
	}
	
	public boolean addRow(DataRow row) {
		rows.add(row);
		columnNames.addAll(row.getColumnNames());
		return true;
	}
	
	public boolean addRowSkipColumnUpdate(DataRow row) {
		return rows.add(row);
	}
	
	public DataRow removeRow(int rowIndex) {
		return rows.remove(rowIndex);
	}
	
	public List<Object> getColumn(String columnName) {
		List<Object> ret = new ArrayList<Object>();
		for (DataRow row : rows) {
			ret.add(row.get(columnName));
		}
		return ret;
	}

	public Object getValue(int rowIndex, String columnName) {
		return rows.get(rowIndex).get(columnName);
	}

	public void setValue(int rowIndex, String columnName, Object value) {
		DataRow row = rows.get(rowIndex);
		row.set(columnName, value);
		columnNames.add(columnName);
	}

	public void sort(Comparator<DataRow> comparator) {
		Collections.sort(rows, comparator);
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
		sb.append("<tr>");
		for (String colName : columns) {
			sb.append("<td>" + colName + "</td>");
		}
		sb.append("</tr>");
		for (DataRow row : rows) {
			sb.append("<tr>");
			for (String colName : columns) {
				sb.append("<td>" + row.get(colName) + "</td>");
			}
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}
	
}

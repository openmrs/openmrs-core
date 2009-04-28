/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.util.OpenmrsUtil;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class DataTable {
	
	private List<String> columnOrder;
	
	private ArrayList<TableRow> rows;
	
	public DataTable() {
		columnOrder = new ArrayList<String>();
		rows = new ArrayList<TableRow>();
	}
	
	public DataTable(List<TableRow> rows) {
		if (rows instanceof ArrayList) {
			this.rows = (ArrayList<TableRow>) rows;
		} else {
			this.rows = new ArrayList<TableRow>();
			this.rows.addAll(rows);
		}
	}
	
	public void addColumn(String colName) {
		if (!columnOrder.contains(colName))
			columnOrder.add(colName);
	}
	
	public void addColumns(Collection<String> colNames) {
		for (String colName : colNames)
			addColumn(colName);
	}
	
	public int getRowCount() {
		return rows.size();
	}
	
	public void addRow(TableRow row) {
		rows.add(row);
	}
	
	public void addRows(Collection<TableRow> rows) {
		this.rows.addAll(rows);
	}
	
	public ArrayList<TableRow> getRows() {
		return rows;
	}
	
	public void sortByColumn(final String colName) {
		Collections.sort(rows, new Comparator<TableRow>() {
			
			@SuppressWarnings("unchecked")
			public int compare(TableRow left, TableRow right) {
				Comparable l = (Comparable) left.get(colName);
				Comparable r = (Comparable) right.get(colName);
				return OpenmrsUtil.compareWithNullAsLowest(l, r);
			}
		});
	}
	
	public Map<String, DataTable> split(TableRowClassifier trc) {
		Map<String, DataTable> ret = new HashMap<String, DataTable>();
		for (TableRow row : rows) {
			String classification = trc.classify(row);
			DataTable thisClass = ret.get(classification);
			if (thisClass == null) {
				thisClass = new DataTable();
				ret.put(classification, thisClass);
			}
			thisClass.addRow(row);
		}
		return ret;
	}
	
	public String toString() {
		if (rows.size() == 0)
			return "DataTable with no rows";
		List<String> columns;
		if (columnOrder.size() > 0) {
			columns = columnOrder;
		} else {
			columns = new ArrayList<String>(rows.get(0).getColumnNames());
			Collections.sort(columns);
		}
		StringBuilder sb = new StringBuilder();
		for (String colName : columns) {
			sb.append(colName).append(",");
		}
		for (TableRow row : rows) {
			sb.append("\n");
			for (String colName : columns) {
				sb.append(row.get(colName)).append(",");
			}
		}
		return sb.toString();
	}
	
	public String getHtmlTable() {
		if (rows.size() == 0)
			return "DataTable with no rows";
		List<String> columns;
		if (columnOrder.size() > 0) {
			columns = columnOrder;
		} else {
			columns = new ArrayList<String>(rows.get(0).getColumnNames());
			Collections.sort(columns);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<table border=\"1\" cellspacing=\"0\" cellpadding=\"2\">");
		sb.append("<thead><tr>");
		for (String colName : columns) {
			sb.append("<th>").append(colName).append("</th>");
		}
		sb.append("</tr></thead><tbody>");
		for (TableRow row : rows) {
			sb.append("<tr>");
			for (String colName : columns) {
				sb.append("<td>").append(row.get(colName)).append("</td>");
			}
			sb.append("</tr>");
		}
		sb.append("</tbody></table>");
		return sb.toString();
		
	}
	
}

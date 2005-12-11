package org.openmrs.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataTableGrouper {

	private List<String> columnNames;
	private DataRowClassifier classifier;
	private String columnNameForClassification;
	private DataRowAggregator aggregator;
	private String columnNameForAggregation;
	
	public DataTableGrouper(List<String> columnNames, DataRowAggregator aggregator, String columnNameForAggregation) {
		if (columnNames == null) {
			throw new IllegalArgumentException("columnNames can't be null");
		}
		this.columnNames = columnNames;
		this.aggregator = aggregator;
		this.columnNameForAggregation = columnNameForAggregation;
	}
		
	public DataTableGrouper(DataRowClassifier classifier, String columnNameForClassification, DataRowAggregator aggregator, String columnNameForAggregation) {
		if (classifier == null) {
			throw new IllegalArgumentException("classifier can't be null");
		}
		this.classifier = classifier;
		this.columnNameForClassification = columnNameForClassification;
		this.aggregator = aggregator;
		this.columnNameForAggregation = columnNameForAggregation;
	}

	public DataTable group(DataTable input) {
		if (columnNames != null) {
			return groupBy(input, columnNames, aggregator, columnNameForAggregation);
		} else {
			return groupBy(input, classifier, columnNameForClassification, aggregator, columnNameForAggregation);
		}
	}
	

	public static DataTable groupBy(DataTable input,
							 List<String> columnNames,
							 DataRowAggregator aggregator, String columnNameForAggregation) {
		Map<List<Object>, List<DataRow>> map = new HashMap<List<Object>, List<DataRow>>();
		for (DataRow row : input.getRows()) {
			List<Object> key = new ArrayList<Object>();
			for (String columnName : columnNames) {
				key.add(row.get(columnName));
			}
			List<DataRow> list = map.get(key);
			if (list == null) {
				list = new ArrayList<DataRow>();
				map.put(key, list);
			}
			list.add(row);
		}
		DataTable output = new SimpleDataTable();
		for (Map.Entry<List<Object>, List<DataRow>> e : map.entrySet()) {
			List<Object> keys = e.getKey();
			Object o = aggregator.aggregate(e.getValue());
			DataRow row = new SimpleDataRow();
			row.set(columnNameForAggregation, o);
			for (int i = 0; i < keys.size(); ++i) {
				String columnName = columnNames.get(i);
				Object value = keys.get(i);
				row.set(columnName, value);
			}
			output.addRow(row);
		}
		return output;
	}
	
	public static DataTable groupBy(DataTable input,
							 DataRowClassifier classifier, String columnNameForClassification,
							 DataRowAggregator aggregator, String columnNameForAggregation) {
		Map<Object, List<DataRow>> map = new HashMap<Object, List<DataRow>>();
		for (DataRow row : input.getRows()) {
			Object key = classifier.classify(row);
			List<DataRow> list = map.get(key);
			if (list == null) {
				list = new ArrayList<DataRow>();
				map.put(key, list);
			}
			list.add(row);
		}
		DataTable output = new SimpleDataTable();
		for (Map.Entry<Object, List<DataRow>> e : map.entrySet()) {
			Object key = e.getKey();
			Object o = aggregator.aggregate(e.getValue());
			DataRow row = new SimpleDataRow();
			row.set(columnNameForClassification, key);
			row.set(columnNameForAggregation, o);
			output.addRow(row);
		}
		return output;
	}

	
}

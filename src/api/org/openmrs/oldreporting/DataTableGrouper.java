package org.openmrs.oldreporting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataTableGrouper {

	private List<String> columnNames;
	private DataRowClassifier classifier;
	private String columnNameForClassification;
	private DataRowAggregator aggregator;
	private String columnNameForAggregation;
	
	public DataTableGrouper() { }
	
	/**
	 * @return Returns the aggregator.
	 */
	public DataRowAggregator getAggregator() {
		return aggregator;
	}

	/**
	 * @param aggregator The aggregator to set.
	 */
	public void setAggregator(DataRowAggregator aggregator) {
		this.aggregator = aggregator;
	}

	/**
	 * @return Returns the classifier.
	 */
	public DataRowClassifier getClassifier() {
		return classifier;
	}

	/**
	 * @param classifier The classifier to set.
	 */
	public void setClassifier(DataRowClassifier classifier) {
		this.classifier = classifier;
	}

	/**
	 * @return Returns the columnNameForAggregation.
	 */
	public String getColumnNameForAggregation() {
		return columnNameForAggregation;
	}

	/**
	 * @param columnNameForAggregation The columnNameForAggregation to set.
	 */
	public void setColumnNameForAggregation(String columnNameForAggregation) {
		this.columnNameForAggregation = columnNameForAggregation;
	}

	/**
	 * @return Returns the columnNameForClassification.
	 */
	public String getColumnNameForClassification() {
		return columnNameForClassification;
	}

	/**
	 * @param columnNameForClassification The columnNameForClassification to set.
	 */
	public void setColumnNameForClassification(String columnNameForClassification) {
		this.columnNameForClassification = columnNameForClassification;
	}

	/**
	 * @return Returns the columnNames.
	 */
	public List<String> getColumnNames() {
		return columnNames;
	}

	/**
	 * @param columnNames The columnNames to set.
	 */
	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

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
		} else if (classifier != null) {
			return groupBy(input, classifier, columnNameForClassification, aggregator, columnNameForAggregation);
		} else {
			throw new IllegalStateException("You must set either columnNames or classifier before calling group()");
		}
	}
	
	public static DataTable groupBy(DataTable input, List<String> columnNames,
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
		for (List<DataRow> list : map.values()) {
			DataRow row = aggregator.aggregate(list, columnNameForAggregation, columnNames);
			output.addRow(row);
		}
		return output;
	}
	
	public static DataTable groupBy(DataTable input,
			DataRowClassifier classifier, String columnNameForClassification,
			DataRowAggregator aggregator, String columnNameForAggregation) {
		for (DataRow row : input.getRows()) {
			row.set(columnNameForClassification, classifier.classify(row));
		}
		return groupBy(input, Collections.singletonList(columnNameForClassification), aggregator, columnNameForAggregation);
	}

}

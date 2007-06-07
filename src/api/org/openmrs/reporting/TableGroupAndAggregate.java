package org.openmrs.reporting;

import java.util.Map;

public class TableGroupAndAggregate {

	private TableRowClassifier classifier;
	private TableRowAggregator aggregator;
	private String labelColumnName;
	private String valueColumnName;
	
	public TableGroupAndAggregate(TableRowClassifier classifier, TableRowAggregator aggregator, String labelColumnName, String valueColumnName) {
		this.classifier = classifier;
		this.aggregator = aggregator;
		this.labelColumnName = labelColumnName;
		this.valueColumnName = valueColumnName;
		if (labelColumnName == null)
			labelColumnName = "label";
		if (valueColumnName == null)
			valueColumnName = "value";
	}
	
	public DataTable run(DataTable input) {
		Map<String, DataTable> temp = input.split(classifier);
		DataTable ret = new DataTable();
		ret.addColumn(labelColumnName);
		ret.addColumn(valueColumnName);
		for (Map.Entry<String, DataTable> e : temp.entrySet()) {
			Object val = aggregator.aggregate(e.getValue());
			TableRow tr = new TableRow();
			tr.put(labelColumnName, e.getKey());
			tr.put(valueColumnName, val);
			ret.addRow(tr);
		}
		return ret;
	}
	
}

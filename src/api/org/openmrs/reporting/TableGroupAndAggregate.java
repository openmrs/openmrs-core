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

import java.util.Map;

public class TableGroupAndAggregate {
	
	private TableRowClassifier classifier;
	
	private TableRowAggregator aggregator;
	
	private String labelColumnName;
	
	private String valueColumnName;
	
	public TableGroupAndAggregate(TableRowClassifier classifier, TableRowAggregator aggregator, String labelColumnName,
	    String valueColumnName) {
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

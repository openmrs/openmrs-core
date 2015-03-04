/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

import java.util.Map;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
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

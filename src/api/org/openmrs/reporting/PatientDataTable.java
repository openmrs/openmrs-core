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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientDataTable extends HashMap<Integer, TableRow> {
	
	private static final long serialVersionUID = 1L;
	
	private List<String> columnNames;
	
	public PatientDataTable(Collection<Integer> patientIds) {
		super();
		for (Integer ptId : patientIds) {
			put(ptId, new TableRow("patientId", ptId));
		}
		columnNames = new ArrayList<String>();
		columnNames.add("patientId");
	}

	public PatientDataTable(PatientSet ps) {
		this(ps.getPatientIds());
	}
	
	// if values contains any patientIds not found in this, then they are ignored
	public void addColumn(String columnName, Map<Integer, Object> values) {
		if (!columnNames.contains(columnName))
			columnNames.add(columnName);
		for (Map.Entry<Integer, Object> e : values.entrySet()) {
			TableRow row = get(e.getKey());
			if (row != null)
				row.put(columnName, e.getValue());
		}
	}

	public DataTable toDataTable() {
		DataTable ret = new DataTable();
		ret.addRows(values());
		ret.addColumns(columnNames);
		return ret;
	}
	
}

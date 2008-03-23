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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportElement {

	private Map<String, PatientDataProducer> producers;
	private List<TableGroupAndAggregate> groupAndAggregate;

	public ReportElement() {
		producers = new LinkedHashMap<String, PatientDataProducer>();
		groupAndAggregate = new ArrayList<TableGroupAndAggregate>();
	}
	
	public void addProducer(String columnName, PatientDataProducer p) {
		producers.put(columnName, p);
	}
	
	public void addGroupAndAggregate(TableGroupAndAggregate tga) {
		groupAndAggregate.add(tga);
	}
	
	public DataTable run(PatientSet ps) {
		PatientDataTable patientTable = new PatientDataTable(ps);
		List<Integer> patientIds = new ArrayList<Integer>(patientTable.keySet());
		for (Map.Entry<String, PatientDataProducer> e : producers.entrySet()) {
			patientTable.addColumn(e.getKey(), e.getValue().produceData(patientIds));
		}
		DataTable table = patientTable.toDataTable();
		

		for (TableGroupAndAggregate tga : groupAndAggregate)
			table = tga.run(table);

		return table;
	}
	
}
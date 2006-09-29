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
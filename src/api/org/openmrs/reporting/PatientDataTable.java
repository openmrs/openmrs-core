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

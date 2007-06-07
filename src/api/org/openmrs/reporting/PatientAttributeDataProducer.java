package org.openmrs.reporting;

import java.util.Collection;
import java.util.Map;

import org.openmrs.api.context.Context;

public class PatientAttributeDataProducer extends AbstractReportObject implements PatientDataProducer {

	private String name;
	private String tableName;
	private String columnName;
	private DataTransformer transformer;
	
	public PatientAttributeDataProducer(String tableName, String columnName) {
		this.tableName = tableName;
		this.columnName = columnName;
		if (this.tableName == null)
			this.tableName = "Patient";
	}
	
	public PatientAttributeDataProducer(String tableName, String columnName, DataTransformer transformer) {
		this(tableName, columnName);
		this.transformer = transformer;
	}
	
	public Map<Integer, Object> produceData(Collection<Integer> patientIds) {
		// TODO: getPatientAttributes shouldn't need a PatientSet.
		PatientSet temp = new PatientSet();
		temp.copyPatientIds(patientIds);
		Map<Integer, Object> ret = Context.getPatientSetService().getPatientAttributes(temp, tableName, columnName, false);
		if (transformer != null)
			for (Map.Entry<Integer, Object> e : ret.entrySet())
				e.setValue(transformer.transform(e.getValue()));
		return ret;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getReportObjectId() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setReportObjectId(Integer id) {
		// TODO Auto-generated method stub
		
	}
}

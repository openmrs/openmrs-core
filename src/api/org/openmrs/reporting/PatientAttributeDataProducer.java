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

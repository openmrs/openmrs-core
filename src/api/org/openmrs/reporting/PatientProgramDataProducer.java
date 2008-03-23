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
import java.util.HashMap;
import java.util.Map;

import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.api.context.Context;

public class PatientProgramDataProducer extends AbstractReportObject implements PatientDataProducer {

	public enum WhichField {
		ENROLLMENT_DATE (new DataTransformer() {
				public Object transform(Object o) {
					return ((PatientProgram) o).getDateEnrolled();
				}
			}),
		COMPLETION_DATE (new DataTransformer() {
				public Object transform(Object o) {
					return ((PatientProgram) o).getDateCompleted();
				}
			});
		
		private final DataTransformer transformer;

		WhichField(DataTransformer transformer) {
			this.transformer = transformer;
		}
		
		public DataTransformer getTransformer() {
			return transformer;
		}
	}
	
	private String name;
	private Program program;
	private WhichField field;
	
	public PatientProgramDataProducer(Program program, WhichField field) {
		this.program = program;
		this.field = field;
	}
	
	public Map<Integer, Object> produceData(Collection<Integer> patientIds) {
		PatientSet ps = new PatientSet();
		ps.copyPatientIds(patientIds);
		Map<Integer, PatientProgram> programs = Context.getPatientSetService().getPatientPrograms(ps, program);
		Map<Integer, Object> ret = new HashMap<Integer, Object>();
		DataTransformer trans = field.getTransformer();
		for (Map.Entry<Integer, PatientProgram> e : programs.entrySet()) {
			ret.put(e.getKey(), trans.transform(e.getValue()));
		}
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

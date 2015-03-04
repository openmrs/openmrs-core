/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.report;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openmrs.PatientProgram;
import org.openmrs.api.context.Context;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class RowPerProgramEnrollmentDataSet implements DataSet<Object> {
	
	private RowPerProgramEnrollmentDataSetDefinition definition;
	
	private EvaluationContext evaluationContext;
	
	private List<PatientProgram> data;
	
	public RowPerProgramEnrollmentDataSet() {
	}
	
	/**
	 * This is wrapped around (List<Obs>).iterator() This implementation is NOT thread-safe, so do
	 * not access the wrapped iterator.
	 */
	class HelperIterator implements Iterator<Map<String, Object>> {
		
		private Iterator<PatientProgram> iter;
		
		public HelperIterator(Iterator<PatientProgram> iter) {
			this.iter = iter;
		}
		
		public boolean hasNext() {
			return iter.hasNext();
		}
		
		public Map<String, Object> next() {
			Locale locale = Context.getLocale();
			PatientProgram pp = iter.next();
			Map<String, Object> ret = new HashMap<String, Object>();
			ret.put("patientId", pp.getPatient().getPatientId());
			ret.put("programName", pp.getProgram().getConcept().getName(locale, false).getName());
			ret.put("programId", pp.getProgram().getProgramId());
			ret.put("enrollmentDate", pp.getDateEnrolled());
			ret.put("completionDate", pp.getDateCompleted());
			ret.put("patientProgramId", pp.getPatientProgramId());
			return ret;
		}
		
		public void remove() {
			iter.remove();
		}
		
	}
	
	public DataSetDefinition getDefinition() {
		return definition;
	}
	
	public Iterator<Map<String, Object>> iterator() {
		return new HelperIterator(data.iterator());
	}
	
	public EvaluationContext getEvaluationContext() {
		return evaluationContext;
	}
	
	public List<PatientProgram> getData() {
		return data;
	}
	
	public void setData(List<PatientProgram> data) {
		this.data = data;
	}
	
	public void setDefinition(RowPerProgramEnrollmentDataSetDefinition definition) {
		this.definition = definition;
	}
	
	public void setEvaluationContext(EvaluationContext evaluationContext) {
		this.evaluationContext = evaluationContext;
	}
	
}

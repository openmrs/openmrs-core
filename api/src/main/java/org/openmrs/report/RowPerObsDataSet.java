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

import org.openmrs.Obs;
import org.openmrs.api.context.Context;

/**
 * A dataset with one-row-per-obs.
 *
 * @see RowPerObsDataSetDefinition
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class RowPerObsDataSet implements DataSet<Object> {
	
	private RowPerObsDataSetDefinition definition;
	
	private EvaluationContext evaluationContext;
	
	private List<Obs> data;
	
	public RowPerObsDataSet() {
	}
	
	/**
	 * This is wrapped around (List<Obs>).iterator() This implementation is NOT thread-safe, so do
	 * not access the wrapped iterator.
	 */
	class HelperIterator implements Iterator<Map<String, Object>> {
		
		private Iterator<Obs> iter;
		
		public HelperIterator(Iterator<Obs> iter) {
			this.iter = iter;
		}
		
		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return iter.hasNext();
		}
		
		/**
		 * @see java.util.Iterator#next()
		 */
		public Map<String, Object> next() {
			Locale locale = Context.getLocale();
			Obs obs = iter.next();
			Map<String, Object> ret = new HashMap<String, Object>();
			ret.put("patientId", obs.getPersonId());
			ret.put("question", obs.getConcept().getName(locale, false));
			ret.put("questionConceptId", obs.getConcept().getConceptId());
			ret.put("answer", obs.getValueAsString(locale));
			if (obs.getValueCoded() != null) {
				ret.put("answerConceptId", obs.getValueCoded());
			}
			ret.put("obsDatetime", obs.getObsDatetime());
			if (obs.getEncounter() != null) {
				ret.put("encounterId", obs.getEncounter().getEncounterId());
			}
			if (obs.getObsGroup() != null) {
				ret.put("obsGroupId", obs.getObsGroup().getObsId());
			}
			return ret;
		}
		
		/**
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			iter.remove();
		}
		
	}
	
	/**
	 * @see org.openmrs.report.DataSet#iterator()
	 */
	public Iterator<Map<String, Object>> iterator() {
		Iterator<Map<String, Object>> ret = new HelperIterator(data.iterator());
		return ret;
	}
	
	/**
	 * @return the data
	 */
	public List<Obs> getData() {
		return data;
	}
	
	/**
	 * @param data the data to set
	 */
	public void setData(List<Obs> data) {
		this.data = data;
	}
	
	/**
	 * @return the definition
	 */
	public RowPerObsDataSetDefinition getDefinition() {
		return definition;
	}
	
	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(RowPerObsDataSetDefinition definition) {
		this.definition = definition;
	}
	
	/**
	 * @see org.openmrs.report.DataSet#getEvaluationContext()
	 */
	public EvaluationContext getEvaluationContext() {
		return evaluationContext;
	}
	
	/**
	 * @param evaluationContext the evaluationContext to set
	 */
	public void setEvaluationContext(EvaluationContext evaluationContext) {
		this.evaluationContext = evaluationContext;
	}
	
}

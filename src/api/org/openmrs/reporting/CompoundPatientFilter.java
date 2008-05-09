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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.PatientSetService.BooleanOperator;
import org.openmrs.report.EvaluationContext;

public class CompoundPatientFilter extends AbstractPatientFilter implements
		PatientFilter {

	protected transient final Log log = LogFactory.getLog(getClass());
	
	private BooleanOperator operator;
	private List<PatientFilter> filters;
	
	public CompoundPatientFilter() { }
	
	public CompoundPatientFilter(BooleanOperator operator, List<PatientFilter> filters) {
		this.operator = operator;
		this.filters = filters;
	}
	
	public List<PatientFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<PatientFilter> filters) {
		this.filters = filters;
	}

	public BooleanOperator getOperator() {
		return operator;
	}

	public void setOperator(BooleanOperator operator) {
		this.operator = operator;
	}

	public Cohort filter(Cohort input, EvaluationContext context) {
		if (operator == BooleanOperator.AND) {
			Cohort temp = input;
			for (PatientFilter pf : filters) {
				temp = pf.filter(temp, context);
			}
			return temp;
		} else {
			Set<Integer> ptIds = new HashSet<Integer>();
			for (PatientFilter pf : filters) {
				ptIds.addAll(pf.filter(input, context).getMemberIds());
				log.debug("or " + pf.getName() + " (" + pf.toString() + ")");
			}
			Cohort ret = new Cohort();
			ret.setMemberIds(ptIds);
			return ret;
		}
	}

	public Cohort filterInverse(Cohort input, EvaluationContext context) {
		if (operator == BooleanOperator.AND) {
			// NOT(AND(x, y)) -> OR(NOT x, NOT y)
			Set<Integer> ptIds = new HashSet<Integer>();
			for (PatientFilter pf : filters)
				ptIds.addAll(pf.filterInverse(input, context).getMemberIds());
			Cohort ret = new Cohort();
			ret.setMemberIds(ptIds);
			return ret;
		} else {
			// NOT(OR(x, y)) -> AND(NOT x, NOT y)
			Cohort temp = input;
			for (PatientFilter pf : filters) {
				temp = pf.filterInverse(temp, context);
			}
			return temp;
		}
	}

	public String getDescription() {
		if (super.getDescription() != null)
			return super.getDescription();
		else {
			StringBuilder ret = new StringBuilder();
			for (Iterator<PatientFilter> i = filters.iterator(); i.hasNext(); ) {
				PatientFilter pf = i.next();
				ret.append("[").append(pf.getName() == null ? pf.getDescription() : pf.getName()).append("]");
				if (i.hasNext())
					ret.append(" " + operator + " ");
			}
			return ret.toString();
		}
	}

	public boolean isReadyToRun() {
		return operator != null && filters != null;
	}

}

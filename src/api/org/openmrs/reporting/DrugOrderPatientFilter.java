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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PatientSetService.GroupMethod;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.util.OpenmrsUtil;

/**
 * @deprecated Use org.openmrs.reporting.DrugOrderFilter instead
 */
@Deprecated
public class DrugOrderPatientFilter extends AbstractPatientFilter implements PatientFilter, Comparable<DrugOrderPatientFilter> {
	
	protected transient final Log log = LogFactory.getLog(getClass());
	
	private static final long serialVersionUID = 1L;
	
	private Integer drugId; // replace this with drug
	
	private Concept drugConcept;
	
	private GroupMethod groupMethod;
	
	private Date onDate;
	
	public DrugOrderPatientFilter() {
		super.setType("Patient Filter");
		super.setSubType("Drug Order Patient Filter");
	}
	
	public boolean isReadyToRun() {
		return true;
	}
	
	public int compareTo(DrugOrderPatientFilter other) {
		return compareHelper().compareTo(other.compareHelper());
	}
	
	private Integer compareHelper() {
		if (groupMethod == GroupMethod.NONE)
			return -1;
		else
			return (drugId == null ? 0 : drugId)
			        + (onDate == null ? 0 : (int) (System.currentTimeMillis() - onDate.getTime()));
	}
	
	public GroupMethod getGroupMethod() {
		return groupMethod;
	}
	
	public void setGroupMethod(GroupMethod groupMethod) {
		this.groupMethod = groupMethod;
	}
	
	public java.util.Date getOnDate() {
		return onDate;
	}
	
	public void setOnDate(Date onDate) {
		this.onDate = onDate;
	}
	
	public Integer getDrugId() {
		return drugId;
	}
	
	public void setDrugId(Integer drugId) {
		this.drugId = drugId;
	}
	
	public Concept getDrugConcept() {
		return drugConcept;
	}
	
	public void setDrugConcept(Concept drugConcept) {
		this.drugConcept = drugConcept;
	}
	
	public Cohort filter(Cohort input, EvaluationContext context) {
		Set<Integer> drugIds = new HashSet<Integer>();
		if (groupMethod != null && groupMethod == GroupMethod.NONE) {
			drugIds = null;
		} else {
			if (drugId != null)
				drugIds.add(drugId);
			if (drugConcept != null) {
				List<Drug> drugs = Context.getConceptService().getDrugs();
				for (Drug drug : drugs)
					if (drug.getConcept().equals(drugConcept))
						drugIds.add(drug.getDrugId());
			}
		}
		PatientSetService service = Context.getPatientSetService();
		return service.getPatientsHavingDrugOrder(input == null ? null : input.getMemberIds(), drugIds, onDate);
	}
	
	public Cohort filterInverse(Cohort input, EvaluationContext context) {
		Set<Integer> drugIds = new HashSet<Integer>();
		if (groupMethod != null && groupMethod == GroupMethod.NONE) {
			drugIds = null;
		} else {
			if (drugId != null)
				drugIds.add(drugId);
			if (drugConcept != null) {
				List<Drug> drugs = Context.getConceptService().getDrugs();
				for (Drug drug : drugs)
					if (drug.getConcept().equals(drugConcept))
						drugIds.add(drug.getDrugId());
			}
		}
		PatientSetService service = Context.getPatientSetService();
		Cohort temp = service.getPatientsHavingDrugOrder(input.getMemberIds(), drugIds, onDate);
		return Cohort.subtract(input, temp);
	}
	
	public String getDescription() {
		// TODO: internationalize this
		StringBuilder sb = new StringBuilder();
		if (groupMethod != null && groupMethod == GroupMethod.NONE)
			sb.append("No drug orders");
		else if (drugId != null || drugConcept != null) {
			sb.append("Taking ");
			SortedSet<String> names = new TreeSet<String>();
			if (drugId != null) {
				Drug drug = Context.getConceptService().getDrug(drugId);
				if (drug == null) {
					log.error("Can't find drug with id " + drugId);
					names.add("MISSING DRUG " + drugId);
				} else
					names.add(drug.getName());
			}
			if (drugConcept != null)
				names.add(drugConcept.getName(Context.getLocale(), false).getName());
			sb.append(OpenmrsUtil.join(names, " or "));
		} else
			sb.append("Any Drug Order");
		if (getOnDate() != null)
			sb.append(" on " + getOnDate());
		return sb.toString();
	}
	
}

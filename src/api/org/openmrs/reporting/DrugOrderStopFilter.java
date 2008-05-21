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
import java.util.Iterator;
import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;
import org.openmrs.util.OpenmrsUtil;

public class DrugOrderStopFilter extends CachingPatientFilter {
	
	private Date stopDate;
	private List<Drug> drugList;
	private List<Concept> genericDrugList;
	private Boolean discontinued;
	private List<Concept> discontinuedReasonList;
	private Integer withinLastDays;
	private Integer withinLastMonths;
	private Integer untilDaysAgo;
	private Integer untilMonthsAgo;
	private Date sinceDate;
	private Date untilDate;
	
	public DrugOrderStopFilter() { }
	
	@Override
    public String getCacheKey() {
	    StringBuilder sb = new StringBuilder();
	    sb.append(getClass().getName()).append(".");
	    sb.append(getStopDate()).append(".");
	    sb.append(getDiscontinued()).append(".");
	    sb.append(OpenmrsUtil.fromDateHelper(null, withinLastDays, withinLastMonths, untilDaysAgo, untilMonthsAgo, sinceDate, untilDate)).append(".");
	    sb.append(OpenmrsUtil.toDateHelper(null, withinLastDays, withinLastMonths, untilDaysAgo, untilMonthsAgo, sinceDate, untilDate)).append(".");
	    if (drugList != null)
		    for (Drug d : drugList)
		    	sb.append(d.getDrugId()).append(",");
	    sb.append(".");
	    if (genericDrugList != null)
		    for (Concept c : genericDrugList)
		    	sb.append(c.getConceptId()).append(",");
	    sb.append(".");
	    if (discontinuedReasonList != null)
	    	for (Concept c : discontinuedReasonList)
	    		sb.append(c.getConceptId()).append(",");
	    return sb.toString();
    }

	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Patients who stopped or changed ");
		if ( (getDrugList() != null && getDrugList().size() > 0 ) || (getGenericDrugList() != null && getGenericDrugList().size() > 0 )) {
			if (getDrugList() != null && getDrugList().size() > 0) {
				if (getDrugList().size() == 1)
					sb.append(getDrugList().get(0).getName());
				else {
					sb.append("any of [");
					for (Iterator<Drug> i = getDrugList().iterator(); i.hasNext(); ) {
						sb.append(" " + i.next().getName() + " ");
						if (i.hasNext())
							sb.append(",");
					}
					sb.append("]");	
				}
			}
			if (getGenericDrugList() != null && getGenericDrugList().size() > 0) {
				if (getGenericDrugList().size() == 1)
					sb.append("any form of " + getGenericDrugList().get(0).getName().getName());
				else {
					sb.append("any form of [");
					for (Iterator<Concept> i = getGenericDrugList().iterator(); i.hasNext(); ) {
						sb.append(" " + i.next().getName().getName() + " ");
						if (i.hasNext())
							sb.append(",");
					}
					sb.append(" ]");	
				}
			}
		} else
			sb.append("any drug");
		if (getDiscontinuedReasonList() != null && getDiscontinuedReasonList().size() > 0) {
			if (getDiscontinuedReasonList().size() == 1) {
				String reason = "[name not defined]";
				ConceptName cn = getDiscontinuedReasonList().get(0).getName();
				if ( cn != null ) reason = cn.getName(); 
				sb.append(" because of " + reason);
			}
			else {
				sb.append(" because of any of [");
				for (Iterator<Concept> i = getDiscontinuedReasonList().iterator(); i.hasNext(); ) {
					sb.append(" " + i.next().getName().getName() + " ");
					if (i.hasNext())
						sb.append(",");
				}
				sb.append("]");
			}
		}
		if (withinLastMonths != null || withinLastDays != null) {
			sb.append(" within the last ");
			if (withinLastMonths != null)
				sb.append(withinLastMonths + " month(s) ");
			if (withinLastDays != null)
				sb.append(withinLastDays + " day(s) ");
		}
		// TODO untilDaysAgo untilMonthsAgo
		if (sinceDate != null)
			sb.append(" on or after " + sinceDate + " ");
		if (untilDate != null)
			sb.append(" on or before " + untilDate + " ");
		return sb.toString();
	}

	@Override
	public Cohort filterImpl(EvaluationContext context) {
		return Context.getPatientSetService().getPatientsHavingDrugOrder(getDrugList(), getGenericDrugList(), null, null,
				OpenmrsUtil.fromDateHelper(null, withinLastDays, withinLastMonths, untilDaysAgo, untilMonthsAgo, sinceDate, untilDate),
				OpenmrsUtil.toDateHelper(null, withinLastDays, withinLastMonths, untilDaysAgo, untilMonthsAgo, sinceDate, untilDate),
				discontinued, getDiscontinuedReasonList());
	}

	public boolean isReadyToRun() {
		return getStopDate() != null;
	}
	
	// getters and setters

	public Boolean getDiscontinued() {
		return discontinued;
	}

	public void setDiscontinued(Boolean discontinued) {
		this.discontinued = discontinued;
	}

	public List<Concept> getDiscontinuedReasonList() {
		return discontinuedReasonList;
	}

	public void setDiscontinuedReasonList(List<Concept> discontinuedReasonList) {
		this.discontinuedReasonList = discontinuedReasonList;
	}

	public Date getStopDate() {
		return stopDate;
	}

	public void setStopDate(Date stopDate) {
		this.stopDate = stopDate;
	}

	public Date getSinceDate() {
		return sinceDate;
	}

	public void setSinceDate(Date sinceDate) {
		this.sinceDate = sinceDate;
	}

	public Date getUntilDate() {
		return untilDate;
	}

	public void setUntilDate(Date untilDate) {
		this.untilDate = untilDate;
	}

	public Integer getUntilDaysAgo() {
		return untilDaysAgo;
	}

	public void setUntilDaysAgo(Integer untilDaysAgo) {
		this.untilDaysAgo = untilDaysAgo;
	}

	public Integer getUntilMonthsAgo() {
		return untilMonthsAgo;
	}

	public void setUntilMonthsAgo(Integer untilMonthsAgo) {
		this.untilMonthsAgo = untilMonthsAgo;
	}

	public Integer getWithinLastDays() {
		return withinLastDays;
	}

	public void setWithinLastDays(Integer withinLastDays) {
		this.withinLastDays = withinLastDays;
	}

	public Integer getWithinLastMonths() {
		return withinLastMonths;
	}

	public void setWithinLastMonths(Integer withinLastMonths) {
		this.withinLastMonths = withinLastMonths;
	}

	public List<Drug> getDrugList() {
		return drugList;
	}

	public void setDrugList(List<Drug> drugList) {
		this.drugList = drugList;
	}

	public List<Concept> getGenericDrugList() {
		return genericDrugList;
	}

	public void setGenericDrugList(List<Concept> genericDrugList) {
		this.genericDrugList = genericDrugList;
	}
	
}

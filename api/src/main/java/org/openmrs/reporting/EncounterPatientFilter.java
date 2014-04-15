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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.openmrs.Cohort;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.report.EvaluationContext;
import org.openmrs.util.OpenmrsUtil;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class EncounterPatientFilter extends CachingPatientFilter {
	
	private static final long serialVersionUID = 1L;
	
	private EncounterType encounterType;
	
	private List<EncounterType> encounterTypeList;
	
	private Form form;
	
	private Integer atLeastCount;
	
	private Integer atMostCount;
	
	private Integer withinLastDays;
	
	private Integer withinLastMonths;
	
	private Integer untilDaysAgo;
	
	private Integer untilMonthsAgo;
	
	private Date sinceDate;
	
	private Date untilDate;
	
	private Location location;
	
	public EncounterPatientFilter() {
	}
	
	@Override
	public String getCacheKey() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName()).append(".");
		sb.append(getForm() == null ? null : getForm().getFormId()).append(".");
		sb.append(
		    OpenmrsUtil.fromDateHelper(null, withinLastDays, withinLastMonths, untilDaysAgo, untilMonthsAgo, sinceDate,
		        untilDate)).append(".");
		sb.append(
		    OpenmrsUtil.toDateHelper(null, withinLastDays, withinLastMonths, untilDaysAgo, untilMonthsAgo, sinceDate,
		        untilDate)).append(".");
		sb.append(getAtLeastCount()).append(".");
		sb.append(getAtMostCount()).append(".");
		sb.append(getLocation() == null ? null : getLocation().getLocationId()).append(".");
		if (getEncounterTypeList() != null) {
			for (EncounterType t : getEncounterTypeList()) {
				sb.append(t.getEncounterTypeId()).append(",");
			}
		}
		return sb.toString();
	}
	
	public String getDescription() {
		MessageSourceService msa = Context.getMessageSourceService();
		Locale locale = Context.getLocale();
		StringBuilder ret = new StringBuilder();
		ret.append(msa.getMessage("reporting.patientsWith")).append(" ");
		if (atLeastCount != null || atMostCount != null) {
			if (atLeastCount != null) {
				ret.append(msa.getMessage("reporting.atLeast", new Object[] { atLeastCount }, locale)).append(" ");
			}
			if (atMostCount != null) {
				ret.append(msa.getMessage("reporting.atMost", new Object[] { atMostCount }, locale)).append(" ");
			}
		} else {
			ret.append(msa.getMessage("reporting.any")).append(" ");
		}
		if (encounterTypeList != null) {
			ret.append("[");
			for (Iterator<EncounterType> i = encounterTypeList.iterator(); i.hasNext();) {
				ret.append(" ").append(i.next().getName());
				if (i.hasNext()) {
					ret.append(" ,");
				}
			}
			ret.append(" ] ");
		}
		ret.append(msa.getMessage("reporting.encounters")).append(" ");
		if (location != null) {
			ret.append(msa.getMessage("reporting.at", new Object[] { location.getName() }, locale)).append(" ");
		}
		if (withinLastMonths != null || withinLastDays != null) {
			if (withinLastMonths != null) {
				ret.append(" ").append(
				    msa.getMessage("reporting.withinTheLastMonths", new Object[] { withinLastMonths }, locale));
			}
			if (withinLastDays != null) {
				ret.append(" ").append(
				    msa.getMessage("reporting.withinTheLastDays", new Object[] { withinLastDays }, locale));
			}
		}
		// TODO untilDaysAgo untilMonthsAgo
		if (sinceDate != null) {
			ret.append(msa.getMessage("reporting.onOrAfter", new Object[] { sinceDate }, locale));
		}
		if (untilDate != null) {
			ret.append(msa.getMessage("reporting.onOrBefore", new Object[] { untilDate }, locale));
		}
		if (form != null) {
			ret.append(msa.getMessage("reporting.fromThe", new Object[] { form.getName() }, locale));
		}
		return ret.toString();
	}
	
	@Override
	public Cohort filterImpl(EvaluationContext context) {
		PatientSetService service = Context.getPatientSetService();
		return service.getPatientsHavingEncounters(encounterTypeList, location, form, OpenmrsUtil.fromDateHelper(null,
		    withinLastDays, withinLastMonths, untilDaysAgo, untilMonthsAgo, sinceDate, untilDate), OpenmrsUtil.toDateHelper(
		    null, withinLastDays, withinLastMonths, untilDaysAgo, untilMonthsAgo, sinceDate, untilDate), atLeastCount,
		    atMostCount);
	}
	
	public boolean isReadyToRun() {
		return true;
	}
	
	// getters and setters
	@Deprecated
	public EncounterType getEncounterType() {
		return encounterType;
	}
	
	@Deprecated
	public void setEncounterType(EncounterType encounterType) {
		this.encounterType = encounterType;
		if (getEncounterTypeList() == null) {
			setEncounterTypeList(new ArrayList<EncounterType>());
		}
		getEncounterTypeList().add(encounterType);
	}
	
	public List<EncounterType> getEncounterTypeList() {
		return encounterTypeList;
	}
	
	public void setEncounterTypeList(List<EncounterType> encounterTypeList) {
		this.encounterTypeList = encounterTypeList;
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
	
	public Integer getAtLeastCount() {
		return atLeastCount;
	}
	
	public void setAtLeastCount(Integer atLeastCount) {
		this.atLeastCount = atLeastCount;
	}
	
	public Integer getAtMostCount() {
		return atMostCount;
	}
	
	public void setAtMostCount(Integer atMostCount) {
		this.atMostCount = atMostCount;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public Form getForm() {
		return form;
	}
	
	public void setForm(Form form) {
		this.form = form;
	}
	
}

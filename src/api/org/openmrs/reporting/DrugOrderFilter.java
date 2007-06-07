package org.openmrs.reporting;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.openmrs.Drug;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PatientSetService.GroupMethod;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

public class DrugOrderFilter extends AbstractPatientFilter implements PatientFilter {

	private List<Drug> drugList;
	private PatientSetService.GroupMethod anyOrAll;
	private Integer withinLastDays;
	private Integer withinLastMonths;
	private Integer untilDaysAgo;
	private Integer untilMonthsAgo;
	private Date sinceDate;
	private Date untilDate;
	
	public DrugOrderFilter() {
		super.setType("Patient Filter");
		super.setSubType("Drug Order Filter");	
	}
	
	public String getDescription() {
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Context.getLocale());
		StringBuffer ret = new StringBuffer();
		ret.append("Patients taking ");
		if (getDrugList() == null || getDrugList().size() == 0) {
			if (getAnyOrAll() == GroupMethod.NONE)
				ret.append("no drugs");
			else
				ret.append("any drugs");
		} else {
			ret.append(getAnyOrAll() + " of [");
			for (Iterator<Drug> i = getDrugList().iterator(); i.hasNext(); ) {
				ret.append(i.next().getName());
				if (i.hasNext())
					ret.append(" , ");
			}
			ret.append("]");
		}
		if (getWithinLastDays() != null)
			ret.append(" WithinLastDays = " + getWithinLastDays());
		if (getWithinLastMonths() != null)
			ret.append(" WithinLastMonths = " + getWithinLastMonths());
		if (getSinceDate() != null)
			ret.append(" SinceDate = " + df.format(getSinceDate()));
		if (getUntilDate() != null)
			ret.append(" UntilDate = " + df.format(getUntilDate()));
		return ret.toString();
	}
	
	public PatientSet filter(PatientSet input) {
		List<Integer> drugIds = new ArrayList<Integer>();
		if (getDrugList() != null)
			for (Drug d : getDrugList())
				drugIds.add(d.getDrugId());
		PatientSet ps = Context.getPatientSetService().getPatientsHavingDrugOrder(input.getPatientIds(), drugIds, getAnyOrAll(),  
				OpenmrsUtil.fromDateHelper(null,
					getWithinLastDays(), getWithinLastMonths(),
					getUntilDaysAgo(), getUntilMonthsAgo(),
					getSinceDate(), getUntilDate()),
				OpenmrsUtil.fromDateHelper(null,
					getWithinLastDays(), getWithinLastMonths(),
					getUntilDaysAgo(), getUntilMonthsAgo(),
					getSinceDate(), getUntilDate()));
		
		return input.intersect(ps);
	}

	public PatientSet filterInverse(PatientSet input) {
		List<Integer> drugIds = new ArrayList<Integer>();
		for (Drug d : drugList)
			drugIds.add(d.getDrugId());
		PatientSet ps = Context.getPatientSetService().getPatientsHavingDrugOrder(input.getPatientIds(), drugIds, getAnyOrAll(),  
				OpenmrsUtil.fromDateHelper(null,
					getWithinLastDays(), getWithinLastMonths(),
					getUntilDaysAgo(), getUntilMonthsAgo(),
					getSinceDate(), getUntilDate()),
				OpenmrsUtil.fromDateHelper(null,
					getWithinLastDays(), getWithinLastMonths(),
					getUntilDaysAgo(), getUntilMonthsAgo(),
					getSinceDate(), getUntilDate()));
		
		return input.subtract(ps);
	}

	public boolean isReadyToRun() {
		return true;
	}
	
	// getters and setters

	public PatientSetService.GroupMethod getAnyOrAll() {
		return anyOrAll;
	}

	public void setAnyOrAll(PatientSetService.GroupMethod anyOrAll) {
		this.anyOrAll = anyOrAll;
	}

	public List<Drug> getDrugList() {
		return drugList;
	}

	public void setDrugList(List<Drug> drugList) {
		this.drugList = drugList;
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

}

package org.openmrs.reporting;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PatientSetService.GroupMethod;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

public class DrugOrderFilter extends AbstractPatientFilter implements PatientFilter {

	protected final Log log = LogFactory.getLog(getClass());
	
	private List<Drug> drugList;
	private List<Concept> drugSets;
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
		boolean currentlyCase = getWithinLastDays() != null && getWithinLastDays() == 0 && (getWithinLastMonths() == null || getWithinLastMonths() == 0);
		if (currentlyCase)
			ret.append("Patients currently ");
		else
			ret.append("Patients ");
		if (getDrugListToUse() == null || getDrugListToUse().size() == 0) {
			if (getAnyOrAll() == GroupMethod.NONE)
				ret.append(currentlyCase ? "taking no drugs" : "who never took any drugs");
			else
				ret.append(currentlyCase ? "taking any drugs" : "ever taking any drugs");
		} else {
			if (getDrugListToUse().size() == 1) {
				if (getAnyOrAll() == GroupMethod.NONE)
					ret.append("not taking ");
				else
					ret.append("taking ");
				ret.append(getDrugListToUse().get(0).getName());
			} else {
				ret.append("taking " + getAnyOrAll() + " of [");
				for (Iterator<Drug> i = getDrugListToUse().iterator(); i.hasNext(); ) {
					ret.append(i.next().getName());
					if (i.hasNext())
						ret.append(" , ");
				}
				ret.append("]");
			}
		}
		if (!currentlyCase)
			if (getWithinLastDays() != null || getWithinLastMonths() != null) {
				ret.append(" withing the last");
				if (getWithinLastMonths() != null)
					ret.append(" " + getWithinLastMonths() + " months");
				if (getWithinLastDays() != null)
					ret.append(" " + getWithinLastDays() + " days");
			}
		if (getSinceDate() != null)
			ret.append(" since " + df.format(getSinceDate()));
		if (getUntilDate() != null)
			ret.append(" until " + df.format(getUntilDate()));
		return ret.toString();
	}
	
	public PatientSet filter(PatientSet input) {
		List<Integer> drugIds = new ArrayList<Integer>();
		if (getDrugListToUse() != null)
			for (Drug d : getDrugListToUse())
				drugIds.add(d.getDrugId());
		log.debug("filtering with these ids " + drugIds);
		PatientSet ps = Context.getPatientSetService().getPatientsHavingDrugOrder(input == null ? null : input.getPatientIds(), drugIds, getAnyOrAll(),  
				OpenmrsUtil.fromDateHelper(null,
					getWithinLastDays(), getWithinLastMonths(),
					getUntilDaysAgo(), getUntilMonthsAgo(),
					getSinceDate(), getUntilDate()),
				OpenmrsUtil.fromDateHelper(null,
					getWithinLastDays(), getWithinLastMonths(),
					getUntilDaysAgo(), getUntilMonthsAgo(),
					getSinceDate(), getUntilDate()));
		
		return input == null ? ps : input.intersect(ps);
	}

	public PatientSet filterInverse(PatientSet input) {
		List<Integer> drugIds = new ArrayList<Integer>();
		for (Drug d : getDrugListToUse())
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
	
	public List<Drug> getDrugListToUse() {
		List<Drug> drugList = getDrugList();
		List<Concept> drugSets = getDrugSets();
		if (drugList == null && drugSets == null)
			return null;
		List<Drug> ret = new ArrayList<Drug>();
		if (drugList != null)
			ret.addAll(drugList);
		if (drugSets != null) {
			Set<Concept> generics = new HashSet<Concept>();
			for (Concept drugSet : drugSets) {
				List<Concept> list = Context.getConceptService().getConceptsInSet(drugSet);
				generics.addAll(list);
			}
			for (Concept generic : generics) {
				ret.addAll(Context.getConceptService().getDrugs(generic));
			}
		}
		return ret;
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

	public List<Concept> getDrugSets() {
    	return drugSets;
    }

	public void setDrugSets(List<Concept> drugSets) {
    	this.drugSets = drugSets;
    }

}

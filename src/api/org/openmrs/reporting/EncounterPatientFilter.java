package org.openmrs.reporting;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

public class EncounterPatientFilter extends AbstractPatientFilter implements PatientFilter {

	private EncounterType encounterType;
	private Integer atLeastCount;
	private Integer atMostCount;
	private Integer withinLastDays;
	private Integer withinLastMonths;
	private Integer untilDaysAgo;
	private Integer untilMonthsAgo;
	private Date sinceDate;
	private Date untilDate;
	private Location location;
	
	public EncounterPatientFilter() { }
	
	public String getDescription() {
		Locale locale = Context.getLocale();
		StringBuffer ret = new StringBuffer();
		ret.append("Patients with ");
		if (atLeastCount != null || atMostCount != null) {
			if (atLeastCount != null)
				ret.append("at least " + atLeastCount + " ");
			if (atMostCount != null)
				ret.append("at most " + atMostCount + " ");
		} else {
			ret.append("any ");
		}
		if (encounterType != null)
			ret.append(encounterType.getName() + " ");
		ret.append("encounters ");
		if (location != null) {
			ret.append("at " + location.getName() + " ");
		}
		if (withinLastMonths != null || withinLastDays != null) {
			ret.append("within the last ");
			if (withinLastMonths != null)
				ret.append(withinLastMonths + " month(s) ");
			if (withinLastDays != null)
				ret.append(withinLastDays + " day(s) ");
		}
		// TODO untilDaysAgo untilMonthsAgo
		if (sinceDate != null)
			ret.append("on or after " + sinceDate + " ");
		if (untilDate != null)
			ret.append("on or before " + untilDate + " ");
		return ret.toString();
	}
	
	public PatientSet filter(PatientSet input) {
		PatientSetService service = Context.getPatientSetService();
		return input.intersect(service.getPatientsHavingEncounters(encounterType, location,
				OpenmrsUtil.fromDateHelper(null, withinLastDays, withinLastMonths, untilDaysAgo, untilMonthsAgo, sinceDate, untilDate),
				OpenmrsUtil.toDateHelper(null, withinLastDays, withinLastMonths, untilDaysAgo, untilMonthsAgo, sinceDate, untilDate),
				atLeastCount, atMostCount));
	}

	public PatientSet filterInverse(PatientSet input) {
		PatientSetService service = Context.getPatientSetService();
		return input.subtract(service.getPatientsHavingEncounters(encounterType, location, fromDateHelper(), toDateHelper(), atLeastCount, atMostCount));
	}

	public boolean isReadyToRun() {
		return true;
	}
	
	private Date fromDateHelper() {
		Date ret = null;
		if (withinLastDays != null || withinLastMonths != null) {
			Calendar gc = new GregorianCalendar();
			if (withinLastDays != null)
				gc.add(Calendar.DAY_OF_MONTH, -withinLastDays);
			if (withinLastMonths != null)
				gc.add(Calendar.MONTH, -withinLastMonths);
			ret = gc.getTime();
		}
		if (sinceDate != null && (ret == null || sinceDate.after(ret)))
			ret = sinceDate;
		return ret;
	}
	
	private Date toDateHelper() {
		Date ret = null;
		if (untilDaysAgo != null || untilMonthsAgo != null) {
			Calendar gc = new GregorianCalendar();
			if (untilDaysAgo != null)
				gc.add(Calendar.DAY_OF_MONTH, -untilDaysAgo);
			if (untilMonthsAgo != null)
				gc.add(Calendar.MONTH, -untilMonthsAgo);
			ret = gc.getTime();
		}
		if (untilDate != null && (ret == null || untilDate.before(ret)))
			ret = untilDate;
		return ret;
	}
	
	// getters and setters

	public EncounterType getEncounterType() {
		return encounterType;
	}

	public void setEncounterType(EncounterType encounterType) {
		this.encounterType = encounterType;
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

}

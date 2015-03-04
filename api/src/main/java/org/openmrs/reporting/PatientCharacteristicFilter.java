/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

import java.text.DateFormat;
import java.util.Date;

import org.openmrs.Cohort;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.report.EvaluationContext;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class PatientCharacteristicFilter extends CachingPatientFilter implements Comparable<PatientCharacteristicFilter> {
	
	private String gender;
	
	private Date minBirthdate;
	
	private Date maxBirthdate;
	
	private Integer minAge;
	
	private Integer maxAge;
	
	private Boolean aliveOnly;
	
	private Boolean deadOnly;
	
	private Date effectiveDate;
	
	public PatientCharacteristicFilter() {
		super.setType("Patient Filter");
		super.setSubType("Patient Characteristic Filter");
	}
	
	public PatientCharacteristicFilter(String gender, Date minBirthdate, Date maxBirthdate) {
		super.setType("Patient Filter");
		super.setSubType("Patient Characteristic Filter");
		this.gender = gender == null ? null : gender.toUpperCase();
		this.minBirthdate = minBirthdate;
		this.maxBirthdate = maxBirthdate;
	}
	
	@Override
	public String getCacheKey() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName()).append(".");
		sb.append(getGender()).append(".");
		sb.append(getMinBirthdate()).append(".");
		sb.append(getMaxBirthdate()).append(".");
		sb.append(getMinAge()).append(".");
		sb.append(getMaxAge()).append(".");
		sb.append(getAliveOnly()).append(".");
		sb.append(getDeadOnly()).append(".");
		sb.append(getEffectiveDate());
		return sb.toString();
	}
	
	public boolean isReadyToRun() {
		return true;
	}
	
	public int compareTo(PatientCharacteristicFilter o) {
		return -compareHelper().compareTo(o.compareHelper());
	}
	
	private Integer compareHelper() {
		int ret = 0;
		if (deadOnly != null)
			ret += deadOnly ? 2 : 1;
		if (aliveOnly != null)
			ret += aliveOnly ? 20 : 10;
		if (minAge != null)
			ret += minAge * 100;
		if (maxAge != null)
			ret += maxAge * 1000;
		if (gender != null)
			ret += gender.equals("M") ? 1000000 : 2000000;
		return ret;
	}
	
	public String getDescription() {
		MessageSourceService msa = Context.getMessageSourceService();
		if (gender == null && minBirthdate == null && maxBirthdate == null && minAge == null && maxAge == null
		        && aliveOnly == null && deadOnly == null)
			return msa.getMessage("reporting.allPatients");
		
		StringBuilder ret = new StringBuilder();
		if (gender != null) {
			if ("M".equals(gender)) {
				ret.append(msa.getMessage("reporting.male"));
			} else {
				ret.append(msa.getMessage("reporting.female"));
			}
		}
		ret.append(gender == null ? msa.getMessage("reporting.patients") + " " : " "
		        + msa.getMessage("reporting.patients").toLowerCase() + " ");
		
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Context.getLocale());
		
		if (minBirthdate != null) {
			if (maxBirthdate != null) {
				ret.append(" ").append(
				    msa.getMessage("reporting.bornBetween", new Object[] { (Object) df.format(minBirthdate),
				            (Object) df.format(maxBirthdate) }, Context.getLocale()));
			} else {
				ret.append(" ").append(msa.getMessage("reporting.bornAfter")).append(" ").append(df.format(minBirthdate));
			}
		} else {
			if (maxBirthdate != null) {
				ret.append(" ").append(msa.getMessage("reporting.bornBefore")).append(" ").append(df.format(maxBirthdate));
			}
		}
		if (minAge != null) {
			if (maxAge != null) {
				ret.append(" ").append(
				    msa.getMessage("reporting.betweenTheAgesOf", new Object[] { (Object) minAge, (Object) maxAge }, Context
				            .getLocale()));
				
			} else {
				ret.append(" ").append(
				    msa.getMessage("reporting.atLeastYearsOld", new Object[] { minAge }, Context.getLocale()));
			}
		} else {
			if (maxAge != null) {
				ret.append(" ").append(
				    msa.getMessage("reporting.upToYearsOld", new Object[] { maxAge }, Context.getLocale()));
			}
		}
		if (aliveOnly != null && aliveOnly) {
			ret.append(" ").append(msa.getMessage("reporting.whoAreAlive"));
		}
		if (deadOnly != null && deadOnly) {
			ret.append(" ").append(msa.getMessage("reporting.whoAreDead"));
		}
		return ret.toString();
	}
	
	/**
	 * @return Returns the gender.
	 */
	public String getGender() {
		return gender;
	}
	
	/**
	 * @param gender The gender to set.
	 */
	public void setGender(String gender) {
		this.gender = null;
		if (gender != null) {
			gender = gender.toUpperCase();
			if ("M".equals(gender) || "F".equals(gender)) {
				this.gender = gender;
			}
		}
	}
	
	/**
	 * @return Returns the maxBirthdate.
	 */
	public Date getMaxBirthdate() {
		return maxBirthdate;
	}
	
	/**
	 * @param maxBirthdate The maxBirthdate to set.
	 */
	public void setMaxBirthdate(Date maxBirthdate) {
		this.maxBirthdate = maxBirthdate;
	}
	
	/**
	 * @return Returns the minBirthdate.
	 */
	public Date getMinBirthdate() {
		return minBirthdate;
	}
	
	/**
	 * @param minBirthdate The minBirthdate to set.
	 */
	public void setMinBirthdate(Date minBirthdate) {
		this.minBirthdate = minBirthdate;
	}
	
	public Boolean getAliveOnly() {
		return aliveOnly;
	}
	
	public void setAliveOnly(Boolean aliveOnly) {
		this.aliveOnly = aliveOnly;
	}
	
	public Boolean getDeadOnly() {
		return deadOnly;
	}
	
	public void setDeadOnly(Boolean deadOnly) {
		this.deadOnly = deadOnly;
	}
	
	public Integer getMaxAge() {
		return maxAge;
	}
	
	public void setMaxAge(Integer maxAge) {
		this.maxAge = maxAge;
	}
	
	public Integer getMinAge() {
		return minAge;
	}
	
	public void setMinAge(Integer minAge) {
		this.minAge = minAge;
	}
	
	public Date getEffectiveDate() {
		return effectiveDate;
	}
	
	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	
	@Override
	public Cohort filterImpl(EvaluationContext context) {
		PatientSetService service = Context.getPatientSetService();
		return service.getPatientsByCharacteristics(gender, minBirthdate, maxBirthdate, minAge, maxAge, aliveOnly, deadOnly,
		    effectiveDate);
	}
	
}

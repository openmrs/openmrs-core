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

import java.text.DateFormat;
import java.util.Date;

import org.openmrs.Cohort;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.context.Context;
import org.openmrs.report.EvaluationContext;

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
	
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o instanceof PatientCharacteristicFilter) {
			PatientCharacteristicFilter other = (PatientCharacteristicFilter) o;
			return equals(gender, other.gender) && equals(minBirthdate, other.minBirthdate)
			        && equals(maxBirthdate, other.maxBirthdate) && equals(minAge, other.minAge)
			        && equals(maxAge, other.maxAge) && equals(aliveOnly, other.aliveOnly)
			        && equals(deadOnly, other.deadOnly);
		} else {
			return false;
		}
	}
	
	public String getDescription() {
		if (gender == null && minBirthdate == null && maxBirthdate == null && minAge == null && maxAge == null
		        && aliveOnly == null && deadOnly == null)
			return "All Patients";
		
		StringBuffer ret = new StringBuffer();
		if (gender != null) {
			if ("M".equals(gender)) {
				ret.append("Male");
			} else {
				ret.append("Female");
			}
		}
		ret.append(gender == null ? "Patients " : " patients ");
		
		DateFormat df = null;
		if (minBirthdate != null || maxBirthdate != null) {
			df = DateFormat.getDateInstance(DateFormat.SHORT, Context.getLocale());
		}
		if (minBirthdate != null) {
			if (maxBirthdate != null) {
				ret.append(" born between " + df.format(minBirthdate) + " and " + df.format(maxBirthdate));
			} else {
				ret.append(" born after " + df.format(minBirthdate));
			}
		} else {
			if (maxBirthdate != null) {
				ret.append(" born before " + df.format(maxBirthdate));
			}
		}
		if (minAge != null) {
			if (maxAge != null) {
				ret.append(" between the ages of " + minAge + " and " + maxAge);
			} else {
				ret.append(" at least " + minAge + " years old");
			}
		} else {
			if (maxAge != null) {
				ret.append(" up to " + maxAge + " years old");
			}
		}
		if (aliveOnly != null && aliveOnly) {
			ret.append(" who are alive");
		}
		if (deadOnly != null && deadOnly) {
			ret.append(" who are dead");
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

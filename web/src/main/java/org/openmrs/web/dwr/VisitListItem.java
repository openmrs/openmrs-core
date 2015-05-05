/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonName;
import org.openmrs.Visit;
import org.openmrs.util.Format;

/**
 * Represents a DWR List Item for a visit
 *
 * @since 1.9
 */
public class VisitListItem {
	
	protected static final Log log = LogFactory.getLog(VisitListItem.class);
	
	private Integer visitId;
	
	private String visitType;
	
	private String personName;
	
	private String location;
	
	private String indicationConcept;
	
	private Date startDatetime;
	
	private Date stopDatetime;
	
	private String startDatetimeString;
	
	private String stopDatetimeString;
	
	private boolean voided = false;
	
	public VisitListItem() {
	}
	
	public VisitListItem(Visit visit) {
		
		if (visit != null) {
			visitId = visit.getVisitId();
			visitType = visit.getVisitType().getName();
			startDatetime = visit.getStartDatetime();
			startDatetimeString = Format.format(visit.getStartDatetime());
			if (visit.getStopDatetime() != null) {
				stopDatetime = visit.getStopDatetime();
				stopDatetimeString = Format.format(visit.getStopDatetime());
			}
			PersonName pn = visit.getPatient().getPersonName();
			if (pn != null) {
				personName = "";
				if (pn.getGivenName() != null) {
					personName += pn.getGivenName();
				}
				if (pn.getMiddleName() != null) {
					personName += " " + pn.getMiddleName();
				}
				if (pn.getFamilyName() != null) {
					personName += " " + pn.getFamilyName();
				}
			}
			
			if (visit.getLocation() != null) {
				location = visit.getLocation().getName();
			}
			
			if (visit.getIndication() != null && visit.getIndication().getName() != null) {
				indicationConcept = visit.getIndication().getName().getName();
			}
			
			voided = visit.isVoided();
		}
	}
	
	/**
	 * @return the visitId
	 */
	public Integer getVisitId() {
		return visitId;
	}
	
	/**
	 * @param visitId the visitId to set
	 */
	public void setVisitId(Integer visitId) {
		this.visitId = visitId;
	}
	
	/**
	 * @return the visitType
	 */
	public String getVisitType() {
		return visitType;
	}
	
	/**
	 * @param visitType the visitType to set
	 */
	public void setVisitType(String visitType) {
		this.visitType = visitType;
	}
	
	/**
	 * @return the personName
	 */
	public String getPersonName() {
		return personName;
	}
	
	/**
	 * @param personName the personName to set
	 */
	public void setPersonName(String personName) {
		this.personName = personName;
	}
	
	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}
	
	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	
	/**
	 * @return the indicationConcept
	 */
	public String getIndicationConcept() {
		return indicationConcept;
	}
	
	/**
	 * @param indicationConcept the indicationConcept to set
	 */
	public void setIndicationConcept(String indicationConcept) {
		this.indicationConcept = indicationConcept;
	}
	
	/**
	 * @return the startDatetime
	 */
	public Date getStartDatetime() {
		return startDatetime;
	}
	
	/**
	 * @param startDatetime the startDatetime to set
	 */
	public void setStartDatetime(Date startDatetime) {
		this.startDatetime = startDatetime;
	}
	
	/**
	 * @return the stopDatetime
	 */
	public Date getStopDatetime() {
		return stopDatetime;
	}
	
	/**
	 * @param stopDatetime the stopDatetime to set
	 */
	public void setStopDatetime(Date stopDatetime) {
		this.stopDatetime = stopDatetime;
	}
	
	/**
	 * @return the startDatetimeString
	 */
	public String getStartDatetimeString() {
		return startDatetimeString;
	}
	
	/**
	 * @param startDatetimeString the startDatetimeString to set
	 */
	public void setStartDatetimeString(String startDatetimeString) {
		this.startDatetimeString = startDatetimeString;
	}
	
	/**
	 * @return the stopDatetimeString
	 */
	public String getStopDatetimeString() {
		return stopDatetimeString;
	}
	
	/**
	 * @param stopDatetimeString the stopDatetimeString to set
	 */
	public void setStopDatetimeString(String stopDatetimeString) {
		this.stopDatetimeString = stopDatetimeString;
	}
	
	/**
	 * @return the voided
	 */
	public boolean isVoided() {
		return voided;
	}
	
	/**
	 * @param voided the voided to set
	 */
	public void setVoided(boolean voided) {
		this.voided = voided;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VisitListItem)) {
			return false;
		}
		VisitListItem rhs = (VisitListItem) obj;
		if (this.visitId != null && rhs.visitId != null) {
			return (this.visitId.equals(rhs.visitId));
		}
		
		return this == obj;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.visitId == null) {
			return super.hashCode();
		}
		int hash = 5;
		hash = hash + 51 * this.visitId;
		return hash;
	}
}

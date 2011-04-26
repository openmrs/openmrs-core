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
package org.openmrs;

import java.util.Date;

import org.openmrs.attribute.AttributeHolder;

/**
 * A 'visit' is a contiguous time period where encounters occur between patients and healthcare
 * providers. This can function as a grouper for encounters
 * 
 * @since 1.9
 */

public class Visit extends BaseAttributableData<VisitAttribute> implements Auditable, AttributeHolder<VisitAttribute> {
	
	private Integer visitId;
	
	private Patient patient;
	
	private VisitType visitType;
	
	private Concept indication;
	
	private Location location;
	
	private Date startDatetime;
	
	private Date stopDatetime;
	
	/**
	 * Default Constructor
	 */
	public Visit() {
	}
	
	/**
	 * Constructor that takes in a visitId
	 * 
	 * @param visitId
	 */
	public Visit(Integer visitId) {
		this.visitId = visitId;
	}
	
	/**
	 * Convenience constructor that takes in the required fields i.e {@link Patient},
	 * {@link VisitType} and dateStarted
	 * 
	 * @see VisitType
	 * @param patient the patient asscociated to this visit
	 * @param visitType The type of visit
	 * @param startDatetime the date this visit was started
	 */
	public Visit(Patient patient, VisitType visitType, Date startDatetime) {
		this.patient = patient;
		this.visitType = visitType;
		this.startDatetime = startDatetime;
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
	 * @return the patient
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * @param patient the patient to set
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	/**
	 * @return the visitType
	 */
	public VisitType getVisitType() {
		return visitType;
	}
	
	/**
	 * @param visitType the visitType to set
	 */
	public void setVisitType(VisitType visitType) {
		this.visitType = visitType;
	}
	
	/**
	 * @return the indication
	 */
	public Concept getIndication() {
		return indication;
	}
	
	/**
	 * @param indication the indication to set
	 */
	public void setIndication(Concept indication) {
		this.indication = indication;
	}
	
	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}
	
	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
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
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return visitId;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		visitId = id;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Visit)) {
			return false;
		}
		Visit rhs = (Visit) obj;
		if (this.visitId != null && rhs.visitId != null)
			return (this.visitId.equals(rhs.visitId));
		
		return this == obj;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.visitId == null)
			return super.hashCode();
		int hash = 3;
		hash = hash + 31 * this.visitId;
		return hash;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Visit #" + visitId;
	}
	
}

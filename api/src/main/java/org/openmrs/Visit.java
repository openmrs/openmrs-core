/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;
import org.openmrs.customdatatype.Customizable;

/**
 * A 'visit' is a contiguous time period where encounters occur between patients and healthcare
 * providers. This can function as a grouper for encounters
 * 
 * @since 1.9
 */
@Entity
@Table(name = "visit")
public class Visit extends BaseCustomizableData<VisitAttribute> implements Auditable, Customizable<VisitAttribute> {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "visit_id")
	private Integer visitId;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "patient_id")
	private Patient patient;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "visit_type_id")
	private VisitType visitType;
	
	@ManyToOne
	@JoinColumn(name = "indication_concept_id")
	private Concept indication;
	
	@ManyToOne
	@JoinColumn(name = "location_id")
	private Location location;
	
	@Column(name = "date_started", nullable = false, length = 19)
	private Date startDatetime;
	
	@Column(name = "date_stopped", length = 19)
	private Date stopDatetime;
	
	@OneToMany(mappedBy = "visit")
	@OrderBy("encounter_datetime desc, encounter_id desc")
	private Set<Encounter> encounters;
	
	@Access(AccessType.PROPERTY)
	@OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("voided asc")
	@BatchSize(size = 100)
	private Set<VisitAttribute> attributes = new LinkedHashSet<>();
	
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
	 * @param patient the patient associated to this visit
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
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Visit #" + visitId;
	}
	
	/**
	 * @return the encounters
	 */
	public Set<Encounter> getEncounters() {
		if (encounters == null) {
			encounters = new HashSet<>();
		}
		return encounters;
	}
	
	/**
	 * @param encounters the encounters to set
	 */
	public void setEncounters(Set<Encounter> encounters) {
		this.encounters = encounters;
	}
	
	/**
	 * Gets a list of non voided encounters
	 * 
	 * @return the non voided encounter list
	 * @since 1.11.0, 1.12.0
	 */
	public List<Encounter> getNonVoidedEncounters() {
		return getEncounters().stream().filter(e -> !e.getVoided()).collect(Collectors.toList());
	}
	
	/**
	 * adds an individual encounter to a visit
	 *
	 * @param encounter the encounter to add
	 * @since 1.9.2, 1.10.0
	 */
	public void addEncounter(Encounter encounter) {
		if (encounter != null) {
			encounter.setVisit(this);
			getEncounters().add(encounter);
		}
	}
}

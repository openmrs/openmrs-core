/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.event;

import java.util.Date;

import org.openmrs.OpenmrsObject;
import org.openmrs.Patient;

/**
 * Represents a clinical data change event. Published when clinical entities (encounters,
 * observations, conditions, diagnoses, orders, etc.) are created, updated, or voided.
 * <p>
 * AI modules and other consumers can subscribe to these events to keep their indexes
 * (embeddings, vector stores, search indexes) up to date in real time.
 *
 * @since 3.0.0
 */
public class ClinicalEvent {
	
	private ClinicalEventType eventType;
	
	private Class<? extends OpenmrsObject> entityType;
	
	private String entityUuid;
	
	private Patient patient;
	
	private Date timestamp;
	
	public ClinicalEvent() {
		this.timestamp = new Date();
	}
	
	public ClinicalEvent(ClinicalEventType eventType, Class<? extends OpenmrsObject> entityType, String entityUuid,
	    Patient patient) {
		this.eventType = eventType;
		this.entityType = entityType;
		this.entityUuid = entityUuid;
		this.patient = patient;
		this.timestamp = new Date();
	}
	
	/**
	 * @return the type of change that occurred
	 */
	public ClinicalEventType getEventType() {
		return eventType;
	}
	
	/**
	 * @param eventType the type of change that occurred
	 */
	public void setEventType(ClinicalEventType eventType) {
		this.eventType = eventType;
	}
	
	/**
	 * @return the class of the entity that changed (e.g., Obs.class, Encounter.class)
	 */
	public Class<? extends OpenmrsObject> getEntityType() {
		return entityType;
	}
	
	/**
	 * @param entityType the class of the entity that changed
	 */
	public void setEntityType(Class<? extends OpenmrsObject> entityType) {
		this.entityType = entityType;
	}
	
	/**
	 * @return the UUID of the entity that changed
	 */
	public String getEntityUuid() {
		return entityUuid;
	}
	
	/**
	 * @param entityUuid the UUID of the entity that changed
	 */
	public void setEntityUuid(String entityUuid) {
		this.entityUuid = entityUuid;
	}
	
	/**
	 * @return the patient associated with this clinical event
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * @param patient the patient associated with this clinical event
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	/**
	 * @return the timestamp when this event occurred
	 */
	public Date getTimestamp() {
		return timestamp;
	}
	
	/**
	 * @param timestamp the timestamp when this event occurred
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}

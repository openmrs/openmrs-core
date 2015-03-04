/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification;

import java.io.Serializable;
import java.util.Date;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Patient;

/**
 * Not currently used.
 */
public class Note extends BaseOpenmrsData implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5392076713513109152L;
	
	// Data
	
	/**
	 * noteId, an identifier for a patient note.
	 */
	private Integer noteId;
	
	private String text;
	
	private Date dateChanged;
	
	private Date dateCreated;
	
	private Integer priority;
	
	private Integer weight;
	
	// Relationships
	private Patient patient;
	
	private NoteType noteType;
	
	//private Observation observation;
	//private Encounter encounter;
	//private User createdBy;		
	//private User changedBy;
	
	public Note() {
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setNoteId(noteId);
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getNoteId();
	}
	
	public Date getDateChanged() {
		return dateChanged;
	}
	
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public NoteType getNoteType() {
		return noteType;
	}
	
	public void setNoteType(NoteType noteType) {
		this.noteType = noteType;
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public Integer getPriority() {
		return priority;
	}
	
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public Integer getWeight() {
		return weight;
	}
	
	public void setWeight(Integer weight) {
		this.weight = weight;
	}
	
	/**
	 * @return Returns the noteId.
	 */
	public Integer getNoteId() {
		return noteId;
	}
	
	/**
	 * @param noteId the noteId to set.
	 */
	public void setNoteId(Integer noteId) {
		this.noteId = noteId;
	}
}

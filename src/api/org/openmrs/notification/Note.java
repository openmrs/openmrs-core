package org.openmrs.notification;

import java.io.Serializable;
import java.util.Date;

import org.openmrs.Patient;

/**
 * Not currently used.
 *   
 * @author Justin Miranda
 *
 */
public class Note implements Serializable {    

	/**
	 * 
	 */
	private static final long serialVersionUID = -5392076713513109152L;

	// Data
	private Integer id;	
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
	
	
	public Note() {  }
	
	public void setId(Integer id) { 
		this.id = id; 
	}
	
	public Integer getId() { 
		return this.id; 
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
	
}

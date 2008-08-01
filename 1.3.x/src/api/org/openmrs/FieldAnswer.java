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

/**
 * FieldAnswer 
 * @version 1.0
 */
public class FieldAnswer implements java.io.Serializable {

	public static final long serialVersionUID = 5656L;

	// Fields

	private Date dateCreated;
	private Concept concept;
	private User creator;
	private Field field;
	private boolean dirty;

	// Constructors

	/** default constructor */
	public FieldAnswer() {
	}

	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof FieldAnswer) {
			FieldAnswer fa = (FieldAnswer)obj;
			return (field.equals(fa.getField()) &&
					concept.equals(fa.getConcept()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getField() == null || this.getConcept() == null) return super.hashCode();
		return this.getConcept().hashCode() + this.getField().hashCode();
	}

	/**
	 * 
	 * @return boolean whether or not this fieldAnswer has been modified
	 */
	public boolean isDirty() {
		return dirty;
	}
	
	public void setClean() {
		dirty = false;
	}
	
	// Property accessors

	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}

	/**
	 * @param concept The concept to set.
	 */
	public void setConcept(Concept concept) {
		this.dirty = true;
		this.concept = concept;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(User creator) {
		this.dirty = true;
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dirty = true;
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the field.
	 */
	public Field getField() {
		return field;
	}

	/**
	 * @param field The field to set.
	 */
	public void setField(Field field) {
		this.dirty = true;
		this.field = field;
	}
	
}
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

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * FieldAnswer
 * 
 * @version 1.0
 */
public class FieldAnswer extends BaseOpenmrsObject {
	
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
	 * @return boolean whether or not this fieldAnswer has been modified
	 *
	 * @deprecated as of 2.0, use {@link #getDirty()}
	 */
	@Deprecated
	@JsonIgnore
	public boolean isDirty() {
		return getDirty();
	}
	
	/**
	 * @return boolean whether or not this fieldAnswer has been modified
	 */
	public boolean getDirty() {
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
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		throw new UnsupportedOperationException();
	}
	
}

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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * FieldAnswer
 * 
 * @version 1.0
 */
@Entity
@Table(name = "field_answer")
public class FieldAnswer extends BaseOpenmrsObject implements java.io.Serializable {
	
	public static final long serialVersionUID = 5656L;
	
	// Fields
	@Column(name = "date_created", length = 19)
	private Date dateCreated;
	
	@ManyToOne
	@JoinColumn(name = "answer_id")
	@Id
	private Concept concept;
	
	@ManyToOne
	@JoinColumn(name = "creator", nullable = false)
	private User creator;
	

	@ManyToOne
	@JoinColumn(name = "field_id")
	@Id
	private Field field;
	
	@Transient
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
	public Integer getId() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		throw new UnsupportedOperationException();
	}
	
}

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

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
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
public class FieldAnswer extends BaseOpenmrsObject {

	public static final long serialVersionUID = 5656L;

	// Fields

	@EmbeddedId
	private PK FieldAnswerId;

	@Transient
	private Date dateCreated;

	@ManyToOne
	@JoinColumn(name = "creator", nullable = false)
	private User creator;

	@Transient
	private boolean dirty;

	@Embeddable
	private class PK implements Serializable {

		@ManyToOne
		@JoinColumn(name="answer_id")
		private Concept concept;

		@ManyToOne
		@JoinColumn(name="field_id")
		private Field field;

		public PK(){
		}

		public PK(Field field, Concept concept) {
			this.field = field;
			this.concept = concept;
		}

		public Concept getConcept() {
			return concept;
		}

		public void setConcept(Concept concept) {
			setDirty(true);
			this.concept = concept;
		}

		public Field getField() {
			setDirty(true);
			return field;
		}

		public void setField(Field field) {
			this.field = field;
		}
	}
	
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

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	// Property accessors
	
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

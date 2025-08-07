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

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

/**
 * Superclass for {@link ConceptMap}s and {@link ConceptReferenceTermMap}s
 * 
 * @since 1.9
 */
@MappedSuperclass
public abstract class BaseConceptMap extends BaseOpenmrsObject implements Auditable {
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "a_is_to_b_id", nullable = false)
	private ConceptMapType conceptMapType;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "creator", nullable = false)
	private User creator;
	
	@ManyToOne
	@JoinColumn(name = "changed_by")
	private User changedBy;
	
	@Column(name = "date_created", nullable = false)
	private Date dateCreated;
	
	@Column(name = "date_changed")
	private Date dateChanged;
	
	/**
	 * @return the conceptMapType
	 */
	public ConceptMapType getConceptMapType() {
		return conceptMapType;
	}
	
	/**
	 * @param conceptMapType the conceptMapType to set
	 */
	public void setConceptMapType(ConceptMapType conceptMapType) {
		this.conceptMapType = conceptMapType;
	}
	
	/**
	 * @return the creator
	 */
	@Override
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @param creator the creator to set
	 */
	@Override
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @return the changedBy
	 */
	@Override
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @param changedBy the changedBy to set
	 */
	@Override
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @return the dateCreated
	 */
	@Override
	public Date getDateCreated() {
		if(dateCreated == null) {
			return null;
		}
		return (Date) dateCreated.clone();
	}
	
	/**
	 * @param dateCreated the dateCreated to set
	 */
	@Override
	public void setDateCreated(Date dateCreated) {
		if(dateCreated == null) {
			this.dateCreated = null;
			return;
		}
		this.dateCreated = new Date(dateCreated.getTime());
	}
	
	/**
	 * @return the dateChanged
	 */
	@Override
	public Date getDateChanged() {
		if(dateChanged == null) {
			return null;
		}
		return (Date) dateChanged.clone();
	}
	
	/**
	 * @param dateChanged the dateChanged to set
	 */
	@Override
	public void setDateChanged(Date dateChanged) {
		if(dateChanged == null) {
			this.dateChanged = null;
			return;
		}
		this.dateChanged = new Date(dateChanged.getTime());
	}
	
}

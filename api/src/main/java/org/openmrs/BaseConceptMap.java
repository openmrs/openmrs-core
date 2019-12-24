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

/**
 * Superclass for {@link ConceptMap}s and {@link ConceptReferenceTermMap}s
 * 
 * @since 1.9
 */
public abstract class BaseConceptMap extends BaseOpenmrsObject implements Auditable {
	
	private ConceptMapType conceptMapType;
	
	private User creator;
	
	private User changedBy;
	
	private Date dateCreated;
	
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

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
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @param creator the creator to set
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @return the changedBy
	 */
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @param changedBy the changedBy to set
	 */
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @return the dateChanged
	 */
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @param dateChanged the dateChanged to set
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
}

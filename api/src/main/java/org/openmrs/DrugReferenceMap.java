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

import org.simpleframework.xml.Attribute;

import java.io.Serializable;
import java.util.Date;

/**
 * The DrugReferenceMap map object represents a mapping between a drug and alternative drug terminologies.
 *
 * @since 1.10
 */
public class DrugReferenceMap extends BaseOpenmrsObject implements Auditable, Retireable, Serializable {
	
	public static final long serialVersionUID = 2806L;
	
	private Integer drugReferenceMapId;
	
	private Drug drug;
	
	private ConceptReferenceTerm conceptReferenceTerm;
	
	private ConceptMapType conceptMapType;
	
	private User creator;
	
	private Date dateCreated;
	
	private Boolean retired = false;
	
	private User retiredBy;
	
	private Date dateRetired;
	
	private User changedBy;
	
	private Date dateChanged;
	
	private String retireReason;
	
	/**
	 * @return Returns the drugReferenceMapId.
	 */
	public Integer getDrugReferenceMapId() {
		return drugReferenceMapId;
	}
	
	/**
	 * @param drugReferenceMapId The drugReferenceMapId to set.
	 */
	public void setDrugReferenceMapId(Integer drugReferenceMapId) {
		this.drugReferenceMapId = drugReferenceMapId;
	}
	
	/**
	 * @return Returns the drug.
	 */
	public Drug getDrug() {
		return drug;
	}
	
	/**
	 * @param drug The drug to set.
	 */
	public void setDrug(Drug drug) {
		this.drug = drug;
	}
	
	/**
	 * @return Returns the conceptReferenceTerm.
	 */
	public ConceptReferenceTerm getConceptReferenceTerm() {
		return conceptReferenceTerm;
	}
	
	/**
	 * @param conceptReferenceTerm The conceptReferenceTerm to set.
	 */
	public void setConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm) {
		this.conceptReferenceTerm = conceptReferenceTerm;
	}
	
	/**
	 * @return Returns the conceptMapType.
	 */
	public ConceptMapType getConceptMapType() {
		return conceptMapType;
	}
	
	/**
	 * @param conceptMapType The conceptMapType to set.
	 */
	public void setConceptMapType(ConceptMapType conceptMapType) {
		this.conceptMapType = conceptMapType;
	}
	
	/**
	 * @return id - The unique Identifier for the object
	 */
	@Override
	public Integer getId() {
		return getDrugReferenceMapId();
	}
	
	/**
	 * @param id - The unique Identifier for the object
	 */
	@Override
	public void setId(Integer id) {
		setDrugReferenceMapId(id);
	}
	
	/**
	 * @return User - the user who created the object
	 */
	@Override
	public User getCreator() {
		return this.creator;
	}
	
	/**
	 * @param creator - the user who created the object
	 */
	@Override
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @return Date - the date the object was created
	 */
	@Override
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @param dateCreated - the date the object was created
	 */
	@Override
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @return User - the user who last changed the object
	 */
	@Override
	public User getChangedBy() {
		return this.changedBy;
	}
	
	/**
	 * @param changedBy - the user who last changed the object
	 */
	@Override
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @return Date - the date the object was last changed
	 */
	@Override
	public Date getDateChanged() {
		return this.dateChanged;
	}
	
	/**
	 * @param dateChanged - the date the object was last changed
	 */
	@Override
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	/**
	 * @return Boolean - whether of not this object is retired
	 */
	@Override
	public Boolean isRetired() {
		return this.retired;
	}
	
	/**
	 * This method exists to satisfy spring and hibernates slightly bung use of Boolean object
	 * getters and setters.
	 *
	 * @see org.openmrs.Concept#isRetired()
	 * @deprecated Use the "proper" isRetired method.
	 */
	@Deprecated
	@Attribute
	public Boolean getRetired() {
		return isRetired();
	}
	
	/**
	 * @param retired - whether of not this object is retired
	 */
	@Override
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	
	/**
	 * @return User - the user who retired the object
	 */
	@Override
	public User getRetiredBy() {
		return this.retiredBy;
	}
	
	/**
	 * @param retiredBy - the user who retired the object
	 */
	@Override
	public void setRetiredBy(User retiredBy) {
		this.retiredBy = retiredBy;
	}
	
	/**
	 * @return Date - the date the object was retired
	 */
	@Override
	public Date getDateRetired() {
		return dateRetired;
	}
	
	/**
	 * @param dateRetired - the date the object was retired
	 */
	@Override
	public void setDateRetired(Date dateRetired) {
		this.dateRetired = dateRetired;
	}
	
	/**
	 * @return String - the reason the object was retired
	 */
	@Override
	public String getRetireReason() {
		return this.retireReason;
	}
	
	/**
	 * @param retireReason - the reason the object was retired
	 */
	@Override
	public void setRetireReason(String retireReason) {
		this.retireReason = retireReason;
	}
}

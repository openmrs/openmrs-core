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

import java.io.Serializable;
import java.util.Date;

import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * The DrugReferenceMap map object represents a mapping between a drug and alternative drug
 * terminologies.
 * 
 * @since 1.10
 */
public class DrugReferenceMap extends BaseOpenmrsObject implements Auditable, Serializable {
	
	public static final long serialVersionUID = 1L;
	
	@DocumentId
	private Integer drugReferenceMapId;
	
	@ContainedIn
	private Drug drug;
	
	@IndexedEmbedded
	private ConceptReferenceTerm conceptReferenceTerm;
	
	private ConceptMapType conceptMapType;
	
	private User creator;
	
	private Date dateCreated;
	
	private User changedBy;
	
	private Date dateChanged;
	
	/** default constructor */
	public DrugReferenceMap() {
	}
	
	/**
	 * @param term
	 * @param conceptMapType
	 */
	public DrugReferenceMap(ConceptReferenceTerm term, ConceptMapType conceptMapType) {
		this.conceptReferenceTerm = term;
		this.conceptMapType = conceptMapType;
	}
	
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
}

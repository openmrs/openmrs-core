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

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * A concept source is defined as any institution that keeps a concept dictionary. Examples are
 * ICD9, ICD10, SNOMED, or any other OpenMRS implementation
 */
@Entity
@Table(name = "concept_reference_source")
@AttributeOverride(name = "name", column = @Column(name = "name", length = 50))
public class ConceptSource extends BaseChangeableOpenmrsMetadata {

	public static final long serialVersionUID = 375L;

	// Fields

	@Id
	@Column(name = "concept_source_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer conceptSourceId;

	@Column(name = "hl7_code", length = 50)
	private String hl7Code;

	@Column(name = "unique_id", unique = true, length = 250)
	private String uniqueId;
	
	// Constructors
	
	/** default constructor */
	public ConceptSource() {
	}
	
	/** constructor with id */
	public ConceptSource(Integer conceptSourceId) {
		this.conceptSourceId = conceptSourceId;
	}
	
	/**
	 * @return Returns the conceptSourceId.
	 */
	public Integer getConceptSourceId() {
		return conceptSourceId;
	}
	
	/**
	 * @param conceptSourceId The conceptSourceId to set.
	 */
	public void setConceptSourceId(Integer conceptSourceId) {
		this.conceptSourceId = conceptSourceId;
	}
	
	@Override
	public User getCreator() {
		return super.getCreator();
	}
	
	@Override
	public void setCreator(User creator) {
		super.setCreator(creator);
	}
	
	/**
	 * @return Returns the dateCreated.
	 */
	@Override
	public Date getDateCreated() {
		return super.getDateCreated();
	}
	
	/**
	 * @param dateCreated The dateCreated to set.
	 */
	@Override
	public void setDateCreated(Date dateCreated) {
		super.setDateCreated(dateCreated);
	}
	
	@Override
	public String getDescription() {
		return super.getDescription();
	}
	
	@Override
	public void setDescription(String description) {
		super.setDescription(description);
	}
	
	/**
	 * @return Returns the hl7Code.
	 */
	public String getHl7Code() {
		return hl7Code;
	}
	
	/**
	 * @param hl7Code The hl7Code to set.
	 */
	public void setHl7Code(String hl7Code) {
		this.hl7Code = hl7Code;
	}
	
	/**
	 * @return the unique id
	 */
	public String getUniqueId() {
		return uniqueId;
	}
	
	/**
	 * @param uniqueId the unique id to set
	 */
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
	@Override
	public String getName() {
		return super.getName();
	}
	
	@Override
	public void setName(String name) {
		super.setName(name);
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConceptSourceId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setConceptSourceId(id);
	}
}

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

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;

import java.util.Date;

/**
 * A concept source is defined as any institution that keeps a concept dictionary. Examples are
 * ICD9, ICD10, SNOMED, or any other OpenMRS implementation
 */
@Entity
@Table(name = "concept_reference_source")
@AttributeOverrides({
	@AttributeOverride(name = "name", column = @Column(name = "name", nullable = false, length = 50)),
	@AttributeOverride(name = "description", column = @Column(name= "description", nullable = false, length = 1024))
})
@Audited
public class ConceptSource extends BaseChangeableOpenmrsMetadata {
	
	public static final long serialVersionUID = 375L;
	
	// Fields
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "concept_source_id_seq")
	@GenericGenerator(
	name = "concept_source_id_seq",
	strategy = "native",
	parameters = @Parameter(name = "sequence", value = "concept_reference_source_concept_source_id_seq")
	)
	@Column(name = "concept_source_id", nullable = false)
	private Integer conceptSourceId;
	
	@Column(name = "hl7_code", length = 50)
	private String hl7Code;
	
	@Column(name = "unique_id", length = 250, unique = true)
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

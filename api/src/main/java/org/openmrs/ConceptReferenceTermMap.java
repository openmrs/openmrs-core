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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;

/**
 * The concept Reference Term map object represents a mapping between two Concept Reference Terms. A
 * concept reference term can have 0 to N concept reference term mappings to any or all Concept
 * Reference Terms
 *
 * @since 1.9
 */
@Entity
@Table(name = "concept_reference_term_map")
@Audited
public class ConceptReferenceTermMap extends BaseConceptMap {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "concept_reference_term_map_id_seq")
	@GenericGenerator(
		name = "concept_reference_term_map_id_seq", 
		parameters = @Parameter(name = "sequence", value = "concept_reference_term_map_concept_reference_term_map_id_seq")
	)
	@Column(name = "concept_reference_term_map_id", nullable = false)
	private Integer conceptReferenceTermMapId;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "term_a_id", nullable = false)
	private ConceptReferenceTerm termA;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "term_b_id", nullable = false)
	private ConceptReferenceTerm termB;
	
	// Constructors
	
	/** default constructor */
	public ConceptReferenceTermMap() {
	}
	
	/** constructor with concept reference term map id */
	public ConceptReferenceTermMap(Integer conceptReferenceTermMapId) {
		this.conceptReferenceTermMapId = conceptReferenceTermMapId;
	}
	
	/**
	 * Convenience constructor that takes the term to be mapped to and the type of the map
	 *
	 * @param termB the other concept reference term to map to
	 * @param conceptMapType the concept map type for this concept reference term map
	 */
	public ConceptReferenceTermMap(ConceptReferenceTerm termB, ConceptMapType conceptMapType) {
		this.termB = termB;
		setConceptMapType(conceptMapType);
	}
	
	/**
	 * @return the conceptReferenceTermMapId
	 */
	public Integer getConceptReferenceTermMapId() {
		return conceptReferenceTermMapId;
	}
	
	/**
	 * @param conceptReferenceTermMapId the conceptReferenceTermMapId to set
	 */
	public void setConceptReferenceTermMapId(Integer conceptReferenceTermMapId) {
		this.conceptReferenceTermMapId = conceptReferenceTermMapId;
	}
	
	/**
	 * @return the termA
	 */
	public ConceptReferenceTerm getTermA() {
		return termA;
	}
	
	/**
	 * @param termA the termA to set
	 */
	public void setTermA(ConceptReferenceTerm termA) {
		this.termA = termA;
	}
	
	/**
	 * @return the termB
	 */
	public ConceptReferenceTerm getTermB() {
		return termB;
	}
	
	/**
	 * @param termB the termB to set
	 */
	public void setTermB(ConceptReferenceTerm termB) {
		this.termB = termB;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConceptReferenceTermMapId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setConceptReferenceTermMapId(id);
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ConceptReferenceTermMap)) {
			return false;
		}
		ConceptReferenceTermMap rhs = (ConceptReferenceTermMap) obj;
		if (this.conceptReferenceTermMapId != null && rhs.conceptReferenceTermMapId != null) {
			return this.conceptReferenceTermMapId.equals(rhs.conceptReferenceTermMapId);
		}
		
		return this == obj;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.conceptReferenceTermMapId == null) {
			return super.hashCode();
		}
		int hash = 3;
		hash = hash + 31 * this.conceptReferenceTermMapId;
		return hash;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (conceptReferenceTermMapId == null) {
			return "";
		}
		return conceptReferenceTermMapId.toString();
	}
	
}

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

import org.hibernate.envers.Audited;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The concept Reference Term map object represents a mapping between two Concept Reference Terms. A
 * concept reference term can have 0 to N concept reference term mappings to any or all Concept
 * Reference Terms
 *
 * @since 1.9
 */
@Audited
@Entity
@Table(name = "concept_reference_term_map")
public class ConceptReferenceTermMap extends BaseConceptMap {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@GenericGenerator(
			name = "concept_reference_term_map_id_seq",
			strategy = "native",
			parameters = @Parameter(name = "sequence", value = "concept_reference_term_map_concept_reference_term_map_id_seq")
	)
	@Column(name = "concept_reference_term_map_id", nullable = false)
	private Integer conceptReferenceTermMapId;
	
	@ManyToOne
	@JoinColumn(name = "term_a_id", nullable = false)
	private ConceptReferenceTerm termA;
	
	@ManyToOne
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

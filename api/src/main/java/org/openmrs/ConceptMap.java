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

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.search.mapper.pojo.automaticindexing.ReindexOnUpdate;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexingDependency;

/**
 * The concept map object represents a mapping of Concept to ConceptSource. A concept can have 0 to
 * N mappings to any and all concept sources in the database.
 */
@Audited
@Entity
@Table(name = "concept_reference_map")
@AssociationOverride(
	name = "conceptMapType",
	joinColumns = @JoinColumn(name = "concept_map_type_id", nullable = false)
)
public class ConceptMap extends BaseConceptMap {
	
	public static final long serialVersionUID = 754677L;
	
	// Fields
	@DocumentId
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "concept_map_id")
	private Integer conceptMapId;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "concept_id", nullable = false)
	private Concept concept;
	
	@IndexedEmbedded
	@IndexingDependency(reindexOnUpdate = ReindexOnUpdate.SHALLOW)
	@ManyToOne(optional = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "concept_reference_term_id", nullable = false)
	private ConceptReferenceTerm conceptReferenceTerm;
	
	// Constructors
	
	/** default constructor */
	public ConceptMap() {
	}
	
	/** constructor with concept map id */
	public ConceptMap(Integer conceptMapId) {
		this.conceptMapId = conceptMapId;
	}
	
	/**
	 * Convenience constructor that takes the term to be mapped to and the type of the map
	 *
	 * @param conceptReferenceTerm the concept reference term to map to
	 * @param conceptMapType the concept map type for this concept reference term map
	 */
	public ConceptMap(ConceptReferenceTerm conceptReferenceTerm, ConceptMapType conceptMapType) {
		this.conceptReferenceTerm = conceptReferenceTerm;
		setConceptMapType(conceptMapType);
	}
	
	/**
	 * @see org.openmrs.BaseOpenmrsObject#toString()
	 */
	@Override
	public String toString() {
		if (conceptMapId == null) {
			return "";
		}
		return conceptMapId.toString();
	}
	
	/**
	 * @return the concept
	 */
	public Concept getConcept() {
		return concept;
	}
	
	/**
	 * @param concept the concept to set
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * @return Returns the conceptMapId.
	 */
	public Integer getConceptMapId() {
		return conceptMapId;
	}
	
	/**
	 * @param conceptMapId The conceptMapId to set.
	 */
	public void setConceptMapId(Integer conceptMapId) {
		this.conceptMapId = conceptMapId;
	}
	
	/**
	 * @return the conceptReferenceTerm
	 * @since 1.9
	 */
	public ConceptReferenceTerm getConceptReferenceTerm() {
		if (conceptReferenceTerm == null) {
			conceptReferenceTerm = new ConceptReferenceTerm();
		}
		return conceptReferenceTerm;
	}
	
	/**
	 * @param conceptReferenceTerm the conceptReferenceTerm to set
	 * @since 1.9
	 */
	public void setConceptReferenceTerm(ConceptReferenceTerm conceptReferenceTerm) {
		this.conceptReferenceTerm = conceptReferenceTerm;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConceptMapId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setConceptMapId(id);
	}
	
}

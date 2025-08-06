/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under the terms
 * of the Healthcare Disclaimer located at http://openmrs.org/license. Copyright (C) OpenMRS Inc. OpenMRS is a registered
 * trademark and the OpenMRS graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.envers.Audited;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.DocumentId;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;

/**
 * A concept reference term is typically name for a concept by which it is referred in another institution like ICD9, ICD10,
 * SNOMED that keeps a concept dictionary or any other OpenMRS implementation
 *
 * @since 1.9
 */
@Audited
@Entity
@Table(name = "concept_reference_term")
@AttributeOverrides({
	@AttributeOverride(name = "name", column = @Column(name = "name"))
})
public class ConceptReferenceTerm extends BaseChangeableOpenmrsMetadata {
	
	private static final long serialVersionUID = 1L;
	
	@DocumentId
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "concept_reference_term_id_seq")
	@GenericGenerator(
		name = "concept_reference_term_id_seq",
		parameters = @Parameter(name = "sequence", value = "concept_reference_term_concept_reference_term_id_seq")
	)
	@Column(name = "concept_reference_term_id")
	private Integer conceptReferenceTermId;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "concept_source_id", nullable = false)
	private ConceptSource conceptSource;
	
	//The unique code used to identify the reference term in it's reference terminology
	@GenericField
	@Column(name = "code", nullable = false)
	private String code;
	
	@Column(name = "version", length = 50)
	private String version;
	
	@OneToMany(mappedBy = "termA", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private Set<ConceptReferenceTermMap> conceptReferenceTermMaps = new LinkedHashSet<>();
	
	/** default constructor */
	public ConceptReferenceTerm() {
	}
	
	/** constructor with conceptReferenceTermId */
	public ConceptReferenceTerm(Integer conceptReferenceTermId) {
		this.conceptReferenceTermId = conceptReferenceTermId;
	}
	
	/**
	 * Convenience constructor with the required fields filled in
	 *
	 * @param source the ConceptSource belongs in
	 * @param code   the code within that concept
	 * @param name   the user readable name of this term
	 * @since 1.9.2, 1.10.0
	 */
	public ConceptReferenceTerm(ConceptSource source, String code, String name) {
		this.conceptSource = source;
		this.code = code;
		setName(name);
	}
	
	/**
	 * @return the conceptReferenceTermId
	 */
	public Integer getConceptReferenceTermId() {
		return conceptReferenceTermId;
	}
	
	/**
	 * @param conceptReferenceTermId the conceptReferenceTermId to set
	 */
	public void setConceptReferenceTermId(Integer conceptReferenceTermId) {
		this.conceptReferenceTermId = conceptReferenceTermId;
	}
	
	/**
	 * @return the conceptSource
	 */
	public ConceptSource getConceptSource() {
		return conceptSource;
	}
	
	/**
	 * @param conceptSource the conceptSource to set
	 */
	public void setConceptSource(ConceptSource conceptSource) {
		this.conceptSource = conceptSource;
	}
	
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	
	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * @return the conceptReferenceTermMaps
	 */
	public Set<ConceptReferenceTermMap> getConceptReferenceTermMaps() {
		if (conceptReferenceTermMaps == null) {
			conceptReferenceTermMaps = new LinkedHashSet<>();
		}
		return conceptReferenceTermMaps;
	}
	
	/**
	 * @param conceptReferenceTermMaps the conceptReferenceTermMaps to set
	 */
	public void setConceptReferenceTermMaps(Set<ConceptReferenceTermMap> conceptReferenceTermMaps) {
		this.conceptReferenceTermMaps = conceptReferenceTermMaps;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConceptReferenceTermId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setConceptReferenceTermId(id);
	}
	
	/**
	 * Add the given {@link ConceptReferenceTermMap} object to this concept reference term's list of concept reference term
	 * maps. If there is already a corresponding ConceptReferenceTermMap object for this concept reference term already, this
	 * one will not be added.
	 *
	 * @param conceptReferenceTermMap <strong>Should</strong> not add a map where termB is itself
	 *                                <strong>Should</strong> set termA as the term to which a mapping is being added
	 *                                <strong>Should</strong> not add duplicate concept reference term maps
	 */
	public void addConceptReferenceTermMap(ConceptReferenceTermMap conceptReferenceTermMap) {
		if (conceptReferenceTermMap != null && conceptReferenceTermMap.getTermB() != null
			&& !this.equals(conceptReferenceTermMap.getTermB())) {
			//can't map a term to itself
			conceptReferenceTermMap.setTermA(this);
			if (conceptReferenceTermMaps == null) {
				conceptReferenceTermMaps = new LinkedHashSet<>();
			}
			if (!conceptReferenceTermMaps.contains(conceptReferenceTermMap)) {
				conceptReferenceTermMaps.add(conceptReferenceTermMap);
			}
		}
	}
	
	/**
	 * Remove the given ConceptReferenceTermMap from the list of conceptReferenceTermMaps for this
	 * {@link ConceptReferenceTerm}
	 *
	 * @param conceptReferenceTermMap
	 * @return true if the entity was removed, false otherwise
	 */
	public boolean removeConceptReferenceTermMap(ConceptReferenceTermMap conceptReferenceTermMap) {
		if (conceptReferenceTermMaps != null) {
			return conceptReferenceTermMaps.remove(conceptReferenceTermMap);
		}
		
		return false;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (getCode() != null && getName() != null) {
			return getName() + "(" + getCode() + ")";
		} else if (getCode() == null) {
			return getName();
		} else if (getName() == null) {
			return getCode();
		}
		
		return "";
	}
}

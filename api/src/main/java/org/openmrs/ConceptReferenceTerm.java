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

import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

/**
 * A concept reference term is typically name for a concept by which it is referred in another
 * institution like ICD9, ICD10, SNOMED that keeps a concept dictionary or any other OpenMRS
 * implementation
 *
 * @since 1.9
 */
@Indexed
public class ConceptReferenceTerm extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@DocumentId
	private Integer conceptReferenceTermId;
	
	private ConceptSource conceptSource;
	
	//The unique code used to identify the reference term in it's reference terminology
	@Field(index = Index.UN_TOKENIZED)
	private String code;
	
	private String version;
	
	private Set<ConceptReferenceTermMap> conceptReferenceTermMaps;
	
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
	 * @param code the code within that concept
	 * @param name the user readable name of this term
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
			conceptReferenceTermMaps = new LinkedHashSet<ConceptReferenceTermMap>();
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
	 * Add the given {@link ConceptReferenceTermMap} object to this concept reference term's list of
	 * concept reference term maps. If there is already a corresponding ConceptReferenceTermMap
	 * object for this concept reference term already, this one will not be added.
	 *
	 * @param conceptReferenceTermMap
	 * @should not add a map where termB is itself
	 * @should set termA as the term to which a mapping is being added
	 * @should not add duplicate concept reference term maps
	 */
	public void addConceptReferenceTermMap(ConceptReferenceTermMap conceptReferenceTermMap) {
		if (conceptReferenceTermMap != null) {
			//can't map a term to itself
			if (conceptReferenceTermMap.getTermB() != null && !this.equals(conceptReferenceTermMap.getTermB())) {
				conceptReferenceTermMap.setTermA(this);
				if (conceptReferenceTermMaps == null) {
					conceptReferenceTermMaps = new LinkedHashSet<ConceptReferenceTermMap>();
				}
				if (!conceptReferenceTermMaps.contains(conceptReferenceTermMap)) {
					conceptReferenceTermMaps.add(conceptReferenceTermMap);
				}
			}
		}
	}
	
	/**
	 * Remove the given ConceptReferenceTermMap from the list of conceptReferenceTermMaps for this
	 * {@link ConceptReferenceTerm}
	 *
	 * @param conceptReferenceMap
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

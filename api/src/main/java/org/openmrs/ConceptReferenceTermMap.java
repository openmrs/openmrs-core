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

/**
 * The concept Reference Term map object represents a mapping between two Concept Reference Terms. A
 * concept reference term can have 0 to N concept reference term mappings to any or all Concept
 * Reference Terms
 *
 * @since 1.9
 */
public class ConceptReferenceTermMap extends BaseConceptMap implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer conceptReferenceTermMapId;
	
	private ConceptReferenceTerm termA;
	
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
	public Integer getId() {
		return getConceptReferenceTermMapId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setConceptReferenceTermMapId(id);
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ConceptReferenceTermMap)) {
			return false;
		}
		ConceptReferenceTermMap rhs = (ConceptReferenceTermMap) obj;
		if (this.conceptReferenceTermMapId != null && rhs.conceptReferenceTermMapId != null) {
			return (this.conceptReferenceTermMapId.equals(rhs.conceptReferenceTermMapId));
		}
		
		return this == obj;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
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
	public String toString() {
		if (conceptReferenceTermMapId == null) {
			return "";
		}
		return conceptReferenceTermMapId.toString();
	}
	
}

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

import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.util.OpenmrsUtil;

/**
 * Child class of Concept that has a {@link ComplexObsHandler} associated with the Concept.
 * 
 * @since 1.5
 */
public class ConceptComplex extends Concept implements Serializable {
	
	public static final long serialVersionUID = 473231233L;
	
	private String handler;
	
	/**
	 * Default Constructor
	 */
	public ConceptComplex() {
	}
	
	/**
	 * @param conceptId
	 */
	public ConceptComplex(Integer conceptId) {
		super(conceptId);
	}
	
	/**
	 * Constructor with conceptId and ConceptComplexHandler
	 * 
	 * @param conceptId
	 * @param handler
	 */
	public ConceptComplex(Integer conceptId, String handler) {
		super(conceptId);
		this.handler = handler;
	}
	
	/**
	 * Constructor from Concept.
	 * 
	 * @param c
	 */
	public ConceptComplex(Concept c) {
		this.setAnswers(c.getAnswers(true));
		this.setChangedBy(c.getChangedBy());
		this.setConceptClass(c.getConceptClass());
		this.setConceptId(c.getConceptId());
		this.setConceptSets(c.getConceptSets());
		this.setCreator(c.getCreator());
		this.setDatatype(c.getDatatype());
		this.setDateChanged(c.getDateChanged());
		this.setDateCreated(c.getDateCreated());
		this.setSet(c.isSet());
		this.setNames(c.getNames());
		this.setDescriptions(c.getDescriptions());
		this.setConceptMappings(c.getConceptMappings());
		this.setRetired(c.isRetired());
		this.setVersion(c.getVersion());
		
		this.handler = "";
	}
	
	/**
	 * @see org.openmrs.Concept#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ConceptComplex) {
			ConceptComplex c = (ConceptComplex) obj;
			return (this.getConceptId().equals(c.getConceptId()));
		} else if (obj instanceof Concept) {
			// use the reverse .equals in case we have hibernate proxies - #1511
			return OpenmrsUtil.nullSafeEquals(((Concept) obj).getConceptId(), this.getConceptId());
		}
		
		// fall back to object equality
		return obj == this;
	}
	
	/**
	 * @see org.openmrs.Concept#hashCode()
	 */
	@Override
	public int hashCode() {
		if (getConceptId() == null)
			return super.hashCode();
		int hash = 6;
		if (getConceptId() != null)
			hash = hash + getConceptId().hashCode() * 31;
		return hash;
	}
	
	/**
	 * Overrides parent method and returns true if this Concept.getDatatype() equals "Complex"..
	 * 
	 * @see org.openmrs.Concept#isComplex()
	 */
	@Override
	public boolean isComplex() {
		if (getDatatype() == null || getDatatype().getHl7Abbreviation() == null)
			return false;
		
		return getDatatype().getHl7Abbreviation().equals("ED");
	}
	
	/**
	 * Set the ConceptComplexHandler. This should be the ComplexObsHandler key
	 * 
	 * @param handler
	 */
	public void setHandler(String handler) {
		this.handler = handler;
	}
	
	/**
	 * @return Returns the key to the ComplexObsHandler associated with this ConceptComplex.
	 */
	public String getHandler() {
		return this.handler;
	}
	
}

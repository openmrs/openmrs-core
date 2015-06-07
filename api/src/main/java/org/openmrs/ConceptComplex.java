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

import java.io.Serializable;

import org.hibernate.search.annotations.Indexed;
import org.openmrs.obs.ComplexObsHandler;

/**
 * Child class of Concept that has a {@link ComplexObsHandler} associated with the Concept.
 *
 * @since 1.5
 */
@Indexed
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
		this.setUuid(c.getUuid());
		
		this.handler = "";
	}
	
	/**
	 * Overrides parent method and returns true if this Concept.getDatatype() equals "Complex"..
	 *
	 * @see org.openmrs.Concept#isComplex()
	 */
	@Override
	public boolean isComplex() {
		if (getDatatype() == null || getDatatype().getHl7Abbreviation() == null) {
			return false;
		}
		
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

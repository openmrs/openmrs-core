/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.hl7.handler;

import org.openmrs.Concept;
import org.openmrs.api.APIException;

/**
 * Represents a obs that should be a proposed concept
 */
public class ProposingConceptException extends APIException {
	
	public static final long serialVersionUID = 120002000200L;
	
	private Concept concept;
	
	private String valueName;
	
	/**
	 * Default constructor that takes in the required parameters
	 * 
	 * @param concept the question for this proposed concept
	 * @param valueName the proposed text for this concept
	 */
	public ProposingConceptException(Concept concept, String valueName) {
		this.concept = concept;
		this.valueName = valueName;
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
	 * @return the valueName
	 */
	public String getValueName() {
		return valueName;
	}
	
	/**
	 * @param valueName the valueName to set
	 */
	public void setValueName(String valueName) {
		this.valueName = valueName;
	}
	
}

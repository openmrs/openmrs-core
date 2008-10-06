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
package org.openmrs.hl7.handler;

import org.openmrs.Concept;
import org.openmrs.api.APIException;

/**
 * Represents a obs that should be a proposed concept
 * 
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

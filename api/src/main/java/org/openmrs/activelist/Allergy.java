/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.activelist;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Patient;

/**
 * TODO
 */
public class Allergy extends ActiveListItem {
	
	public static final ActiveListType ACTIVE_LIST_TYPE = new ActiveListType(1);
	
	private AllergyType allergyType;
	
	private Concept reaction; // must be of class=Symptom
	
	private AllergySeverity severity;
	
	public Allergy() {
		this.activeListType = new ActiveListType(1);
	}
	
	public Allergy(Integer activeListId) {
		super(activeListId);
		this.activeListType = new ActiveListType(1);
	}
	
	public Allergy(Patient person, Concept concept, Date startDate, AllergyType allergyType, Concept reaction,
	    AllergySeverity severity) {
		super(person, new ActiveListType(1), concept, startDate);
		this.allergyType = allergyType;
		this.reaction = reaction;
		this.severity = severity;
	}
	
	/**
	 * @return the allergyType
	 */
	public AllergyType getAllergyType() {
		return allergyType;
	}
	
	/**
	 * @param allergyType the allergyType to set
	 */
	public void setAllergyType(AllergyType allergyType) {
		this.allergyType = allergyType;
	}
	
	public void setAllergyType(String type) {
		this.allergyType = StringUtils.isBlank(type) ? null : AllergyType.valueOf(type);
	}
	
	/**
	 * @return the reaction
	 */
	public Concept getReaction() {
		return reaction;
	}
	
	/**
	 * must be of class=Symptom
	 * 
	 * @param reaction the reaction to set
	 */
	public void setReaction(Concept reaction) {
		this.reaction = reaction;
	}
	
	/**
	 * @return the severity
	 */
	public AllergySeverity getSeverity() {
		return severity;
	}
	
	/**
	 * @param severity the severity to set
	 */
	public void setSeverity(AllergySeverity severity) {
		this.severity = severity;
	}
	
	public void setSeverity(String severity) {
		this.severity = StringUtils.isBlank(severity) ? null : AllergySeverity.valueOf(severity);
	}
	
	/**
	 * @return the allergen
	 */
	public Concept getAllergen() {
		return getConcept();
	}
	
	/**
	 * Set the allergen concept
	 * 
	 * @param allergen
	 */
	public void setAllergen(Concept allergen) {
		setConcept(allergen);
	}
}

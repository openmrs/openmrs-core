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
 * This keeps track of patient's allergies. Each medicine that is ordered must be carefully checked against the patient's allergy list to avoid harming the patient.
 * @since 1.7
 */
public class Allergy extends ActiveListItem {
	
	public static final ActiveListType ACTIVE_LIST_TYPE = new ActiveListType(1);
	
	private AllergyType allergyType;
	
	private Concept reaction; // must be of class=Symptom
	
	private AllergySeverity severity;
	
	/**
	 * no argument constructor to construct a allergy
	 */
	public Allergy() {
		this.activeListType = new ActiveListType(1);
	}
	
	/**
	 * Constructs an allergy with a given id
	 * @param activeListId the activeListId to set. this parameter is the id of the ActiveListItem
	 */
	public Allergy(Integer activeListId) {
		super(activeListId);
		this.activeListType = new ActiveListType(1);
	}
	
	/**
	 * Convenience constructor to construct an allergy with a given Patient, Concept, start date of the allergy, allergy type and the reaction of the allergy
	 * @param person the person to set this Allergy
	 * @param concept the concept to set
	 * @param startDate the startDate to set, when the allergy occured 
	 * @param allergyType the allergyType to set
	 * @param reaction the reaction to set
	 * @param severity the severity to set
	 */
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
	 * set the allergyType of the Allergy
	 * @param allergyType the allergyType to set
	 */
	public void setAllergyType(AllergyType allergyType) {
		this.allergyType = allergyType;
	}
	
	/**
	 * set the allergyType of the Allergy. Here the allergy type will be chosen from the enum values in the {@link AllergyType}, according to the given String type. 
	 * @param type the allergyType to set   
	 */
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
	 * @return the severity of the allergy
	 */
	public AllergySeverity getSeverity() {
		return severity;
	}
	
	/**
	 * Set the severity of the allergy
	 * @param severity the severity to set
	 */
	public void setSeverity(AllergySeverity severity) {
		this.severity = severity;
	}
	
	/**
	 * Set the severity of the allergy. Here the allergy type will be chosen from the enum values in the {@link AllergySeverity}, according to the given String severity.
	 * @param severity the severity to set
	 */
	public void setSeverity(String severity) {
		this.severity = StringUtils.isBlank(severity) ? null : AllergySeverity.valueOf(severity);
	}
	
	/**
	 * Get the allergen concept. Allergen is a type of antigen that produces an abnormally
	 * @return the allergen
	 */
	public Concept getAllergen() {
		return getConcept();
	}
	
	/**
	 * Set the allergen concept. Allergen is a type of antigen that produces an abnormally
	 * 
	 * @param allergen
	 */
	public void setAllergen(Concept allergen) {
		setConcept(allergen);
	}
}

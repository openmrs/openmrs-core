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
package org.openmrs.web.controller.patient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.ListUtils;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Relationship;
import org.openmrs.api.PersonService.ATTR_VIEW_TYPE;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants.PERSON_TYPE;
import org.springframework.util.CollectionUtils;

/**
 * The Model Object to be used for the short patient form.
 */
public class ShortPatientModel {
	
	private Patient patient;
	
	private List<PatientIdentifier> identifiers;
	
	private PersonName personName;
	
	private PersonAddress personAddress;
	
	private Map<String, Relationship> relationshipsMap;
	
	private List<PersonAttribute> personAttributes;
	
	public ShortPatientModel() {
		
	}
	
	/**
	 * Constructor that creates a shortPatientModel object from a given patient object
	 * 
	 * @param patient
	 */
	@SuppressWarnings("unchecked")
	public ShortPatientModel(Patient patient) {
		if (patient != null) {
			this.patient = patient;
			this.personName = patient.getPersonName();
			this.personAddress = patient.getPersonAddress();
			identifiers = ListUtils.lazyList(new ArrayList<PatientIdentifier>(patient.getActiveIdentifiers()), FactoryUtils
			        .instantiateFactory(PatientIdentifier.class));
			
			List<PersonAttributeType> viewableAttributeTypes = Context.getPersonService().getPersonAttributeTypes(
			    PERSON_TYPE.PATIENT, ATTR_VIEW_TYPE.VIEWING);
			List<PersonAttribute> activePatientAttributes = patient.getActiveAttributes();
			
			personAttributes = new ArrayList<PersonAttribute>();
			if (!CollectionUtils.isEmpty(viewableAttributeTypes)) {
				for (PersonAttribute personAttribute : activePatientAttributes) {
					if (viewableAttributeTypes.contains(personAttribute.getAttributeType()))
						personAttributes.add(personAttribute);
				}
			}
		}
	}
	
	/**
	 * @return the identifiers
	 */
	public List<PatientIdentifier> getIdentifiers() {
		return identifiers;
	}
	
	/**
	 * @param identifiers the identifiers to set
	 */
	public void setIdentifiers(List<PatientIdentifier> identifiers) {
		this.identifiers = identifiers;
	}
	
	/**
	 * @return the personName
	 */
	public PersonName getPersonName() {
		return personName;
	}
	
	/**
	 * @param personName the personName to set
	 */
	public void setPersonName(PersonName personName) {
		this.personName = personName;
	}
	
	/**
	 * @return the personAddress
	 */
	public PersonAddress getPersonAddress() {
		return personAddress;
	}
	
	/**
	 * @param personAddress the personAddress to set
	 */
	public void setPersonAddress(PersonAddress personAddress) {
		this.personAddress = personAddress;
	}
	
	/**
	 * @return the relationshipsMap
	 */
	public Map<String, Relationship> getRelationshipsMap() {
		return relationshipsMap;
	}
	
	/**
	 * @param relationshipsMap the relationshipsMap to set
	 */
	public void setRelationshipsMap(Map<String, Relationship> relationshipsMap) {
		this.relationshipsMap = relationshipsMap;
	}
	
	/**
	 * @return the personAttributes
	 */
	public List<PersonAttribute> getPersonAttributes() {
		return personAttributes;
	}
	
	/**
	 * @param personAttributes the personAttributes to set
	 */
	public void setPersonAttributes(List<PersonAttribute> personAttributes) {
		this.personAttributes = personAttributes;
	}
	
	/**
	 * @return the patient
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * @param patient the patient to set
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
}

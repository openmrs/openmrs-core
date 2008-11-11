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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;

public class ShortPatientModel {
	private Integer patientId;
	private String identifier = "";
	private String otherIdentifiers = "";
	private PersonName name = new PersonName();
	private String otherNames = "";
	private String gender;
	private Date birthdate;
	private Boolean birthdateEstimated = false;
	private PersonAddress address;
	private Boolean voided = false;
	private Boolean dead = false;
	private Concept causeOfDeath = null;
	private Date deathDate = null;
	
	// convenience map:
	// Map<attribute.getAttributeType().getName(), attribute>
	Map<String, PersonAttribute> attributeMap = null;
	
	// private Location healthCenter = null;
	// private String mothersName;
	
	public Boolean getDead() {
		return dead;
	}

	public void setDead(Boolean dead) {
		this.dead = dead;
	}

	public ShortPatientModel() { }
	
	public ShortPatientModel(Patient patient) {
		this();
		if (patient != null) {
			patientId = patient.getPatientId();
			
			// get patient's identifiers
			boolean first = true;
			for (PatientIdentifier pi : patient.getIdentifiers()) {
				if (first) {
					identifier = pi.getIdentifier();
					first = false;
				}
				else {
					if (otherIdentifiers != "")
						otherIdentifiers += ",";
					otherIdentifiers += " " + pi.getIdentifier();
				}
			}
			
			// get patient's names
			first = true;
			for (PersonName pn : patient.getNames()) {
				if (first) {
					setName(pn);
					first = false;
				} else {
					if (otherNames != "")
						otherNames += ",";
					otherNames += " " + pn.getGivenName() + " " + pn.getMiddleName() + " " + pn.getFamilyName();
				}
			}
			
			gender = patient.getGender();
			
			birthdate = patient.getBirthdate();
			birthdateEstimated = patient.isBirthdateEstimated();
			//mothersName = patient.getMothersName();
			//healthCenter = patient.getHealthCenter();
			voided = patient.isVoided();
			dead = patient.isDead();
			causeOfDeath = patient.getCauseOfDeath();
			deathDate = patient.getDeathDate();
						
			address = patient.getPersonAddress();
			
			attributeMap = new HashMap<String, PersonAttribute>();
			for (PersonAttribute attribute : patient.getActiveAttributes()) {
				attributeMap.put(attribute.getAttributeType().getName(), attribute);
			}
		}
	}

	public PersonAddress getAddress() {
		return address;
	}

	public void setAddress(PersonAddress address) {
		this.address = address;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public Boolean getBirthdateEstimated() {
		return birthdateEstimated;
	}

	public void setBirthdateEstimated(Boolean birthdateEstimated) {
		this.birthdateEstimated = birthdateEstimated;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getOtherIdentifiers() {
		return otherIdentifiers;
	}

	public void setOtherIdentifiers(String otherIdentifiers) {
		this.otherIdentifiers = otherIdentifiers;
	}

	public String getOtherNames() {
		return otherNames;
	}

	public void setOtherNames(String otherNames) {
		this.otherNames = otherNames;
	}

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public Boolean getVoided() {
		return voided;
	}

	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	public Concept getCauseOfDeath() {
		return causeOfDeath;
	}

	public void setCauseOfDeath(Concept causeOfDeath) {
		this.causeOfDeath = causeOfDeath;
	}

	public Date getDeathDate() {
		return deathDate;
	}

	public void setDeathDate(Date deathDate) {
		this.deathDate = deathDate;
	}

	public Map<String, PersonAttribute> getAttributeMap() {
		return attributeMap;
	}

	public PersonName getName() {
		return name;
	}

	public void setName(PersonName name) {
		this.name = name;
	}
	
}
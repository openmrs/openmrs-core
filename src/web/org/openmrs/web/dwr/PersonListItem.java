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
package org.openmrs.web.dwr;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;

public class PersonListItem {
	
	protected final Log log = LogFactory.getLog(getClass());

	private Integer personId;
	private String familyName = "";
	private String middleName = "";
	private String givenName = "";
	private String otherNames = "";
	private String gender;
	private Date birthdate;
	private Boolean birthdateEstimated = false;
	private String address1;
	private String address2;
	private Boolean voided = false;
	
	private Map<String, String> attributes = new HashMap<String, String>();
	// private String healthCenter = "";
	// private String mothersName;
	
	public PersonListItem() { }
	
	public PersonListItem(Person person) {

		if (person != null) {
			personId = person.getPersonId();
			
			// get patient's names
			boolean first = true;
			for (PersonName pn : person.getNames()) {
				if (first) {
					familyName = pn.getFamilyName();
					middleName = pn.getMiddleName();
					givenName = pn.getGivenName();
					first = false;
				} else {
					if (otherNames != "")
						otherNames += ",";
					otherNames += " " + pn.getGivenName() + " " + pn.getMiddleName() + " " + pn.getFamilyName();
				}
			}
			
			gender = person.getGender();
			birthdate = person.getBirthdate();
			birthdateEstimated = person.isBirthdateEstimated();
			voided = person.isPersonVoided();
			
			// add in the person attributes
			for (PersonAttribute attribute : person.getActiveAttributes()) {
				Object object = attribute.getHydratedObject();
				if (object != null)
					attributes.put(attribute.getAttributeType().getName(), object.toString());
			}
			
		}
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof PersonListItem) {
			PersonListItem pi = (PersonListItem)obj;
			if (pi.getPersonId() == null || personId == null)
				return false;
			return pi.getPersonId().equals(personId);
		}
		return false;
	}
	
	public int hashCode() {
		if (personId == null)
			return super.hashCode();
		return personId.hashCode();
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

	public String getFamilyName() {
		if (familyName == null) familyName = "";
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getMiddleName() {
		if (middleName == null) middleName = "";
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getGivenName() {
		if (givenName == null) givenName = "";
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	
	/**
	 * Convenience method to retrieve the givenName middleName familyName
	 * 
	 * @return String this person's name
	 */
	public String getPersonName() {
		String name = "";
		
		if (givenName != null)
			name = givenName;
		
		if (middleName != null)
			name = name + (name.length() > 0 ? " " : "") + middleName;
		
		if (familyName != null)
			name = name + (name.length() > 0 ? " " : "") + familyName;
		
		return name;
	}
	
	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}
	
	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public Boolean getVoided() {
		return voided;
	}

	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	public String getOtherNames() {
		return otherNames;
	}

	public void setOtherNames(String otherNames) {
		this.otherNames = otherNames;
	}

	public Integer getAge() {
		
		if (birthdate == null)
			return null;
		
		Calendar today = Calendar.getInstance();
		
		Calendar bday = Calendar.getInstance();
		bday.setTime(birthdate);
		
		int age = today.get(Calendar.YEAR) - bday.get(Calendar.YEAR);
		
		//tricky bit:
		// set birthday calendar to this year
		// if the current date is less that the new 'birthday', subtract a year
		bday.set(Calendar.YEAR, today.get(Calendar.YEAR));
		if (today.before(bday)) {
				age = age -1;
		}
		
		return age;
	}
	
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * @return the personId
	 */
	public Integer getPersonId() {
		return personId;
	}

	/**
	 * @param personId the personId to set
	 */
	public void setPersonId(Integer personId) {
		this.personId = personId;
	}
	
}

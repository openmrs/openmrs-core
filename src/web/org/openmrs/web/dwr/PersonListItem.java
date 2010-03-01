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
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.User;

/**
 * A mini/simplified Person object. Used as the return object from DWR methods to allow javascript
 * and other consumers to easily use all methods. This class guarantees that all objects in this
 * class will be initialized (copied) off of the Person object.
 * 
 * @see Person
 * @see DWRPersonService
 */
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

    private Integer age;

    private String address1;
	
	private String address2;
	
	private Boolean voided = false;
	
	private Map<String, String> attributes = new HashMap<String, String>();
	
	/**
	 * Creates an instance of a subclass of PersonListItem which is best suited for the parameter.
	 * If a {@link Patient} is passed in, a {@link PatientListItem} is returned, otherwise a
	 * {@link PersonListItem} is returned.
	 * 
	 * @param person the {@link Person} object to covert to a {@link PersonListItem}
	 * @return a {@link PersonListItem} or subclass thereof
	 * @should return PatientListItem given patient parameter
	 * @should return PersonListItem given person parameter
	 */
	public static PersonListItem createBestMatch(Person person) {
		if (person instanceof Patient) {
			return new PatientListItem((Patient) person);
		} else {
			return new PersonListItem(person);
		}
	}
	
	/**
	 * Default empty constructor
	 */
	public PersonListItem() {
	}
	
	/**
	 * Convenience constructor to create a PersonListItem that has only this personId
	 * 
	 * @param personId the person id to assign
	 */
	public PersonListItem(Integer personId) {
		this.personId = personId;
	}
	
	/**
	 * Convenience constructor that creates a PersonListItem from the given Person. All relevant
	 * attributes are pulled off of the Person object and copied to this PersonListItem
	 * 
	 * @param person the Person to turn into a PersonListItem
	 * @should put attribute toString value into attributes map
	 */
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
            age = person.getAge();
            voided = person.isPersonVoided();
			
			// add in the person attributes
			for (PersonAttribute attribute : person.getActiveAttributes()) {
				attributes.put(attribute.getAttributeType().getName(), attribute.toString());
			}
			
		}
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PersonListItem) {
			PersonListItem pi = (PersonListItem) obj;
			if (pi.getPersonId() == null || personId == null)
				return false;
			return pi.getPersonId().equals(personId);
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
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
		if (familyName == null)
			familyName = "";
		return familyName;
	}
	
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	
	public String getMiddleName() {
		if (middleName == null)
			middleName = "";
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
		if (givenName == null)
			givenName = "";
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

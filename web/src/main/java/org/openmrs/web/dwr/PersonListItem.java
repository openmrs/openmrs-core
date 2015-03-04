/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.web.WebUtil;

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
	
	private String uuid = "";
	
	private String familyName = "";
	
	private String middleName = "";
	
	private String givenName = "";
	
	private String otherNames = "";
	
	private String gender;
	
	private Date birthdate;
	
	private String birthdateString;
	
	private Boolean birthdateEstimated = false;
	
	private String deathDateString;
	
	private Boolean deathdateEstimated = false;
	
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
			uuid = person.getUuid();
			
			// get patient's names
			boolean first = true;
			for (PersonName pn : person.getNames()) {
				if (first) {
					familyName = pn.getFamilyName();
					middleName = pn.getMiddleName();
					givenName = pn.getGivenName();
					first = false;
				} else {
					if (!StringUtils.isBlank(otherNames)) {
						otherNames += ",";
					}
					otherNames += " " + pn.getGivenName() + " " + pn.getMiddleName() + " " + pn.getFamilyName();
				}
			}
			
			gender = person.getGender();
			birthdate = person.getBirthdate();
			birthdateString = WebUtil.formatDate(person.getBirthdate());
			birthdateEstimated = person.isBirthdateEstimated();
			age = person.getAge();
			voided = person.isPersonVoided();
			
			if (person.getDeathDate() != null) {
				this.deathDateString = WebUtil.formatDate(person.getDeathDate());
			}
			deathdateEstimated = person.getDeathdateEstimated();
			
			// add in the person attributes
			for (PersonAttribute attribute : person.getActiveAttributes()) {
				attributes.put(attribute.getAttributeType().getName(), attribute.toString());
			}
			
		}
	}
	
	/**
	 * Convenience constructor that creates a PersonListItem from the given Person. All relevant
	 * attributes are pulled off of the Person object and copied to this PersonListItem. And
	 * set the best match name based on the search criteria.
	 *
	 * @param person the Person to turn into a PersonListItem
	 * @param searchName Search query string of the name
	 * @should identify best matching name for the family name
	 * @should identify best matching name as preferred name even if other names match
	 * @should identify best matching name as other name for the middle name
	 * @should identify best matching name as other name for the given name
	 * @should identify best matching name in multiple search names
	 */
	public PersonListItem(Person person, String searchName) {
		this(person);
		
		if (person != null && !StringUtils.isBlank(searchName)) {
			String[] searchNames = searchName.split(" ");
			String fullName;
			boolean foundABestMatch = false;
			for (PersonName personName : person.getNames()) {
				fullName = personName.getFullName();
				if (!foundABestMatch && containsAll(fullName, searchNames)) {
					familyName = personName.getFamilyName();
					givenName = personName.getGivenName();
					middleName = personName.getMiddleName();
					foundABestMatch = true;
					continue; // process the next name
				}
				if (!StringUtils.isBlank(otherNames)) {
					otherNames += ",";
				}
				otherNames += " " + fullName;
			}
		}
	}
	
	/**
	 *Helper method to check if all the search names(separated by spaces) are contained in the person's full name.

	 *@param fullName the fullName upon which the search names are to be compared
	 *@param searchNames Array<String> of searched names
	 *@should return true when all searched names are found in full name
	 *@should return false if even one of the searched names are not found in full name
	 */
	private boolean containsAll(String fullName, String[] searchNames) {
		for (String name : searchNames) {
			if (StringUtils.containsIgnoreCase(fullName, name)) {
				continue;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PersonListItem) {
			PersonListItem pi = (PersonListItem) obj;
			if (pi.getPersonId() == null || personId == null) {
				return false;
			}
			return pi.getPersonId().equals(personId);
		}
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (personId == null) {
			return super.hashCode();
		}
		return personId.hashCode();
	}
	
	public Date getBirthdate() {
		return birthdate;
	}
	
	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}
	
	/**
	 * Returns a formatted birthdate value
	 *
	 * @since 1.8
	 */
	public String getBirthdateString() {
		return birthdateString;
	}
	
	/**
	 * @since 1.8
	 */
	public void setBirthdateString(String birthdateString) {
		this.birthdateString = birthdateString;
	}
	
	public Boolean getBirthdateEstimated() {
		return birthdateEstimated;
	}
	
	public void setBirthdateEstimated(Boolean birthdateEstimated) {
		this.birthdateEstimated = birthdateEstimated;
	}
	
	public String getDeathDateString() {
		return deathDateString;
	}
	
	public void setDeathDateString(String deathDateString) {
		this.deathDateString = deathDateString;
	}
	
	public Boolean getDeathdateEstimated() {
		return deathdateEstimated;
	}
	
	public void setDeathdateEstimated(Boolean deathdateEstimated) {
		this.deathdateEstimated = deathdateEstimated;
	}
	
	public String getFamilyName() {
		if (familyName == null) {
			familyName = "";
		}
		return familyName;
	}
	
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	
	public String getMiddleName() {
		if (middleName == null) {
			middleName = "";
		}
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
		if (givenName == null) {
			givenName = "";
		}
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
		
		if (!StringUtils.isBlank(givenName)) {
			name = givenName;
		}
		
		if (!StringUtils.isBlank(middleName)) {
			name = name + (name.length() > 0 ? " " : "") + middleName;
		}
		
		if (!StringUtils.isBlank(familyName)) {
			name = name + (name.length() > 0 ? " " : "") + familyName;
		}
		
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
	
	public String getUuid() {
		if (uuid == null) {
			return "";
		}
		return uuid;
	}
	
}

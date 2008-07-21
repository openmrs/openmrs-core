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
package org.openmrs;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsUtil;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.load.Replace;

/**
 * A Person in the system. This can be either a small person stub, or 
 * indicative of an actual Patient or User in the system as well.
 * 
 * This class holds the generic person things that both users and patients
 * share.  Things like birthdate, names, addresses, and attributes are all 
 * generified into the person table (and hence this super class)
 * 
 * @see org.openmrs.Patient
 * @see org.openmrs.User
 */
@Root(strict=false)
public class Person implements java.io.Serializable {

	public static final Log log = LogFactory.getLog(Person.class);
	
	public static final long serialVersionUID = 13533L;

	protected Integer personId;
	
	private Set<PersonAddress> addresses = null;
	private Set<PersonName> names = null;
	private Set<PersonAttribute> attributes = null;
	
	private String gender;
	private Date birthdate;
	private Boolean birthdateEstimated = false;
	private Boolean dead = false;
	private Date deathDate;
	private Concept causeOfDeath;

	private User personCreator;
	private Date dateCreated;
	private User changedBy;
	private Date dateChanged;
	private Boolean voided = false;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;
	
	private boolean isPatient;
	private boolean isUser;
	
	/** 
	 * Convenience map from PersonAttributeType.name to PersonAttribute
	 * This is "cached" for each user upon first load.  When an 
	 * attribute is changed, the cache is cleared and rebuilt on next
	 * access. 
	 */
	Map<String, PersonAttribute> attributeMap = null;
	
	/**
	 * default empty constructor
	 */
	public Person() { }
	
	/**
	 * This constructor is used to build a person object from another
	 * person object (usually a patient or a user subobject).
	 * 
	 * @param person Person to create this person object from
	 */
	public Person(Person person) {
		if (person == null)
			return;
		
		personId = person.getPersonId();
		addresses = person.getAddresses();
		names = person.getNames();
		attributes = person.getAttributes();
		
		gender = person.getGender();
		birthdate = person.getBirthdate();
		birthdateEstimated = person.getBirthdateEstimated();
		dead = person.isDead();
		deathDate = person.getDeathDate();
		causeOfDeath = person.getCauseOfDeath();
		
		personCreator = person.getPersonCreator();
		dateCreated = person.getPersonDateCreated();
		changedBy = person.getPersonChangedBy();
		dateChanged = person.getPersonDateChanged();
		voided = person.isPersonVoided();
		voidedBy = person.getPersonVoidedBy();
		dateVoided = person.getPersonDateVoided();
		voidReason=  person.getPersonVoidReason();
	}

	/**
	 * Default constructor taking in the primary key personId value
	 *  
	 * @param personId Integer internal id for this person
	 */
	public Person(Integer personId) {
		this.personId = personId;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Person) {
			Person person = (Person) obj;
			
			if (getPersonId() != null && person.getPersonId() != null)
				return personId.equals(person.getPersonId());
		}
		
		// if personId is null for either object, for equality the
		// two objects must be the same
		return this == obj;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getPersonId() == null)
			return super.hashCode();
		return this.getPersonId().hashCode();
	}

	// Property accessors

	/**
	 * @return Returns the personId.
	 */
	@Attribute(required=true)
	public Integer getPersonId() {
		return personId;
	}

	/**
	 * @param personId
	 *            The personId to set.
	 */
	@Attribute(required=true)
	public void setPersonId(Integer personId) {
		this.personId = personId;
	}
	
	/**
	 * @return person's gender
	 */
	@Attribute(required=false)
	public String getGender() {
		return this.gender;
	}

	/**
	 * @param gender
	 *            person's gender
	 */
	@Attribute(required=false)
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	/**
	 * @return person's date of birth
	 */
	@Element(required=false)
	public Date getBirthdate() {
		return this.birthdate;
	}

	/**
	 * @param birthdate
	 *            person's date of birth
	 */
	@Element(required=false)
	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	/**
	 * @return true if person's birthdate is estimated
	 */
	public Boolean isBirthdateEstimated() {
		// if (this.birthdateEstimated == null) {
		// return new Boolean(false);
		// }
		return this.birthdateEstimated;
	}

	@Attribute(required=true)
	public Boolean getBirthdateEstimated() {
		return isBirthdateEstimated();
	}

	/**
	 * @param birthdateEstimated
	 *            true if person's birthdate is estimated
	 */
	@Attribute(required=true)
	public void setBirthdateEstimated(Boolean birthdateEstimated) {
		this.birthdateEstimated = birthdateEstimated;
	}
	
	/**
	 * @return Returns the death status.
	 */
	public Boolean isDead() {
		return dead;
	}
	
	/**
	 * @return Returns the death status.
	 */
	@Attribute(required=true)
	public Boolean getDead() {
		return isDead();
	}

	/**
	 * @param dead The dead to set.
	 */
	@Attribute(required=true)
	public void setDead(Boolean dead) {
		this.dead = dead;
	}

	/**
	 * @return date of person's death
	 */
	@Element(required=false)
	public Date getDeathDate() {
		return this.deathDate;
	}

	/**
	 * @param deathDate
	 *            date of person's death
	 */
	@Element(required=false)
	public void setDeathDate(Date deathDate) {
		this.deathDate = deathDate;
	}

	/**
	 * @return cause of person's death
	 */
	@Element(required=false)
	public Concept getCauseOfDeath() {
		return this.causeOfDeath;
	}

	/**
	 * @param causeOfDeath
	 *            cause of person's death
	 */
	@Element(required=false)
	public void setCauseOfDeath(Concept causeOfDeath) {
		this.causeOfDeath = causeOfDeath;
	}
	
	/**
	 * @return list of known addresses for person
	 * @see org.openmrs.PersonAddress
	 */
	@ElementList(required=false)
	public Set<PersonAddress> getAddresses() {
		if (addresses == null)
			addresses = new TreeSet<PersonAddress>();
		return this.addresses;
	}

	/**
	 * @param personAddresses
	 *            list of known addresses for person
	 * @see org.openmrs.PersonAddress
	 */
	@ElementList(required=false)
	public void setAddresses(Set<PersonAddress> addresses) {
		this.addresses = addresses;
	}

	/**
	 * @return all known names for person
	 * @see org.openmrs.PersonName
	 */
	@ElementList
	public Set<PersonName> getNames() {
		if (names == null)
			names = new TreeSet<PersonName>();
		return this.names;
	}

	/**
	 * @param names
	 *            update all known names for person
	 * @see org.openmrs.PersonName
	 */
	@ElementList
	public void setNames(Set<PersonName> names) {
		this.names = names;
	}

	/**
	 * @return all known attributes for person
	 * @see org.openmrs.PersonAttribute
	 */
	@ElementList
	public Set<PersonAttribute> getAttributes() {
		if (attributes == null)
			attributes = new TreeSet<PersonAttribute>();
		return this.attributes;
	}

	/**
	 * Returns only the non-voided attributes for this person
	 * 
	 * @return list attributes
	 */
	public List<PersonAttribute> getActiveAttributes() {
		List<PersonAttribute> attrs = new Vector<PersonAttribute>();
		for (PersonAttribute attr : getAttributes()) {
			if (!attr.isVoided())
				attrs.add(attr);
		}
		return attrs;
	}
	
	/**
	 * @param attributes
	 *            update all known attributes for person
	 * @see org.openmrs.PersonAttribute
	 */
	@ElementList
	public void setAttributes(Set<PersonAttribute> attributes) {
		this.attributes = attributes;
		attributeMap = null;
	}
	
	
	// Convenience methods
	
	/**
	 * Convenience Method
	 * Adds the <code>attribute</code> to this person's attribute list if the attribute doesn't exist already
	 * Voids any current attribute with type = <code>newAttribute.getAttributeType()</code>
	 * 
	 * ** NOTE: This effectively limits persons to only one attribute of any given type **
	 * 
	 * @param name
	 */
	public void addAttribute(PersonAttribute newAttribute) {
		newAttribute.setPerson(this);
		for (PersonAttribute currentAttribute : getActiveAttributes()) {
			if (currentAttribute.equals(newAttribute))
				return; // if we have the same PersonAttributeId, don't add the new attribute
			else if (currentAttribute.getAttributeType().equals(newAttribute.getAttributeType())) {
				if (currentAttribute.getValue() != null && currentAttribute.getValue().equals(newAttribute.getValue()))
					// this person already has this attribute
					return;
				
				// if the to-be-added attribute isn't already voided itself
				// and if we have the same type, different value
				if (newAttribute.isVoided() == false) {
					if (currentAttribute.getCreator() != null)
						currentAttribute.voidAttribute("New value: " + newAttribute.getValue());
					else
						// remove the attribute if it was just temporary (didn't have a creator attached to it yet)
						removeAttribute(currentAttribute);
				}
			}
		}
		attributeMap = null;
		if (!OpenmrsUtil.collectionContains(attributes, newAttribute))
			attributes.add(newAttribute);
	}

	/**
	 * Convenience Method
	 * Removes the <code>attribute</code> from this person's attribute list if the attribute exists already
	 * @param attribute
	 */
	public void removeAttribute(PersonAttribute attribute) {
		if (attributes != null)
			if (attributes.remove(attribute))
				attributeMap = null;
	}
		
	/**
	 * Convenience Method
	 * Returns the first non-voided person attribute matching a person attribute type
	 * 
	 * @param pat
	 * @return PersonAttribute
	 */
	public PersonAttribute getAttribute(PersonAttributeType pat) {
			if (pat != null)
				for (PersonAttribute attribute : getAttributes()) {
					if (pat.equals(attribute.getAttributeType()) && !attribute.isVoided()) {
						return attribute;
					}
				}	
		return null;
	}
	
	/**
	 * Convenience Method
	 * Returns this person's first attribute that has a PersonAttributeType.name equal to <code>attributeName</code>
	 * @param attribute
	 */
	public PersonAttribute getAttribute(String attributeName) {
		if (attributeName != null)
			for (PersonAttribute attribute : getAttributes()) {
				PersonAttributeType type = attribute.getAttributeType();
				if (type != null && attributeName.equals(type.getName()) && !attribute.isVoided()) {
					return attribute;
				}
			}
		
		return null;
	}
	
	/**
	 * Convenience Method
	 * Returns this person's first attribute that has a PersonAttributeType.id equal to <code>attributeTypeId</code>
	 * @param attribute
	 */
	public PersonAttribute getAttribute(Integer attributeTypeId) {
		for (PersonAttribute attribute : getActiveAttributes()) {
			if (attributeTypeId.equals(attribute.getAttributeType().getPersonAttributeTypeId()) && !attribute.isVoided()) {
				return attribute;
			}
		}	
		return null;
	}
	
	/**
	 * Convenience Method
	 * Returns all of this person's attributes that have a PersonAttributeType.name equal to <code>attributeName</code>
	 * @param attribute
	 */
	public List<PersonAttribute> getAttributes(String attributeName) {
		List<PersonAttribute> ret = new Vector<PersonAttribute>();
		
		for (PersonAttribute attribute : getActiveAttributes()) {
			PersonAttributeType type = attribute.getAttributeType();
			if (type != null && attributeName.equals(type.getName()) && !attribute.isVoided()) {
				ret.add(attribute);
			}
		}
		
		return ret;
	}
	
	/**
	 * Convenience Method
	 * Returns all of this person's attributes that have a PersonAttributeType.id equal to <code>attributeTypeId</code>
	 * @param attribute
	 */
	public List<PersonAttribute> getAttributes(Integer attributeTypeId) {
		List<PersonAttribute> ret = new Vector<PersonAttribute>();
		
		for (PersonAttribute attribute : getActiveAttributes()) {
			if (attributeTypeId.equals(attribute.getAttributeType().getPersonAttributeTypeId()) && !attribute.isVoided()) {
				ret.add(attribute);
			}
		}
		
		return ret;
	}
	
	
	/**
	 * Convenience Method
	 * Returns all of this person's attributes that have a PersonAttributeType equal to <code>personAttributeType</code>
	 * @param attribute
	 */
	public List<PersonAttribute> getAttributes(PersonAttributeType personAttributeType) {
		List<PersonAttribute> ret = new Vector<PersonAttribute>();	
		for (PersonAttribute attribute : getAttributes()) {
			if (personAttributeType.equals(attribute.getAttributeType()) && !attribute.isVoided()) {
				ret.add(attribute);
			}
		}	
		return ret;
	}
	
	/**
	 * Convenience Method
	 * Returns all of this person's attributes in map form: <String, PersonAttribute>
	 * 
	 * @param attribute
	 */
	public Map<String, PersonAttribute> getAttributeMap() {
		if (attributeMap != null)
			return attributeMap;
		
		if (log.isDebugEnabled())
			log.debug("Current Person Attributes: \n" + printAttributes());
		
		attributeMap = new HashMap<String, PersonAttribute>();
		for (PersonAttribute attribute : getActiveAttributes()) {
			attributeMap.put(attribute.getAttributeType().getName(), attribute);
		}
		
		return attributeMap;
	}
	
	/**
	 * Convenience method for viewing all of the person's current attributes
	 */
	public String printAttributes() {
		String s = "";
		
		for (PersonAttribute attribute : getAttributes()) {
			s += attribute.getAttributeType() + " : " + attribute.getValue() + " : voided? " + attribute.isVoided() + "\n";
		}
		
		return s;
	}
	
	/**
	 * Convenience Method
	 * Adds the <code>name</code> to this person's name list if the name doesn't exist already
	 * @param name
	 */
	public void addName(PersonName name) {
		if (name != null) {
			name.setPerson(this);
			if (names == null)
				names = new TreeSet<PersonName>();
			if (!OpenmrsUtil.collectionContains(names, name))
				names.add(name);
		}
	}

	/**
	 * Convenience Method
	 * Removes the <code>name</code> from this person's name list if the name exists already
	 * @param name
	 */
	public void removeName(PersonName name) {
		if (names != null)
			names.remove(name);
	}

	/**
	 * Convenience Method
	 * Adds the <code>address</code> to this person's address list if the address doesn't exist already
	 * @param address
	 */
	public void addAddress(PersonAddress address) {
		if (address != null) {
			address.setPerson(this);
			if (addresses == null)
				addresses = new TreeSet<PersonAddress>();
			if (!OpenmrsUtil.collectionContains(addresses, address))
				addresses.add(address);
		}
	}

	/**
	 * Convenience Method
	 * Removes the <code>address</code> from this person's address list if the address exists already
	 * @param address
	 */
	public void removeAddress(PersonAddress address) {
		if (addresses != null)
			addresses.remove(address);
	}
	
	/**
	 * Convenience Method 
	 * Get the "preferred" name for the person.
	 * 
	 * Returns a blank PersonName object if no names are given
	 * 
	 * @return Returns the "preferred" person name.
	 */
	public PersonName getPersonName() {
		if (getNames() != null && names.size() > 0) {
			return (PersonName) names.toArray()[0];
		} else {
			return new PersonName();
		}
	}
	
	/**
	 * Convenience method to get the given name attribute on this
	 * person's preferred PersonName
	 * 
	 * @return String given name of the person
	 */
	public String getGivenName() {
		PersonName personName = getPersonName();
		if (personName == null)
			return "";
		else
			return personName.getGivenName();
	}
	
	/**
	 * Convenience method to get the middle name attribute on this
	 * person's preferred PersonName
	 * 
	 * @return String middle name of the person
	 */
	public String getMiddleName() {
		PersonName personName = getPersonName();
		if (personName == null)
			return "";
		else
			return personName.getMiddleName();
	}
	
	/**
	 * Convenience method to get the family name attribute on this
	 * person's preferred PersonName
	 * 
	 * @return String family name of the person
	 */
	public String getFamilyName() {
		PersonName personName = getPersonName();
		if (personName == null)
			return "";
		else
			return personName.getFamilyName();
	}
	
	/**
	 * Convenience Method
	 * To get the "preferred" address for person.
	 * 
	 * @return Returns the "preferred" person address.
	 */
	public PersonAddress getPersonAddress() {
		if (addresses != null && addresses.size() > 0) {
			return (PersonAddress) addresses.toArray()[0];
		} else {
			return null;
		}
	}
	
	/**
	 * Convenience Method
	 * Calculates person's age based on the birthdate
	 * 
	 * @return integer age
	 */
	public Integer getAge() {
		return getAge(null);
	}
	
	/**
	 * Convenience method: calculates the person's age on a given date based on the birthdate
	 * 
	 * @param onDate (null defaults to today)
	 * @return
	 */
	public Integer getAge(Date onDate) {
		if (birthdate == null)
			return null;
		
		Calendar today = Calendar.getInstance();
		if (onDate != null)
			today.setTime(onDate);
		
		Calendar bday = new GregorianCalendar();
		bday.setTime(birthdate);
		
		int age = today.get(Calendar.YEAR) - bday.get(Calendar.YEAR);
		
		//tricky bit:
		// set birthday calendar to this year
		// if the current date is less that the new 'birthday', subtract a year
		bday.set(Calendar.YEAR, today.get(Calendar.YEAR));
		if (today.before(bday))
			age = age -1;
		
		return age;
	}
	
	public User getPersonChangedBy() {
		return changedBy;
	}

	public void setPersonChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}

	public Date getPersonDateChanged() {
		return dateChanged;
	}

	public void setPersonDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}

	public User getPersonCreator() {
		return personCreator;
	}

	public void setPersonCreator(User creator) {
		this.personCreator = creator;
	}

	public Date getPersonDateCreated() {
		return dateCreated;
	}

	public void setPersonDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getPersonDateVoided() {
		return dateVoided;
	}

	public void setPersonDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	public void setPersonVoided(Boolean voided) {
		this.voided = voided;
	}

	public Boolean getPersonVoided() {
		return isPersonVoided();
	}

	public Boolean isPersonVoided() {
		return voided;
	}

	public User getPersonVoidedBy() {
		return voidedBy;
	}

	public void setPersonVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	public String getPersonVoidReason() {
		return voidReason;
	}

	public void setPersonVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
	
	/**
     * @return true/false whether this person is a patient or not
     */
    public boolean isPatient() {
    	return isPatient;
    }

	/**
	 * This should only be set by the database layer by looking
	 * at whether a row exists in the patient table
	 * 
     * @param isPatient whether this person is a patient or not
     */
    @SuppressWarnings("unused")
    private void setPatient(boolean isPatient) {
    	this.isPatient = isPatient;
    }
    
    /**
     * @return true/false whether this person is a user or not
     */
    public boolean isUser() {
    	return isUser;
    }

	/**
	 * This should only be set by the database layer by looking
	 * at whether a row exists in the user table
	 * 
     * @param isUser whether this person is a user or not
     */
    @SuppressWarnings("unused")
    private void setUser(boolean isUser) {
    	this.isUser = isUser;
    }
    

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Person(personId=" + personId + ")";
	}

	/**
	 * If the serializer wishes, don't serialize this entire object, just the important
	 * parts
	 * 
	 * @param sessionMap serialization session information
	 * @return Person object to serialize 
	 * 
	 * @see OpenmrsUtil#isShortSerialization(Map)
	 */
	@Replace
	public Person replaceSerialization(Map<?, ?> sessionMap) {
		if (OpenmrsUtil.isShortSerialization(sessionMap)) {
			// only serialize the person id
			return new Person(getPersonId());
		}
		
		// don't do short serialization
		return this;
	}

}
/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Transient;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.DateBridge;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.EncodingType;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Resolution;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * A Person in the system. This can be either a small person stub, or indicative of an actual
 * Patient in the system. This class holds the generic person things that both the stubs and
 * patients share. Things like birthdate, names, addresses, and attributes are all generified into
 * the person table (and hence this super class)
 * 
 * @see org.openmrs.Patient
 */
public class Person extends BaseChangeableOpenmrsData {
	
	public static final long serialVersionUID = 2L;
	
	private static final Logger log = LoggerFactory.getLogger(Person.class);
	
	@DocumentId
	protected Integer personId;
	
	private Set<PersonAddress> addresses = null;
	
	@ContainedIn
	private Set<PersonName> names = null;
	
	@ContainedIn
	private Set<PersonAttribute> attributes = null;
	
	@Field
	private String gender;
	

	@Field(analyze = Analyze.YES)
	@DateBridge(encoding = EncodingType.STRING, resolution = Resolution.DAY)
	private Date birthdate;
	
	private Date birthtime;
	
	private Boolean birthdateEstimated = false;
	
	private Boolean deathdateEstimated = false;
	
	@Field
	private Boolean dead = false;
	
	private Date deathDate;
	
	private Concept causeOfDeath;
	
	private String causeOfDeathNonCoded;
	
	private User personCreator;
	
	private Date personDateCreated;
	
	private User personChangedBy;
	
	private Date personDateChanged;
	
	private Boolean personVoided = false;
	
	private User personVoidedBy;
	
	private Date personDateVoided;
	
	private String personVoidReason;
	
	@Field
	private boolean isPatient;
	
	/**
	 * Convenience map from PersonAttributeType.name to PersonAttribute.<br>
	 * <br>
	 * This is "cached" for each user upon first load. When an attribute is changed, the cache is
	 * cleared and rebuilt on next access.
	 */
	@Transient
	Map<String, PersonAttribute> attributeMap = null;
	
	@Transient
	private Map<String, PersonAttribute> allAttributeMap = null;
	
	/**
	 * default empty constructor
	 */
	public Person() {
	}
	
	/**
	 * This constructor is used to build a new Person object copy from another person object
	 * (usually a patient or a user subobject). All attributes are copied over to the new object.
	 * NOTE! All child collection objects are copied as pointers, each individual element is not
	 * copied. <br>
	 *
	 * @param person Person to create this person object from
	 */
	public Person(Person person) {
		if (person == null) {
			return;
		}
		
		personId = person.getPersonId();
		setUuid(person.getUuid());
		addresses = person.getAddresses();
		names = person.getNames();
		attributes = person.getAttributes();
		
		gender = person.getGender();
		birthdate = person.getBirthdate();
		birthtime = person.getBirthDateTime();
		birthdateEstimated = person.getBirthdateEstimated();
		deathdateEstimated = person.getDeathdateEstimated();
		dead = person.getDead();
		deathDate = person.getDeathDate();
		causeOfDeath = person.getCauseOfDeath();
		causeOfDeathNonCoded = person.getCauseOfDeathNonCoded();
		// base creator/voidedBy/changedBy info is not copied here
		// because that is specific to and will be recreated
		// by the subobject upon save
		
		setPersonCreator(person.getPersonCreator());
		setPersonDateCreated(person.getPersonDateCreated());
		setPersonChangedBy(person.getPersonChangedBy());
		setPersonDateChanged(person.getPersonDateChanged());
		setPersonVoided(person.getPersonVoided());
		setPersonVoidedBy(person.getPersonVoidedBy());
		setPersonDateVoided(person.getPersonDateVoided());
		setPersonVoidReason(person.getPersonVoidReason());
		
		setPatient(person.getIsPatient());
	}
	
	/**
	 * Default constructor taking in the primary key personId value
	 * 
	 * @param personId Integer internal id for this person
	 * <strong>Should</strong> set person id
	 */
	public Person(Integer personId) {
		this.personId = personId;
	}
	
	// Property accessors
	
	/**
	 * @return Returns the personId.
	 */
	public Integer getPersonId() {
		return personId;
	}
	
	/**
	 * @param personId The personId to set.
	 */
	public void setPersonId(Integer personId) {
		this.personId = personId;
	}
	
	/**
	 * @return person's gender
	 */
	public String getGender() {
		return this.gender;
	}
	
	/**
	 * @param gender person's gender
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	/**
	 * @return person's date of birth
	 */
	public Date getBirthdate() {
		return this.birthdate;
	}
	
	/**
	 * @param birthdate person's date of birth
	 */
	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}
	
	/**
	 * @return true if person's birthdate is estimated
	 * @deprecated as of 2.0, use {@link #getBirthdateEstimated()}
	 */
	@Deprecated
	@JsonIgnore
	public Boolean isBirthdateEstimated() {
		return getBirthdateEstimated();
	}
	
	public Boolean getBirthdateEstimated() {
		return birthdateEstimated;
	}
	
	/**
	 * @param birthdateEstimated true if person's birthdate is estimated
	 */
	public void setBirthdateEstimated(Boolean birthdateEstimated) {
		this.birthdateEstimated = birthdateEstimated;
	}
	
	public Boolean getDeathdateEstimated() {
		return this.deathdateEstimated;
	}
	
	/**
	 * @param deathdateEstimated true if person's deathdate is estimated
	 */
	public void setDeathdateEstimated(Boolean deathdateEstimated) {
		this.deathdateEstimated = deathdateEstimated;
	}
	
	/**
	 * @param birthtime person's time of birth
	 */
	public void setBirthtime(Date birthtime) {
		this.birthtime = birthtime;
	}
	
	/**
	 * @return person's time of birth with the date portion set to the date from person's birthdate
	 */
	public Date getBirthDateTime() {
		if (birthdate != null && birthtime != null) {
			String birthDateString = new SimpleDateFormat("yyyy-MM-dd").format(birthdate);
			String birthTimeString = new SimpleDateFormat("HH:mm:ss").format(birthtime);
			
			try {
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(birthDateString + " " + birthTimeString);
			}
			catch (ParseException e) {
				log.error("Failed to parse birth date string", e);
			}
		}
		return null;
	}
	
	/**
	 * @return person's time of birth.
	 */
	public Date getBirthtime() {
		return this.birthtime;
	}
	
	/**
	 * @return Returns the death status.
	 * @deprecated as of 2.0, use {@link #getDead()}
	 */
	@Deprecated
	@JsonIgnore
	public Boolean isDead() {
		return getDead();
	}
	
	/**
	 * @return Returns the death status.
	 */
	public Boolean getDead() {
		return dead;
	}
	
	/**
	 * @param dead The dead to set.
	 */
	public void setDead(Boolean dead) {
		this.dead = dead;
	}
	
	/**
	 * @return date of person's death
	 */
	public Date getDeathDate() {
		return this.deathDate;
	}
	
	/**
	 * @param deathDate date of person's death
	 */
	public void setDeathDate(Date deathDate) {
		this.deathDate = deathDate;
		if (deathDate != null) {
			setDead(true);
		}
	}
	
	/**
	 * @return cause of person's death
	 */
	public Concept getCauseOfDeath() {
		return this.causeOfDeath;
	}
	
	/**
	 * @param causeOfDeath cause of person's death
	 */
	public void setCauseOfDeath(Concept causeOfDeath) {
		this.causeOfDeath = causeOfDeath;
	}
	
	/**
	 * This method returns the non coded cause of death
	 * 
	 * @return non coded cause of death
	 * @since 2.2.0
	 */
	public String getCauseOfDeathNonCoded() {
		return this.causeOfDeathNonCoded;
	}
	
	/**
	 * This method sets the non coded cause of death with the value given as parameter
	 * 
	 * @param causeOfDeathNonCoded is a String that describes as text the cause of death
	 * @since 2.2.0
	 * <strong>Should</strong> not fail with null causeOfDeathNonCoded
	 * <strong>Should</strong> set the attribute causeOfDeathNonCoded with the given parameter
	 */
	public void setCauseOfDeathNonCoded(String causeOfDeathNonCoded) {
		this.causeOfDeathNonCoded = causeOfDeathNonCoded;
	}
	
	/**
	 * @return list of known addresses for person
	 * @see org.openmrs.PersonAddress
	 * <strong>Should</strong> not get voided addresses
	 * <strong>Should</strong> not fail with null addresses
	 */
	public Set<PersonAddress> getAddresses() {
		if (addresses == null) {
			addresses = new TreeSet<>();
		}
		return this.addresses;
	}
	
	/**
	 * @param addresses Set&lt;PersonAddress&gt; list of known addresses for person
	 * @see org.openmrs.PersonAddress
	 */
	public void setAddresses(Set<PersonAddress> addresses) {
		this.addresses = addresses;
	}
	
	/**
	 * @return all known names for person
	 * @see org.openmrs.PersonName
	 * <strong>Should</strong> not get voided names
	 * <strong>Should</strong> not fail with null names
	 */
	public Set<PersonName> getNames() {
		if (names == null) {
			names = new TreeSet<>();
		}
		return this.names;
	}
	
	/**
	 * @param names update all known names for person
	 * @see org.openmrs.PersonName
	 */
	public void setNames(Set<PersonName> names) {
		this.names = names;
	}
	
	/**
	 * @return all known attributes for person
	 * @see org.openmrs.PersonAttribute
	 * <strong>Should</strong> not get voided attributes
	 * <strong>Should</strong> not fail with null attributes
	 */
	public Set<PersonAttribute> getAttributes() {
		if (attributes == null) {
			attributes = new TreeSet<>();
		}
		return this.attributes;
	}
	
	/**
	 * Returns only the non-voided attributes for this person
	 * 
	 * @return list attributes
	 * <strong>Should</strong> not get voided attributes
	 * <strong>Should</strong> not fail with null attributes
	 */
	public List<PersonAttribute> getActiveAttributes() {
		List<PersonAttribute> attrs = new ArrayList<>();
		for (PersonAttribute attr : getAttributes()) {
			if (!attr.getVoided()) {
				attrs.add(attr);
			}
		}
		return attrs;
	}
	
	/**
	 * @param attributes update all known attributes for person
	 * @see org.openmrs.PersonAttribute
	 */
	public void setAttributes(Set<PersonAttribute> attributes) {
		this.attributes = attributes;
		attributeMap = null;
		allAttributeMap = null;
	}
	
	// Convenience methods
	
	/**
	 * Convenience method to add the <code>attribute</code> to this person's attribute list if the
	 * attribute doesn't exist already.<br>
	 * <br>
	 * Voids any current attribute with type = <code>newAttribute.getAttributeType()</code><br>
	 * <br>
	 * NOTE: This effectively limits persons to only one attribute of any given type **
	 * 
	 * @param newAttribute PersonAttribute to add to the Person
	 * <strong>Should</strong> fail when new attribute exist
	 * <strong>Should</strong> fail when new atribute are the same type with same value
	 * <strong>Should</strong> void old attribute when new attribute are the same type with different value
	 * <strong>Should</strong> remove attribute when old attribute are temporary
	 * <strong>Should</strong> not save an attribute with a null value
	 * <strong>Should</strong> not save an attribute with a blank string value
	 * <strong>Should</strong> void old attribute when a null or blank string value is added
	 */
	public void addAttribute(PersonAttribute newAttribute) {
		newAttribute.setPerson(this);
		boolean newIsNull = !StringUtils.hasText(newAttribute.getValue());
		
		for (PersonAttribute currentAttribute : getActiveAttributes()) {
			if (currentAttribute.equals(newAttribute)) {
				// if we have the same PersonAttributeId, don't add the new attribute
				return;
			} else if (currentAttribute.getAttributeType().equals(newAttribute.getAttributeType())) {
				if (currentAttribute.getValue() != null && currentAttribute.getValue().equals(newAttribute.getValue())) {
					// this person already has this attribute
					return;
				}
				
				// if the to-be-added attribute isn't already voided itself
				// and if we have the same type, different value
				if (!newAttribute.getVoided() || newIsNull) {
					if (currentAttribute.getCreator() != null) {
						currentAttribute.voidAttribute("New value: " + newAttribute.getValue());
					} else {
						// remove the attribute if it was just temporary (didn't have a creator
						// attached to it yet)
						removeAttribute(currentAttribute);
					}
				}
			}
		}
		attributeMap = null;
		allAttributeMap = null;
		if (!OpenmrsUtil.collectionContains(attributes, newAttribute) && !newIsNull) {
			attributes.add(newAttribute);
		}
	}
	
	/**
	 * Convenience method to get the <code>attribute</code> from this person's attribute list if the
	 * attribute exists already.
	 * 
	 * @param attribute
	 * <strong>Should</strong> not fail when person attribute is null
	 * <strong>Should</strong> not fail when person attribute is not exist
	 * <strong>Should</strong> remove attribute when exist
	 */
	public void removeAttribute(PersonAttribute attribute) {
		if (attributes != null && attributes.remove(attribute)) {
			attributeMap = null;
			allAttributeMap = null;
		}
	}
	
	/**
	 * Convenience Method to return the first non-voided person attribute matching a person
	 * attribute type. <br>
	 * <br>
	 * Returns null if this person has no non-voided {@link PersonAttribute} with the given
	 * {@link PersonAttributeType}, the given {@link PersonAttributeType} is null, or this person
	 * has no attributes.
	 * 
	 * @param pat the PersonAttributeType to look for (can be a stub, see
	 *            {@link PersonAttributeType#equals(Object)} for how its compared)
	 * @return PersonAttribute that matches the given type
	 * <strong>Should</strong> not fail when attribute type is null
	 * <strong>Should</strong> not return voided attribute
	 * <strong>Should</strong> return null when existing PersonAttributeType is voided
	 */
	public PersonAttribute getAttribute(PersonAttributeType pat) {
		if (pat != null) {
			for (PersonAttribute attribute : getAttributes()) {
				if (pat.equals(attribute.getAttributeType()) && !attribute.getVoided()) {
					return attribute;
				}
			}
		}
		return null;
	}
	
	/**
	 * Convenience method to get this person's first attribute that has a PersonAttributeType.name
	 * equal to <code>attributeName</code>.<br>
	 * <br>
	 * Returns null if this person has no non-voided {@link PersonAttribute} with the given type
	 * name, the given name is null, or this person has no attributes.
	 * 
	 * @param attributeName the name string to match on
	 * @return PersonAttribute whose {@link PersonAttributeType#getName()} matchs the given name
	 *         string
	 * <strong>Should</strong> return person attribute based on attributeName
	 * <strong>Should</strong> return null if AttributeName is voided
	 */
	public PersonAttribute getAttribute(String attributeName) {
		if (attributeName != null) {
			for (PersonAttribute attribute : getAttributes()) {
				PersonAttributeType type = attribute.getAttributeType();
				if (type != null && attributeName.equals(type.getName()) && !attribute.getVoided()) {
					return attribute;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Convenience method to get this person's first attribute that has a PersonAttributeTypeId
	 * equal to <code>attributeTypeId</code>.<br>
	 * <br>
	 * Returns null if this person has no non-voided {@link PersonAttribute} with the given type id
	 * or this person has no attributes.<br>
	 * <br>
	 * The given id cannot be null.
	 * 
	 * @param attributeTypeId the id of the {@link PersonAttributeType} to look for
	 * @return PersonAttribute whose {@link PersonAttributeType#getId()} equals the given Integer id
	 * <strong>Should</strong> return PersonAttribute based on attributeTypeId
	 * <strong>Should</strong> return null when existing personAttribute with matching attribute type id is voided
	 */
	public PersonAttribute getAttribute(Integer attributeTypeId) {
		for (PersonAttribute attribute : getActiveAttributes()) {
			if (attributeTypeId.equals(attribute.getAttributeType().getPersonAttributeTypeId())) {
				return attribute;
			}
		}
		return null;
	}
	
	/**
	 * Convenience method to get all of this person's attributes that have a
	 * PersonAttributeType.name equal to <code>attributeName</code>.
	 * 
	 * @param attributeName
	 * <strong>Should</strong> return all PersonAttributes with matching attributeType names
	 */
	public List<PersonAttribute> getAttributes(String attributeName) {
		List<PersonAttribute> ret = new ArrayList<>();
		
		for (PersonAttribute attribute : getActiveAttributes()) {
			PersonAttributeType type = attribute.getAttributeType();
			if (type != null && attributeName.equals(type.getName())) {
				ret.add(attribute);
			}
		}
		
		return ret;
	}
	
	/**
	 * Convenience method to get all of this person's attributes that have a PersonAttributeType.id
	 * equal to <code>attributeTypeId</code>.
	 * 
	 * @param attributeTypeId
	 * <strong>Should</strong> return empty list when matching personAttribute by id is voided
	 * <strong>Should</strong> return list of person attributes based on AttributeTypeId
	 */
	public List<PersonAttribute> getAttributes(Integer attributeTypeId) {
		List<PersonAttribute> ret = new ArrayList<>();
		
		for (PersonAttribute attribute : getActiveAttributes()) {
			if (attributeTypeId.equals(attribute.getAttributeType().getPersonAttributeTypeId())) {
				ret.add(attribute);
			}
		}
		
		return ret;
	}
	
	/**
	 * Convenience method to get all of this person's attributes that have a PersonAttributeType
	 * equal to <code>personAttributeType</code>.
	 * 
	 * @param personAttributeType
	 */
	public List<PersonAttribute> getAttributes(PersonAttributeType personAttributeType) {
		List<PersonAttribute> ret = new ArrayList<>();
		for (PersonAttribute attribute : getAttributes()) {
			if (personAttributeType.equals(attribute.getAttributeType()) && !attribute.getVoided()) {
				ret.add(attribute);
			}
		}
		return ret;
	}
	
	/**
	 * Convenience method to get this person's active attributes in map form: &lt;String,
	 * PersonAttribute&gt;.
	 */
	public Map<String, PersonAttribute> getAttributeMap() {
		if (attributeMap != null) {
			return attributeMap;
		}
		
		log.debug("Current Person Attributes: \n{}", printAttributes());
		
		attributeMap = new HashMap<>();
		for (PersonAttribute attribute : getActiveAttributes()) {
			attributeMap.put(attribute.getAttributeType().getName(), attribute);
		}
		
		return attributeMap;
	}
	
	/**
	 * Convenience method to get all of this person's attributes (including voided ones) in map
	 * form: &lt;String, PersonAttribute&gt;.
	 * 
	 * @return All person's attributes in map form
	 * @since 1.12
	 */
	public Map<String, PersonAttribute> getAllAttributeMap() {
		if (allAttributeMap != null) {
			return allAttributeMap;
		}
		
		log.debug("Current Person Attributes: \n{}", printAttributes());
		
		allAttributeMap = new HashMap<>();
		for (PersonAttribute attribute : getAttributes()) {
			allAttributeMap.put(attribute.getAttributeType().getName(), attribute);
		}
		
		return allAttributeMap;
	}
	
	/**
	 * Convenience method for viewing all of the person's current attributes
	 * 
	 * @return Returns a string with all the attributes
	 */
	public String printAttributes() {
		StringBuilder s = new StringBuilder("");
		
		for (PersonAttribute attribute : getAttributes()) {
			s.append(attribute.getAttributeType()).append(" : ").append(attribute.getValue()).append(" : voided? ")
			        .append(attribute.getVoided()).append("\n");
		}
		
		return s.toString();
	}
	
	/**
	 * Convenience method to add the <code>name</code> to this person's name list if the name
	 * doesn't exist already.
	 * 
	 * @param name
	 */
	public void addName(PersonName name) {
		if (name != null) {
			name.setPerson(this);
			if (names == null) {
				names = new TreeSet<>();
			}
			if (!OpenmrsUtil.collectionContains(names, name)) {
				names.add(name);
			}
		}
	}
	
	/**
	 * Convenience method remove the <code>name</code> from this person's name list if the name
	 * exists already.
	 * 
	 * @param name
	 */
	public void removeName(PersonName name) {
		if (names != null) {
			names.remove(name);
		}
	}
	
	/**
	 * Convenience method to add the <code>address</code> to this person's address list if the
	 * address doesn't exist already.
	 * 
	 * @param address
	 * <strong>Should</strong> not add a person address with blank fields
	 */
	public void addAddress(PersonAddress address) {
		if (address != null) {
			address.setPerson(this);
			if (addresses == null) {
				addresses = new TreeSet<>();
			}
			if (!OpenmrsUtil.collectionContains(addresses, address) && !address.isBlank()) {
				addresses.add(address);
			}
		}
	}
	
	/**
	 * Convenience method to remove the <code>address</code> from this person's address list if the
	 * address exists already.
	 * 
	 * @param address
	 */
	public void removeAddress(PersonAddress address) {
		if (addresses != null) {
			addresses.remove(address);
		}
	}
	
	/**
	 * Convenience method to get the {@link PersonName} object that is marked as "preferred". <br>
	 * <br>
	 * If two names are marked as preferred (or no names), the database ordering comes into effect
	 * and the one that was created most recently will be returned. <br>
	 * <br>
	 * This method will never return a voided name, even if it is marked as preferred. <br>
	 * <br>
	 * Null is returned if this person has no names or all voided names.
	 * 
	 * @return the "preferred" person name.
	 * @see #getNames()
	 * @see PersonName#getPreferred()
	 * <strong>Should</strong> get preferred and not-voided person name if exist
	 * <strong>Should</strong> get not-voided person name if preferred address does not exist
	 * <strong>Should</strong> get voided person address if person is voided and not-voided address does not exist
	 * <strong>Should</strong> return null if person is not-voided and have voided names
	 */
	public PersonName getPersonName() {
		// normally the DAO layer returns these in the correct order, i.e. preferred and non-voided first, but it's possible that someone
		// has fetched a Person, changed their names around, and then calls this method, so we have to be careful.
		if (getNames() != null && !getNames().isEmpty()) {
			for (PersonName name : getNames()) {
				if (name.getPreferred() && !name.getVoided()) {
					return name;
				}
			}
			for (PersonName name : getNames()) {
				if (!name.getVoided()) {
					return name;
				}
			}
			
			if (getVoided()) {
				return getNames().iterator().next();
			}
		}
		return null;
	}
	
	/**
	 * Convenience method to get the given name attribute on this person's preferred PersonName
	 * 
	 * @return String given name of the person
	 */
	public String getGivenName() {
		PersonName personName = getPersonName();
		if (personName == null) {
			return "";
		} else {
			return personName.getGivenName();
		}
	}
	
	/**
	 * Convenience method to get the middle name attribute on this person's preferred PersonName
	 * 
	 * @return String middle name of the person
	 */
	public String getMiddleName() {
		PersonName personName = getPersonName();
		if (personName == null) {
			return "";
		} else {
			return personName.getMiddleName();
		}
	}
	
	/**
	 * Convenience method to get the family name attribute on this person's preferred PersonName
	 * 
	 * @return String family name of the person
	 */
	public String getFamilyName() {
		PersonName personName = getPersonName();
		if (personName == null) {
			return "";
		} else {
			return personName.getFamilyName();
		}
	}
	
	/**
	 * Convenience method to get the {@link PersonAddress} object that is marked as "preferred". <br>
	 * <br>
	 * If two addresses are marked as preferred (or no addresses), the database ordering comes into
	 * effect and the one that was created most recently will be returned. <br>
	 * <br>
	 * This method will never return a voided address, even if it is marked as preferred. <br>
	 * <br>
	 * Null is returned if this person has no addresses or all voided addresses.
	 * 
	 * @return the "preferred" person address.
	 * @see #getAddresses()
	 * @see PersonAddress#getPreferred()
	 * <strong>Should</strong> get preferred and not-voided person address if exist
	 * <strong>Should</strong> get not-voided person address if preferred address does not exist
	 * <strong>Should</strong> get voided person address if person is voided and not-voided address does not exist
	 * <strong>Should</strong> return null if person is not-voided and have voided address
	 */
	public PersonAddress getPersonAddress() {
		// normally the DAO layer returns these in the correct order, i.e. preferred and non-voided first, but it's possible that someone
		// has fetched a Person, changed their addresses around, and then calls this method, so we have to be careful.
		if (getAddresses() != null && !getAddresses().isEmpty()) {
			for (PersonAddress addr : getAddresses()) {
				if (addr.getPreferred() && !addr.getVoided()) {
					return addr;
				}
			}
			for (PersonAddress addr : getAddresses()) {
				if (!addr.getVoided()) {
					return addr;
				}
			}
			
			if (getVoided()) {
				return getAddresses().iterator().next();
			}
		}
		return null;
	}
	
	/**
	 * Convenience method to calculate this person's age based on the birthdate For a person who
	 * lived 1990 to 2000, age would be -5 in 1985, 5 in 1995, 10 in 2000, and 10 2010.
	 * 
	 * @return Returns age as an Integer.
	 * <strong>Should</strong> get correct age after death
	 */
	public Integer getAge() {
		return getAge(null);
	}
	
	/**
	 * Convenience method: calculates the person's age on a given date based on the birthdate
	 * 
	 * @param onDate (null defaults to today)
	 * @return int value of the person's age
	 * <strong>Should</strong> get age before birthday
	 * <strong>Should</strong> get age on birthday with no minutes defined
	 * <strong>Should</strong> get age on birthday with minutes defined
	 * <strong>Should</strong> get age after birthday
	 * <strong>Should</strong> get age after death
	 * <strong>Should</strong> get age with given date after death
	 * <strong>Should</strong> get age with given date before death
	 * <strong>Should</strong> get age with given date before birth
	 */
	public Integer getAge(Date onDate) {
		if (birthdate == null) {
			return null;
		}
		
		// Use default end date as today.
		Calendar today = Calendar.getInstance();
		// But if given, use the given date.
		if (onDate != null) {
			today.setTime(onDate);
		}
		
		// If date given is after date of death then use date of death as end date
		if (getDeathDate() != null && today.getTime().after(getDeathDate())) {
			today.setTime(getDeathDate());
		}
		
		Calendar bday = Calendar.getInstance();
		bday.setTime(birthdate);
		
		int age = today.get(Calendar.YEAR) - bday.get(Calendar.YEAR);
		
		// Adjust age when today's date is before the person's birthday
		int todaysMonth = today.get(Calendar.MONTH);
		int bdayMonth = bday.get(Calendar.MONTH);
		int todaysDay = today.get(Calendar.DAY_OF_MONTH);
		int bdayDay = bday.get(Calendar.DAY_OF_MONTH);
		
		if (todaysMonth < bdayMonth) {
			age--;
		} else if (todaysMonth == bdayMonth && todaysDay < bdayDay) {
			// we're only comparing on month and day, not minutes, etc
			age--;
		}
		
		return age;
	}
	
	/**
	 * Convenience method: sets a person's birth date from an age as of the given date Also sets
	 * flag indicating that the birth date is inexact. This sets the person's birth date to January
	 * 1 of the year that matches this age and date
	 * 
	 * @param age (the age to set)
	 * @param ageOnDate (null defaults to today)
	 */
	public void setBirthdateFromAge(int age, Date ageOnDate) {
		Calendar c = Calendar.getInstance();
		c.setTime(ageOnDate == null ? new Date() : ageOnDate);
		c.set(Calendar.DATE, 1);
		c.set(Calendar.MONTH, Calendar.JANUARY);
		c.add(Calendar.YEAR, -1 * age);
		setBirthdate(c.getTime());
		setBirthdateEstimated(true);
		
	}
	
	public User getPersonChangedBy() {
		return personChangedBy;
	}
	
	public void setPersonChangedBy(User changedBy) {
		this.personChangedBy = changedBy;
		this.setChangedBy(changedBy);
	}
	
	public Date getPersonDateChanged() {
		return personDateChanged;
	}
	
	public void setPersonDateChanged(Date dateChanged) {
		this.personDateChanged = dateChanged;
		this.setDateChanged(dateChanged);
	}
	
	public User getPersonCreator() {
		return personCreator;
	}
	
	public void setPersonCreator(User creator) {
		this.personCreator = creator;
		this.setCreator(creator);
	}
	
	public Date getPersonDateCreated() {
		return personDateCreated;
	}
	
	public void setPersonDateCreated(Date dateCreated) {
		this.personDateCreated = dateCreated;
		this.setDateCreated(dateCreated);
	}
	
	public Date getPersonDateVoided() {
		return personDateVoided;
	}
	
	public void setPersonDateVoided(Date dateVoided) {
		this.personDateVoided = dateVoided;
		this.setDateVoided(dateVoided);
	}
	
	public void setPersonVoided(Boolean voided) {
		this.personVoided = voided;
		this.setVoided(voided);
	}
	
	public Boolean getPersonVoided() {
		return personVoided;
	}
	
	/**
	 * @deprecated as of 2.0, use {@link #getPersonVoided()}
	 */
	@Deprecated
	@JsonIgnore
	public Boolean isPersonVoided() {
		return getPersonVoided();
	}
	
	public User getPersonVoidedBy() {
		return personVoidedBy;
	}
	
	public void setPersonVoidedBy(User voidedBy) {
		this.personVoidedBy = voidedBy;
		this.setVoidedBy(voidedBy);
	}
	
	public String getPersonVoidReason() {
		return personVoidReason;
	}
	
	public void setPersonVoidReason(String voidReason) {
		this.personVoidReason = voidReason;
		this.setVoidReason(voidReason);
	}
	
	/**
	 * @return true/false whether this person is a patient or not
	 * @deprecated as of 2.0, use {@link #getIsPatient()}
	 */
	@Deprecated
	@JsonIgnore
	public boolean isPatient() {
		return getIsPatient();
	}
	
	public boolean getIsPatient() {
		return isPatient;
	}
	
	/**
	 * This should only be set by the database layer by looking at whether a row exists in the
	 * patient table
	 * 
	 * @param isPatient whether this person is a patient or not
	 */
	protected void setPatient(boolean isPatient) {
		this.isPatient = isPatient;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Person(personId=" + personId + ")";
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		
		return getPersonId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setPersonId(id);
		
	}
	
}

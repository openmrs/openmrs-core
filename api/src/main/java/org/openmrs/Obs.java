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

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.annotation.AllowDirectAccess;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.util.Format;
import org.openmrs.util.Format.FORMAT_TYPE;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An observation is a single unit of clinical information. <br>
 * <br>
 * Observations are collected and grouped together into one Encounter (one visit). Obs can be
 * grouped in a hierarchical fashion. <br>
 * <br>
 * <p>
 * The {@link #getObsGroup()} method returns an optional parent. That parent object is also an Obs.
 * The parent Obs object knows about its child objects through the {@link #getGroupMembers()}
 * method.
 * </p>
 * <p>
 * (Multi-level hierarchies are achieved by an Obs parent object being a member of another Obs
 * (grand)parent object) Read up on the obs table: http://openmrs.org/wiki/Obs_Table_Primer In an
 * OpenMRS installation, there may be an occasion need to change an Obs.
 * </p>
 * <p>
 * For example, a site may decide to replace a concept in the dictionary with a more specific set of
 * concepts. An observation is part of the official record of an encounter. There may be legal,
 * ethical, and auditing consequences from altering a record. It is recommended that you create a
 * new Obs and void the old one:
 * </p>
 * Obs newObs = Obs.newInstance(oldObs); //copies values from oldObs
 * newObs.setPreviousVersion(oldObs);
 * Context.getObsService().saveObs(newObs,"Your reason for the change here");
 * Context.getObsService().voidObs(oldObs, "Your reason for the change here");
 * 
 * @see Encounter
 */
public class Obs extends BaseFormRecordableOpenmrsData {
	
	/**
	 * @since 2.1.0
	 */
	public enum Interpretation {
		NORMAL, ABNORMAL, CRITICALLY_ABNORMAL, NEGATIVE, POSITIVE, CRITICALLY_LOW, LOW, HIGH, CRITICALLY_HIGH, VERY_SUSCEPTIBLE, SUSCEPTIBLE, INTERMEDIATE, RESISTANT, SIGNIFICANT_CHANGE_DOWN, SIGNIFICANT_CHANGE_UP, OFF_SCALE_LOW, OFF_SCALE_HIGH
	}
	
	/**
	 * @since 2.1.0
	 */
	public enum Status {
		PRELIMINARY, FINAL, AMENDED
	}
	
	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";
	
	private static final String TIME_PATTERN = "HH:mm";
	
	private static final String DATE_PATTERN = "yyyy-MM-dd";
	
	public static final long serialVersionUID = 112342333L;
	
	private static final Logger log = LoggerFactory.getLogger(Obs.class);
	
	protected Integer obsId;
	
	protected Concept concept;
	
	protected Date obsDatetime;
	
	protected String accessionNumber;
	
	/**
	 * The "parent" of this obs. It is the grouping that brings other obs together. note:
	 * obsGroup.getConcept().isSet() should be true This will be non-null if this obs is a member of
	 * another groupedObs
	 * 
	 * @see #isObsGrouping() (??)
	 */
	protected Obs obsGroup;
	
	/**
	 * The list of obs grouped under this obs.
	 */
	@AllowDirectAccess
	protected Set<Obs> groupMembers;
	
	protected Concept valueCoded;
	
	protected ConceptName valueCodedName;
	
	protected Drug valueDrug;
	
	protected Integer valueGroupId;
	
	protected Date valueDatetime;
	
	protected Double valueNumeric;
	
	protected String valueModifier;
	
	protected String valueText;
	
	protected String valueComplex;
	
	// ComplexData is not persisted in the database.
	protected transient ComplexData complexData;
	
	protected String comment;
	
	protected transient Integer personId;
	
	protected Person person;
	
	protected Order order;
	
	protected Location location;
	
	protected Encounter encounter;
	
	private Obs previousVersion;
	
	private Boolean dirty = Boolean.FALSE;
	
	private Interpretation interpretation;
	
	private Status status = Status.FINAL;
	
	/** default constructor */
	public Obs() {
	}
	
	/**
	 * Required parameters constructor A value is also required, but that can be one of: valueCoded,
	 * valueDrug, valueNumeric, or valueText
	 * 
	 * @param person The Person this obs is acting on
	 * @param question The question concept this obs is related to
	 * @param obsDatetime The time this obs took place
	 * @param location The location this obs took place
	 */
	public Obs(Person person, Concept question, Date obsDatetime, Location location) {
		this.person = person;
		if (person != null) {
			this.personId = person.getPersonId();
		}
		this.concept = question;
		this.obsDatetime = obsDatetime;
		this.location = location;
	}
	
	/** constructor with id */
	public Obs(Integer obsId) {
		this.obsId = obsId;
	}
	
	/**
	 * This is an equivalent to a copy constructor. Creates a new copy of the given
	 * <code>obsToCopy</code> with a null obs id
	 * 
	 * @param obsToCopy The Obs that is going to be copied
	 * @return a new Obs object with all the same attributes as the given obs
	 */
	public static Obs newInstance(Obs obsToCopy) {
		Obs newObs = new Obs(obsToCopy.getPerson(), obsToCopy.getConcept(), obsToCopy.getObsDatetime(),
		        obsToCopy.getLocation());
		
		newObs.setObsGroup(obsToCopy.getObsGroup());
		newObs.setAccessionNumber(obsToCopy.getAccessionNumber());
		newObs.setValueCoded(obsToCopy.getValueCoded());
		newObs.setValueDrug(obsToCopy.getValueDrug());
		newObs.setValueGroupId(obsToCopy.getValueGroupId());
		newObs.setValueDatetime(obsToCopy.getValueDatetime());
		newObs.setValueNumeric(obsToCopy.getValueNumeric());
		newObs.setValueModifier(obsToCopy.getValueModifier());
		newObs.setValueText(obsToCopy.getValueText());
		newObs.setComment(obsToCopy.getComment());
		newObs.setEncounter(obsToCopy.getEncounter());
		newObs.setCreator(obsToCopy.getCreator());
		newObs.setDateCreated(obsToCopy.getDateCreated());
		newObs.setVoided(obsToCopy.getVoided());
		newObs.setVoidedBy(obsToCopy.getVoidedBy());
		newObs.setDateVoided(obsToCopy.getDateVoided());
		newObs.setVoidReason(obsToCopy.getVoidReason());
		newObs.setStatus(obsToCopy.getStatus());
		newObs.setInterpretation(obsToCopy.getInterpretation());
		newObs.setOrder(obsToCopy.getOrder());
		
		newObs.setValueComplex(obsToCopy.getValueComplex());
		newObs.setComplexData(obsToCopy.getComplexData());
		newObs.setFormField(obsToCopy.getFormFieldNamespace(), obsToCopy.getFormFieldPath());
		
		// Copy list of all members, including voided, and put them in respective groups
		if (obsToCopy.hasGroupMembers(true)) {
			for (Obs member : obsToCopy.getGroupMembers(true)) {
				// if the obs hasn't been saved yet, no need to duplicate it
				if (member.getObsId() == null) {
					newObs.addGroupMember(member);
				} else {
					Obs newMember = Obs.newInstance(member);
					newMember.setPreviousVersion(member);
					newObs.addGroupMember(newMember);
				}
			}
		}
		
		return newObs;
	}
	
	// Property accessors
	
	/**
	 * @return Returns the comment.
	 */
	public String getComment() {
		return comment;
	}
	
	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment) {
		markAsDirty(this.comment, comment);
		this.comment = comment;
	}
	
	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}
	
	/**
	 * @param concept The concept to set.
	 */
	public void setConcept(Concept concept) {
		markAsDirty(this.concept, concept);
		this.concept = concept;
	}
	
	/**
	 * Get the concept description that is tied to the concept name that was used when making this
	 * observation
	 * 
	 * @return ConceptDescription the description used
	 */
	public ConceptDescription getConceptDescription() {
		// if we don't have a question for this concept,
		// then don't bother looking for a description
		if (getConcept() == null) {
			return null;
		}
		
		// ABKTOD: description in which locale?
		return concept.getDescription();
	}
	
	/**
	 * @return Returns the encounter.
	 */
	public Encounter getEncounter() {
		return encounter;
	}
	
	/**
	 * @param encounter The encounter to set.
	 */
	public void setEncounter(Encounter encounter) {
		markAsDirty(this.encounter, encounter);
		this.encounter = encounter;
	}
	
	/**
	 * @return Returns the location.
	 */
	public Location getLocation() {
		return location;
	}
	
	/**
	 * @param location The location to set.
	 */
	public void setLocation(Location location) {
		markAsDirty(this.location, location);
		this.location = location;
	}
	
	/**
	 * @return Returns the obsDatetime.
	 */
	public Date getObsDatetime() {
		return obsDatetime;
	}
	
	/**
	 * @param obsDatetime The obsDatetime to set.
	 */
	public void setObsDatetime(Date obsDatetime) {
		markAsDirty(this.obsDatetime, obsDatetime);
		this.obsDatetime = obsDatetime;
	}
	
	/**
	 * An obs grouping occurs when the question (#getConcept()) is a set. (@link
	 * org.openmrs.Concept#isSet()) If this is non-null, it means the current Obs is in the list
	 * returned by <code>obsGroup</code>.{@link #getGroupMembers()}
	 * 
	 * @return the Obs that is the grouping factor
	 */
	public Obs getObsGroup() {
		return obsGroup;
	}
	
	/**
	 * This method does NOT add this current obs to the list of obs in obsGroup.getGroupMembers().
	 * That must be done (and should be done) manually. (I am not doing it here for fear of screwing
	 * up the normal loading and creation of this object via hibernate/spring)
	 * 
	 * @param obsGroup the obsGroup to set
	 */
	public void setObsGroup(Obs obsGroup) {
		markAsDirty(this.obsGroup, obsGroup);
		this.obsGroup = obsGroup;
	}
	
	/**
	 * Convenience method that checks for if this obs has 1 or more group members (either voided or
	 * non-voided) Note this method differs from hasGroupMembers(), as that method excludes voided
	 * obs; logic is that while a obs that has only voided group members should be seen as
	 * "having no group members" it still should be considered an "obs grouping"
	 * <p>
	 * NOTE: This method could also be called "isObsGroup" for a little less confusion on names.
	 * However, jstl in a web layer (or any psuedo-getter) access isn't good with both an
	 * "isObsGroup" method and a "getObsGroup" method. Which one should be returned with a
	 * simplified jstl call like ${obs.obsGroup} ? With this setup, ${obs.obsGrouping} returns a
	 * boolean of whether this obs is a parent and has members. ${obs.obsGroup} returns the parent
	 * object to this obs if this obs is a group member of some other group.
	 * 
	 * @return true if this is the parent group of other obs
	 */
	public boolean isObsGrouping() {
		return hasGroupMembers(true);
	}
	
	/**
	 * A convenience method to check for nullity and length to determine if this obs has group
	 * members. By default, this ignores voided-objects. To include voided, use
	 * {@link #hasGroupMembers(boolean)} with value true.
	 * 
	 * @return true if this is the parent group of other obs
	 * <strong>Should</strong> not include voided obs
	 */
	public boolean hasGroupMembers() {
		return hasGroupMembers(false);
	}
	
	/**
	 * Convenience method that checks for nullity and length to determine if this obs has group
	 * members. The parameter specifies if this method whether or not voided obs should be
	 * considered.
	 * 
	 * @param includeVoided determines if Voided members should be considered as group members.
	 * @return true if this is the parent group of other Obs
	 * <strong>Should</strong> return true if this obs has group members based on parameter
	 */
	public boolean hasGroupMembers(boolean includeVoided) {
		// ! symbol used because if it's not empty, we want true
		return !org.springframework.util.CollectionUtils.isEmpty(getGroupMembers(includeVoided));
	}
	
	/**
	 * Get the non-voided members of the obs group, if this obs is a group. By default this method
	 * only returns non-voided group members. To get all group members, use
	 * {@link #getGroupMembers(boolean)} with value true.
	 * <p>
	 * If it's not a group (i.e. {@link #getConcept()}.{@link org.openmrs.Concept#getSet()} is not
	 * true, then this returns null.
	 * 
	 * @return a Set&lt;Obs&gt; of the members of this group.
	 * @see #addGroupMember(Obs)
	 * @see #hasGroupMembers()
	 */
	public Set<Obs> getGroupMembers() {
		//same as just returning groupMembers
		return getGroupMembers(false);
	}
	
	/**
	 * Get the group members of this obs group, if this obs is a group. This method will either
	 * return all group members, or only non-voided group members, depending on if the argument is
	 * set to be true or false respectively.
	 * 
	 * @param includeVoided
	 * @return the set of group members in this obs group
	 * <strong>Should</strong> Get all group members if passed true, and non-voided if passed false
	 */
	public Set<Obs> getGroupMembers(boolean includeVoided) {
		if (includeVoided) {
			//just return all group members
			return groupMembers;
		}
		if (groupMembers == null) {
			//Empty set so return null
			return null;
		}
		Set<Obs> nonVoided = new LinkedHashSet<>(groupMembers);
		nonVoided.removeIf(BaseOpenmrsData::getVoided);
		return nonVoided;
	}
	
	/**
	 * Set the members of the obs group, if this obs is a group.
	 * <p>
	 * If it's not a group (i.e. {@link #getConcept()}.{@link org.openmrs.Concept#getSet()} is not
	 * true, then this returns null.
	 * 
	 * @param groupMembers the groupedObs to set
	 * @see #addGroupMember(Obs)
	 * @see #hasGroupMembers()
	 * <strong>Should</strong> mark the obs as dirty when the set is changed from null to a non empty one
	 * <strong>Should</strong> not mark the obs as dirty when the set is changed from null to an empty one
	 * <strong>Should</strong> mark the obs as dirty when the set is replaced with another with different members
	 * <strong>Should</strong> not mark the obs as dirty when the set is replaced with another with same members
	 */
	public void setGroupMembers(Set<Obs> groupMembers) {
		//Copy over the entire list
		this.groupMembers = groupMembers;
		
	}
	
	/**
	 * Convenience method to add the given <code>obs</code> to this grouping. Will implicitly make
	 * this obs an ObsGroup.
	 * 
	 * @param member Obs to add to this group
	 * @see #setGroupMembers(Set)
	 * @see #getGroupMembers()
	 * <strong>Should</strong> return true when a new obs is added as a member
	 * <strong>Should</strong> return false when a duplicate obs is added as a member
	 */
	public void addGroupMember(Obs member) {
		if (member == null) {
			return;
		}
		
		if (getGroupMembers() == null) {
			groupMembers = new HashSet<>();
		}
		
		// a quick sanity check to make sure someone isn't adding
		// itself to the group
		if (member.equals(this)) {
			throw new APIException("Obs.error.groupCannotHaveItselfAsAMentor", new Object[] { this, member });
		}
		
		member.setObsGroup(this);
		groupMembers.add(member);
	}
	
	/**
	 * Convenience method to remove an Obs from this grouping This also removes the link in the
	 * given <code>obs</code>object to this obs grouper
	 * 
	 * @param member Obs to remove from this group
	 * @see #setGroupMembers(Set)
	 * @see #getGroupMembers()
	 * <strong>Should</strong> return true when an obs is removed
	 * <strong>Should</strong> return false when a non existent obs is removed
	 */
	public void removeGroupMember(Obs member) {
		if (member == null || getGroupMembers() == null) {
			return;
		}
		
		if (groupMembers.remove(member)) {
			member.setObsGroup(null);
		}
	}
	
	/**
	 * Convenience method that returns related Obs If the Obs argument is not an ObsGroup: a
	 * Set&lt;Obs&gt; will be returned containing all of the children of this Obs' parent that are
	 * not ObsGroups themselves. This will include this Obs by default, unless getObsGroup() returns
	 * null, in which case an empty set is returned. If the Obs argument is an ObsGroup: a
	 * Set&lt;Obs&gt; will be returned containing 1. all of this Obs' group members, and 2. all
	 * ancestor Obs that are not themselves obsGroups.
	 * 
	 * @return Set&lt;Obs&gt;
	 */
	public Set<Obs> getRelatedObservations() {
		Set<Obs> ret = new HashSet<>();
		if (this.isObsGrouping()) {
			ret.addAll(this.getGroupMembers());
			Obs parentObs = this;
			while (parentObs.getObsGroup() != null) {
				for (Obs obsSibling : parentObs.getObsGroup().getGroupMembers()) {
					if (!obsSibling.isObsGrouping()) {
						ret.add(obsSibling);
					}
				}
				parentObs = parentObs.getObsGroup();
			}
		} else if (this.getObsGroup() != null) {
			for (Obs obsSibling : this.getObsGroup().getGroupMembers()) {
				if (!obsSibling.isObsGrouping()) {
					ret.add(obsSibling);
				}
			}
		}
		return ret;
	}
	
	/**
	 * @return Returns the obsId.
	 */
	public Integer getObsId() {
		return obsId;
	}
	
	/**
	 * @param obsId The obsId to set.
	 */
	public void setObsId(Integer obsId) {
		this.obsId = obsId;
	}
	
	/**
	 * @return Returns the order.
	 */
	public Order getOrder() {
		return order;
	}
	
	/**
	 * @param order The order to set.
	 */
	public void setOrder(Order order) {
		markAsDirty(this.order, order);
		this.order = order;
	}
	
	/**
	 * The person id of the person on this object. This should be the same as
	 * <code>{@link #getPerson()}.getPersonId()</code>. It is duplicated here for speed and
	 * simplicity reasons
	 * 
	 * @return the integer person id of the person this obs is acting on
	 */
	public Integer getPersonId() {
		return personId;
	}
	
	/**
	 * Set the person id on this obs object. This method is here for convenience, but really the
	 * {@link #setPerson(Person)} method should be used like
	 * <code>setPerson(new Person(personId))</code>
	 * 
	 * @see #setPerson(Person)
	 * @param personId
	 */
	protected void setPersonId(Integer personId) {
		markAsDirty(this.personId, personId);
		this.personId = personId;
	}
	
	/**
	 * Get the person object that this obs is acting on.
	 * 
	 * @see #getPersonId()
	 * @return the person object
	 */
	public Person getPerson() {
		return person;
	}
	
	/**
	 * Set the person object to this obs object. This will also set the personId on this obs object
	 * 
	 * @see #setPersonId(Integer)
	 * @param person the Patient/Person object that this obs is acting on
	 */
	public void setPerson(Person person) {
		markAsDirty(this.person, person);
		this.person = person;
		if (person != null) {
			setPersonId(person.getPersonId());
		}
	}
	
	/**
	 * Sets the value of this obs to the specified valueBoolean if this obs has a boolean concept.
	 * 
	 * @param valueBoolean the boolean value matching the boolean coded concept to set to
	 */
	public void setValueBoolean(Boolean valueBoolean) {
		if (getConcept() != null && getConcept().getDatatype() != null && getConcept().getDatatype().isBoolean()) {
			if (valueBoolean != null) {
				setValueCoded(valueBoolean ? Context.getConceptService().getTrueConcept() : Context.getConceptService()
				        .getFalseConcept());
			} else {
				setValueCoded(null);
			}
		}
	}
	
	/**
	 * Coerces a value to a Boolean representation
	 * 
	 * @return Boolean representation of the obs value
	 * <strong>Should</strong> return true for value_numeric concepts if value is 1
	 * <strong>Should</strong> return false for value_numeric concepts if value is 0
	 * <strong>Should</strong> return null for value_numeric concepts if value is neither 1 nor 0
	 */
	public Boolean getValueAsBoolean() {
		
		if (getValueCoded() != null) {
			if (getValueCoded().equals(Context.getConceptService().getTrueConcept())) {
				return Boolean.TRUE;
			} else if (getValueCoded().equals(Context.getConceptService().getFalseConcept())) {
				return Boolean.FALSE;
			}
		} else if (getValueNumeric() != null) {
			if (getValueNumeric() == 1) {
				return Boolean.TRUE;
			} else if (getValueNumeric() == 0) {
				return Boolean.FALSE;
			}
		}
		//returning null is preferred to defaulting to false to support validation of user input is from a form
		return null;
	}
	
	/**
	 * Returns the boolean value if the concept of this obs is of boolean datatype
	 * 
	 * @return true or false if value is set otherwise null
	 * <strong>Should</strong> return true if value coded answer concept is true concept
	 * <strong>Should</strong> return false if value coded answer concept is false concept
	 */
	public Boolean getValueBoolean() {
		if (getConcept() != null && valueCoded != null && getConcept().getDatatype().isBoolean()) {
			Concept trueConcept = Context.getConceptService().getTrueConcept();
			return trueConcept != null && valueCoded.getId().equals(trueConcept.getId());
		}
		
		return null;
	}
	
	/**
	 * @return Returns the valueCoded.
	 */
	public Concept getValueCoded() {
		return valueCoded;
	}
	
	/**
	 * @param valueCoded The valueCoded to set.
	 */
	public void setValueCoded(Concept valueCoded) {
		markAsDirty(this.valueCoded, valueCoded);
		this.valueCoded = valueCoded;
	}
	
	/**
	 * Gets the specific name used for the coded value.
	 * 
	 * @return the name of the coded value
	 */
	public ConceptName getValueCodedName() {
		return valueCodedName;
	}
	
	/**
	 * Sets the specific name used for the coded value.
	 * 
	 * @param valueCodedName the name of the coded value
	 */
	public void setValueCodedName(ConceptName valueCodedName) {
		markAsDirty(this.valueCodedName, valueCodedName);
		this.valueCodedName = valueCodedName;
	}
	
	/**
	 * @return Returns the valueDrug
	 */
	public Drug getValueDrug() {
		return valueDrug;
	}
	
	/**
	 * @param valueDrug The valueDrug to set.
	 */
	public void setValueDrug(Drug valueDrug) {
		markAsDirty(this.valueDrug, valueDrug);
		this.valueDrug = valueDrug;
	}
	
	/**
	 * @return Returns the valueDatetime.
	 */
	public Date getValueDatetime() {
		return valueDatetime;
	}
	
	/**
	 * @param valueDatetime The valueDatetime to set.
	 */
	public void setValueDatetime(Date valueDatetime) {
		markAsDirty(this.valueDatetime, valueDatetime);
		this.valueDatetime = valueDatetime;
	}
	
	/**
	 * @return the value of this obs as a Date. Note that this uses a java.util.Date, so it includes
	 *         a time component, that should be ignored.
	 * @since 1.9
	 */
	public Date getValueDate() {
		return valueDatetime;
	}
	
	/**
	 * @param valueDate The date value to set.
	 * @since 1.9
	 */
	public void setValueDate(Date valueDate) {
		markAsDirty(this.valueDatetime, valueDate);
		this.valueDatetime = valueDate;
	}
	
	/**
	 * @return the time value of this obs. Note that this uses a java.util.Date, so it includes a
	 *         date component, that should be ignored.
	 * @since 1.9
	 */
	public Date getValueTime() {
		return valueDatetime;
	}
	
	/**
	 * @param valueTime the time value to set
	 * @since 1.9
	 */
	public void setValueTime(Date valueTime) {
		markAsDirty(this.valueDatetime, valueTime);
		this.valueDatetime = valueTime;
	}
	
	/**
	 * @return Returns the valueGroupId.
	 */
	public Integer getValueGroupId() {
		return valueGroupId;
	}
	
	/**
	 * @param valueGroupId The valueGroupId to set.
	 */
	public void setValueGroupId(Integer valueGroupId) {
		markAsDirty(this.valueGroupId, valueGroupId);
		this.valueGroupId = valueGroupId;
	}
	
	/**
	 * @return Returns the valueModifier.
	 */
	public String getValueModifier() {
		return valueModifier;
	}
	
	/**
	 * @param valueModifier The valueModifier to set.
	 */
	public void setValueModifier(String valueModifier) {
		markAsDirty(this.valueModifier, valueModifier);
		this.valueModifier = valueModifier;
	}
	
	/**
	 * @return Returns the valueNumeric.
	 */
	public Double getValueNumeric() {
		return valueNumeric;
	}
	
	/**
	 * @param valueNumeric The valueNumeric to set.
	 */
	public void setValueNumeric(Double valueNumeric) {
		markAsDirty(this.valueNumeric, valueNumeric);
		this.valueNumeric = valueNumeric;
	}
	
	/**
	 * @return Returns the valueText.
	 */
	public String getValueText() {
		return valueText;
	}
	
	/**
	 * @param valueText The valueText to set.
	 */
	public void setValueText(String valueText) {
		markAsDirty(this.valueText, valueText);
		this.valueText = valueText;
	}
	
	/**
	 * @return Returns true if this Obs is complex.
	 * @since 1.5
	 * <strong>Should</strong> return true if the concept is complex
	 */
	public boolean isComplex() {
		if (getConcept() != null) {
			return getConcept().isComplex();
		}
		
		return false;
	}
	
	/**
	 * Get the value for the ComplexData. This method is used by the ComplexObsHandler. The
	 * valueComplex has two parts separated by a bar '|' character: part A) the title; and part B)
	 * the URI. The title is the readable description of the valueComplex that is returned by
	 * {@link Obs#getValueAsString(java.util.Locale)}. The URI is the location where the ComplexData is stored.
	 * 
	 * @return readable title and URI for the location of the ComplexData binary object.
	 * @since 1.5
	 */
	public String getValueComplex() {
		return this.valueComplex;
	}
	
	/**
	 * Set the value for the ComplexData. This method is used by the ComplexObsHandler. The
	 * valueComplex has two parts separated by a bar '|' character: part A) the title; and part B)
	 * the URI. The title is the readable description of the valueComplex that is returned by
	 * {@link Obs#getValueAsString(java.util.Locale)}. The URI is the location where the ComplexData is stored.
	 * 
	 * @param valueComplex readable title and URI for the location of the ComplexData binary object.
	 * @since 1.5
	 */
	public void setValueComplex(String valueComplex) {
		markAsDirty(this.valueComplex, valueComplex);
		this.valueComplex = valueComplex;
	}
	
	/**
	 * Set the ComplexData for this Obs. The ComplexData is stored in the file system or elsewhere,
	 * but is not persisted to the database. <br>
	 * <br>
	 * {@link ComplexObsHandler}s that are registered to {@link ConceptComplex}s will persist the
	 * {@link ComplexData#getData()} object to the correct place for the given concept.
	 * 
	 * @param complexData
	 * @since 1.5
	 */
	public void setComplexData(ComplexData complexData) {
		markAsDirty(this.complexData, complexData);
		this.complexData = complexData;
	}
	
	/**
	 * Get the ComplexData. This is retrieved by the {@link ComplexObsHandler} from the file system
	 * or another location, not from the database. <br>
	 * <br>
	 * This will be null unless you call:
	 * 
	 * <pre>
	 * Obs obsWithComplexData =
	 * Context.getObsService().getComplexObs(obsId, OpenmrsConstants.RAW_VIEW);
	 * 
	 * <pre/>
	 *
	 * @return the complex data for this obs (if its a complex obs)
	 * @since 1.5
	 */
	public ComplexData getComplexData() {
		return this.complexData;
	}
	
	/**
	 * @return Returns the accessionNumber.
	 */
	public String getAccessionNumber() {
		return accessionNumber;
	}
	
	/**
	 * @param accessionNumber The accessionNumber to set.
	 */
	public void setAccessionNumber(String accessionNumber) {
		markAsDirty(this.accessionNumber, accessionNumber);
		this.accessionNumber = accessionNumber;
	}
	
	/***************************************************************************
	 * Convenience methods
	 **************************************************************************/
	
	/**
	 * Convenience method for obtaining the observation's value as a string If the Obs is complex,
	 * returns the title of the complexData denoted by the section of getValueComplex() before the
	 * first bar '|' character; or returns the entire getValueComplex() if the bar '|' character is
	 * missing.
	 *
	 * @param locale locale for locale-specific depictions of value
	 * <strong>Should</strong> return first part of valueComplex for complex obs
	 * <strong>Should</strong> return first part of valueComplex for non null valueComplexes
	 * <strong>Should</strong> return non precise values for NumericConcepts
	 * <strong>Should</strong> return date in correct format
	 * <strong>Should</strong> not return long decimal numbers as scientific notation
	 * <strong>Should</strong> use commas or decimal places depending on locale
	 * <strong>Should</strong> not use thousand separator
	 * <strong>Should</strong> return regular number for size of zero to or greater than ten digits
	 * <strong>Should</strong> return regular number if decimal places are as high as six
	 */
	public String getValueAsString(Locale locale) {
		// formatting for the return of numbers of type double
		NumberFormat nf = NumberFormat.getNumberInstance(locale);
		DecimalFormat df = (DecimalFormat) nf;
		// formatting style up to 6 digits
		df.applyPattern("#0.0#####");
		//branch on hl7 abbreviations
		if (getConcept() != null) {
			String abbrev = getConcept().getDatatype().getHl7Abbreviation();
			if ("BIT".equals(abbrev)) {
				return getValueAsBoolean() == null ? "" : getValueAsBoolean().toString();
			} else if ("CWE".equals(abbrev)) {
				if (getValueCoded() == null) {
					return "";
				}
				if (getValueDrug() != null) {
					return getValueDrug().getFullName(locale);
				} else {
					ConceptName codedName = getValueCodedName();
					if (codedName != null) {
						return getValueCoded().getName(locale, false).getName();
					} else {
						ConceptName fallbackName = getValueCoded().getName();
						if (fallbackName != null) {
							return fallbackName.getName();
						} else {
							return "";
						}
						
					}
				}
			} else if ("NM".equals(abbrev) || "SN".equals(abbrev)) {
				if (getValueNumeric() == null) {
					return "";
				} else {
					if (getConcept() instanceof ConceptNumeric) {
						ConceptNumeric cn = (ConceptNumeric) getConcept();
						if (!cn.getAllowDecimal()) {
							double d = getValueNumeric();
							int i = (int) d;
							return Integer.toString(i);
						} else {
							df.format(getValueNumeric());
						}
					}
				}
			} else if ("DT".equals(abbrev)) {
				DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
				return (getValueDatetime() == null ? "" : dateFormat.format(getValueDatetime()));
			} else if ("TM".equals(abbrev)) {
				return (getValueDatetime() == null ? "" : Format.format(getValueDatetime(), locale, FORMAT_TYPE.TIME));
			} else if ("TS".equals(abbrev)) {
				return (getValueDatetime() == null ? "" : Format.format(getValueDatetime(), locale, FORMAT_TYPE.TIMESTAMP));
			} else if ("ST".equals(abbrev)) {
				return getValueText();
			} else if ("ED".equals(abbrev) && getValueComplex() != null) {
				String[] valuesComplex = getValueComplex().split("\\|");
				for (String value : valuesComplex) {
					if (StringUtils.isNotEmpty(value)) {
						return value.trim();
					}
				}
			}
		}
		
		// if the datatype is 'unknown', default to just returning what is not null
		if (getValueNumeric() != null) {
			return df.format(getValueNumeric());
		} else if (getValueCoded() != null) {
			if (getValueDrug() != null) {
				return getValueDrug().getFullName(locale);
			} else {
				ConceptName valudeCodedName = getValueCodedName();
				if (valudeCodedName != null) {
					return valudeCodedName.getName();
				} else {
					return "";
				}
			}
		} else if (getValueDatetime() != null) {
			return Format.format(getValueDatetime(), locale, FORMAT_TYPE.DATE);
		} else if (getValueText() != null) {
			return getValueText();
		} else if (hasGroupMembers()) {
			// all of the values are null and we're an obs group...so loop
			// over the members and just do a getValueAsString on those
			// this could potentially cause an infinite loop if an obs group
			// is a member of its own group at some point in the hierarchy
			StringBuilder sb = new StringBuilder();
			for (Obs groupMember : getGroupMembers()) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(groupMember.getValueAsString(locale));
			}
			return sb.toString();
		}
		
		// returns the title portion of the valueComplex
		// which is everything before the first bar '|' character.
		if (getValueComplex() != null) {
			String[] valuesComplex = getValueComplex().split("\\|");
			for (String value : valuesComplex) {
				if (StringUtils.isNotEmpty(value)) {
					return value.trim();
				}
			}
		}
		
		return "";
	}
	
	/**
	 * Sets the value for the obs from a string depending on the datatype of the question concept
	 *
	 * @param s the string to coerce to a boolean
	 * <strong>Should</strong> set value as boolean if the datatype of the question concept is boolean
	 * <strong>Should</strong> fail if the value of the string is null
	 * <strong>Should</strong> fail if the value of the string is empty
	 */
	public void setValueAsString(String s) throws ParseException {
		log.debug("getConcept() == {}", getConcept());
		
		if (getConcept() != null && !StringUtils.isBlank(s)) {
			String abbrev = getConcept().getDatatype().getHl7Abbreviation();
			if ("BIT".equals(abbrev)) {
				setValueBoolean(Boolean.valueOf(s));
			} else if ("CWE".equals(abbrev)) {
				throw new RuntimeException("Not Yet Implemented");
			} else if ("NM".equals(abbrev) || "SN".equals(abbrev)) {
				setValueNumeric(Double.valueOf(s));
			} else if ("DT".equals(abbrev)) {
				DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
				setValueDatetime(dateFormat.parse(s));
			} else if ("TM".equals(abbrev)) {
				DateFormat timeFormat = new SimpleDateFormat(TIME_PATTERN);
				setValueDatetime(timeFormat.parse(s));
			} else if ("TS".equals(abbrev)) {
				DateFormat datetimeFormat = new SimpleDateFormat(DATE_TIME_PATTERN);
				setValueDatetime(datetimeFormat.parse(s));
			} else if ("ST".equals(abbrev)) {
				setValueText(s);
			} else {
				throw new RuntimeException("Don't know how to handle " + abbrev + " for concept: " + getConcept().getName().getName());
			}
			
		} else {
			throw new RuntimeException("concept is null for " + this);
		}
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (obsId == null) {
			return "obs id is null";
		}
		
		return "Obs #" + obsId.toString();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getObsId();
		
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setObsId(id);
		
	}
	
	/**
	 * When ObsService updates an obs, it voids the old version, creates a new Obs with the updates,
	 * and adds a reference to the previousVersion in the new Obs. getPreviousVersion returns the
	 * last version of this Obs.
	 */
	public Obs getPreviousVersion() {
		return previousVersion;
	}
	
	/**
	 * A previousVersion indicates that this Obs replaces an earlier one.
	 *
	 * @param previousVersion the Obs that this Obs superceeds
	 */
	public void setPreviousVersion(Obs previousVersion) {
		markAsDirty(this.previousVersion, previousVersion);
		this.previousVersion = previousVersion;
	}
	
	public Boolean hasPreviousVersion() {
		return getPreviousVersion() != null;
	}
	
	/**
	 * @param creator
	 * @see Auditable#setCreator(User)
	 */
	@Override
	public void setCreator(User creator) {
		markAsDirty(getCreator(), creator);
		super.setCreator(creator);
	}
	
	/**
	 * @param dateCreated
	 * @see Auditable#setDateCreated(Date)
	 */
	@Override
	public void setDateCreated(Date dateCreated) {
		markAsDirty(getDateCreated(), dateCreated);
		super.setDateCreated(dateCreated);
	}
	
	/**
	 * @see org.openmrs.FormRecordable#setFormField(String,String)
	 */
	@Override
	public void setFormField(String namespace, String formFieldPath) {
		String oldValue = formNamespaceAndPath;
		super.setFormField(namespace, formFieldPath);
		markAsDirty(oldValue, formNamespaceAndPath);
	}
	
	/**
	 * Returns true if any change has been made to an Obs instance. In general, the only time
	 * isDirty() is going to return false is when a new Obs has just been instantiated or loaded
	 * from the database and no method that modifies it internally has been invoked.
	 *
	 * @return true if not changed otherwise false
	 * @since 2.0
	 * <strong>Should</strong> return false when no change has been made
	 * <strong>Should</strong> return true when any immutable field has been changed
	 * <strong>Should</strong> return false when only mutable fields are changed
	 * <strong>Should</strong> return true when an immutable field is changed from a null to a non null value
	 * <strong>Should</strong> return true when an immutable field is changed from a non null to a null value
	 */
	public boolean isDirty() {
		return dirty;
	}
	
	protected void markAsDirty(Object oldValue, Object newValue) {
		//Should we ignore the case for Strings?
		if (!isDirty() && obsId != null && !OpenmrsUtil.nullSafeEquals(oldValue, newValue)) {
			dirty = true;
		}
	}
	
	/**
	 * Similar to FHIR's Observation.interpretation. Supports a subset of FHIR's Observation
	 * Interpretation Codes. See https://www.hl7.org/fhir/valueset-observation-interpretation.html
	 * 
	 * @since 2.1.0
	 */
	public Interpretation getInterpretation() {
		return interpretation;
	}
	
	/**
	 * @since 2.1.0
	 */
	public void setInterpretation(Interpretation interpretation) {
		markAsDirty(this.interpretation, interpretation);
		this.interpretation = interpretation;
	}
	
	/**
	 * Similar to FHIR's Observation.status. Supports a subset of FHIR's ObservationStatus values.
	 * At present OpenMRS does not support FHIR's REGISTERED and CANCELLED statuses, because we
	 * don't support obs with null values. See:
	 * https://www.hl7.org/fhir/valueset-observation-status.html
	 * 
	 * @since 2.1.0
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * @since 2.1.0
	 */
	public void setStatus(Status status) {
		markAsDirty(this.status, status);
		this.status = status;
	}
}

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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.util.Format;
import org.openmrs.util.Format.FORMAT_TYPE;

/**
 * Observation object. An observation is a single unit of information
 * Observations are collected and grouped together into one Encounter (one visit).
 * 
 * Obs can be grouped in a hierarchical fashion. The {@link #getObsGroup()}
 * method returns an optional parent. That parent object is also an Obs. The
 * parent Obs object knows about its child objects through the {@link #getGroupMembers()}
 * method. (Multi-level hierarchies are achieved by an Obs parent object being
 * a member of another Obs (grand)parent object)
 * 
 * Read up on the obs table: http://openmrs.org/wiki/Obs_Table_Primer
 * 
 * @see Encounter
 */
public class Obs implements java.io.Serializable {

	protected final static Log log = LogFactory.getLog(Obs.class);
	public static final long serialVersionUID = 112342333L;

	protected Integer obsId;
	protected Concept concept;
	protected Date obsDatetime;
	protected String accessionNumber;

	/**
	 * The "parent" of this obs. It is the grouping that brings other obs
	 * together. note: obsGroup.getConcept().isSet() should be true
	 * 
	 * This will be non-null if this obs is a member of another groupedObs
	 * 
	 * @see #isGroupMember()
	 */
	protected Obs obsGroup;

	/**
	 * The list of obs grouped under this obs.
	 */
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
	protected Integer personId;
	protected Person person;
	protected Order order;
	protected Location location;
	protected Encounter encounter;
	protected Date dateStarted;
	protected Date dateStopped;
	protected User creator;
	protected Date dateCreated;
	protected Boolean voided = false;
	protected User voidedBy;
	protected Date dateVoided;
	protected String voidReason;

	/** default constructor */
	public Obs() {
	}

	/**
	 * Required parameters constructor
	 * 
	 * A value is also required, but that can be one of: valueCoded, valueDrug,
	 * valueNumeric, or valueText
	 * 
	 * @param person The Person this obs is acting on
	 * @param question The question concept this obs is related to
	 * @param obsDatetime The time this obs took place
	 * @param location The location this obs took place
	 */
	public Obs(Person person, Concept question, Date obsDatetime, Location location) {
		this.person = person;
		this.personId = person.getPersonId();
		this.concept = question;
		this.obsDatetime = obsDatetime;
		this.location = location;
	}

	/** constructor with id */
	public Obs(Integer obsId) {
		this.obsId = obsId;
	}

	/**
	 * This is an equivalent to a copy constructor.
	 * 
	 * Creates a new copy of the given <code>obsToCopy</code> with a null obs id
	 * 
	 * @param obsToCopy The Obs that is going to be copied
	 * @return a new Obs object with all the same attributes as the given obs
	 */
	public static Obs newInstance(Obs obsToCopy) {
		Obs newObs = new Obs(obsToCopy.getPerson(), obsToCopy.getConcept(),
		                     obsToCopy.getObsDatetime(), obsToCopy.getLocation());

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
		newObs.setOrder(obsToCopy.getOrder());
		newObs.setEncounter(obsToCopy.getEncounter());
		newObs.setDateStarted(obsToCopy.getDateStarted());
		newObs.setDateStopped(obsToCopy.getDateStopped());
		newObs.setCreator(obsToCopy.getCreator());
		newObs.setDateCreated(obsToCopy.getDateCreated());
		newObs.setVoided(obsToCopy.getVoided());
		newObs.setVoidedBy(obsToCopy.getVoidedBy());
		newObs.setDateVoided(obsToCopy.getDateVoided());
		newObs.setVoidReason(obsToCopy.getVoidReason());
		
		newObs.setValueComplex(obsToCopy.getValueComplex());
		newObs.setComplexData(obsToCopy.getComplexData());

		if (obsToCopy.getGroupMembers() != null)
			for (Obs member : obsToCopy.getGroupMembers()) {
				// if the obs hasn't been saved yet, no need to duplicate it
				if (member.getObsId() == null)
					newObs.addGroupMember(member);
				else
					newObs.addGroupMember(Obs.newInstance(member));
			}

		return newObs;
	}

	/**
	 * Compares two Obs for similarity. The comparison is done on obsId of both
	 * this and the given <code>obs</code> object. If either has a null obsId,
	 * then they are not equal
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Obs) {
			Obs o = (Obs) obj;
			if (this.getObsId() != null && o.getObsId() != null)
				return (this.getObsId().equals(o.getObsId()));
			/*
			 * return (this.getConcept().equals(o.getConcept()) &&
			 * this.getPatient().equals(o.getPatient()) &&
			 * this.getEncounter().equals(o.getEncounter()) &&
			 * this.getLocation().equals(o.getLocation()));
			 */
		}

		// if the obsIds don't match, its possible that they are the same
		// exact object. Check that now on the way out.
		return this == obj;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getObsId() == null)
			return super.hashCode();
		return this.getObsId().hashCode();
	}

	/**
	 * Sets the required Obs properties: creator and dateCreated
	 * 
	 * @param creator
	 * @param dateCreated
	 */
	public void setRequiredProperties(User creator, Date dateCreated) {
		if (this.getCreator() == null)
			setCreator(creator);

		if (this.getDateCreated() == null)
			setDateCreated(dateCreated);

		if (getGroupMembers() != null) {
			for (Obs member : getGroupMembers()) {
				// if statement does a quick sanity check to
				// avoid the simplest of infinite loops
				if (member.getCreator() == null
				        || member.getDateCreated() == null)
					member.setRequiredProperties(creator, dateCreated);
			}
		}
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
		this.concept = concept;
	}
	
	/**
	 * Get the concept description that is tied to the concept
	 * name that was used when making this observation
	 * 
	 * @return ConceptDescription the description used
	 */
	public ConceptDescription getConceptDescription() {
		// if we don't have a question for this concept,
		// then don't bother looking for a description
		if (getConcept() == null)
			return null;

		// ABKTOD: description in which locale?
		return concept.getDescription();
	}
	
	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the dateVoided.
	 */
	public Date getDateVoided() {
		return dateVoided;
	}

	/**
	 * @param dateVoided The dateVoided to set.
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
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
		this.obsDatetime = obsDatetime;
	}

	/**
	 * @return Returns the obsId of the parent obs group
	 * @deprecated The {@link #getObsGroup()} method should be used
	 * @see #getObsGroup()
	 */
	public Integer getObsGroupId() {
		if (getObsGroup() == null)
			return null;

		return obsGroup.getObsId();
	}

	/**
	 * @param obsGroupId The obsGroupId to set.
	 * @deprecated This method should not be used. The #setObsGroup() method
	 *             should be used instead
	 * @see #setObsGroup(Obs)
	 */
	public void setObsGroupId(Integer obsGroupId) {
		throw new APIException("I don't know what to do here because I don't"
		        + "know what the parent is of the group I'm "
		        + "being put into. This method is deprecated "
		        + "and should not be used.");
	}

	/**
	 * An obs grouping occurs when the question (#getConcept()) is a set. (@link
	 * org.openmrs.Concept#isSet())
	 * 
	 * If this is non-null, it means the current Obs is in the list returned by
	 * <code>obsGroup</code>.{@link #getGroupMembers()}
	 * 
	 * @return the Obs that is the grouping factor
	 */
	public Obs getObsGroup() {
		return obsGroup;
	}

	/**
	 * This method does NOT add this current obs to the list of obs in
	 * obsGroup.getGroupMembers(). That must be done (and should be done)
	 * manually. (I am not doing it here for fear of screwing up the normal
	 * loading and creation of this object via hibernate/spring)
	 * 
	 * @param obsGroup the obsGroup to set
	 */
	public void setObsGroup(Obs obsGroup) {
		this.obsGroup = obsGroup;
	}

	/**
	 * Convenience method that checks for nullity and length of the (@link
	 * #getGroupMembers()) method
	 * 
	 * NOTE: This method could also be called "isObsGroup" for a little less
	 * confusion on names. However, jstl in a web layer (or any psuedo-getter)
	 * access isn't good with both an "isObsGroup" method and a "getObsGroup"
	 * method. Which one should be returned with a simplified jstl call like
	 * ${obs.obsGroup} ? With this setup, ${obs.obsGrouping} returns a boolean
	 * of whether this obs is a parent and has members. ${obs.obsGroup} returns
	 * the parent object to this obs if this obs is a group member of some other
	 * group.
	 * 
	 * @return true if this is the parent group of other obs
	 */
	public boolean isObsGrouping() {
		return hasGroupMembers();
	}

	/**
	 * Convenience method that checks for nullity and length of the (@link
	 * #getGroupMembers()) method
	 * 
	 * @return true if this is the parent group of other obs
	 */
	public boolean hasGroupMembers() {
		return getGroupMembers() != null && getGroupMembers().size() > 0;
	}

	/**
	 * This should only be true if this obs is a grouping obs.
	 * {@link #getConcept()}.{@link org.openmrs.Concept#isSet()} should be
	 * true for this to be non-null.
	 * 
	 * @return the Obs that are members of this group.
	 * @see #addGroupMember(Obs)
	 * @see #hasGroupMembers()
	 */
	public Set<Obs> getGroupMembers() {
		return groupMembers;
	}

	/**
	 * This should only be true if this obs is a grouping obs.
	 * {@link #getConcept()}.{@link org.openmrs.Concept#isSet()} should be
	 * true for this to be non-null.
	 * 
	 * @param groupMembers the groupedObs to set
	 * @see #addGroupMember(Obs)
	 * @see #hasGroupMembers()
	 */
	public void setGroupMembers(Set<Obs> groupMembers) {
		this.groupMembers = groupMembers;
	}

	/**
	 * Convenience method to add the given <code>obs</code> to this grouping.
	 * Will implicitly make this obs an ObsGroup
	 * 
	 * @param member Obs to add to this group
	 * @see #setGroupMembers(Set)
	 * @see #getGroupMembers()
	 */
	public void addGroupMember(Obs member) {
		if (member == null)
			return;

		if (getGroupMembers() == null)
			groupMembers = new HashSet<Obs>();

		// a quick sanity check to make sure someone isn't adding
		// itself to the group
		if (member.equals(this))
			throw new APIException("An obsGroup cannot have itself as a mentor. obsGroup: "
			        + this + " obsMember attempting to add: " + member);

		member.setObsGroup(this);
		groupMembers.add(member);
	}

	/**
	 * Convenience method to remove an Obs from this grouping This also removes
	 * the link in the given <code>obs</code>object to this obs grouper
	 * 
	 * @param member Obs to remove from this group
	 * @see #setGroupMembers(Set)
	 * @see #getGroupMembers()
	 */
	public void removeGroupMember(Obs member) {
		if (member == null || getGroupMembers() == null)
			return;

		if (groupMembers.remove(member))
			member.setObsGroup(null);
	}

	/**
	 * Convenience method that returns related Obs
	 * 
	 * If the Obs argument is not an ObsGroup: a Set<Obs> will be returned
	 * containing all of the children of this Obs' parent that are not ObsGroups
	 * themselves. This will include this Obs by default, unless getObsGroup()
	 * returns null, in which case an empty set is returned.
	 * 
	 * If the Obs argument is an ObsGroup: a Set<Obs> will be returned
	 * containing 1. all of this Obs' group members, and 2. all ancestor Obs
	 * that are not themselves obsGroups.
	 * 
	 * @return Set<Obs>
	 */
	public Set<Obs> getRelatedObservations() {
		Set<Obs> ret = new HashSet<Obs>();
		if (this.isObsGrouping()) {
			ret.addAll(this.getGroupMembers());
			Obs parentObs = this;
			while (parentObs.getObsGroup() != null) {
				for (Obs obsSibling : parentObs.getObsGroup().getGroupMembers()) {
					if (!obsSibling.isObsGrouping())
						ret.add(obsSibling);
				}
				parentObs = parentObs.getObsGroup();
			}
		} else if (this.getObsGroup() != null) {
			for (Obs obsSibling : this.getObsGroup().getGroupMembers()) {
				if (!obsSibling.isObsGrouping())
					ret.add(obsSibling);
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
		this.order = order;
	}

	/**
	 * @deprecated use getPerson()
	 * @return Returns the patient.
	 */
	public Patient getPatient() {
		return (Patient) getPerson();
	}

	/**
	 * To associate a patient with an obs, use
	 * <code>setPerson(org.openmrs.Person)</code>
	 * 
	 * @deprecated use setPerson(org.openmrs.Person)
	 * @param patient
	 */
	public void setPatient(Patient patient) {
		setPerson(patient);
	}

	/**
	 * The person id of the person on this object.  This should be the same
	 * as <code>{@link #getPerson()}.getPersonId()</code>.  It is duplicated
	 * here for speed and simplicity reasons
	 * 
	 * @return the integer person id of the person this obs is acting on
	 */
	public Integer getPersonId() {
		return personId;
	}

	/**
	 * Set the person id on this obs object.  This method 
	 * is here for convenience, but really the {@link #setPerson(Person)}
	 * method should be used like <code>setPerson(new Person(personId))</code>
	 * 
	 * @see #setPerson(Person)
	 * 
	 * 
	 * @param personId
	 */
	protected void setPersonId(Integer personId) {
		this.personId = personId;
	}

	/**
	 * Get the person object that this obs is acting on.
	 * 
	 * @see #getPersonId()
	 * 
	 * @return the person object
	 */
	public Person getPerson() {
		return person;
	}

	/**
	 * Set the person object to this obs object.  This will 
	 * also set the personId on this obs object
	 * 
	 * @see #setPersonId(Integer)
	 * 
	 * @param person the Patient/Person object that this obs is acting on 
	 */
	public void setPerson(Person person) {
		this.person = person;
		if (person != null)
			this.personId = person.getPersonId();
	}

	/**
	 * This converts the value_numeric to a value_boolean, essentially
	 * 
	 * @return Boolean of the obs value
	 */
	public Boolean getValueAsBoolean() {
		return (getValueNumeric() == null ? null : getValueNumeric() != 0);
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
	 * @param valueCodedName the name of the coded value
	 */
	public void setValueCodedName(ConceptName valueCodedName) {
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
		this.valueDatetime = valueDatetime;
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
		this.valueText = valueText;
	}

	/**
	 * Returns true if this Obs is complex.
	 * 
	 * @return
	 * 
	 * @should return true if the concept is complex
	 */
	public boolean isComplex() {
//		if (getValueComplex() != null) {
//			return true;
//		}
		
		if (getConcept() != null) {
			return getConcept().isComplex();
		}
		
		return false;
	}

	/**
	 * Get the value for the ComplexData. This method is used by the
	 * ComplexObsHandler. The valueComplex has two parts separated by a bar '|'
	 * character: part A) the title; and part B) the URI. The title is the
	 * readable description of the valueComplex that is returned by
	 * Obs.getValueAsString(). The URI is the location where the ComplexData is
	 * stored.
	 * 
	 * @return readable title and URI for the location of the ComplexData binary object.
	 */
	public String getValueComplex() {
		return this.valueComplex;
	}

	/**
	 * Set the value for the ComplexData. This method is used by the
	 * ComplexObsHandler. The valueComplex has two parts separated by a bar '|'
	 * character: part A) the title; and part B) the URI. The title is the
	 * readable description of the valueComplex that is returned by
	 * Obs.getValueAsString(). The URI is the location where the ComplexData is
	 * stored.
	 * 
	 * @param valueComplex readable title and URI for the location of the ComplexData binary
	 *        object.
	 */
	public void setValueComplex(String valueComplex) {
		this.valueComplex = valueComplex;
	}

	/**
	 * Set the ComplexData for this Obs. The ComplexData is stored in the file
	 * system or elsewhere, but is not persisted to the database.
	 * <br/><br/>
	 * {@link ComplexObsHandler}s that are registered to {@link ConceptComplex}s will persist
	 * the {@link ComplexData#getData()} object to the correct place for the given concept.
	 * 
	 * @param complexData
	 */
	public void setComplexData(ComplexData complexData) {
		this.complexData = complexData;
	}

	/**
	 * Get the ComplexData. This is retrieved by the {@link ComplexObsHandler} from the
	 * file system or another location, not from the database.
	 * <br/><br/>
	 * This will be null unless you call:
	 * <pre>
	 *   Obs obsWithComplexData = Context.getObsService().getComplexObs(obsId, OpenmrsConstants.RAW_VIEW);
	 * </pre>
	 * 
	 * @return the complex data for this obs (if its a complex obs)
	 */
	public ComplexData getComplexData() {
		return this.complexData;
	}

	/**
	 * @return Returns the voided.
	 */
	public Boolean isVoided() {
		return voided;
	}

	/**
	 * @return Returns the voided.
	 * @see #isVoided()
	 */
	public Boolean getVoided() {
		return isVoided();
	}

	/**
	 * @param voided The voided to set.
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	/**
	 * @return Returns the voidedBy.
	 */
	public User getVoidedBy() {
		return voidedBy;
	}

	/**
	 * @param voidedBy The voidedBy to set.
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	/**
	 * @return Returns the voidReason.
	 */
	public String getVoidReason() {
		return voidReason;
	}

	/**
	 * @param voidReason The voidReason to set.
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
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
		this.accessionNumber = accessionNumber;
	}

	/**
	 * @return Returns the dateStarted.
	 */
	public Date getDateStarted() {
		return dateStarted;
	}

	/**
	 * @param dateStarted The dateStarted to set.
	 */
	public void setDateStarted(Date dateStarted) {
		this.dateStarted = dateStarted;
	}

	/**
	 * @return Returns the dateStopped.
	 */
	public Date getDateStopped() {
		return dateStopped;
	}

	/**
	 * @param dateStopped The dateStopped to set.
	 */
	public void setDateStopped(Date dateStopped) {
		this.dateStopped = dateStopped;
	}

	/***************************************************************************
	 * Convenience methods
	 **************************************************************************/

	/**
	 * Convenience method for obtaining the observation's value as a string
	 * 
	 * If the Obs is complex, returns the title of the complexData denoted by
	 * the section of getValueComplex() before the first bar '|' character; or
	 * returns the entire getValueComplex() if the bar '|' character is missing.
	 * 
	 * @param locale locale for locale-specific depictions of value
	 * 
	 * @should return first part of valueComplex for complex obs
	 * @should return first part of valueComplex for non null valueComplexes
	 */
	public String getValueAsString(Locale locale) {
		// branch on hl7 abbreviations
		if (getConcept() != null) {
			String abbrev = getConcept().getDatatype().getHl7Abbreviation();
			if (abbrev.equals("BIT"))
				return getValueAsBoolean() == null ? "" : getValueAsBoolean().toString();
			else if (abbrev.equals("CWE")) {
				if (getValueCoded() == null)
					return "";
				if (getValueDrug() != null)
					return getValueDrug().getFullName(locale);
				else {
					ConceptName valueCodedName = getValueCodedName();
					if (valueCodedName != null) {
						return valueCodedName.getName();
					} else {
						ConceptName fallbackName = getValueCoded().getName();
						if (fallbackName != null) {
							return fallbackName.getName();
						}
						else {
							return "";
						}
							
					}
				}
			}
			else if (abbrev.equals("NM") || abbrev.equals("SN"))
				return getValueNumeric() == null ? "" : getValueNumeric().toString();
			else if (abbrev.equals("DT"))
				return (getValueDatetime() == null ? "" : Format.format(getValueDatetime(), locale, FORMAT_TYPE.DATE));
			else if (abbrev.equals("TM"))
				return (getValueDatetime() == null ? "" : Format.format(getValueDatetime(), locale, FORMAT_TYPE.TIME));
			else if (abbrev.equals("TS"))
				return (getValueDatetime() == null ? "" : Format.format(getValueDatetime(), locale, FORMAT_TYPE.TIMESTAMP));
			else if (abbrev.equals("ST"))
				return getValueText();
			else if (abbrev.equals("ED") && getValueComplex() != null) {
				String[] valueComplex = getValueComplex().split("\\|");
				for (int i = 0; i < valueComplex.length; i++) {
					if (!"".equals(valueComplex[i])) {
						return valueComplex[i].trim();
					}
				}
			}
		}

		// if the datatype is 'unknown', default to just returning what is not null
		if (getValueNumeric() != null)
			return getValueNumeric().toString();
		else if (getValueCoded() != null) {
			if (getValueDrug() != null)
				return getValueDrug().getFullName(locale);
			else {
				ConceptName valudeCodedName = getValueCodedName();
				if (valudeCodedName != null) {
					return valudeCodedName.getName();
				} else {
					return "";
				}
			}
		}
		else if (getValueDatetime() != null)
			return Format.format(getValueDatetime(), locale, FORMAT_TYPE.DATE);
		else if (getValueText() != null)
			return getValueText();
		else if (hasGroupMembers()) {
			// all of the values are null and we're an obs group...so loop
			// over the members and just do a getValueAsString on those
			// this could potentially cause an infinite loop if an obs group
			// is a member of its own group at some point in the hierarchy
			StringBuilder sb = new StringBuilder();
			for (Obs groupMember : getGroupMembers()) {
				if (sb.length() > 0)
					sb.append(", ");
				sb.append(groupMember.getValueAsString(locale));
			}
			return sb.toString();
		}

		// returns the title portion of the valueComplex
		// which is everything before the first bar '|' character.
		if (getValueComplex() != null) {
			String[] valueComplex = getValueComplex().split("\\|");
			for (int i = 0; i < valueComplex.length; i++) {
				if (!"".equals(valueComplex[i])) {
					return valueComplex[i].trim();
				}
			}
		}

		return "";
	}

	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	public void setValueAsString(String s) throws ParseException {
		if (log.isDebugEnabled())
			log.debug("getConcept() == " + getConcept());
		
		if (getConcept() != null) {
			String abbrev = getConcept().getDatatype().getHl7Abbreviation();
			if (abbrev.equals("BIT")) {
				setValueNumeric(Boolean.valueOf(s) ? 1.0 : 0.0);
			} else if (abbrev.equals("CWE")) {
				throw new RuntimeException("Not Yet Implemented");
			} else if (abbrev.equals("NM") || abbrev.equals("SN")) {
				setValueNumeric(Double.valueOf(s));
			} else if (abbrev.equals("DT") || abbrev.equals("TM") || abbrev.equals("TS")) {
				setValueDatetime(df.parse(s));
			} else if (abbrev.equals("ST")) {
				setValueText(s);
			} else {
				throw new RuntimeException("Don't know how to handle " + abbrev);
			}
		} else {
			throw new RuntimeException("concept is null for " + this);
		}
	}

	/**
	 * This was a convenience method for obtaining a Map of available locale 
	 * to observation's value as a string
	 * 
	 * This method is a waste and should be not be used.  This was used in the web
	 * layer because jstl can't pass parameters to a method (${obs.valueAsString[locale]}
	 * was used instead of what would be convenient ${obs.valueAsString(locale)})
	 * Now the openmrs:format tag should be used in the web layer:
	 * <openmrs:format obsValue="${obs}"/> 
	 * 
	 * @deprecated 
	 */
	public Map<Locale, String> getValueAsString() {
		Map<Locale, String> localeMap = new HashMap<Locale, String>();
		Locale[] locales = Locale.getAvailableLocales(); // ABKTODO: get actual available locales
		for (int i=0; i<locales.length; i++) {
			localeMap.put(locales[i], getValueAsString(locales[i]));
		}
		return localeMap;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (obsId == null)
			return "obs id is null";
		
		return "Obs #" + obsId.toString();
	}

}

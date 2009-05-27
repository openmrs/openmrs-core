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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import org.openmrs.api.context.Context;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * ConceptName is the real world term used to express a Concept within the idiom of a particular
 * locale.
 */
@Root
public class ConceptName extends BaseOpenmrsObject implements Auditable, java.io.Serializable {
	
	public static final long serialVersionUID = 33226787L;
	
	// Fields
	private Integer conceptNameId;
	
	private Concept concept;
	
	private String name;
	
	private Locale locale; // ABK: upgraded from a plain string to a full locale object
	
	private User creator;
	
	private Date dateCreated;
	
	private Boolean voided = false;
	
	private User voidedBy;
	
	private Date dateVoided;
	
	private String voidReason;
	
	private Collection<ConceptNameTag> tags;
	
	// Constructors
	
	/** default constructor */
	public ConceptName() {
	}
	
	/**
	 * Convenience constructor to create a ConceptName object by primary key
	 * 
	 * @param conceptNameId
	 */
	public ConceptName(Integer conceptNameId) {
		this.conceptNameId = conceptNameId;
	}
	
	public ConceptName(String name, Locale locale) {
		setName(name);
		setLocale(locale);
	}
	
	/**
	 * Short name and description are no longer attributes of ConceptName.
	 * 
	 * @param name
	 * @param shortName
	 * @param description
	 * @param locale
	 * @deprecated
	 */
	public ConceptName(String name, String shortName, String description, Locale locale) {
		setName(name);
		setLocale(locale);
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @should compare on conceptNameId if non null
	 * @should not return true with different objects and null ids
	 * @should default to object equality
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ConceptName)) {
			return false;
		}
		ConceptName rhs = (ConceptName) obj;
		if (this.conceptNameId != null && rhs.conceptNameId != null)
			return (this.conceptNameId == rhs.conceptNameId);
		else
			return this == obj;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getConcept() == null || this.getName() == null || this.getLocale() == null)
			return super.hashCode();
		int hash = 3;
		hash = hash + 31 * this.getConcept().hashCode();
		hash = hash + 31 * this.getName().hashCode();
		hash = hash + 31 * this.getLocale().hashCode();
		return hash;
	}
	
	/**
	 * Call {@link Concept#getShortestName(Locale, Boolean)} instead.
	 * 
	 * @deprecated
	 * @return Returns the appropriate short name
	 */
	public String getShortestName() {
		if (concept != null) {
			ConceptName bestShortName = concept.getBestShortName(this.locale);
			if (bestShortName != null)
				return bestShortName.getName();
		}
		
		return getName();
	}
	
	/**
	 * @return Returns the conceptId.
	 */
	@Attribute
	public Integer getConceptNameId() {
		return conceptNameId;
	}
	
	/**
	 * @param conceptNameId The conceptId to set.
	 */
	@Attribute
	public void setConceptNameId(Integer conceptNameId) {
		this.conceptNameId = conceptNameId;
	}
	
	/**
	 * 
	 */
	@Element
	public Concept getConcept() {
		return concept;
	}
	
	@Element
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
	/**
	 * 
	 */
	@Element(data = true)
	public String getName() {
		return name;
	}
	
	@Element(data = true)
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 */
	@Element
	public Locale getLocale() {
		return locale;
	}
	
	@Element
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	/**
	 * @deprecated
	 * @return Returns the shortName.
	 */
	public String getShortName() {
		if (concept != null) {
			ConceptName bestShortName = concept.getBestShortName(Context.getLocale());
			if (bestShortName != null)
				return bestShortName.getName();
		}
		
		return null;
	}
	
	/**
	 * @deprecated
	 * @return Returns the description.
	 */
	public String getDescription() {
		if (concept != null) {
			ConceptDescription description = concept.getDescription();
			if (description != null)
				return description.getDescription();
		}
		
		return null;
	}
	
	/**
	 * @return Returns the creator.
	 */
	@Element
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @param creator The creator to set.
	 */
	@Element
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @return Returns the dateCreated.
	 */
	@Element
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @param dateCreated The dateCreated to set.
	 */
	@Element
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * Returns whether the ConceptName has been voided.
	 * 
	 * @return true if the ConceptName has been voided, false otherwise.
	 */
	public Boolean isVoided() {
		return voided;
	}
	
	/**
	 * Returns whether the ConceptName has been voided.
	 * 
	 * @return true if the ConceptName has been voided, false otherwise.
	 */
	@Attribute
	public Boolean getVoided() {
		return isVoided();
	}
	
	/**
	 * Sets the voided status of this ConceptName.
	 * 
	 * @param voided the voided status to set.
	 */
	@Attribute
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}
	
	/**
	 * Returns the User who voided this ConceptName.
	 * 
	 * @return the User who voided this ConceptName, or null if not set
	 */
	@Element(required = false)
	public User getVoidedBy() {
		return voidedBy;
	}
	
	/**
	 * Sets the User who voided this ConceptName.
	 * 
	 * @param voidedBy the user who voided this ConceptName.
	 */
	@Element(required = false)
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}
	
	/**
	 * Returns the Date this ConceptName was voided.
	 * 
	 * @return the Date this ConceptName was voided.
	 */
	@Element(required = false)
	public Date getDateVoided() {
		return dateVoided;
	}
	
	/**
	 * Sets the Data this ConceptName was voided.
	 * 
	 * @param dateVoided the date the ConceptName was voided.
	 */
	@Element(required = false)
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}
	
	/**
	 * Returns the reason this ConceptName was voided.
	 * 
	 * @return the reason this ConceptName was voided
	 */
	@Element(required = false)
	public String getVoidReason() {
		return voidReason;
	}
	
	/**
	 * Sets the reason this ConceptName was voided.
	 * 
	 * @param voidReason the reason this ConceptName was voided
	 */
	@Element(required = false)
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
	
	/**
	 * Returns the tags which have been attached to this ConceptName.
	 * 
	 * @return the tags.
	 */
	@ElementList
	public Collection<ConceptNameTag> getTags() {
		return tags;
	}
	
	/**
	 * Set the tags which are attached to this ConceptName.
	 * 
	 * @param tags the tags to set.
	 */
	@ElementList
	public void setTags(Collection<ConceptNameTag> tags) {
		this.tags = tags;
	}
	
	/**
	 * Adds a tag to the concept name. If the tag is new (has no existing occurrences) a new
	 * ConceptNameTag will be created with a blank description.
	 * 
	 * @param tag human-readable text string for the tag
	 */
	public void addTag(String tag) {
		addTag(tag, "");
	}
	
	/**
	 * Adds a tag to the concept name. If the tag is new (has no existing occurrences) a new
	 * ConceptNameTag will be created with the given description.
	 * 
	 * @param tag human-readable text string for the tag
	 * @param description description of the tag's purpose
	 */
	public void addTag(String tag, String description) {
		ConceptNameTag nameTag = new ConceptNameTag(tag, description);
		addTag(nameTag);
	}
	
	/**
	 * Attaches a tag to the concept name.
	 * 
	 * @param tag the tag to add
	 */
	public void addTag(ConceptNameTag tag) {
		if (tags == null)
			tags = new HashSet<ConceptNameTag>();
		
		if (!tags.contains(tag))
			tags.add(tag);
	}
	
	/**
	 * Removes a tag from the concept name.
	 * 
	 * @param tag the tag to remove
	 */
	public void removeTag(ConceptNameTag tag) {
		if (tags.contains(tag))
			tags.remove(tag);
	}
	
	/**
	 * Checks whether the name has a particular tag.
	 * 
	 * @param tagToFind the tag for which to check
	 * @return true if the tags include the specified tag, false otherwise
	 */
	public Boolean hasTag(ConceptNameTag tagToFind) {
		return hasTag(tagToFind.getTag());
	}
	
	/**
	 * Checks whether the name has a particular tag.
	 * 
	 * @param tagToFind the string of the tag for which to check
	 * @return true if the tags include the specified tag, false otherwise
	 */
	public Boolean hasTag(String tagToFind) {
		boolean foundTag = false;
		if (tags != null) {
			for (ConceptNameTag nameTag : getTags()) {
				if (nameTag.getTag().equals(tagToFind)) {
					foundTag = true;
					break;
				}
			}
		}
		return foundTag;
	}
	
	/**
	 * Checks whether the name is explicitly preferred in a particular language.
	 * 
	 * @param language ISO 639 2-letter code for a language
	 * @return true if the name is preferred in the given language, false otherwise
	 */
	public Boolean isPreferredInLanguage(String language) {
		return hasTag(ConceptNameTag.preferredLanguageTagFor(language));
	}
	
	/**
	 * Checks whether the name is explicitly preferred in a particular country.
	 * 
	 * @param country ISO 3166 2-letter code for a country
	 * @return true if the name is preferred in the given country, false otherwise
	 */
	public Boolean isPreferredInCountry(String country) {
		return hasTag(ConceptNameTag.preferredCountryTagFor(country));
	}
	
	/**
	 * Checks whether the name is the preferred name explicitly preferred
	 * 
	 * @return true if the name is tagged as 'preferred'
	 * @should return true if this tag has a preferred tag
	 * @should return false if this tag doesnt have the preferred tag
	 */
	public Boolean isPreferred() {
		return hasTag(ConceptNameTag.PREFERRED);
	}
	
	/**
	 * Convenience method for determining whether this is a short name.
	 * 
	 * @return true if the tags include "short", false otherwise
	 */
	public Boolean isShort() {
		return hasTag(ConceptNameTag.SHORT);
	}
	
	/**
	 * Checks whether the name is the preferred short name in a particular language.
	 * 
	 * @param language ISO 639 2-letter code for a language
	 * @return true if the name is the preferred short in the given language, false otherwise
	 */
	public Boolean isPreferredShortInLanguage(String language) {
		return hasTag(ConceptNameTag.preferredLanguageTagFor(language));
	}
	
	/**
	 * Checks whether the name is the preferred short name in a particular country.
	 * 
	 * @param country ISO 3166 2-letter code for a country
	 * @return true if the name is the preferred short in the given country, false otherwise
	 */
	public Boolean isPreferredShortInCountry(String country) {
		return hasTag(ConceptNameTag.shortCountryTagFor(country));
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.name;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getConceptNameId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setConceptNameId(id);
	}
	
	/**
	 * Not currently used. Always returns null.
	 * 
	 * @see org.openmrs.Auditable#getChangedBy()
	 */
	public User getChangedBy() {
		return null;
	}
	
	/**
	 * Not currently used. Always returns null.
	 * 
	 * @see org.openmrs.Auditable#getDateChanged()
	 */
	public Date getDateChanged() {
		return null;
	}
	
	/**
	 * Not currently used.
	 * 
	 * @see org.openmrs.Auditable#setChangedBy(org.openmrs.User)
	 */
	public void setChangedBy(User changedBy) {
	}
	
	/**
	 * Not currently used.
	 * 
	 * @see org.openmrs.Auditable#setDateChanged(java.util.Date)
	 */
	public void setDateChanged(Date dateChanged) {
	}
}

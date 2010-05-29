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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.util.LocaleUtility;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * A Concept object can represent either a question or an answer to a data point. That data point is
 * usually an {@link Obs}. <br/>
 * <br/>
 * A Concept can have multiple names and multiple descriptions within one locale and across multiple
 * locales.<br/>
 * <br/>
 * To save a Concept to the database, first build up the Concept object in java, then pass that
 * object to the {@link ConceptService}.<br/>
 * <br/>
 * To get a Concept that is stored in the database, call a method in the {@link ConceptService} to
 * fetch an object. To get child objects off of that Concept, further calls to the
 * {@link ConceptService} or the database are not needed. e.g. To get the list of answers that are
 * stored to a concept, get the concept, then call {@link Concept#getAnswers()}
 * 
 * @see ConceptName
 * @see ConceptNameTag
 * @see ConceptDescription
 * @see ConceptAnswer
 * @see ConceptSet
 * @see ConceptMap
 * @see ConceptService
 */
@Root
public class Concept extends BaseOpenmrsObject implements Auditable, Retireable, java.io.Serializable, Attributable<Concept> {
	
	public static final long serialVersionUID = 57332L;
	
	private static final Log log = LogFactory.getLog(Concept.class);
	
	// Fields
	
	private Integer conceptId;
	
	private Boolean retired = false;
	
	private User retiredBy;
	
	private Date dateRetired;
	
	private String retireReason;
	
	private ConceptDatatype datatype;
	
	private ConceptClass conceptClass;
	
	private Boolean set = false;
	
	private String version;
	
	private User creator;
	
	private Date dateCreated;
	
	private User changedBy;
	
	private Date dateChanged;
	
	private Collection<ConceptName> names;
	
	private Collection<ConceptAnswer> answers;
	
	private Collection<ConceptSet> conceptSets;
	
	private Collection<ConceptDescription> descriptions;
	
	private Collection<ConceptMap> conceptMappings;
	
	/**
	 * A cache of locales to names which have compatible locales. Built on-the-fly by
	 * getCompatibleNames().
	 */
	private Map<Locale, List<ConceptName>> compatibleCache;
	
	/** default constructor */
	public Concept() {
		names = new HashSet<ConceptName>();
		answers = new HashSet<ConceptAnswer>();
		conceptSets = new TreeSet<ConceptSet>();
		descriptions = new HashSet<ConceptDescription>();
		conceptMappings = new HashSet<ConceptMap>();
	}
	
	/**
	 * Convenience constructor with conceptid to save to {@link #setConceptId(Integer)}. This
	 * effectively creates a concept stub that can be used to make other calls. Because the
	 * {@link #equals(Object)} and {@link #hashCode()} methods rely on conceptId, this allows a stub
	 * to masquerade as a full concept as long as other objects like {@link #getAnswers()} and
	 * {@link #getNames()} are not needed/called.
	 * 
	 * @param conceptId the concept id to set
	 */
	public Concept(Integer conceptId) {
		this.conceptId = conceptId;
	}
	
	/**
	 * Possibly used for decapitating a ConceptNumeric (to remove the row in
	 * 
	 * @param cn
	 * @deprecated
	 */
	@Deprecated
    public Concept(ConceptNumeric cn) {
		conceptId = cn.getConceptId();
		retired = cn.isRetired();
		datatype = cn.getDatatype();
		conceptClass = cn.getConceptClass();
		version = cn.getVersion();
		creator = cn.getCreator();
		dateCreated = cn.getDateCreated();
		changedBy = cn.getChangedBy();
		dateChanged = cn.getDateChanged();
		names = cn.getNames();
		descriptions = cn.getDescriptions();
		answers = cn.getAnswers(true);
		conceptSets = cn.getConceptSets();
		conceptMappings = cn.getConceptMappings();
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @should not fail if given obj has null conceptid
	 * @should not fail if given obj is null
	 * @should not fail if concept id is null
	 * @should confirm two new concept objects are equal
	 */
	@Override
    public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj instanceof Concept) {
			Concept c = (Concept) obj;
			if (getConceptId() == null && c.getConceptId() == null)
				return this == obj;
			if (getConceptId() != null)
				return (this.getConceptId().equals(c.getConceptId()));
		}
		return this == obj;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 * @should not fail if concept id is null
	 */
	@Override
    public int hashCode() {
		if (this.getConceptId() == null)
			return super.hashCode();
		int hash = 8;
		hash = 31 * this.getConceptId() + hash;
		return hash;
	}
	
	/**
	 * @return Returns the non-retired answers.
	 * @should not return retired answers
	 * @should not return null if no answers defined
	 */
	@ElementList
	public Collection<ConceptAnswer> getAnswers() {
		Collection<ConceptAnswer> newAnswers = new HashSet<ConceptAnswer>();
		if (answers != null) {
			for (ConceptAnswer ca : answers) {
				if (!ca.getAnswerConcept().isRetired())
					newAnswers.add(ca);
			}
		}
		return newAnswers;
	}
	
	/**
	 * TODO describe use cases
	 * 
	 * @param locale
	 * @return the answers for this concept sorted according to ConceptAnswerComparator
	 */
	@Deprecated
	public Collection<ConceptAnswer> getSortedAnswers(Locale locale) {
		Vector<ConceptAnswer> sortedAnswers = new Vector<ConceptAnswer>(getAnswers());
		Collections.sort(sortedAnswers);
		return sortedAnswers;
	}
	
	/**
	 * If <code>includeRetired</code> is true, then the returned object is the actual stored list of
	 * {@link ConceptAnswer}s (which may be null.)
	 * 
	 * @param includeRetired true/false whether to also include the retired answers
	 * @return Returns the answers for this Concept
	 * @should return actual answers object if given includeRetired is true
	 */
	public Collection<ConceptAnswer> getAnswers(boolean includeRetired) {
		if (!includeRetired)
			return getAnswers();
		return answers;
	}
	
	/**
	 * Set this Concept as having the given <code>answers</code>; This method assumes that the
	 * sort_weight has already been set.
	 * 
	 * @param answers The answers to set.
	 */
	@ElementList
	public void setAnswers(Collection<ConceptAnswer> answers) {
		this.answers = answers;
	}
	
	/**
	 * Add the given ConceptAnswer to the list of answers for this Concept
	 * 
	 * @param conceptAnswer
	 * @should add the ConceptAnswer to Concept
	 * @should not fail if answers list is null
	 * @should not fail if answers contains ConceptAnswer already
	 * @should set the sort weight to the max plus one if not provided
	 */
	public void addAnswer(ConceptAnswer conceptAnswer) {
		if (conceptAnswer != null) {
			if (getAnswers(true) == null) {
				answers = new HashSet<ConceptAnswer>();
				conceptAnswer.setConcept(this);
				answers.add(conceptAnswer);
			} else if (!answers.contains(conceptAnswer)) {
				conceptAnswer.setConcept(this);
				answers.add(conceptAnswer);
			}
			
			if ((conceptAnswer.getSortWeight() == null) || (conceptAnswer.getSortWeight() <= 0)) {
				//find largest sort weight
				ConceptAnswer a = Collections.max(answers);
				Double sortWeight = (a == null) ? 1d : ((a.getSortWeight() == null) ? 1d : a.getSortWeight() + 1d);//a.sortWeight can be NULL
				conceptAnswer.setSortWeight(sortWeight);
			}
		}
	}
	
	/**
	 * Remove the given answer from the list of answers for this Concept
	 * 
	 * @param conceptAnswer answer to remove
	 * @return true if the entity was removed, false otherwise
	 * @should not fail if answers is empty
	 * @should not fail if given answer does not exist in list
	 */
	public boolean removeAnswer(ConceptAnswer conceptAnswer) {
		if (getAnswers() != null)
			return answers.remove(conceptAnswer);
		else
			return false;
	}
	
	/**
	 * @return Returns the changedBy.
	 */
	@Element(required = false)
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @param changedBy The changedBy to set.
	 */
	@Element(required = false)
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @return Returns the conceptClass.
	 */
	@Element
	public ConceptClass getConceptClass() {
		return conceptClass;
	}
	
	/**
	 * @param conceptClass The conceptClass to set.
	 */
	@Element
	public void setConceptClass(ConceptClass conceptClass) {
		this.conceptClass = conceptClass;
	}
	
	/**
	 * whether or not this concept is a set
	 */
	public Boolean isSet() {
		return set;
	}
	
	/**
	 * @param set whether or not this concept is a set
	 */
	@Attribute
	public void setSet(Boolean set) {
		this.set = set;
	}
	
	@Attribute
	public Boolean getSet() {
		return isSet();
	}
	
	/**
	 * @return Returns the conceptDatatype.
	 */
	@Element
	public ConceptDatatype getDatatype() {
		return datatype;
	}
	
	/**
	 * @param conceptDatatype The conceptDatatype to set.
	 */
	@Element
	public void setDatatype(ConceptDatatype conceptDatatype) {
		this.datatype = conceptDatatype;
	}
	
	/**
	 * @return Returns the conceptId.
	 */
	@Attribute(required = true)
	public Integer getConceptId() {
		return conceptId;
	}
	
	/**
	 * @param conceptId The conceptId to set.
	 */
	@Attribute(required = true)
	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
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
	 * @return Returns the dateChanged.
	 */
	@Element(required = false)
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @param dateChanged The dateChanged to set.
	 */
	@Element(required = false)
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
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
	 * Sets the preferred name for a locale. This sets tags on the concept name to indicate that it
	 * is preferred for the language and country. Also, the name is added to the concept. If the
	 * country is specified in the locale, then the language is considered to be only implied as
	 * preferred &mdash; it will only get set if there is not an existing preferred language name.
	 * 
	 * @param locale the locale for which to set the preferred name
	 * @param preferredName name which is preferred in the locale
	 * @should only allow one preferred name
	 */
	public void setPreferredName(Locale locale, ConceptName preferredName) {
		ConceptNameTag preferredLanguage = ConceptNameTag.preferredLanguageTagFor(locale);
		ConceptNameTag preferredCountry = ConceptNameTag.preferredCountryTagFor(locale);
		
		ConceptName currentPreferredNameInLanguage = getPreferredNameInLanguage(locale.getLanguage());
		
		if (preferredCountry != null) {
			if (currentPreferredNameInLanguage == null) {
				preferredName.addTag(preferredLanguage);
			}
			
			ConceptName currentPreferredForCountry = getPreferredNameForCountry(locale.getCountry());
			if (currentPreferredForCountry != null) {
				currentPreferredForCountry.removeTag(preferredCountry);
			}
			preferredName.addTag(preferredCountry);
		} else {
			if (currentPreferredNameInLanguage != null) {
				currentPreferredNameInLanguage.removeTag(preferredLanguage);
			}
			preferredName.addTag(preferredLanguage);
		}
		
		addName(preferredName);
	}
	
	/**
	 * Gets the explicitly preferred name for a country.
	 * 
	 * @param country ISO-3166 two letter country code
	 * @return the preferred name, or null if none has been explicitly set
	 */
	public ConceptName getPreferredNameForCountry(String country) {
		return findNameTaggedWith(ConceptNameTag.preferredCountryTagFor(country));
	}
	
	/**
	 * Gets the explicitly preferred name in a language.
	 * 
	 * @param language ISO-639 two letter language code
	 * @return the preferred name, or null if none has been explicitly set
	 */
	public ConceptName getPreferredNameInLanguage(String language) {
		return findNameTaggedWith(ConceptNameTag.preferredLanguageTagFor(language));
	}
	
	/**
	 * A convenience method to get the concept-name (if any) which has a particular tag. This does
	 * not guarantee that the returned name is the only one with the tag.
	 * 
	 * @param conceptNameTag the tag for which to look
	 * @return the tagged name, or null if no name has the tag
	 */
	public ConceptName findNameTaggedWith(ConceptNameTag conceptNameTag) {
		ConceptName taggedName = null;
		for (ConceptName possibleName : getNames()) {
			if (possibleName.hasTag(conceptNameTag)) {
				taggedName = possibleName;
				break;
			}
		}
		return taggedName;
	}
	
	/**
	 * Returns a name in the given locale. If a name isn't found with an exact match, a compatible
	 * locale match is returned. If no name is found matching either of those, the first name
	 * defined for this concept is returned.
	 * 
	 * @param locale the locale to fetch for
	 * @return ConceptName attributed to the Concept in the given locale
	 * @since 1.5
	 * @see Concept#getNames(Locale) to get all the names for a locale,
	 * @see Concept#getPreferredName(Locale) for the preferred name (if any),
	 * @see Concept#getBestName(Locale) to get the best match for a locale.
	 * @should get preferred fully specified country
	 */
	public ConceptName getName(Locale locale) {
		return getName(locale, false);
	}
	
	/**
	 * Returns a name in the current User's chosen locale via Context.getLocale(). If a name isn't
	 * found with an exact match, a compatible locale match is returned. If no name is found
	 * matching either of those, the first name defined for this concept is returned.
	 * 
	 * @return {@link ConceptName} in the current locale or any locale if none found
	 * @since 1.5
	 * @see Concept#getNames(Locale) to get all the names for a locale
	 * @see Concept#getPreferredName(Locale) for the preferred name (if any)
	 * @see Concept#getBestName(Locale) to get the best match for a locale
	 */
	public ConceptName getName() {
		return getName(Context.getLocale());
	}
	
	/**
	 * Checks whether this concept has the given string in any of the names in the given locale
	 * already.
	 * 
	 * @param name the ConceptName.name to compare to
	 * @param locale the locale to look in (null to check all locales)
	 * @return true/false whether the name exists already
	 */
	public boolean hasName(String name, Locale locale) {
		if (name == null)
			return false;
		
		Collection<ConceptName> currentNames = null;
		if (locale == null)
			currentNames = getNames();
		else
			currentNames = getNames(locale);
		
		for (ConceptName currentName : currentNames) {
			if (name.equals(currentName.getName()))
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns a name in the given locale. If a name isn't found with an exact match, a compatible
	 * locale match is returned. If no name is found matching either of those, the first name
	 * defined for this concept is returned.
	 * 
	 * @param locale the language and country in which the name is used
	 * @param exact true/false to return only exact locale (no default locale)
	 * @return the closest name in the given locale, or the first name
	 * @see Concept#getNames(Locale) to get all the names for a locale,
	 * @see Concept#getPreferredName(Locale) for the preferred name (if any),
	 * @see Concept#getBestName(Locale) to get the best match for a locale.
	 * @should return exact name locale match given exact equals true
	 * @should return loose match given exact equals false
	 * @should return any name if no locale match given exact equals false
	 * @should not fail if no names are defined
	 * @should return null if no locale match and exact equals true
	 * @should support plain preferred
	 */
	public ConceptName getName(Locale locale, boolean exact) {
		
		// fail early if this concept has no names defined
		if (getNames().size() == 0) {
			if (log.isDebugEnabled())
				log.debug("there are no names defined for: " + conceptId);
			return null;
		}
		
		if (log.isDebugEnabled())
			log.debug("Getting conceptName for locale: " + locale);
		
		// matches on any name in the current locale, or first name available
		ConceptName bestName = getBestName(locale);
		
		if (exact && !bestName.getLocale().equals(locale))
			return null; // no exact match found
		else
			return bestName;
	}
	
	/**
	 * Returns the name which is explicitly marked as preferred for a given locale. If the country
	 * is specified in the locale, then the language of the name must match and the name must have a
	 * tag indicating that it is preferred in the locale's country. If no country is specified, then
	 * the name must have a tag indicating that it is preferred in the locale's language
	 * 
	 * @param forLocale locale for which to return a preferred name
	 * @return preferred name for the locale, or null if none is tagged as such
	 * @should support plain preferred
	 * @should match to best name
	 */
	public ConceptName getPreferredName(Locale forLocale) {
		// fail early if this concept has no names defined
		if (getNames().size() == 0) {
			if (log.isDebugEnabled())
				log.debug("there are no names defined for: " + conceptId);
			return null;
		}
		
		if (log.isDebugEnabled())
			log.debug("Getting preferred conceptName for locale: " + forLocale);
		
		ConceptName preferredName = null; // name which exactly match the locale
		// and is preferred
		if (forLocale == null)
			forLocale = Context.getLocale(); // Don't presume en_US;
			
		ConceptNameTag desiredLanguageTag = ConceptNameTag.preferredLanguageTagFor(forLocale);
		ConceptNameTag desiredCountryTag = ConceptNameTag.preferredCountryTagFor(forLocale);
		
		for (ConceptName possibleName : getCompatibleNames(forLocale)) {
			if (forLocale.equals(possibleName.getLocale()) && possibleName.hasTag(ConceptNameTag.PREFERRED)) {
				preferredName = possibleName;
				break;
			}
			if (desiredCountryTag != null) {
				// country was specified, exact match must be preferred in country
				if (possibleName.hasTag(desiredCountryTag)) {
					preferredName = possibleName;
					break;
				}
			} else {
				// no country specified, so only worry about matching language
				if (possibleName.hasTag(desiredLanguageTag)) {
					preferredName = possibleName;
					break;
				}
			}
			if ((preferredName == null) && possibleName.hasTag(ConceptNameTag.PREFERRED)) {
				preferredName = possibleName;
			}
		}
		
		if (log.isDebugEnabled()) {
			if (preferredName == null) {
				log.warn("No preferred concept name found for concept id " + conceptId + " in locale " + forLocale);
			}
		}
		
		return preferredName;
	}
	
	/**
	 * Returns the best compatible name for a locale. The names are ordered as "best" according to
	 * these rules:
	 * <ol>
	 * <li>preferred name in matching country (for example, tagged as PREFERRED_UG for preferred in
	 * Uganda)</li>
	 * <li>preferred name in matching language (for example, tagged as PREFERRED_EN for preferred
	 * name in English)</li>
	 * <li>any name in matching country (for example, matching Uganda)</li>
	 * <li>any name in matching language (for example, matching English)</li>
	 * <li>first name in any matching language</li>
	 * </ol>
	 * 
	 * @param locale the language and country in which the name is used
	 * @return the best name possible {@link ConceptName}, never null
	 * @should support plain preferred
	 * @should always have a best name even if none match locale
	 */
	public ConceptName getBestName(Locale locale) {
		
		// fail early if this concept has no names defined
		if (getNames().size() == 0) {
			if (log.isDebugEnabled())
				log.debug("there are no names defined for: " + conceptId);
			return null;
		}
		
		if (log.isDebugEnabled())
			log.debug("Getting conceptName for locale: " + locale);
		
		ConceptName bestMatch = null;
		
		if (locale == null)
			locale = Context.getLocale(); // Don't presume en_US;
			
		ConceptNameTag desiredLanguageTag = ConceptNameTag.preferredLanguageTagFor(locale);
		ConceptNameTag desiredCountryTag = ConceptNameTag.preferredCountryTagFor(locale);
		
		List<ConceptName> compatibleNames = getCompatibleNames(locale);
		
		if (compatibleNames.size() == 0) {
			// no compatible names, so return first available name
			Iterator<ConceptName> nameIt = getNames().iterator();
			bestMatch = nameIt.next();
		} else if (compatibleNames.size() == 1) {
			bestMatch = compatibleNames.get(0);
		} else {
			// more than 1 choice? search through to find the "best"
			for (ConceptName possibleName : compatibleNames) {
				if (locale.equals(possibleName.getLocale()) && possibleName.hasTag(ConceptNameTag.PREFERRED)) {
					bestMatch = possibleName;
					break;
				}
				if (desiredCountryTag != null) {
					// country was specified, exact match must be preferred in country
					if (possibleName.hasTag(desiredCountryTag)) {
						bestMatch = possibleName;
						break; // can't get any better than this match
					} else if (possibleName.hasTag(desiredLanguageTag)) {
						bestMatch = possibleName;
					} else if (possibleName.hasTag(ConceptNameTag.PREFERRED)) {
						bestMatch = possibleName;
					} else if (bestMatch == null) {
						bestMatch = possibleName;
					}
				} else {
					// no country specified, so only worry about matching language
					if (possibleName.hasTag(desiredLanguageTag)) {
						bestMatch = possibleName;
						break;
					} else if (possibleName.hasTag(ConceptNameTag.PREFERRED)) {
						bestMatch = possibleName;
					} else if (bestMatch == null) {
						bestMatch = possibleName;
					}
				}
			}
		}
		
		if (bestMatch == null) {
			log.warn("No compatible concept name found for for concept id " + conceptId);
		}
		
		return bestMatch;
		
	}
	
	/**
	 * Returns all names available in a specific locale. <br/>
	 * <br/>
	 * This is recommended when managing the concept dictionary.
	 * 
	 * @param locale locale for which names should be returned
	 * @return Collection of ConceptNames with the given locale
	 */
	public Collection<ConceptName> getNames(Locale locale) {
		Collection<ConceptName> localeNames = new Vector<ConceptName>();
		for (ConceptName possibleName : getNames()) {
			if (possibleName.getLocale().equals(locale)) {
				localeNames.add(possibleName);
			}
		}
		return localeNames;
	}
	
	/**
	 * Returns all names from compatible locales. A locale is considered compatible if it is exactly
	 * the same locale, or if either locale has no country specified and the language matches. <br/>
	 * <br/>
	 * This is recommended when presenting possible names to the use.
	 * 
	 * @param desiredLocale locale with which the names should be compatible
	 * @return Collection of compatible names
	 * @should exclude incompatible country locales
	 * @should exclude incompatible language locales
	 */
	public List<ConceptName> getCompatibleNames(Locale desiredLocale) {
		// lazy create the cache
		List<ConceptName> compatibleNames = null;
		if (compatibleCache == null) {
			compatibleCache = new HashMap<Locale, List<ConceptName>>();
		} else {
			compatibleNames = compatibleCache.get(desiredLocale);
		}
		
		if (compatibleNames == null) {
			compatibleNames = new Vector<ConceptName>();
			for (ConceptName possibleName : getNames()) {
				if (LocaleUtility.areCompatible(possibleName.getLocale(), desiredLocale)) {
					compatibleNames.add(possibleName);
				}
			}
			compatibleCache.put(desiredLocale, compatibleNames);
		}
		return compatibleNames;
	}
	
	/**
	 * Returns the best compatible short name for a locale. The names are ordered as "best"
	 * according to these rules:
	 * <ol>
	 * <li>preferred short name in matching country (for example, tagged as SHORT_UG for preferred
	 * short in Uganda)</li>
	 * <li>preferred short name in matching language (for example, tagged as SHORT_EN for preferred
	 * short name in English)</li>
	 * <li>any short name in matching country (for example, tagged as SHORT and matching the Uganda)
	 * </li>
	 * <li>any short name in matching language (for example, tagged as SHORT and matching the
	 * English)</li>
	 * <li>any name matching the locale</li>
	 * </ol>
	 * 
	 * @param locale the language and country in which the short name is used
	 * @return the best short name
	 * @should always return a short name even if no names are tagged as short
	 */
	public ConceptName getBestShortName(Locale locale) {
		
		// fail early if this concept has no names defined
		if (getNames().size() == 0) {
			if (log.isDebugEnabled())
				log.debug("there are no names defined for: " + conceptId);
			return null;
		}
		
		if (log.isDebugEnabled())
			log.debug("Getting short conceptName for locale: " + locale);
		
		ConceptName bestMatch = null;
		
		if (locale == null)
			locale = Context.getLocale(); // Don't presume en_US;
			
		ConceptNameTag desiredLanguageTag = ConceptNameTag.shortLanguageTagFor(locale);
		ConceptNameTag desiredCountryTag = ConceptNameTag.shortCountryTagFor(locale);
		
		List<ConceptName> compatibleNames = getCompatibleNames(locale);
		
		if (compatibleNames.size() == 0) {
			// no compatible names, so return first available name
			Iterator<ConceptName> nameIt = getNames().iterator();
			bestMatch = nameIt.next();
		} else if (compatibleNames.size() == 1) {
			// only 1? it must be the best
			bestMatch = compatibleNames.get(0);
		} else {
			for (ConceptName possibleName : getCompatibleNames(locale)) {
				if (desiredCountryTag != null) {
					// country was specified, exact match must be preferred in country
					if (possibleName.hasTag(desiredCountryTag)) {
						bestMatch = possibleName;
						break;
					} else if (possibleName.hasTag(desiredLanguageTag)) {
						bestMatch = possibleName;
					} else if (possibleName.isShort()) {
						bestMatch = possibleName;
					} else if (bestMatch == null) {
						bestMatch = possibleName;
					}
				} else {
					// no country specified, so only worry about matching language
					if (possibleName.hasTag(desiredLanguageTag)) {
						bestMatch = possibleName;
						break;
					} else if (bestMatch.isShort()) {
						bestMatch = possibleName;
					} else if (bestMatch == null) {
						bestMatch = possibleName;
					}
				}
			}
		}
		
		if (bestMatch == null) {
			log.info("No compatible concept name found for default locale for concept id " + conceptId);
		}
		
		return bestMatch;
		
	}
	
	/**
	 * Sets the short name for a locale. This sets tags on the concept name to indicate that it is
	 * short for the language and country. Also, the name is added to the concept (if needed). <br/>
	 * <br/>
	 * If the country is specified in the locale, then the language is considered to be only implied
	 * &mdash; it will only get set if there is not an existing short language name. <br/>
	 * <br/>
	 * If the country is not specified in the locale, then the language is considered an explicit
	 * designation and the call is the equivalent of calling {@link #getShortNameInLanguage(String)}
	 * .
	 * 
	 * @param locale the locale for which to set the short name
	 * @param shortName name which is preferred in the locale
	 */
	public void setShortName(Locale locale, ConceptName shortName) {
		ConceptNameTag shortLanguage = ConceptNameTag.shortLanguageTagFor(locale);
		ConceptNameTag shortCountry = ConceptNameTag.shortCountryTagFor(locale);
		
		ConceptName currentShortNameInLanguage = getShortNameInLanguage(locale.getLanguage());
		if (shortCountry != null) {
			if (currentShortNameInLanguage == null) {
				shortName.addTag(shortLanguage);
			}
			
			ConceptName currentPreferredForCountry = getPreferredNameForCountry(locale.getCountry());
			if (currentPreferredForCountry != null) {
				currentPreferredForCountry.removeTag(shortCountry);
			}
			shortName.addTag(shortCountry);
		} else {
			if (currentShortNameInLanguage != null) {
				currentShortNameInLanguage.removeTag(shortLanguage);
			}
			shortName.addTag(shortLanguage);
		}
		
		addName(shortName);
	}
	
	/**
	 * Gets the explicitly specified short name for a country.
	 * 
	 * @param country ISO-3166 two letter country code
	 * @return the short name, or null if none has been explicitly set
	 */
	public ConceptName getShortNameForCountry(String country) {
		return findNameTaggedWith(ConceptNameTag.shortCountryTagFor(country));
	}
	
	/**
	 * Gets the explicitly specified short name in a language.
	 * 
	 * @param language ISO-639 two letter language code
	 * @return the short name, or null if none has been explicitly set
	 */
	public ConceptName getShortNameInLanguage(String language) {
		return findNameTaggedWith(ConceptNameTag.shortLanguageTagFor(language));
	}
	
	/**
	 * Gets the explicitly specified short name for a locale. The name returned depends on the
	 * specificity of the locale. If country is indicated, then the name must be tagged as short in
	 * that country, otherwise the name must be tagged as short in that language.
	 * 
	 * @param locale locale for which to return a short name
	 * @return the short name, or null if none has been explicitly set
	 */
	public ConceptName getShortNameInLocale(Locale locale) {
		ConceptName shortName = null;
		// ABK: country will always be non-null. Empty string (instead
		// of null) indicates no country was specified
		String country = locale.getCountry();
		if (country.length() != 0) {
			shortName = getShortNameForCountry(country);
		} else {
			shortName = getShortNameInLanguage(locale.getLanguage());
		}
		// default to getting the name in the specific locale tagged as "short"
		if (shortName == null) {
			for (ConceptName name : getCompatibleNames(locale)) {
				if (name.hasTag(ConceptNameTag.SHORT))
					return name;
			}
		}
		return shortName;
	}
	
	/**
	 * Returns the preferred short form name for a locale, or if none has been identified, the
	 * shortest name available in the locale.
	 * 
	 * @param locale the language and country in which the short name is used
	 * @param exact true/false to return only exact locale (no default locale)
	 * @return the appropriate short name, or null if not found
	 */
	public ConceptName getShortestName(Locale locale, Boolean exact) {
		if (log.isDebugEnabled())
			log.debug("Getting shortest conceptName for locale: " + locale);
		
		ConceptName foundName = null;
		ConceptName shortestName = null;
		
		if (locale == null)
			locale = LocaleUtility.getDefaultLocale();
		
		String desiredLanguage = locale.getLanguage();
		if (desiredLanguage.length() > 2)
			desiredLanguage = desiredLanguage.substring(0, 2);
		
		for (Iterator<ConceptName> i = getNames().iterator(); i.hasNext() && foundName == null;) {
			ConceptName possibleName = i.next();
			if ((shortestName == null) || (possibleName.getName().length() < shortestName.getName().length())) {
				shortestName = possibleName;
			}
		}
		
		if (foundName == null) {
			// no name with the given locale was found.
			if (exact) {
				// return null if exact match desired
				log.warn("No short concept name found for concept id " + conceptId + " for locale "
				        + locale.getDisplayName());
			} else if (shortestName != null) {
				// returning default name locale ("en") if exact match not
				// desired
				foundName = shortestName;
			} else {
				log.warn("No concept name found for default locale for concept id " + conceptId);
			}
		}
		
		return foundName;
	}
	
	/**
	 * @param name A name
	 * @return whether this concept has the given name in any locale
	 */
	public boolean isNamed(String name) {
		for (ConceptName cn : getNames())
			if (name.equals(cn.getName()))
				return true;
		return false;
	}
	
	/**
	 * @return Returns the names.
	 */
	@ElementList
	public Collection<ConceptName> getNames() {
		return getNames(false);
	}
	
	/**
	 * @return Returns the names.
	 * @param includeVoided Include voided ConceptNames if true.
	 */
	public Collection<ConceptName> getNames(boolean includeVoided) {
		Collection<ConceptName> ret = new HashSet<ConceptName>();
		if (includeVoided) {
			if (names != null)
				return names;
			else
				return ret;
		} else {
			if (names != null) {
				for (ConceptName cn : names) {
					if (!cn.isVoided())
						ret.add(cn);
				}
			}
			return ret;
		}
	}
	
	/**
	 * @param names The names to set.
	 */
	@ElementList
	public void setNames(Collection<ConceptName> names) {
		this.names = names;
	}
	
	/**
	 * Add the given ConceptName to the list of names for this Concept
	 * 
	 * @param conceptName
	 */
	public void addName(ConceptName conceptName) {
		conceptName.setConcept(this);
		if (names == null)
			names = new HashSet<ConceptName>();
		if (conceptName != null && !names.contains(conceptName)) {
			names.add(conceptName);
			if (compatibleCache != null) {
				compatibleCache.clear(); // clear the locale cache, forcing it to be rebuilt
			}
		}
	}
	
	/**
	 * Remove the given name from the list of names for this Concept
	 * 
	 * @param conceptName
	 * @return true if the entity was removed, false otherwise
	 */
	public boolean removeName(ConceptName conceptName) {
		if (names != null)
			return names.remove(conceptName);
		else
			return false;
	}
	
	/**
	 * Finds the description of the concept using the current locale in Context.getLocale(). Returns
	 * null if none found.
	 * 
	 * @return ConceptDescription attributed to the Concept in the given locale
	 */
	public ConceptDescription getDescription() {
		return getDescription(Context.getLocale());
	}
	
	/**
	 * Finds the description of the concept in the given locale. Returns null if none found.
	 * 
	 * @param locale
	 * @return ConceptDescription attributed to the Concept in the given locale
	 */
	public ConceptDescription getDescription(Locale locale) {
		return getDescription(locale, false);
	}
	
	/**
	 * Returns the preferred description for a locale.
	 * 
	 * @param locale the language and country in which the description is used
	 * @param exact true/false to return only exact locale (no default locale)
	 * @return the appropriate description, or null if not found
	 * @should return match on locale exactly
	 * @should return match on language only
	 * @should not return match on language only if exact match exists
	 * @should not return language only match for exact matches
	 */
	public ConceptDescription getDescription(Locale locale, boolean exact) {
		log.debug("Getting ConceptDescription for locale: " + locale);
		
		ConceptDescription foundDescription = null;
		
		if (locale == null)
			locale = LocaleUtility.getDefaultLocale();
		
		Locale desiredLocale = locale;
		
		ConceptDescription defaultDescription = null;
		for (Iterator<ConceptDescription> i = getDescriptions().iterator(); i.hasNext();) {
			ConceptDescription availableDescription = i.next();
			Locale availableLocale = availableDescription.getLocale();
			if (availableLocale.equals(desiredLocale)) {
				foundDescription = availableDescription;
				break; // skip out now because we found an exact locale match
			}
			if (!exact && LocaleUtility.areCompatible(availableLocale, desiredLocale))
				foundDescription = availableDescription;
			if (availableLocale.equals(LocaleUtility.getDefaultLocale()))
				defaultDescription = availableDescription;
		}
		
		if (foundDescription == null) {
			// no description with the given locale was found.
			// return null if exact match desired
			if (exact) {
				log.debug("No concept description found for concept id " + conceptId + " for locale "
				        + desiredLocale.toString());
			} else {
				// returning default description locale ("en") if exact match
				// not desired
				if (defaultDescription == null)
					log.debug("No concept description found for default locale for concept id " + conceptId);
				else {
					foundDescription = defaultDescription;
				}
			}
		}
		return foundDescription;
	}
	
	/**
	 * @return the retiredBy
	 */
	public User getRetiredBy() {
		return retiredBy;
	}
	
	/**
	 * @param retiredBy the retiredBy to set
	 */
	public void setRetiredBy(User retiredBy) {
		this.retiredBy = retiredBy;
	}
	
	/**
	 * @return the dateRetired
	 */
	public Date getDateRetired() {
		return dateRetired;
	}
	
	/**
	 * @param dateRetired the dateRetired to set
	 */
	public void setDateRetired(Date dateRetired) {
		this.dateRetired = dateRetired;
	}
	
	/**
	 * @return the retireReason
	 */
	public String getRetireReason() {
		return retireReason;
	}
	
	/**
	 * @param retireReason the retireReason to set
	 */
	public void setRetireReason(String retireReason) {
		this.retireReason = retireReason;
	}
	
	/**
	 * @return Returns the descriptions.
	 */
	@ElementList
	public Collection<ConceptDescription> getDescriptions() {
		return descriptions;
	}
	
	/**
	 * Sets the collection of descriptions for this Concept.
	 * 
	 * @param descriptions the collection of descriptions
	 */
	@ElementList
	public void setDescriptions(Collection<ConceptDescription> descriptions) {
		this.descriptions = descriptions;
	}
	
	/**
	 * Add the given description to the list of descriptions for this Concept
	 * 
	 * @param description the description to add
	 */
	public void addDescription(ConceptDescription description) {
		if (description != null) {
			if (getDescriptions() == null) {
				descriptions = new HashSet<ConceptDescription>();
				description.setConcept(this);
				descriptions.add(description);
			} else if (!descriptions.contains(description)) {
				description.setConcept(this);
				descriptions.add(description);
			}
		}
	}
	
	/**
	 * Remove the given description from the list of descriptions for this Concept
	 * 
	 * @param description the description to remove
	 * @return true if the entity was removed, false otherwise
	 */
	public boolean removeDescription(ConceptDescription description) {
		if (getDescriptions() != null)
			return descriptions.remove(description);
		else
			return false;
	}
	
	/**
	 * @return Returns the retired.
	 */
	public Boolean isRetired() {
		return retired;
	}
	
	/**
	 * This method exists to satisfy spring and hibernates slightly bung use of Boolean object
	 * getters and setters.
	 * 
	 * @deprecated Use the "proper" isRetired method.
	 * @see org.openmrs.Concept#isRetired()
	 */
	@Deprecated
    @Attribute
	public Boolean getRetired() {
		return isRetired();
	}
	
	/**
	 * @param retired The retired to set.
	 */
	@Attribute
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	
	/**
	 * Gets the synonyms in the given locale. Returns a list of names from the same language, or an
	 * empty list if none found.
	 * 
	 * @param locale
	 * @return Collection of ConceptNames which are synonyms for the Concept in the given locale
	 */
	public Collection<ConceptName> getSynonyms(Locale locale) {
		String desiredLanguage = locale.getLanguage();
		Collection<ConceptName> syns = new Vector<ConceptName>();
		for (ConceptName possibleSynonym : getNames()) {
			if (possibleSynonym.hasTag(ConceptNameTag.SYNONYM)) {
				String lang = possibleSynonym.getLocale().getLanguage();
				if (lang.equals(desiredLanguage))
					syns.add(possibleSynonym);
			}
		}
		log.debug("returning: " + syns);
		return syns;
	}
	
	/**
	 * @return Returns the version.
	 */
	@Attribute(required = false)
	public String getVersion() {
		return version;
	}
	
	/**
	 * @param version The version to set.
	 */
	@Attribute(required = false)
	public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * @return Returns the conceptSets.
	 */
	@ElementList(required = false)
	public Collection<ConceptSet> getConceptSets() {
		return conceptSets;
	}
	
	/**
	 * @param conceptSets The conceptSets to set.
	 */
    @ElementList(required = false)
	public void setConceptSets(Collection<ConceptSet> conceptSets) {
		this.conceptSets = conceptSets;
	}
	
	/**
	 * Whether this concept is numeric or not. This will <i>always</i> return false for concept
	 * objects. ConceptNumeric.isNumeric() will then <i>always</i> return true.
	 * 
	 * @return false
	 */
	public boolean isNumeric() {
		return false;
	}
	
	/**
	 * @return the conceptMappings for this concept
	 */
	@ElementList(required = false)
	public Collection<ConceptMap> getConceptMappings() {
		return conceptMappings;
	}
	
	/**
	 * @param conceptMappings the conceptMappings to set
	 */
	@ElementList(required = false)
	public void setConceptMappings(Collection<ConceptMap> conceptMappings) {
		this.conceptMappings = conceptMappings;
	}
	
	/**
	 * Add the given ConceptMap object to this concept's list of concept mappings. If there is
	 * already a corresponding ConceptMap object for this concept already, this one will not be
	 * added.
	 * 
	 * @param newConceptMap
	 */
	public void addConceptMapping(ConceptMap newConceptMap) {
		newConceptMap.setConcept(this);
		if (getConceptMappings() == null)
			conceptMappings = new HashSet<ConceptMap>();
		if (newConceptMap != null && !conceptMappings.contains(newConceptMap))
			conceptMappings.add(newConceptMap);
	}
	
	/**
	 * Child Class ConceptComplex overrides this method and returns true. See
	 * {@link org.openmrs.ConceptComplex#isComplex()}. Otherwise this method returns false.
	 * 
	 * @return false
	 * @since 1.5
	 */
	public boolean isComplex() {
		return false;
	}
	
	/**
	 * Remove the given ConceptMap from the list of mappings for this Concept
	 * 
	 * @param conceptMap
	 * @return true if the entity was removed, false otherwise
	 */
	public boolean removeConceptMapping(ConceptMap conceptMap) {
		if (getConceptMappings() != null)
			return conceptMappings.remove(conceptMap);
		else
			return false;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
    public String toString() {
		if (conceptId == null)
			return "";
		return conceptId.toString();
	}
	
	/**
	 * @see org.openmrs.Attributable#findPossibleValues(java.lang.String)
	 */
	public List<Concept> findPossibleValues(String searchText) {
		List<Concept> concepts = new Vector<Concept>();
		try {
			
			for (ConceptWord word : Context.getConceptService().getConceptWords(searchText,
			    Collections.singletonList(Context.getLocale()), false, null, null, null, null, null, null, null)) {
				concepts.add(word.getConcept());
			}
		}
		catch (Exception e) {
			// pass
		}
		return concepts;
	}
	
	/**
	 * @see org.openmrs.Attributable#getPossibleValues()
	 */
	public List<Concept> getPossibleValues() {
		try {
			return Context.getConceptService().getConceptsByName("");
		}
		catch (Exception e) {
			// pass
		}
		return Collections.emptyList();
	}
	
	/**
	 * @see org.openmrs.Attributable#hydrate(java.lang.String)
	 */
	public Concept hydrate(String s) {
		try {
			return Context.getConceptService().getConcept(Integer.valueOf(s));
		}
		catch (Exception e) {
			// pass
		}
		return null;
	}
	
	/**
	 * Turns this concept into a very very simple serialized string
	 * 
	 * @see org.openmrs.Attributable#serialize()
	 */
	public String serialize() {
		if (this.getConceptId() == null)
			return "";
		
		return "" + this.getConceptId();
	}
	
	/**
	 * @see org.openmrs.Attributable#getDisplayString()
	 */
	public String getDisplayString() {
		if (getName() == null)
			return toString();
		else
			return getName().getName();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getConceptId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setConceptId(id);
	}

	/**
	 * Sort the ConceptSet based on the weight
	 * 
	 * @return sortedConceptSet Collection<ConceptSet>
	 */
	private List<ConceptSet> getSortedConceptSets() {
		List<ConceptSet> cs = new Vector<ConceptSet>();
		if (conceptSets != null) {
			cs.addAll(conceptSets);
			Collections.sort(cs);
		}
		
		return cs;
	}

	/**
	 * Get all the concept members of current concept
	 * 
	 * @since 1.7
	 * @return List<Concept> the Concepts that are members of this Concept's set
	 * @should return concept set members sorted according to the sort weight
	 * @should return all the conceptMembers of current Concept
	 * @should return unmodifiable list of conceptMember list
	 */
	public List<Concept> getSetMembers() {
		List<Concept> conceptMembers = new Vector<Concept>();
		
		Collection<ConceptSet> sortedConceptSet = getSortedConceptSets();
		
		for (ConceptSet conceptSet : sortedConceptSet) {
			conceptMembers.add(conceptSet.getConcept());
		}
		return Collections.unmodifiableList(conceptMembers);
	}
	
	/**
	 * Appends the concept to the end of the existing list of concept members for this Concept
	 * 
	 * @since 1.7
	 * @param setMember Concept to add to the
	 * @should add concept as a conceptSet
	 * @should append concept to the existing list of conceptSet
	 * @should place the new concept last in the list
	 * @should assign the calling component as parent to the ConceptSet
	 */
	public void addSetMember(Concept setMember) {
		addSetMember(setMember, -1);
	}
	
	/**
	 * Add the concept to the existing member to the list of set members in the given location. <br/>
	 * <br/>
	 * index of 0 is before the first concept<br/>
	 * index of -1 is after last.<br/>
	 * index of 1 is after the first but before the second, etc<br/>
	 * 
	 * @param setMember the Concept to add as a child of this Concept
	 * @param index where in the list of set members to put this setMember
	 * @since 1.7
	 * @should assign the given concept as a ConceptSet
	 * @should insert the concept before the first with zero index
	 * @should insert the concept at the end with negative one index
	 * @should insert the concept in the third slot
	 * @should assign the calling component as parent to the ConceptSet
	 * @should add the concept to the current list of conceptSet
	 * @see #getSortedConceptSets()
	 */
	public void addSetMember(Concept setMember, int index) {
		List<ConceptSet> sortedConceptSets = getSortedConceptSets();
		int setsSize = sortedConceptSets.size();

		double weight;

		if (sortedConceptSets.isEmpty())
			weight = 1000.0;
		else if (index == -1 || index >= setsSize)
			// deals with list size of 1 and any large index given by dev
			weight = sortedConceptSets.get(setsSize - 1).getSortWeight() + 10.0;
		else if (index == 0)
			weight = sortedConceptSets.get(0).getSortWeight() - 10.0;
		else {
			// put the weight between two
			double prevSortWeight = sortedConceptSets.get(index - 1).getSortWeight();
			double nextSortWeight = sortedConceptSets.get(index).getSortWeight();
			weight = (prevSortWeight + nextSortWeight) / 2;
		}
		
		ConceptSet conceptSet = new ConceptSet(setMember, weight);
		conceptSet.setConceptSet(this);
		conceptSets.add(conceptSet);
	}

}

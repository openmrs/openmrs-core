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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FullTextFilterDef;
import org.hibernate.search.annotations.FullTextFilterDefs;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.openmrs.annotation.AllowDirectAccess;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.search.TermsFilterFactory;
import org.openmrs.customdatatype.CustomValueDescriptor;
import org.openmrs.customdatatype.Customizable;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

/**
 * A Concept object can represent either a question or an answer to a data point. That data point is
 * usually an {@link Obs}. <br>
 * <br>
 * A Concept can have multiple names and multiple descriptions within one locale and across multiple
 * locales.<br>
 * <br>
 * To save a Concept to the database, first build up the Concept object in java, then pass that
 * object to the {@link ConceptService}.<br>
 * <br>
 * To get a Concept that is stored in the database, call a method in the {@link ConceptService} to
 * fetch an object. To get child objects off of that Concept, further calls to the
 * {@link ConceptService} or the database are not needed. e.g. To get the list of answers that are
 * stored to a concept, get the concept, then call {@link Concept#getAnswers()}
 * 
 * @see ConceptName
 * @see ConceptDescription
 * @see ConceptAnswer
 * @see ConceptSet
 * @see ConceptMap
 * @see ConceptService
 */
@FullTextFilterDefs( { @FullTextFilterDef(name = "termsFilterFactory", impl = TermsFilterFactory.class) })
public class Concept extends BaseOpenmrsObject implements Auditable, Retireable, Serializable, Attributable<Concept>,Customizable<ConceptAttribute> {
	
	public static final long serialVersionUID = 57332L;
	
	private static final Logger log = LoggerFactory.getLogger(Concept.class);
	
	// Fields
	@DocumentId
	private Integer conceptId;
	
	@Field
	private Boolean retired = false;
	
	private User retiredBy;
	
	private Date dateRetired;
	
	private String retireReason;
	
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private ConceptDatatype datatype;
	
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private ConceptClass conceptClass;
	
	private Boolean set = false;
	
	private String version;
	
	private User creator;
	
	private Date dateCreated;
	
	private User changedBy;
	
	private Date dateChanged;
	
	@AllowDirectAccess
	@ContainedIn
	private Collection<ConceptName> names;
	
	@AllowDirectAccess
	private Collection<ConceptAnswer> answers;
	
	private Collection<ConceptSet> conceptSets;
	
	private Collection<ConceptDescription> descriptions;
	
	@IndexedEmbedded(includeEmbeddedObjectId = true)
	private Collection<ConceptMap> conceptMappings;
	
	/**
	 * A cache of locales to names which have compatible locales. Built on-the-fly by
	 * getCompatibleNames().
	 */
	private Map<Locale, List<ConceptName>> compatibleCache;

	private Set<ConceptAttribute> attributes = new LinkedHashSet<>();

	/** default constructor */
	public Concept() {
		names = new HashSet<>();
		answers = new HashSet<>();
		conceptSets = new TreeSet<>();
		descriptions = new HashSet<>();
		conceptMappings = new HashSet<>();
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
		this();
		this.conceptId = conceptId;
	}
	
	/**
	 * @return Returns all answers (including retired answers).
	 * @should return retired and non-retired answers
	 * @should not return null if answers is null or empty
	 */
	public Collection<ConceptAnswer> getAnswers() {
		if (answers == null) {
			answers = new HashSet<>();
		}
		return answers;
	}
	
	/**
	 * If <code>includeRetired</code> is true, then the returned object is the actual stored list of
	 * {@link ConceptAnswer}s
	 * 
	 * @param includeRetired true/false whether to also include the retired answers
	 * @return Returns the answers for this Concept
	 * @should return the same as getAnswers() if includeRetired is true
	 * @should not return retired answers if includeRetired is false
	 */
	public Collection<ConceptAnswer> getAnswers(boolean includeRetired) {
		if (includeRetired) {
			return getAnswers();
		} else {
			return getAnswers().stream()
					.filter(a -> !a.getAnswerConcept().getRetired())
					.collect(Collectors.toSet());
		}
	}

	/**
	 * Set this Concept as having the given <code>answers</code>; This method assumes that the
	 * sort_weight has already been set.
	 * 
	 * @param answers The answers to set.
	 */
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
			if (!getAnswers().contains(conceptAnswer)) {
				conceptAnswer.setConcept(this);
				getAnswers().add(conceptAnswer);
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
		return getAnswers().remove(conceptAnswer);
	}
	
	/**
	 * @return Returns the changedBy.
	 */
	@Override
	public User getChangedBy() {
		return changedBy;
	}
	
	/**
	 * @param changedBy The changedBy to set.
	 */
	@Override
	public void setChangedBy(User changedBy) {
		this.changedBy = changedBy;
	}
	
	/**
	 * @return Returns the conceptClass.
	 */
	public ConceptClass getConceptClass() {
		return conceptClass;
	}
	
	/**
	 * @param conceptClass The conceptClass to set.
	 */
	public void setConceptClass(ConceptClass conceptClass) {
		this.conceptClass = conceptClass;
	}
	
	/**
	 * whether or not this concept is a set
	 * 
	 * @deprecated as of 2.0, use {@link #getSet()}
	 */
	@Deprecated
	@JsonIgnore
	public Boolean isSet() {
		return getSet();
	}
	
	/**
	 * @param set whether or not this concept is a set
	 */
	public void setSet(Boolean set) {
		this.set = set;
	}
	
	public Boolean getSet() {
		return set;
	}
	
	/**
	 * @return Returns the conceptDatatype.
	 */
	public ConceptDatatype getDatatype() {
		return datatype;
	}
	
	/**
	 * @param conceptDatatype The conceptDatatype to set.
	 */
	public void setDatatype(ConceptDatatype conceptDatatype) {
		this.datatype = conceptDatatype;
	}
	
	/**
	 * @return Returns the conceptId.
	 */
	public Integer getConceptId() {
		return conceptId;
	}
	
	/**
	 * @param conceptId The conceptId to set.
	 */
	public void setConceptId(Integer conceptId) {
		this.conceptId = conceptId;
	}
	
	/**
	 * @return Returns the creator.
	 */
	@Override
	public User getCreator() {
		return creator;
	}
	
	/**
	 * @param creator The creator to set.
	 */
	@Override
	public void setCreator(User creator) {
		this.creator = creator;
	}
	
	/**
	 * @return Returns the dateChanged.
	 */
	@Override
	public Date getDateChanged() {
		return dateChanged;
	}
	
	/**
	 * @param dateChanged The dateChanged to set.
	 */
	@Override
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
	}
	
	/**
	 * @return Returns the dateCreated.
	 */
	@Override
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @param dateCreated The dateCreated to set.
	 */
	@Override
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * Sets the preferred name /in this locale/ to the specified conceptName and its Locale, if
	 * there is an existing preferred name for this concept in the same locale, this one will
	 * replace the old preferred name. Also, the name is added to the concept if it is not already
	 * among the concept names.
	 * 
	 * @param preferredName The name to be marked as preferred in its locale
	 * @should only allow one preferred name
	 * @should add the name to the list of names if it not among them before
	 * @should fail if the preferred name to set to is an index term
	 */
	public void setPreferredName(ConceptName preferredName) {
		
		if (preferredName == null || preferredName.getVoided() || preferredName.isIndexTerm()) {
			throw new APIException("Concept.error.preferredName.null", (Object[]) null);
		} else if (preferredName.getLocale() == null) {
			throw new APIException("Concept.name.locale.null", (Object[]) null);
		}
		
		//first revert the current preferred name(if any) from being preferred
		ConceptName oldPreferredName = getPreferredName(preferredName.getLocale());
		if (oldPreferredName != null) {
			oldPreferredName.setLocalePreferred(false);
		}
		
		preferredName.setLocalePreferred(true);
		//add this name, if it is new or not among this concept's names
		if (preferredName.getConceptNameId() == null || !getNames().contains(preferredName)) {
			addName(preferredName);
		}
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
	 * @see Concept#getPreferredName(Locale) for the preferred name (if any)
	 */
	public ConceptName getName(Locale locale) {
		return getName(locale, false);
	}
	
	/**
	 * Returns concept name, the look up for the appropriate name is done in the following order;
	 * <ul>
	 * <li>First name found in any locale that is explicitly marked as preferred while searching
	 * available locales in order of preference (the locales are traversed in their order as they
	 * are listed in the 'locale.allowed.list' including english global property).</li>
	 * <li>First "Fully Specified" name found while searching available locales in order of
	 * preference.</li>
	 * <li>The first fully specified name found while searching through all names for the concept</li>
	 * <li>The first synonym found while searching through all names for the concept.</li>
	 * <li>The first random name found(except index terms) while searching through all names.</li>
	 * </ul>
	 * 
	 * @return {@link ConceptName} in the current locale or any locale if none found
	 * @since 1.5
	 * @see Concept#getNames(Locale) to get all the names for a locale
	 * @see Concept#getPreferredName(Locale) for the preferred name (if any)
	 * @should return the name explicitly marked as locale preferred if any is present
	 * @should return the fully specified name in a locale if no preferred name is set
	 * @should return null if the only added name is an index term
	 * @should return name in broader locale in case none is found in specific one
	 */
	public ConceptName getName() {
		if (getNames().isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("there are no names defined for: " + conceptId);
			}
			return null;
		}
		
		for (Locale currentLocale : LocaleUtility.getLocalesInOrder()) {
			ConceptName preferredName = getPreferredName(currentLocale);
			if (preferredName != null) {
				return preferredName;
			}
			
			ConceptName fullySpecifiedName = getFullySpecifiedName(currentLocale);
			if (fullySpecifiedName != null) {
				return fullySpecifiedName;
			}
			
			//if the locale has an variants e.g en_GB, try names in the locale excluding the country code i.e en
			if (!StringUtils.isBlank(currentLocale.getCountry()) || !StringUtils.isBlank(currentLocale.getVariant())) {
				Locale broaderLocale = new Locale(currentLocale.getLanguage());
				ConceptName prefNameInBroaderLoc = getPreferredName(broaderLocale);
				if (prefNameInBroaderLoc != null) {
					return prefNameInBroaderLoc;
				}
				
				ConceptName fullySpecNameInBroaderLoc = getFullySpecifiedName(broaderLocale);
				if (fullySpecNameInBroaderLoc != null) {
					return fullySpecNameInBroaderLoc;
				}
			}
		}
		
		for (ConceptName cn : getNames()) {
			if (cn.isFullySpecifiedName()) {
				return cn;
			}
		}
		
		if (!getSynonyms().isEmpty()) {
			return getSynonyms().iterator().next();
		}
		
		// we don't expect to get here since every concept name must have at least
		// one fully specified name, but just in case (probably inconsistent data)
		
		return null;
	}
	
	/**
	 * Checks whether this concept has the given string in any of the names in the given locale
	 * already.
	 * 
	 * @param name the ConceptName.name to compare to
	 * @param locale the locale to look in (null to check all locales)
	 * @return true/false whether the name exists already
	 * @should return false if name is null
	 * @should return true if locale is null but name exists
	 * @should return false if locale is null but name does not exist
	 */
	public boolean hasName(String name, Locale locale) {
		if (name == null) {
			return false;
		}
		
		Collection<ConceptName> currentNames;
		if (locale == null) {
			currentNames = getNames();
		} else {
			currentNames = getNames(locale);
		}
		
		for (ConceptName currentName : currentNames) {
			if (name.equalsIgnoreCase(currentName.getName())) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Returns concept name depending of locale, type (short, fully specified, etc) and tag.
	 * Searches in the locale, and then the locale's parent if nothing is found.
	 * 
	 * @param ofType find a name of this type (optional)
	 * @param havingTag find a name with this tag (optional)
	 * @param locale find a name with this locale (required)
	 * @return a name that matches the arguments, or null if none is found. If there are multiple
	 *         matches and one is locale_preferred, that will be returned, otherwise a random one of
	 *         the matches will be returned.
	 * @since 1.9
	 **/
	public ConceptName getName(Locale locale, ConceptNameType ofType, ConceptNameTag havingTag) {
		Collection<ConceptName> namesInLocale = getNames(locale);
		if (!namesInLocale.isEmpty()) {
			List<ConceptName> matches = new ArrayList<>();
			
			for (ConceptName candidate : namesInLocale) {
				if ((ofType == null || ofType.equals(candidate.getConceptNameType()))
				        && (havingTag == null || candidate.hasTag(havingTag))) {
					matches.add(candidate);
				}
			}
			
			// if we have any matches, we'll return one of them
			if (matches.size() == 1) {
				return matches.get(0);
			} else if (matches.size() > 1) {
				for (ConceptName match : matches) {
					if (match.getLocalePreferred()) {
						return match;
					}
				}
				// none was explicitly marked as preferred
				return matches.get(0);
			}
		}
		
		// if we reach here, there were no matching names, so try to look in the parent locale
		Locale parent = new Locale(locale.getLanguage());
		if (!parent.equals(locale)) {
			return getName(parent, ofType, havingTag);
		} else {
			return null;
		}
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
	 * @see Concept#getPreferredName(Locale) for the preferred name (if any)
	 * @should return exact name locale match given exact equals true
	 * @should return loose match given exact equals false
	 * @should return null if no names are found in locale given exact equals true
	 * @should return any name if no locale match given exact equals false
	 * @should return name in broader locale in case none is found in specific one
	 */
	public ConceptName getName(Locale locale, boolean exact) {
		
		// fail early if this concept has no names defined
		if (getNames().isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("there are no names defined for: " + conceptId);
			}
			return null;
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Getting conceptName for locale: " + locale);
		}
		
		ConceptName exactName = getNameInLocale(locale);
		
		if (exactName != null) {
			return exactName;
		}
		
		if (!exact) {
			Locale broaderLocale = new Locale(locale.getLanguage());
			ConceptName name = getNameInLocale(broaderLocale);
			return name != null ? name : getName();
		}
		return null;
	}
	
	/**
	 * Gets the best name in the specified locale.
	 * 
	 * @param locale
	 * @return null if name in given locale doesn't exist
	 */
	private ConceptName getNameInLocale(Locale locale) {
		ConceptName preferredName = getPreferredName(locale);
		if (preferredName != null) {
			return preferredName;
		}
		
		ConceptName fullySpecifiedName = getFullySpecifiedName(locale);
		if (fullySpecifiedName != null) {
			return fullySpecifiedName;
		} else if (!getSynonyms(locale).isEmpty()) {
			return getSynonyms(locale).iterator().next();
		}
		
		return null;
	}
	
	/**
	 * Returns the name which is explicitly marked as preferred for a given locale.
	 * 
	 * @param forLocale locale for which to return a preferred name
	 * @return preferred name for the locale, or null if no preferred name is specified
	 * @should return the concept name explicitly marked as locale preferred
	 * @should return the fully specified name if no name is explicitly marked as locale preferred
	 */
	public ConceptName getPreferredName(Locale forLocale) {
		
		if (log.isDebugEnabled()) {
			log.debug("Getting preferred conceptName for locale: " + forLocale);
		}
		// fail early if this concept has no names defined
		if (getNames(forLocale).isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("there are no names defined for concept with id: " + conceptId + " in the  locale: " + forLocale);
			}
			return null;
		} else if (forLocale == null) {
			log.warn("Locale cannot be null");
			return null;
		}
		
		for (ConceptName nameInLocale : getNames(forLocale)) {
			if (ObjectUtils.nullSafeEquals(nameInLocale.getLocalePreferred(), true)) {
				return nameInLocale;
			}
		}
		
		// look for partially locale match - any language matches takes precedence over country matches.
		ConceptName bestMatch = null;
		
		for (ConceptName nameInLocale : getPartiallyCompatibleNames(forLocale)) {
			if (ObjectUtils.nullSafeEquals(nameInLocale.getLocalePreferred(), true)) {
				Locale nameLocale = nameInLocale.getLocale();
				if (forLocale.getLanguage().equals(nameLocale.getLanguage())) {
					return nameInLocale;
				} else {
					bestMatch = nameInLocale;
				}
				
			}
		}
		
		if (bestMatch != null) {
			return bestMatch;
		}
		
		return getFullySpecifiedName(forLocale);
	}
	
	/**
	 * Convenience method that returns the fully specified name in the locale
	 * 
	 * @param locale locale from which to look up the fully specified name
	 * @return the name explicitly marked as fully specified for the locale
	 * @should return the name marked as fully specified for the given locale
	 */
	public ConceptName getFullySpecifiedName(Locale locale) {
		if (locale != null && !getNames(locale).isEmpty()) {
			//get the first fully specified name, since every concept must have a fully specified name,
			//then, this loop will have to return a name
			for (ConceptName conceptName : getNames(locale)) {
				if (ObjectUtils.nullSafeEquals(conceptName.isFullySpecifiedName(), true)) {
					return conceptName;
				}
			}
			
			// look for partially locale match - any language matches takes precedence over country matches.
			ConceptName bestMatch = null;
			for (ConceptName conceptName : getPartiallyCompatibleNames(locale)) {
				if (ObjectUtils.nullSafeEquals(conceptName.isFullySpecifiedName(), true)) {
					Locale nameLocale = conceptName.getLocale();
					if (locale.getLanguage().equals(nameLocale.getLanguage())) {
						return conceptName;
					}
					bestMatch = conceptName;
				}
			}
			return bestMatch;
			
		}
		return null;
	}
	
	/**
	 * Returns all names available in a specific locale. <br>
	 * <br>
	 * This is recommended when managing the concept dictionary.
	 * 
	 * @param locale locale for which names should be returned
	 * @return Collection of ConceptNames with the given locale
	 */
	public Collection<ConceptName> getNames(Locale locale) {
		return getNames().stream()
				.filter(n -> n.getLocale().equals(locale))
				.collect(Collectors.toSet());
	}
	
	/**
	 * Returns all names available for locale language "or" country. <br>
	 * <br>
	 * 
	 * @param locale locale for which names should be returned
	 * @return Collection of ConceptNames with the given locale language or country
	 */
	private Collection<ConceptName> getPartiallyCompatibleNames(Locale locale) {
		String language = locale.getLanguage();
		String country = locale.getCountry();
		
		return getNames().stream()
				.filter(n -> language.equals(n.getLocale().getLanguage()) || 
							StringUtils.isNotBlank(country) && country.equals(n.getLocale().getCountry()))
				.collect(Collectors.toSet());
	}
	
	/**
	 * Returns all names from compatible locales. A locale is considered compatible if it is exactly
	 * the same locale, or if either locale has no country specified and the language matches. <br>
	 * <br>
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
			compatibleCache = new HashMap<>();
		} else {
			compatibleNames = compatibleCache.get(desiredLocale);
		}
		
		if (compatibleNames == null) {
			compatibleNames = new ArrayList<>();
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
	 * Sets the specified name as the fully specified name for the locale and the current fully
	 * specified (if any) ceases to be the fully specified name for the locale.
	 * 
	 * @param fullySpecifiedName the new fully specified name to set
	 * @should set the concept name type of the specified name to fully specified
	 * @should convert the previous fully specified name if any to a synonym
	 * @should add the name to the list of names if it not among them before
	 */
	public void setFullySpecifiedName(ConceptName fullySpecifiedName) {
		if (fullySpecifiedName == null || fullySpecifiedName.getLocale() == null) {
			throw new APIException("Concept.name.locale.null", (Object[]) null);
		} else if (fullySpecifiedName.getVoided()) {
			throw new APIException("Concept.error.fullySpecifiedName.null", (Object[]) null);
		}
		
		ConceptName oldFullySpecifiedName = getFullySpecifiedName(fullySpecifiedName.getLocale());
		if (oldFullySpecifiedName != null) {
			oldFullySpecifiedName.setConceptNameType(null);
		}
		fullySpecifiedName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		//add this name, if it is new or not among this concept's names
		if (fullySpecifiedName.getConceptNameId() == null || !getNames().contains(fullySpecifiedName)) {
			addName(fullySpecifiedName);
		}
	}
	
	/**
	 * Sets the specified name as the short name for the locale and the current shortName(if any)
	 * ceases to be the short name for the locale.
	 * 
	 * @param shortName the new shortName to set
	 * @should set the concept name type of the specified name to short
	 * @should convert the previous shortName if any to a synonym
	 * @should add the name to the list of names if it not among them before
	 * @should void old short name if new one is blank (do not save blanks!)
	 */
	public void setShortName(ConceptName shortName) {
		if (shortName != null) {
			if (shortName.getLocale() == null) {
				throw new APIException("Concept.name.locale.null", (Object[]) null);
			}
			ConceptName oldShortName = getShortNameInLocale(shortName.getLocale());
			if (oldShortName != null) {
				oldShortName.setConceptNameType(null);
			}
			shortName.setConceptNameType(ConceptNameType.SHORT);
			if (StringUtils.isNotBlank(shortName.getName())
			        && (shortName.getConceptNameId() == null || !getNames().contains(shortName))) {
				//add this name, if it is new or not among this concept's names
				addName(shortName);
			}
		} else {
			throw new APIException("Concept.error.shortName.null", (Object[]) null);
		}
	}
	
	/**
	 * Gets the explicitly specified short name for a locale.
	 * 
	 * @param locale locale for which to find a short name
	 * @return the short name, or null if none has been explicitly set
	 */
	public ConceptName getShortNameInLocale(Locale locale) {
		ConceptName bestMatch = null;
		if (locale != null && !getShortNames().isEmpty()) {
			for (ConceptName shortName : getShortNames()) {
				Locale nameLocale = shortName.getLocale();
				if (nameLocale.equals(locale)) {
					return shortName;
				}
				// test for partially locale match - any language matches takes precedence over country matches.
				if (OpenmrsUtil.nullSafeEquals(locale.getLanguage(), nameLocale.getLanguage())) {
					bestMatch = shortName;
				} else if (bestMatch == null && StringUtils.isNotBlank(locale.getCountry())
				        && locale.getCountry().equals(nameLocale.getCountry())) {
					bestMatch = shortName;
				}
			}
		}
		return bestMatch;
	}
	
	/**
	 * Gets a collection of short names for this concept from all locales.
	 * 
	 * @return a collection of all short names for this concept
	 */
	public Collection<ConceptName> getShortNames() {
		List<ConceptName> shortNames = new ArrayList<>();
		if (getNames().isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("The Concept with id: " + conceptId + " has no names");
			}
		} else {
			shortNames = getNames().stream()
							.filter(ConceptName::isShort)
							.collect(Collectors.toList());
		}
		return shortNames;
	}
	
	/**
	 * Returns the short form name for a locale, or if none has been identified, the shortest name
	 * available in the locale. If exact is false, the shortest name from any locale is returned
	 * 
	 * @param locale the language and country in which the short name is used
	 * @param exact true/false to return only exact locale (no default locale)
	 * @return the appropriate short name, or null if not found
	 * @should return the name marked as the shortName for the locale if it is present
	 * @should return the shortest name in a given locale for a concept if exact is true
	 * @should return the shortest name for the concept from any locale if exact is false
	 * @should return null if there are no names in the specified locale and exact is true
	 */
	public ConceptName getShortestName(Locale locale, Boolean exact) {
		if (log.isDebugEnabled()) {
			log.debug("Getting shortest conceptName for locale: " + locale);
		}
		
		ConceptName shortNameInLocale = getShortNameInLocale(locale);
		if (shortNameInLocale != null) {
			return shortNameInLocale;
		}
		
		ConceptName shortestNameForLocale = null;
		ConceptName shortestNameForConcept = null;
		
		if (locale != null) {
			for (ConceptName possibleName : getNames()) {
				if (possibleName.getLocale().equals(locale)
				        && ((shortestNameForLocale == null) || (possibleName.getName().length() < shortestNameForLocale
				                .getName().length()))) {
					shortestNameForLocale = possibleName;
				}
				if ((shortestNameForConcept == null)
				        || (possibleName.getName().length() < shortestNameForConcept.getName().length())) {
					shortestNameForConcept = possibleName;
				}
			}
		}
		
		if (exact) {
			if (shortestNameForLocale == null) {
				log.warn("No short concept name found for concept id " + conceptId + " for locale "
				        + locale.getDisplayName());
			}
			return shortestNameForLocale;
		}
		
		return shortestNameForConcept;
	}
	
	/**
	 * @param name A name
	 * @return whether this concept has the given name in any locale
	 */
	public boolean isNamed(String name) {
		return getNames().stream().anyMatch(cn -> name.equals(cn.getName()));
	}
	
	/**
	 * Gets the list of all non-retired concept names which are index terms for this concept
	 * 
	 * @return a collection of concept names which are index terms for this concept
	 * @since 1.7
	 */
	public Collection<ConceptName> getIndexTerms() {
		return getNames().stream()
				.filter(ConceptName::isIndexTerm)
				.collect(Collectors.toSet());		
	}
	
	/**
	 * Gets the list of all non-retired concept names which are index terms in a given locale
	 * 
	 * @param locale the locale for the index terms to return
	 * @return a collection of concept names which are index terms in the given locale
	 * @since 1.7
	 */
	public Collection<ConceptName> getIndexTermsForLocale(Locale locale) {
		return getIndexTerms().stream()
				.filter(n -> n.getLocale().equals(locale))
		        .collect(Collectors.toList());
	}
	
	/**
	 * @return Returns the names.
	 */
	public Collection<ConceptName> getNames() {
		return getNames(false);
	}
	
	/**
	 * @return Returns the names.
	 * @param includeVoided Include voided ConceptNames if true.
	 */
	public Collection<ConceptName> getNames(boolean includeVoided) {
		if (names == null) {
			names = new HashSet<>();
		}

		return names.stream()
				.filter(n -> includeVoided || !n.getVoided())
				.collect(Collectors.toSet());
	}
	
	/**
	 * @param names The names to set.
	 */
	public void setNames(Collection<ConceptName> names) {
		this.names = names;
	}
	
	/**
	 * Add the given ConceptName to the list of names for this Concept
	 * 
	 * @param conceptName
	 * @should replace the old preferred name with a current one
	 * @should replace the old fully specified name with a current one
	 * @should replace the old short name with a current one
	 * @should mark the first name added as fully specified
	 */
	public void addName(ConceptName conceptName) {
		if (conceptName != null) {
			conceptName.setConcept(this);
			if (names == null) {
				names = new HashSet<>();
			}
			if (!names.contains(conceptName)) {
				if (getNames().isEmpty()
				        && !ConceptNameType.FULLY_SPECIFIED.equals(conceptName.getConceptNameType())) {
					conceptName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
				} else {
					if (conceptName.isPreferred() && !conceptName.isIndexTerm() && conceptName.getLocale() != null) {
						ConceptName prefName = getPreferredName(conceptName.getLocale());
						if (prefName != null) {
							prefName.setLocalePreferred(false);
						}
					}
					if (conceptName.isFullySpecifiedName() && conceptName.getLocale() != null) {
						ConceptName fullySpecName = getFullySpecifiedName(conceptName.getLocale());
						if (fullySpecName != null) {
							fullySpecName.setConceptNameType(null);
						}
					} else if (conceptName.isShort() && conceptName.getLocale() != null) {
						ConceptName shortName = getShortNameInLocale(conceptName.getLocale());
						if (shortName != null) {
							shortName.setConceptNameType(null);
						}
					}
				}
				names.add(conceptName);
				if (compatibleCache != null) {
					compatibleCache.clear(); // clear the locale cache, forcing it to be rebuilt
				}
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
		if (names != null) {
			return names.remove(conceptName);
		} else {
			return false;
		}
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
		
		if (locale == null) {
			locale = LocaleUtility.getDefaultLocale();
		}
		
		Locale desiredLocale = locale;
		
		ConceptDescription defaultDescription = null;
		for (ConceptDescription availableDescription : getDescriptions()) {
			Locale availableLocale = availableDescription.getLocale();
			if (availableLocale.equals(desiredLocale)) {
				foundDescription = availableDescription;
				break; // skip out now because we found an exact locale match
			}
			if (!exact && LocaleUtility.areCompatible(availableLocale, desiredLocale)) {
				foundDescription = availableDescription;
			}
			if (availableLocale.equals(LocaleUtility.getDefaultLocale())) {
				defaultDescription = availableDescription;
			}
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
				if (defaultDescription == null) {
					log.debug("No concept description found for default locale for concept id " + conceptId);
				} else {
					foundDescription = defaultDescription;
				}
			}
		}
		return foundDescription;
	}
	
	/**
	 * @return the retiredBy
	 */
	@Override
	public User getRetiredBy() {
		return retiredBy;
	}
	
	/**
	 * @param retiredBy the retiredBy to set
	 */
	@Override
	public void setRetiredBy(User retiredBy) {
		this.retiredBy = retiredBy;
	}
	
	/**
	 * @return the dateRetired
	 */
	@Override
	public Date getDateRetired() {
		return dateRetired;
	}
	
	/**
	 * @param dateRetired the dateRetired to set
	 */
	@Override
	public void setDateRetired(Date dateRetired) {
		this.dateRetired = dateRetired;
	}
	
	/**
	 * @return the retireReason
	 */
	@Override
	public String getRetireReason() {
		return retireReason;
	}
	
	/**
	 * @param retireReason the retireReason to set
	 */
	@Override
	public void setRetireReason(String retireReason) {
		this.retireReason = retireReason;
	}
	
	/**
	 * @return Returns the descriptions.
	 */
	public Collection<ConceptDescription> getDescriptions() {
		if (descriptions == null) {
			descriptions = new HashSet<>();
		}
		return descriptions;
	}
	
	/**
	 * Sets the collection of descriptions for this Concept.
	 * 
	 * @param descriptions the collection of descriptions
	 */
	public void setDescriptions(Collection<ConceptDescription> descriptions) {
		this.descriptions = descriptions;
	}
	
	/**
	 * Add the given description to the list of descriptions for this Concept
	 * 
	 * @param description the description to add
	 */
	public void addDescription(ConceptDescription description) {
		if (description != null && StringUtils.isNotBlank(description.getDescription()) && !descriptions.contains(description)) {
			description.setConcept(this);
			descriptions.add(description);
		}
	}
	
	/**
	 * Remove the given description from the list of descriptions for this Concept
	 * 
	 * @param description the description to remove
	 * @return true if the entity was removed, false otherwise
	 * @Should should remove description passed from list of descriptions
	 */
	public boolean removeDescription(ConceptDescription description) {
		return descriptions.remove(description);
	}
	
	/**
	 * @return Returns the retired.
	 * 
	 * @deprecated as of 2.0, use {@link #getRetired()}
	 */
	@Override
	@Deprecated
	@JsonIgnore
	public Boolean isRetired() {
		return getRetired();
	}
	
	/**
	 * This method delegates to {@link #isRetired()}. This is only needed for jstl syntax like
	 * ${concept.retired} because the return type is a Boolean object instead of a boolean
	 * primitive type.
	 * 
	 * @see org.openmrs.Retireable#isRetired()
	 */
	@Override
	public Boolean getRetired() {
		return retired;
	}
	
	/**
	 * @param retired The retired to set.
	 */
	@Override
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}
	
	/**
	 * Gets the synonyms in the given locale. Returns a list of names from the same language with
	 * the preferred synonym sorted first, or an empty list if none found.
	 * 
	 * @param locale
	 * @return Collection of ConceptNames which are synonyms for the Concept in the given locale
	 */
	public Collection<ConceptName> getSynonyms(Locale locale) {
		
		List<ConceptName> syns = new ArrayList<>();
		ConceptName preferredConceptName = null;
		for (ConceptName possibleSynonymInLoc : getSynonyms()) {
			if (locale.equals(possibleSynonymInLoc.getLocale())) {
				if (possibleSynonymInLoc.isPreferred()) {
					preferredConceptName = possibleSynonymInLoc;
				} else {
					syns.add(possibleSynonymInLoc);
				}
			}
		}
		
		// Add preferred name first in the list.
		if (preferredConceptName != null) {
			syns.add(0, preferredConceptName);
		}
		log.debug("returning: " + syns);
		return syns;
	}
	
	/**
	 * Gets all the non-retired synonyms.
	 * 
	 * @return Collection of ConceptNames which are synonyms for the Concept or an empty list if
	 *         none is found
	 * @since 1.7
	 */
	public Collection<ConceptName> getSynonyms() {
		return getNames().stream()
				.filter(ConceptName::isSynonym)
				.collect(Collectors.toSet());
	}
	
	/**
	 * @return Returns the version.
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * @param version The version to set.
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	
	/**
	 * @return Returns the conceptSets.
	 */
	public Collection<ConceptSet> getConceptSets() {
		return conceptSets;
	}
	
	/**
	 * @param conceptSets The conceptSets to set.
	 */
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
	public Collection<ConceptMap> getConceptMappings() {
		if (conceptMappings == null) {
			conceptMappings = new HashSet<>();
		}
		return conceptMappings;
	}
	
	/**
	 * @param conceptMappings the conceptMappings to set
	 */
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
		if (newConceptMap != null) {
			newConceptMap.setConcept(this);
		}
		if (newConceptMap != null && !getConceptMappings().contains(newConceptMap)) {
			if (newConceptMap.getConceptMapType() == null) {
				newConceptMap.setConceptMapType(Context.getConceptService().getDefaultConceptMapType());
			}
			getConceptMappings().add(newConceptMap);
		}
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
	 * @Should remove concept map passed from list of mappings 
	 */
	public boolean removeConceptMapping(ConceptMap conceptMap) {
		return getConceptMappings().remove(conceptMap);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Concept #" + conceptId;
	}
	
	/**
	 * @see org.openmrs.Attributable#findPossibleValues(java.lang.String)
	 */
	@Override
	public List<Concept> findPossibleValues(String searchText) {
		List<Concept> concepts = new ArrayList<>();
		try {
			
			for (ConceptSearchResult searchResult : Context.getConceptService().getConcepts(searchText,
			    Collections.singletonList(Context.getLocale()), false, null, null, null, null, null, null, null)) {
				concepts.add(searchResult.getConcept());
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
	@Override
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
	@Override
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
	 * Turns this concept into a very simple serialized string
	 * 
	 * @see org.openmrs.Attributable#serialize()
	 */
	@Override
	public String serialize() {
		if (this.getConceptId() == null) {
			return "";
		}
		
		return "" + this.getConceptId();
	}
	
	/**
	 * @see org.openmrs.Attributable#getDisplayString()
	 */
	@Override
	public String getDisplayString() {
		if (getName() == null) {
			return toString();
		} else {
			return getName().getName();
		}
	}
	
	/**
	 * Convenience method that returns a set of all the locales in which names have been added for
	 * this concept.
	 * 
	 * @return a set of all locales for names for this concept
	 * @since 1.7
	 * @should return all locales for conceptNames for this concept without duplicates
	 */
	public Set<Locale> getAllConceptNameLocales() {
		if (getNames().isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("The Concept with id: " + conceptId + " has no names");
			}
			return null;
		}
		
		Set<Locale> locales = new HashSet<>();
		
		for (ConceptName cn : getNames()) {
			locales.add(cn.getLocale());
		}
		
		return locales;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConceptId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setConceptId(id);
	}
	
	/**
	 * Sort the ConceptSet based on the weight
	 * 
	 * @return sortedConceptSet Collection&lt;ConceptSet&gt;
	 */
	private List<ConceptSet> getSortedConceptSets() {
		List<ConceptSet> cs = new ArrayList<>();
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
	 * @return List&lt;Concept&gt; the Concepts that are members of this Concept's set
	 * @should return concept set members sorted according to the sort weight
	 * @should return all the conceptMembers of current Concept
	 * @should return unmodifiable list of conceptMember list
	 * @should return concept set members sorted with retired last
	 */
	public List<Concept> getSetMembers() {
		List<Concept> conceptMembers = new ArrayList<>();
		
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
	 * Add the concept to the existing member to the list of set members in the given location. <br>
	 * <br>
	 * index of 0 is before the first concept<br>
	 * index of -1 is after last.<br>
	 * index of 1 is after the first but before the second, etc<br>
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
		
		//after sorting, we need to reset the sort weights because retired
		//sets have moved to the bottom and hence need to be reassigned
		//higher sort weights than the non retired ones
		double weight = 990.0;
		for (ConceptSet conceptSet : sortedConceptSets) {
			conceptSet.setSortWeight(weight += 10.0);
		}
		
		if (sortedConceptSets.isEmpty()) {
			weight = 1000.0;
		} else if (index == -1 || index >= setsSize) {
			// deals with list size of 1 and any large index given by dev
			weight = sortedConceptSets.get(setsSize - 1).getSortWeight() + 10.0;
		} else if (index == 0) {
			weight = sortedConceptSets.get(0).getSortWeight() - 10.0;
		} else {
			// put the weight between two
			double prevSortWeight = sortedConceptSets.get(index - 1).getSortWeight();
			double nextSortWeight = sortedConceptSets.get(index).getSortWeight();
			weight = (prevSortWeight + nextSortWeight) / 2;
		}
		
		ConceptSet conceptSet = new ConceptSet(setMember, weight);
		conceptSet.setConceptSet(this);
		conceptSets.add(conceptSet);
	}

	/**
	 * @see org.openmrs.customdatatype.Customizable#getAttributes()
	 */
	@Override
	public Set<ConceptAttribute> getAttributes() {
		if (attributes == null) {
			attributes = new LinkedHashSet<>();
		}
		return attributes;
	}

	/**
	 * @see org.openmrs.customdatatype.Customizable#getActiveAttributes()
	 */
	@Override
	public Collection<ConceptAttribute> getActiveAttributes() {
		return getAttributes().stream()
				.filter(attr -> !attr.getVoided())
				.collect(Collectors.toList());
	}

	/**
	 * @see org.openmrs.customdatatype.Customizable#getActiveAttributes(org.openmrs.customdatatype.CustomValueDescriptor)
	 */
	@Override
	public List<ConceptAttribute> getActiveAttributes(CustomValueDescriptor ofType) {
		return getAttributes().stream()
				.filter(attr -> attr.getAttributeType().equals(ofType) && !attr.getVoided())
				.collect(Collectors.toList());
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(Set<ConceptAttribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @see org.openmrs.customdatatype.Customizable#addAttribute(Attribute)
	 */
	@Override
	public void addAttribute(ConceptAttribute attribute) {
		getAttributes().add(attribute);
		attribute.setOwner(this);
	}

}

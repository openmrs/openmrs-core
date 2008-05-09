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
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Concept 
 */
@Root
public class Concept implements java.io.Serializable, Attributable<Concept> {

	public static final long serialVersionUID = 5733L;
	public Log log = LogFactory.getLog(this.getClass());

	// Fields

	@Attribute(required=true)
	private Integer conceptId;
	private Boolean retired = false;
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
	private Collection<ConceptSynonym> synonyms;
	private Collection<ConceptSet> conceptSets;

	// Constructors

	/** default constructor */
	public Concept() {
		names = new HashSet<ConceptName>();
		answers = new HashSet<ConceptAnswer>();
		synonyms = new HashSet<ConceptSynonym>();
		conceptSets = new HashSet<ConceptSet>();
		//conceptNumeric = new ConceptNumeric();
	}

	/** constructor with id */
	public Concept(Integer conceptId) {
		this.conceptId = conceptId;
	}
	
	// Possibly used for decapitating a ConceptNumeric (to remove the row in concept_numeric)
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
		answers = cn.getAnswers(true);
		synonyms = cn.getSynonyms();
		conceptSets = cn.getConceptSets();
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Concept) {
			Concept c = (Concept)obj;
			return (this.getConceptId().equals(c.getConceptId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getConceptId() == null) return super.hashCode();
		int hash = 8;
		hash = 31 * this.getConceptId() + hash;
		return hash;
	}
	
	/**
	 * @return Returns the non-retired answers.
	 */
	public Collection<ConceptAnswer> getAnswers() {
		Collection<ConceptAnswer> newAnswers = new HashSet<ConceptAnswer>();
		for (ConceptAnswer ca : answers) {
			if (!ca.getAnswerConcept().isRetired())
				newAnswers.add(ca);
		}
		return newAnswers;
	}
	
	public Collection<ConceptAnswer> getSortedAnswers(Locale locale) {
		Vector<ConceptAnswer> sortedAnswers = new Vector<ConceptAnswer>(getAnswers());
		Collections.sort(sortedAnswers, new ConceptAnswerComparator(locale));
		return sortedAnswers;
	}

	/**
	 * @return Returns the answers.
	 */
	public Collection<ConceptAnswer> getAnswers(boolean includeRetired) {
		if (includeRetired == false)
			return getAnswers();
		return answers;
	}
	
	/**
	 * @param answers The answers to set.
	 */
	public void setAnswers(Collection<ConceptAnswer> answers) {
		this.answers = answers;
		/*
		if (answers != null) {
			for (ConceptAnswer ca : answers) {
				ca.setConcept(this);
			}
		}
		*/
	}
	
	/**
	 * Add the given ConceptAnswer to the list of answers for this Concept
	 * @param conceptAnswer
	 */
	public void addAnswer(ConceptAnswer conceptAnswer) {
		conceptAnswer.setConcept(this);
		if (answers == null)
			answers = new HashSet<ConceptAnswer>();
		if (!answers.contains(conceptAnswer) && conceptAnswer != null)
		{
			conceptAnswer.setConcept(this);
			answers.add(conceptAnswer);
		}
	}

	/**
	 * Remove the given answer from the list of answers for this Concept
	 * @param conceptAnswer
	 */
	public void removeAnswer(ConceptAnswer conceptAnswer) {
		if (answers != null)
			answers.remove(conceptAnswer);
	}

	/**
	 * @return Returns the changedBy.
	 */
	public User getChangedBy() {
		return changedBy;
	}

	/**
	 * @param changedBy The changedBy to set.
	 */
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
	 */
	public Boolean isSet() {
		return set;
	}

	/**
	 * @param set whether or not this concept is a set
	 */
	public void setSet(Boolean set) {
		this.set = set;
	}

	public Boolean getSet() {
		return isSet();
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
	 * @return Returns the dateChanged.
	 */
	public Date getDateChanged() {
		return dateChanged;
	}

	/**
	 * @param dateChanged The dateChanged to set.
	 */
	public void setDateChanged(Date dateChanged) {
		this.dateChanged = dateChanged;
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
	 * Finds the name of the concept in the given locale.  Returns null if none found. 
	 * 
	 * @param locale
	 * @return ConceptName attributed to the Concept in the given locale
	 */
	public ConceptName getName(Locale locale) {
		return getName(locale, false);
	}
	
	/**
	 * Finds the name of the concept using the current locale in Context.getLocale().
	 * Returns null if none found. 
	 * 
	 * @param locale
	 * @return ConceptName attributed to the Concept in the given locale
	 */
	public ConceptName getName() {
		return getName(Context.getLocale());
	}
	
	/**
	 * 
	 * @param locale
	 * @param exact true/false to return only exact locale (no default locale)
	 * @return
	 */
	public ConceptName getName(Locale locale, boolean exact) {
		log.debug("Getting conceptName for locale: " + locale);
		
		if (locale == null)
			locale = Locale.US;
		
		String loc = locale.getLanguage();
		if (loc.length() > 2)
			loc = loc.substring(0, 2);
		ConceptName defaultName = null;
		for (Iterator<ConceptName> i = getNames().iterator(); i.hasNext();) {
			ConceptName name = i.next();
			String lang = name.getLocale();
			if (lang.equals(loc))
				return name;
			if (lang.equals("en"))
				defaultName = name;
		}
		
		//no name with the given locale was found.
		// return null if exact match desired
		if (exact) {
			log.warn("No concept name found for concept id " + conceptId + " for locale " + loc);
			return null;
		}
		
		// returning default name locale ("en") if exact match not desired
		if (defaultName == null)
			log.warn("No concept name found for default locale for concept id " + conceptId);
		
		return defaultName;
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
	public Collection<ConceptName> getNames() {
		return names;
	}

	/**
	 * @param names The names to set.
	 */
	public void setNames(Collection<ConceptName> names) {
		this.names = names;
	}

	/**
	 * Add the given ConceptName to the list of names for this Concept
	 * @param conceptName
	 */
	public void addName(ConceptName conceptName) {
		conceptName.setConcept(this);
		if (names == null)
			names = new HashSet<ConceptName>();
		if (!names.contains(conceptName) && conceptName != null)
			names.add(conceptName);
	}

	/**
	 * Remove the given name from the list of names for this Concept
	 * @param conceptName
	 */
	public void removeName(ConceptName conceptName) {
		if (names != null)
			names.remove(conceptName);
	}
	
	/**
	 * @return Returns the retired.
	 */
	public Boolean isRetired() {
		return retired;
	}

	public Boolean getRetired() {
		return isRetired();
	}
	
	/**
	 * @param retired The retired to set.
	 */
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}

	/**
	 * @return Returns the synonyms.
	 */
	public Collection<ConceptSynonym> getSynonyms() {
		return synonyms;
	}
	
	/**
	 * Gets the synonyms the given locale.  Returns empty list if none found. 
	 * 
	 * @param locale
	 * @return Collection of ConceptSynonym attributed to the Concept in the given locale
	 */
	public Collection<ConceptSynonym> getSynonyms(Locale locale) {
		String loc = locale.getLanguage().substring(0, 2);
		Collection<ConceptSynonym> syns = new Vector<ConceptSynonym>();
		for (ConceptSynonym syn : getSynonyms()) {
			String lang = syn.getLocale();
			if (lang == null) lang = "en";
			if (lang.equals(loc))
				syns.add(syn);
		}
		log.debug("returning: " + syns);
		return syns;
	}
	
	/**
	 * @param synonyms The synonyms to set.
	 */
	public void setSynonyms(Collection<ConceptSynonym> synonyms) {
		this.synonyms = synonyms;
	}
	
	public void addSynonym(String s, Locale locale) {
		ConceptSynonym cs = new ConceptSynonym(this, s, locale);
		if (!synonyms.contains(cs)) {
			synonyms.add(cs);
		}
	}
	
	public void addSynonym(ConceptSynonym syn) {
		if (!synonyms.contains(syn)) {
			synonyms.add(syn);
		}
	}

	/**
	 * Add the given ConceptSynonym to the list of synonyms for this Concept
	 * @param conceptSynonym
	 */
	/*
	public void addSynonym(ConceptSynonym conceptSynonym) {
		conceptSynonym.setConcept(this);
		if (synonyms == null)
			synonyms = new HashSet<ConceptSynonym>();
		if (!synonyms.contains(conceptSynonym) && conceptSynonym != null)
			synonyms.add(conceptSynonym);
	}*/

	/**
	 * Remove the given synonym from the list of synonyms for this Concept
	 * @param conceptSynonym
	 */
	public void removeSynonym(ConceptSynonym conceptSynonym) {
		if (synonyms != null)
			synonyms.remove(conceptSynonym);
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

	/*
	public ConceptNumeric getConceptNumeric() {
		return conceptNumeric;
	}

	public void setConceptNumeric(ConceptNumeric conceptNumeric) {
		this.conceptNumeric = conceptNumeric;
	}
	*/
	
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

	public boolean isNumeric() {
		return false;
		/*
		if (this.getDatatype() != null) {
			return this.getDatatype().getName().equals("Numeric");
		}
		return false;
		*/
	}
	
	public String toString() {
		if (conceptId == null)
			return "";
		return conceptId.toString();
	}

	/**
	 * Internal class used to sort ConceptAnswer lists. We sort answers
	 * by the concept name, which requires the locale to be specified.
	 */
	private class ConceptAnswerComparator implements Comparator<ConceptAnswer> {
		Locale locale;
		ConceptAnswerComparator(Locale locale) {
			this.locale = locale;
		}
		public int compare(ConceptAnswer a1, ConceptAnswer a2) {
			String n1 = a1.getConcept().getName(locale).getName();
			String n2 = a2.getConcept().getName(locale).getName();
			int c = n1.compareTo(n2);
			if (c == 0)
				c = a1.getConcept().getConceptId().compareTo(a2.getConcept().getConceptId());
			return c;
		}
	}

	
	public List<Concept> findPossibleValues(String searchText) {
		List<Concept> concepts = new Vector<Concept>();
		try {
			for (ConceptWord word : Context.getConceptService().findConcepts(searchText, Context.getLocale(), false)) {
				concepts.add(word.getConcept());
			}
		}
		catch (Exception e) {
			// pass
		}
		return concepts;
	}
	

	public List<Concept> getPossibleValues() {
		try {
			return Context.getConceptService().getConceptsByName("");
		}
		catch (Exception e) {
			// pass
		}
		return Collections.emptyList();
	}
	

	public Concept hydrate(String s) {
		try {
			return Context.getConceptService().getConcept(Integer.valueOf(s));
		}
		catch (Exception e) {
			// pass
		}
		return null;
	}
	

	public String serialize() {
		return "" + this.getConceptId();
	}

	
}
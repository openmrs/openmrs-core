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

import java.util.Date;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * ConceptSynonym 
 */
public class ConceptSynonym implements java.io.Serializable {

	public static final long serialVersionUID = 3785L;
	
	public Log log = LogFactory.getLog(this.getClass());

	// Fields

	private Concept concept;
	private String synonym;
	private Locale locale;
	private User creator;
	private Date dateCreated;

	// Constructors

	/** default constructor */
	public ConceptSynonym() {
	}
	
	public ConceptSynonym(Concept c, String s, Locale l) {
		setConcept(c);
		setSynonym(s);
		setLocale(l);
	}
	
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj instanceof ConceptSynonym) {
			ConceptSynonym c = (ConceptSynonym) obj;
			boolean ret = true;
			if (concept != null && c.getConcept() != null)
				ret = ret && this.concept.equals(c.getConcept());
			if (synonym != null && c.getSynonym() != null)
				ret = ret && this.synonym.equals(c.getSynonym());
			if (locale != null && c.getLocale() != null)
				ret = ret && locale.equals(c.getLocale());
			return ret;
		}

		return false;
	}
	
	public int hashCode() {
		if (this.getConcept() == null && this.getSynonym() == null && this.getLocale() == null) return super.hashCode();
		int hash = 2;
		if (getConcept() != null)
			hash = 31 * hash + getConcept().hashCode();
		if (getSynonym() != null)
			hash = 31 * hash + getSynonym().hashCode();
		if (getLocale() != null)
			hash = 31 * hash + getLocale().hashCode();
		
		return hash;
	}

	// Property accessors

	/**
	 * 
	 */
	public Concept getConcept() {
		return concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
		//id.setConcept(concept);
	}

	/**
	 * 
	 */
	public String getSynonym() {
		return synonym;
	}

	public void setSynonym(String synonym) {
		this.synonym = synonym;
		//id.setSynonym(synonym);
	}

	/**
	 * @return Returns the locale.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale The locale to set.
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
		//id.setLocale(locale);
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            The creator to set.
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
	 * @param dateCreated
	 *            The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public String toString() {
		return synonym;
	}

	/*
	public Integer getConceptSynonymId() {
		return conceptSynonymId;
	}

	public void setConceptSynonymId(Integer conceptSynonymId) {
		this.conceptSynonymId = conceptSynonymId;
	}
	*/

	/*
	/**
	 * @return Returns the id.
	 /
	public ConceptSynonymId getId() {
		return id;
	}

	/**
	 * @param id The id to set.
	 /
	public void setId(ConceptSynonymId id) {
		this.id = id;
	}
	*/
}
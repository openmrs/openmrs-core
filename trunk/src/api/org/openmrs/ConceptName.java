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

/**
 * ConceptName
 * 
 */
public class ConceptName implements java.io.Serializable {

	public static final long serialVersionUID = 33226787L;

	// Fields

	private Integer conceptNameId;
	private Concept concept;
	private String name;
	private String shortName;
	private String description;
	private Locale locale;
	private User creator;
	private Date dateCreated;

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
	
	/**
	 * Convenience constructor taking in the required properties
	 * 
	 * @param name
	 * @param shortName
	 * @param description
	 * @param locale
	 */
	public ConceptName(String name, String shortName, String description, Locale locale) {
		setName(name);
		setShortName(shortName);
		setLocale(locale);
		setDescription(description);  
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ConceptName) {
			ConceptName c = (ConceptName) obj;
			return (this.concept.equals(c.getConcept())
					&& this.name.equals(c.getName()) && this.locale.equals(c
					.getLocale()));
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getConcept() == null || this.getName() == null
				|| this.getLocale() == null)
			return super.hashCode();
		int hash = 3;
		hash = hash + 31 * this.getConcept().hashCode();
		hash = hash + 31 * this.getName().hashCode();
		hash = hash + 31 * this.getLocale().hashCode();
		return hash;
	}
	
	/**
	 * Auto generated method comment
	 * 
	 * @return
	 */
	public String getShortestName() {
		String ret = getShortName();
		if (ret == null || ret.length() == 0)
			ret = getName();
		if (ret == null)
			ret = getDescription();
		return ret;
	}

	/**
     * @return the conceptNameId
     */
    public Integer getConceptNameId() {
    	return conceptNameId;
    }

	/**
     * @param conceptNameId the conceptNameId to set
     */
    public void setConceptNameId(Integer conceptNameId) {
    	this.conceptNameId = conceptNameId;
    }
	
	/**
	 * 
	 */
	public Concept getConcept() {
		return concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * 
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 */
	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return Returns the shortName.
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * @param shortName
	 *            The shortName to set.
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
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

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.name;
	}
}
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

/**
 * ConceptDatatype
 */
public class ConceptDatatype implements java.io.Serializable {

	public static final long serialVersionUID = 473L;

	// HL7 abbreviations (along with our own boolean creature)

	public static final String BOOLEAN = "BIT";
	public static final String CODED = "CWE";
	public static final String DATE = "DT";
	public static final String DATETIME = "TS";
	public static final String DOCUMENT = "RP";
	public static final String NUMERIC = "NM";
	public static final String TEXT = "ST";
	public static final String TIME = "TM";

	// Fields

	private Integer conceptDatatypeId;
	private String name;
	private String description;
	private String hl7Abbreviation;
	private Date dateCreated;
	private User creator;

	// Constructors

	/** default constructor */
	public ConceptDatatype() {
	}

	/** constructor with id */
	public ConceptDatatype(Integer conceptDatatypeId) {
		this.conceptDatatypeId = conceptDatatypeId;
	}

	public boolean equals(Object obj) {
		if (obj instanceof ConceptDatatype) {
			ConceptDatatype c = (ConceptDatatype) obj;
			return (this.conceptDatatypeId.equals(c.getConceptDatatypeId()));
		}
		return false;
	}

	public int hashCode() {
		if (this.getConceptDatatypeId() == null)
			return super.hashCode();
		return this.getConceptDatatypeId().hashCode();
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getConceptDatatypeId() {
		return this.conceptDatatypeId;
	}

	public void setConceptDatatypeId(Integer conceptDatatypeId) {
		this.conceptDatatypeId = conceptDatatypeId;
	}

	/**
	 * 
	 */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 */
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the hl7Abbreviation.
	 */
	public String getHl7Abbreviation() {
		return hl7Abbreviation;
	}

	/**
	 * @param hl7Abbreviation
	 *            The hl7Abbreviation to set.
	 */
	public void setHl7Abbreviation(String hl7Abbreviation) {
		this.hl7Abbreviation = hl7Abbreviation;
	}

	/**
	 * 
	 */
	public User getCreator() {
		return this.creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * 
	 */
	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/*
	 * Convenience methods for resolving common data types
	 */

	/**
	 * @return <code>true</code> if datatype is a numeric datatype
	 */
	public boolean isNumeric() {
		return NUMERIC.equals(getHl7Abbreviation());
	}

	/**
	 * @return <code>true</code> if datatype is coded (i.e., an identifier
	 *         from a vocabulary)
	 */
	public boolean isCoded() {
		return CODED.equals(getHl7Abbreviation());
	}

	/**
	 * @return <code>true</code> if datatype is some representation of date or
	 *         time
	 */
	public boolean isDate() {
		return DATE.equals(getHl7Abbreviation())
				|| DATETIME.equals(getHl7Abbreviation())
				|| TIME.equals(getHl7Abbreviation());
	}

	/**
	 * @return <code>true</code> if datatype is text-based
	 */
	public boolean isText() {
		return TEXT.equals(getHl7Abbreviation())
				|| DOCUMENT.equals(getHl7Abbreviation());
	}
	
	/**
	 * @return <code>true</code> if datatype is boolean
	 */
	public boolean isBoolean() {
		return BOOLEAN.equals(getHl7Abbreviation());
	}

}
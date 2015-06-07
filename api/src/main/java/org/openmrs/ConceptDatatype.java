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

import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Indexed;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * ConceptDatatype
 */
@Root
public class ConceptDatatype extends BaseOpenmrsMetadata implements java.io.Serializable {
	
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
	
	// UUIDs for core datatypes
	
	public static final String NUMERIC_UUID = "8d4a4488-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String CODED_UUID = "8d4a48b6-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String TEXT_UUID = "8d4a4ab4-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String N_A_UUID = "8d4a4c94-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String DOCUMENT_UUID = "8d4a4e74-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String DATE_UUID = "8d4a505e-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String TIME_UUID = "8d4a591e-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String DATETIME_UUID = "8d4a5af4-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String BOOLEAN_UUID = "8d4a5cca-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String RULE_UUID = "8d4a5e96-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String STRUCTURED_NUMERIC_UUID = "8d4a606c-c2cc-11de-8d13-0010c6dffd0f";
	
	public static final String COMPLEX_UUID = "8d4a6242-c2cc-11de-8d13-0010c6dffd0f";
	
	// Fields
	@DocumentId
	private Integer conceptDatatypeId;
	
	private String hl7Abbreviation;
	
	// Constructors
	
	/** default constructor */
	public ConceptDatatype() {
	}
	
	/** constructor with id */
	public ConceptDatatype(Integer conceptDatatypeId) {
		this.conceptDatatypeId = conceptDatatypeId;
	}
	
	// Property accessors
	
	/**
	 * 
	 */
	@Attribute
	public Integer getConceptDatatypeId() {
		return this.conceptDatatypeId;
	}
	
	@Attribute
	public void setConceptDatatypeId(Integer conceptDatatypeId) {
		this.conceptDatatypeId = conceptDatatypeId;
	}
	
	/**
	 * @return Returns the hl7Abbreviation.
	 */
	@Attribute
	public String getHl7Abbreviation() {
		return hl7Abbreviation;
	}
	
	/**
	 * @param hl7Abbreviation The hl7Abbreviation to set.
	 */
	@Attribute
	public void setHl7Abbreviation(String hl7Abbreviation) {
		this.hl7Abbreviation = hl7Abbreviation;
	}
	
	/*
	 * Convenience methods for resolving common data types
	 */

	/**
	 * @return <code>true</code> if datatype is N/A, i.e. this concept is only an answer, not a
	 *         question
	 */
	public boolean isAnswerOnly() {
		return N_A_UUID.equals(getUuid());
	}
	
	/**
	 * @return <code>true</code> if datatype is a numeric datatype
	 */
	public boolean isNumeric() {
		return NUMERIC_UUID.equals(getUuid());
	}
	
	/**
	 * @return <code>true</code> if datatype is coded (i.e., an identifier from a vocabulary)
	 */
	public boolean isCoded() {
		return CODED_UUID.equals(getUuid());
	}
	
	/**
	 * @return <code>true</code> if datatype is representation of date (but NOT a time or
	 *         datatime--see containsDate() and containsTime())
	 */
	public boolean isDate() {
		return DATE_UUID.equals(getUuid());
	}
	
	/**
	 * @return <code>true</code> if datatype is representation of time
	 * @since 1.7
	 */
	public boolean isTime() {
		return TIME_UUID.equals(getUuid());
	}
	
	/**
	 * @return <code>true</code> if datatype is representation of Datetime
	 * @since 1.7
	 */
	public boolean isDateTime() {
		return DATETIME_UUID.equals(getUuid());
	}
	
	/**
	 * @return <code>true</code> if datatype is representation of either date or Datetime
	 * @since 1.7
	 */
	public boolean containsDate() {
		return DATE_UUID.equals(getUuid()) || DATETIME_UUID.equals(getUuid());
	}
	
	/**
	 * @return <code>true</code> if datatype is representation of either time or Datetime
	 * @since 1.7
	 */
	public boolean containsTime() {
		return TIME_UUID.equals(getUuid()) || DATETIME_UUID.equals(getUuid());
	}
	
	/**
	 * @return <code>true</code> if datatype is text-based
	 */
	public boolean isText() {
		return TEXT_UUID.equals(getUuid()) || DOCUMENT_UUID.equals(getUuid());
	}
	
	/**
	 * @return <code>true</code> if datatype is boolean
	 */
	public boolean isBoolean() {
		return BOOLEAN_UUID.equals(getUuid());
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		
		return getConceptDatatypeId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setConceptDatatypeId(id);
		
	}
	
	/**
	 * @return <code>true</code> if datatype is complex
	 * @since 1.7
	 */
	public boolean isComplex() {
		return COMPLEX_UUID.equals(getUuid());
	}
	
	/**
	 * @return <code>true</code> if datatype is a rule
	 * @since 1.7
	 */
	public boolean isRule() {
		return RULE_UUID.equals(getUuid());
	}
	
}

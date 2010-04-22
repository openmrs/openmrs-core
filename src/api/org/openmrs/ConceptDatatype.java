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
	 */
	
	public boolean isTime() {
		return TIME_UUID.equals(getUuid());
	}
	
	/**
	 * @return <code>true</code> if datatype is representation of Datetime
	 */
	
	public boolean isDateTime() {
		return DATETIME_UUID.equals(getUuid());
	}
	
	/**
	 * @return <code>true</code> if datatype is representation of either date or Datetime
	 */
	
	public boolean containsDate() {
		return DATE_UUID.equals(getUuid()) || DATETIME_UUID.equals(getUuid());
	}
	
	/**
	 * @return <code>true</code> if datatype is representation of either time or Datetime
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
	
}

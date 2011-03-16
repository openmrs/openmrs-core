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
package org.openmrs.util;

import java.util.Hashtable;

import org.openmrs.hl7.HL7Constants;

/**
 * Constants relating to forms
 * 
 * @see org.openmrs.Form
 * @see org.openmrs.FormField
 * @see org.openmrs.Field
 * @see org.openmrs.FieldType
 * @see org.openmrs.FieldAnswer
 */
public class FormConstants {
	
	public static final Integer FIELD_TYPE_CONCEPT = 1;
	
	public static final Integer FIELD_TYPE_DATABASE = 2;
	
	public static final Integer FIELD_TYPE_TERM_SET = 3;
	
	public static final Integer FIELD_TYPE_MISC_SET = 4;
	
	public static final Integer FIELD_TYPE_SECTION = 5;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class
	 */
	@Deprecated
	public static final String HL7_TEXT = HL7Constants.HL7_TEXT;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class
	 */
	@Deprecated
	public static final String HL7_CODED = HL7Constants.HL7_CODED;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class
	 */
	@Deprecated
	public static final String HL7_CODED_WITH_EXCEPTIONS = HL7Constants.HL7_CODED_WITH_EXCEPTIONS;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class
	 */
	@Deprecated
	public static final String HL7_NUMERIC = HL7Constants.HL7_NUMERIC;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class
	 */
	@Deprecated
	public static final String HL7_DATE = HL7Constants.HL7_DATE;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class
	 */
	@Deprecated
	public static final String HL7_TIME = HL7Constants.HL7_TIME;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class
	 */
	@Deprecated
	public static final String HL7_DATETIME = HL7Constants.HL7_DATETIME;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class
	 */
	@Deprecated
	public static final String HL7_BOOLEAN = HL7Constants.HL7_BOOLEAN;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class
	 */
	@Deprecated
	public static final String HL7_AUTHORITY_UUID = HL7Constants.HL7_AUTHORITY_UUID;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class
	 */
	@Deprecated
	public static final String HL7_AUTHORITY_LOCAL = HL7Constants.HL7_AUTHORITY_LOCAL;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class
	 */
	@Deprecated
	public static final Object HL7_ID_PERSON = HL7Constants.HL7_ID_PERSON;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class
	 */
	@Deprecated
	public static final Object HL7_ID_PATIENT = HL7Constants.HL7_ID_PATIENT;
	
	@Deprecated
	public static final Integer CLASS_DRUG = HL7Constants.CLASS_DRUG;
	
	/**
	 * Used in hl7 sextuplets: 123^Primary name^99DCT^345^Chosen name^99NAM
	 */
	@Deprecated
	public static final String HL7_LOCAL_CONCEPT = HL7Constants.HL7_LOCAL_CONCEPT;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class
	 */
	@Deprecated
	public static final String HL7_LOCAL_CONCEPT_NAME = HL7Constants.HL7_LOCAL_CONCEPT_NAME;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class
	 */
	@Deprecated
	public static final String HL7_LOCAL_DRUG = HL7Constants.HL7_LOCAL_DRUG;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class
	 */
	@Deprecated
	public static final String HL7_LOCAL_RELATIONSHIP = HL7Constants.HL7_LOCAL_RELATIONSHIP;
	
	/**
	 * @deprecated Moved the constant to HL7Constants class, List of datatypes that do not require
	 *             complex definitions
	 */
	@Deprecated
	public static final Hashtable<String, String> simpleDatatypes = HL7Constants.simpleDatatypes;
	
	public static final int INDENT_SIZE = 2;
	
	// form resource constants for metadata moving to separate tables
	
	public static final String FORM_RESOURCE_FORMENTRY_OWNER = "formentry";
	
	public static final String FORM_RESOURCE_FORMENTRY_XSLT = "xslt";
	
	public static final String FORM_RESOURCE_FORMENTRY_TEMPLATE = "template";
	
}

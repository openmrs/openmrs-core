/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
	
}

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

	public static final String HL7_TEXT = "ST";
	public static final String HL7_CODED = "CE";
	public static final String HL7_CODED_WITH_EXCEPTIONS = "CWE";
	public static final String HL7_NUMERIC = "NM";
	public static final String HL7_DATE = "DT";
	public static final String HL7_TIME = "TM";
	public static final String HL7_DATETIME = "TS";
	public static final String HL7_BOOLEAN = "BIT";

	public static final Integer CLASS_DRUG = 3;

	public static final String HL7_LOCAL_CONCEPT = "99DCT";
	public static final String HL7_LOCAL_DRUG = "99RX";

	// List of datatypes that do not require complex definitions
	public static final Hashtable<String, String> simpleDatatypes = new Hashtable<String, String>();
	static {
		simpleDatatypes.put(HL7_TEXT, "xs:string");
		simpleDatatypes.put(HL7_DATE, "xs:date");
		simpleDatatypes.put(HL7_TIME, "xs:time");
		simpleDatatypes.put(HL7_DATETIME, "xs:dateTime");

		// We make a special boolean type with an extra attribute
		// to get InfoPath to treat booleans properly
		simpleDatatypes.put(HL7_BOOLEAN, "_infopath_boolean");
	}

	public static final int INDENT_SIZE = 2;
	
}

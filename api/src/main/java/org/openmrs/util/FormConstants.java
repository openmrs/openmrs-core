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
	
	private FormConstants() {
	}
	
	public static final Integer FIELD_TYPE_CONCEPT = 1;
	
	public static final Integer FIELD_TYPE_DATABASE = 2;
	
	public static final Integer FIELD_TYPE_TERM_SET = 3;
	
	public static final Integer FIELD_TYPE_MISC_SET = 4;
	
	public static final Integer FIELD_TYPE_SECTION = 5;
	
	public static final int INDENT_SIZE = 2;
	
}

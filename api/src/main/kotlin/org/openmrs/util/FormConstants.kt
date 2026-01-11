/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util

/**
 * Constants relating to forms
 * 
 * @see org.openmrs.Form
 * @see org.openmrs.FormField
 * @see org.openmrs.Field
 * @see org.openmrs.FieldType
 * @see org.openmrs.FieldAnswer
 */
object FormConstants {
	
	const val FIELD_TYPE_CONCEPT: Int = 1
	
	const val FIELD_TYPE_DATABASE: Int = 2
	
	const val FIELD_TYPE_TERM_SET: Int = 3
	
	const val FIELD_TYPE_MISC_SET: Int = 4
	
	const val FIELD_TYPE_SECTION: Int = 5
	
	const val INDENT_SIZE: Int = 2
}

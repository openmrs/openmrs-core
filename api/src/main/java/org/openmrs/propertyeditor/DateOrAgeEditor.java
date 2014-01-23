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
package org.openmrs.propertyeditor;

import java.text.ParseException;

import org.openmrs.util.OpenmrsUtil;

/**
 * Allows a java.util.Date to be converted to/from a String. It tries both Date and Age formats
 */
public class DateOrAgeEditor extends DateOrDatetimeEditor {
	
	/**
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	@Override
	public void setAsText(String asString) throws IllegalArgumentException {
		try {
			// first try date
			super.setAsText(asString);
		}
		catch (IllegalArgumentException dateTimeEx) {
			// next try age
			try {
				setValue(OpenmrsUtil.getBirthDateFromAge(asString));
			}
			catch (ParseException ageEx) {
				// those were the only two options, so we fail
				throw new IllegalArgumentException(dateTimeEx);
			}
		}
	}
}

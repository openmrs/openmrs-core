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

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openmrs.api.context.Context;

/**
 * Allows a java.util.Date to be converted to/from a String. It tries both Date and Date+Time formats
 * but it does not permit partial dates. 
 */
public class DateOrDatetimeEditor extends PropertyEditorSupport {
	
	/**
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 * @should handle date
	 * @should handle date and time
	 * @should fail on partial date
	 * @should fail on partial date and time
	 */
	@Override
	public void setAsText(String asString) throws IllegalArgumentException {
		if (asString == null || asString.equals("")) {
			setValue(null);
			return;
		}
		try {
			// first try date+time
			setValue(Context.getDateTimeFormat().parse(asString));
		}
		catch (ParseException dateTimeEx) {
			// next try just date
			try {
				setValue(Context.getDateFormat().parse(asString));
			}
			catch (ParseException dateEx) {
				// those were the only two options, so we fail
				throw new IllegalArgumentException(dateTimeEx);
			}
		}
	}
	
	/**
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 * @should print date without time
	 * @should print date and time with time
	 */
	@Override
	public String getAsText() {
		Date date = (Date) getValue();
		if (date == null) {
			return "";
		}
		if ("0000".equals(new SimpleDateFormat("HmsS").format(date))) {
			return Context.getDateFormat().format(date);
		} else {
			return Context.getDateTimeFormat().format(date);
		}
	}
	
}

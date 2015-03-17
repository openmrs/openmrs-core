/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
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
		if (StringUtils.isEmpty(asString)) {
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

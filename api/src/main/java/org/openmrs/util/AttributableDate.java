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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Attributable;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

/**
 * This class is a stand in for using "java.util.Date" as a PersonAttribute format. This will allow
 * the end user to store a date as YYYY-MM-DD instead of storing it as a string in the date format
 * of whatever user created the string
 *
 * @see java.util.Date
 * @see org.openmrs.PersonAttribute
 * @see org.openmrs.Attributable
 */
public class AttributableDate extends Date implements Attributable<AttributableDate> {
	
	private static final long serialVersionUID = 4280303636131451746L;
	
	private static final String dateFormat = "yyyy-MM-dd";
	
	/**
	 * Default empty constructor
	 *
	 * @see java.util.Date#Date()
	 */
	public AttributableDate() {
		super();
	}
	
	/**
	 * Convenience constructor allowing creation of an AttributableDate with the given time
	 *
	 * @param time
	 * @see java.util.Date#Date(long)
	 */
	public AttributableDate(long time) {
		super(time);
	}
	
	/**
	 * @see org.openmrs.Attributable#findPossibleValues(java.lang.String)
	 */
	@Override
	public List<AttributableDate> findPossibleValues(String searchText) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @see org.openmrs.Attributable#getDisplayString()
	 */
	@Override
	public String getDisplayString() {
		return new SimpleDateFormat(dateFormat).format(this);
	}
	
	/**
	 * @see org.openmrs.Attributable#getPossibleValues()
	 */
	@Override
	public List<AttributableDate> getPossibleValues() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @see org.openmrs.Attributable#hydrate(java.lang.String)
	 */
	@Override
	public AttributableDate hydrate(String s) {
		// don't do anything to empty dates
		if (StringUtils.isEmpty(s)) {
			return null;
		}
		
		try {
			// try to parse as the current user (
			return new AttributableDate(((Date) Context.getDateFormat().parseObject(s)).getTime());
		}
		catch (ParseException e) {
			try {
				return new AttributableDate(((Date) new SimpleDateFormat(dateFormat).parseObject(s)).getTime());
			}
			catch (ParseException e2) {
				// if we can't parse it as the normalized string or as the current
				// user's date format, bail out
				throw new APIException("unable.parse.string.as.date", new Object[] { s });
			}
		}
	}
	
	/**
	 * @see org.openmrs.Attributable#serialize()
	 */
	@Override
	public String serialize() {
		return new SimpleDateFormat(dateFormat).format(this);
	}
	
}

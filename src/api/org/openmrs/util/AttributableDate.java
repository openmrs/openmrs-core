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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.openmrs.Attributable;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

/**
 * This class is a stand in for using "java.util.Date" as a PersonAttribute format.
 * 
 * This will allow the end user to store a date as YYYY-MM-DD instead of storing it as
 * a string in the date format of whatever user created the string
 * 
 * @see java.util.Date
 * @see org.openmrs.PersonAttribute
 * @see org.openmrs.Attributable
 */
public class AttributableDate extends Date implements Attributable<AttributableDate> {

    private static final long serialVersionUID = 4280303636131451746L;
    private final String dateFormat = "yyyy-MM-dd";

    /**
     * Default empty constructor
     * 
     * @see java.util.Date#Date()
     */
    public AttributableDate() {
    	super();
    }
    
    /**
     * Convenience constructor allowing creation of an AttributableDate with
     * the given time
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
	public List<AttributableDate> findPossibleValues(String searchText) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.openmrs.Attributable#getDisplayString()
	 */
	public String getDisplayString() {
		return new SimpleDateFormat(dateFormat).format(this);
	}

	/**
	 * @see org.openmrs.Attributable#getPossibleValues()
	 */
	public List<AttributableDate> getPossibleValues() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.openmrs.Attributable#hydrate(java.lang.String)
	 */
	public AttributableDate hydrate(String s) {
		try {
			// try to parse as the current user (
			return new AttributableDate(((Date)Context.getDateFormat().parseObject(s)).getTime());
		}
		catch (ParseException e) {
			try {
				return new AttributableDate(((Date)new SimpleDateFormat(dateFormat).parseObject(s)).getTime());
			}
			catch (ParseException e2) {
				// if we can't parse it as the normalized string or as the current
				// user's date format, bail out
				throw new APIException("Unable to parse the given string: '" + s + "' as a date object");
				
				// returning null causes the field to be blanked out
				//return null;
			}
		}
	}

	/**
	 * @see org.openmrs.Attributable#serialize()
	 */
	public String serialize() {
		return new SimpleDateFormat(dateFormat).format(this);
	}
	
	public String toString() {
		System.out.println("toStringTest");
		return super.toString();
	}
}

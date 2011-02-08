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
package org.openmrs.validator;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonAddress;
import org.openmrs.annotation.Handler;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * This class validates a PersonAddress object.
 * 
 * @since 1.9
 */
@Handler(supports = { PersonAddress.class }, order = 50)
public class PersonAddressValidator implements Validator {
	
	private static Log log = LogFactory.getLog(PersonAddressValidator.class);
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class c) {
		return PersonAddress.class.isAssignableFrom(c);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should pass if all the dates are valid
	 * @should fail if the startDate is in the future
	 * @should fail if the endDate is in the future
	 * @should fail if the endDate is before the startDate
	 * @should pass if startDate and endDate are both null
	 * @should pass if startDate is null
	 * @should pass if endDate is null
	 */
	public void validate(Object object, Errors errors) {
		//TODO Validate other aspects of the personAddress object
		if (log.isDebugEnabled())
			log.debug(this.getClass().getName() + ".validate...");
		
		if (object == null)
			throw new IllegalArgumentException("The personAddress object should not be null");
		
		PersonAddress personAddress = (PersonAddress) object;
		
		//resolve a shorter name to display along with the error message
		String addressString = null;
		if (StringUtils.isNotBlank(personAddress.getAddress1()))
			addressString = personAddress.getAddress1();
		else if (StringUtils.isNotBlank(personAddress.getAddress2()))
			addressString = personAddress.getAddress2();
		else if (StringUtils.isNotBlank(personAddress.getCityVillage()))
			addressString = personAddress.getCityVillage();
		else
			addressString = personAddress.toString();
		
		if (OpenmrsUtil.compareWithNullAsEarliest(personAddress.getStartDate(), new Date()) > 0)
			errors.rejectValue("startDate", "PersonAddress.error.startDateInFuture", new Object[] { "'" + addressString
			        + "'" }, "The Start Date for address '" + addressString + "' shouldn't be in the future");
		if (OpenmrsUtil.compareWithNullAsEarliest(personAddress.getEndDate(), new Date()) > 0)
			errors.rejectValue("endDate", "PersonAddress.error.endDateInFuture", new Object[] { "'" + addressString + "'" },
			    "The EndDate for address '" + addressString + "' shouldn't be in the future");
		
		if (personAddress.getStartDate() != null
		        && OpenmrsUtil.compareWithNullAsLatest(personAddress.getStartDate(), personAddress.getEndDate()) > 0)
			errors.rejectValue("endDate", "PersonAddress.error.endDateBeforeStartDate", new Object[] { "'" + addressString
			        + "'" }, "The End Date for address '" + addressString + "' shouldn't be earlier than the Start Date");
		
	}
}

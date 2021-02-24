/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.PersonAddress;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.layout.address.AddressTemplate;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * This class validates a PersonAddress object.
 *
 * @since 1.9
 */
@Handler(supports = { PersonAddress.class }, order = 50)
public class PersonAddressValidator implements Validator {
	
	private static final Logger log = LoggerFactory.getLogger(PersonAddressValidator.class);
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> c) {
		return PersonAddress.class.isAssignableFrom(c);
	}
	
	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> pass if all the dates are valid
	 * <strong>Should</strong> fail if the startDate is in the future
	 * <strong>Should</strong> fail if the endDate is before the startDate
	 * <strong>Should</strong> pass if startDate and endDate are both null
	 * <strong>Should</strong> pass if startDate is null
	 * <strong>Should</strong> pass if endDate is null
	 * <strong>Should</strong> fail if required fields are empty
	 * <strong>Should</strong> pass if required fields are not empty
	 * <strong>Should</strong> pass validation if field lengths are correct
	 * <strong>Should</strong> fail validation if field lengths are not correct
	 */
	@Override
	public void validate(Object object, Errors errors) {
		//TODO Validate other aspects of the personAddress object
		log.debug("{}.validate...", this.getClass().getName());
		
		if (object == null) {
			throw new IllegalArgumentException("The personAddress object should not be null");
		}
		
		PersonAddress personAddress = (PersonAddress) object;
		
		//resolve a shorter name to display along with the error message
		String addressString;
		if (StringUtils.isNotBlank(personAddress.getAddress1())) {
			addressString = personAddress.getAddress1();
		} else if (StringUtils.isNotBlank(personAddress.getAddress2())) {
			addressString = personAddress.getAddress2();
		} else if (StringUtils.isNotBlank(personAddress.getCityVillage())) {
			addressString = personAddress.getCityVillage();
		} else {
			addressString = personAddress.toString();
		}
		
		if (OpenmrsUtil.compareWithNullAsEarliest(personAddress.getStartDate(), new Date()) > 0) {
			errors.rejectValue("startDate", "PersonAddress.error.startDateInFuture", new Object[] { "'" + addressString
			        + "'" }, "The Start Date for address '" + addressString + "' shouldn't be in the future");
		}
		
		if (personAddress.getStartDate() != null
		        && OpenmrsUtil.compareWithNullAsLatest(personAddress.getStartDate(), personAddress.getEndDate()) > 0) {
			errors.rejectValue("endDate", "PersonAddress.error.endDateBeforeStartDate", new Object[] { "'" + addressString
			        + "'" }, "The End Date for address '" + addressString + "' shouldn't be earlier than the Start Date");
		}
		
		String xml = Context.getLocationService().getAddressTemplate();
		List<String> requiredElements;
		
		try {
			AddressTemplate addressTemplate = Context.getSerializationService().getDefaultSerializer().deserialize(xml,
			    AddressTemplate.class);
			requiredElements = addressTemplate.getRequiredElements();
		}
		catch (Exception e) {
			errors.reject(Context.getMessageSourceService().getMessage("AddressTemplate.error"));
			return;
		}
		
		if (requiredElements != null) {
			for (String fieldName : requiredElements) {
				try {
					Object value = PropertyUtils.getProperty(personAddress, fieldName);
					if (StringUtils.isBlank((String) value)) {
						//required field not found
						errors.reject(Context.getMessageSourceService().getMessage(
						    "AddressTemplate.error.requiredAddressFieldIsBlank", new Object[] { fieldName },
						    Context.getLocale()));
					}
				}
				catch (Exception e) {
					//wrong field declared in template
					errors
					        .reject(Context.getMessageSourceService().getMessage(
					            "AddressTemplate.error.fieldNotDeclaredInTemplate", new Object[] { fieldName },
					            Context.getLocale()));
				}
			}
		}
		
		ValidateUtil.validateFieldLengths(errors, object.getClass(), "address1", "address2", "cityVillage", "stateProvince",
		    "postalCode", "country", "latitude", "longitude", "voidReason", "countyDistrict", "address3", "address4",
		    "address5", "address6", "address7", "address8", "address9", "address10", "address11", "address12", "address13", 
		    "address14", "address15");
	}
}

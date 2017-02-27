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

import java.util.Collection;

import org.openmrs.api.APIException;
import org.openmrs.attribute.Attribute;
import org.openmrs.attribute.AttributeType;
import org.openmrs.customdatatype.Customizable;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * This abstract class provides utilities for validators for Customizable subclasses.
 * @since 1.9
 */
public abstract class BaseCustomizableValidator implements Validator {
	
	/**
	 * Validate the attributes of the given Customizable, given the list of relevant attribute types
	 */
	@SuppressWarnings("rawtypes")
	public <T extends AttributeType, A extends Attribute> void validateAttributes(Customizable<A> customizable,
	        Errors errors, Collection<T> attributeTypes) {
		
		// check to make sure that the target has the right number of each type of attribute
		for (T at : attributeTypes) {
			if ((at.getMinOccurs() > 0 || at.getMaxOccurs() != null) && !at.getRetired()) {
				int numFound = 0;
				for (A attr : customizable.getActiveAttributes()) {
					if (attr.getAttributeType().equals(at)) {
						++numFound;
					}
				}
				if (at.getMinOccurs() > 0 && numFound < at.getMinOccurs()) {
					// report an error
					if (at.getMinOccurs() == 1) {
						errors.rejectValue("activeAttributes", "error.required", new Object[] { at.getName() }, null);
					} else {
						errors.rejectValue("activeAttributes", "attribute.error.minOccurs", new Object[] { at.getName(),
						        at.getMinOccurs() }, null);
					}
				}
				if (at.getMaxOccurs() != null && numFound > at.getMaxOccurs()) {
					errors.rejectValue("activeAttributes", "attribute.error.maxOccurs", new Object[] { at.getName(),
					        at.getMaxOccurs() }, null);
				}
			}
		}
		
		// validate all non-voided attributes for their values (we already checked minOccurs and maxOccurs for the types)
		boolean errorsInAttributes = false;
		for (Attribute attr : customizable.getActiveAttributes()) {
			try {
				ValidateUtil.validate(attr);
			}
			catch (APIException ex) {
				errorsInAttributes = true;
				break;
			}
		}
		if (errorsInAttributes) {
			errors.rejectValue("attributes", "Customizable.error.inAttributes");
		}
	}
	
}

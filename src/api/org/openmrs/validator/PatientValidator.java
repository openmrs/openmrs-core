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

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * This class validates a Patient object.  
 * TODO: Eventually, all Patient validation should be done through this class.  I.e.
 * it should not be done in classes like newPatientFormController.
 */
public class PatientValidator implements Validator {

	/**
	 * Returns whether or not this validator supports validating a given class.
	 * @param c The class to check for support.
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return Patient.class.isAssignableFrom(c);
	}

	/**
	 * Validates the given Patient.  Currently just checks for errors in identifiers.
	 * TODO: Check for errors in all Patient fields.
	 * @param obj The patient to validate.
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		Patient patient = (Patient) obj;
		
		// Validate PatientIdentifers
		PatientIdentifierValidator piv = new PatientIdentifierValidator();
		if (patient != null && patient.getIdentifiers() != null) {
			for (PatientIdentifier identifier : patient.getIdentifiers()) {
				piv.validate(identifier, errors);
			}
		}
	}
}

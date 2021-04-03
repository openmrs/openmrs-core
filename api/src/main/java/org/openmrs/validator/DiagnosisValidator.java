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

import org.openmrs.Diagnosis;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates {@link Validator} objects
 * This class ensures that the condition object is valid and properly structured
 * 
 * @since 2.2
 */
@Handler(supports = {Diagnosis.class}, order = 50)
public class DiagnosisValidator implements Validator {

	/**
	 * <strong>Should</strong> support Diagnosis class
	 */
	@Override
	public boolean supports(Class<?> aClass) {
		return Diagnosis.class.isAssignableFrom(aClass);
	}

	/**
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * <strong>Should</strong> fail validation if rank is null or a non-positive integer
	 * <strong>Should</strong> fail validation if certainty is null
	 * <strong>Should</strong> fail validation if diagnosis is null
	 * <strong>Should</strong> fail validation if encounter is null
	 */
	@Override
	public void validate(Object o, Errors errors) {
		Diagnosis diagnosis = (Diagnosis)o;
		
		if(diagnosis == null){
			throw new APIException("Diagnosis can't be null");
		} else if (diagnosis.getVoided()) {
			return;
		}
		
		if  (diagnosis.getEncounter() == null){
			errors.rejectValue("encounter", "error.null");
		}
		
		if (diagnosis.getDiagnosis() == null){
			errors.rejectValue("diagnosis", "error.null");
		}
		
		if (diagnosis.getCertainty() == null){
			errors.rejectValue("certainty", "error.null");
		}
		
		Integer rank = diagnosis.getRank();
		if (rank == null){
			errors.rejectValue("rank", "error.null");
		}else if(rank < 0){
			errors.rejectValue("rank", "error.rank.notPositiveInteger");
		}
	}
}

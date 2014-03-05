package org.openmrs.validator;

import java.util.Date;

import org.openmrs.Concept;
import org.openmrs.Relationship;
import org.openmrs.annotation.Handler;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validator for the Relationship class. This class checks for date set on the Relationship object that will cause
 * errors or is incorrect. Things checked are similar to:
 * <ul>
 * <li>checks for valid relationship dates</li>
 *
 * @see org.openmrs.Validator
 */

@Handler(supports = { Relationship.class }, order = 50)
public class RelationshipValidator implements Validator {
	
	/**
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean supports(Class c) {
		return Relationship.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks that a given Relationship object is valid.
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 *     
	 * @should fail if start date is after end date
	 * @param Relationship
	 * @param errors    
	 *     
	 *      
	 **/
	@Override
	public void validate(Object target, Errors errors) {
		Relationship reldates = (Relationship) target;
		
		if (reldates != null) {
			Date startDate = reldates.getStartDate();
			Date endDate = reldates.getEndDate();
			if (startDate != null && endDate != null) {
				if (startDate.after(endDate)) {
					errors.reject("Relationship.InvalidDate.error");
					
				}
			}
		}
		
	}
	
}

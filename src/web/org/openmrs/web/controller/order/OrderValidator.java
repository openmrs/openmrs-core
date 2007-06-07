package org.openmrs.web.controller.order;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Order;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class OrderValidator implements Validator {

	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * 
	 * Determines if the command object being submitted is a valid type
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	public boolean supports(Class c) {
		return c.equals(Order.class);
	}

	/**
	 * 
	 * Checks the form object for any inconsistencies/errors
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	public void validate(Object obj, Errors errors) {
		Order order = (Order)obj;
		if (order == null) {
			errors.rejectValue("order", "error.general");
		}
		else {
			//ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "error.name");
			//ValidationUtils.rejectIfEmptyOrWhitespace(errors, "description", "error.description");
		}
		//log.debug("errors: " + errors.getAllErrors().toString());
	}

}
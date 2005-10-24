package org.openmrs.web.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.OrderType;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class OrderTypeValidator implements Validator {

	/** Log for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());

	public boolean supports(Class c) {
		return c.equals(OrderType.class);
	}

	public void validate(Object obj, Errors errors) {
		OrderType orderType = (OrderType)obj;
		if (orderType == null) {
			errors.rejectValue("orderType", "error.general", null, "Value required.");
		}
		else {
			if (orderType.getName() == null || orderType.getName().equals("")) {
				errors.rejectValue("name", "error.general.name");
			}
			if (orderType.getDescription() == null || orderType.getDescription().equals("")) {
				errors.rejectValue("description", "error.general.description");
			}
		}
		//log.debug("errors: " + errors.getAllErrors().toString());
	}

}
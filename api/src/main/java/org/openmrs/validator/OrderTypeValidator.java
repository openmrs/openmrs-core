package org.openmrs.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.openmrs.OrderType;

/**
 * Validates the {@link OrderType} class.
 *
 * @since 1.10
 */
@Handler(supports = { OrderType.class })
public class OrderTypeValidator implements Validator {
	
	// Log for this class
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Determines if the command object being submitted is a valid type
	 *
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public boolean supports(Class c) {
		return OrderType.class.isAssignableFrom(c);
	}
	
	/**
	 * Checks theobject for any inconsistencies/errors
	 *
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 *      org.springframework.validation.Errors)
	 * @should fail if the orderType object is null
	 * @should fail if name is null
	 * @should fail if name is empty
	 * @should fail if name is whitespace
	 * @should fail if name is a duplicate
	 * @should pass if all fields are correct
	 */
	public void validate(Object obj, Errors errors) {
		if (obj == null || !(obj instanceof OrderType)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type" + OrderType.class);
		} else {
			OrderType orderType = (OrderType) obj;
			
			String name = orderType.getName();
			if (!StringUtils.hasText(name)) {
				errors.rejectValue("name", "error.name");
			}
			
			OrderType ot = Context.getOrderService().getOrderTypeByName(name);
			if (ot != null && !OpenmrsUtil.nullSafeEquals(ot.getId(), orderType.getId())) {
				errors.rejectValue("name", "OrderType.duplicate.name", "Duplicate order type name: " + name);
			}
		}
	}
}

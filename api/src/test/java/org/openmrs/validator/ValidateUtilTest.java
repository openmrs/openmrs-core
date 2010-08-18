package org.openmrs.validator;

import java.util.Collections;

import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.test.Verifies;
import org.springframework.validation.Validator;

/**
 * Tests methods on the {@link ValidateUtil} class.
 */
public class ValidateUtilTest {
	
	/**
	 * @see {@link ValidateUtil#validate(Object)}
	 */
	@Test(expected = APIException.class)
	@Verifies(value = "should throw APIException if errors occur during validation", method = "validate(Object)")
	public void validate_shouldThrowAPIExceptionIfErrorsOccurDuringValidation() throws Exception {
		ValidateUtil util = new ValidateUtil();
		util.setValidators(Collections.singletonList((Validator) new LocationValidator()));
		
		Location loc = new Location();
		ValidateUtil.validate(loc);
	}
}

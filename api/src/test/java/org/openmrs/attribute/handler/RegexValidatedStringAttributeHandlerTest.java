package org.openmrs.attribute.handler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.attribute.InvalidAttributeValueException;

public class RegexValidatedStringAttributeHandlerTest {
	
	RegexValidatedStringAttributeHandler handler;
	
	@Before
	public void before() {
		// accept only strings of length >= 1, all-lowercase
		handler = new RegexValidatedStringAttributeHandler();
		handler.setConfiguration("[a-z]+");
	}
	
	/**
	 * @see RegexValidatedStringAttributeHandler#validate(String)
	 * @verifies accept a string that matches the regex
	 */
	@Test
	public void validate_shouldAcceptAStringThatMatchesTheRegex() throws Exception {
		handler.validate("thisisgood");
		Assert.assertTrue(true); // failure would be an exception in the line above
	}
	
	/**
	 * @see RegexValidatedStringAttributeHandler#validate(String)
	 * @verifies fail if the string does not match the regex
	 */
	@Test(expected = InvalidAttributeValueException.class)
	public void validate_shouldFailIfTheStringDoesNotMatchTheRegex() throws Exception {
		handler.validate("spaces not allowed");
	}
	
	/**
	 * @see RegexValidatedStringAttributeHandler#serialize(String)
	 * @verifies fail if the string does not match the regex
	 */
	@Test(expected = InvalidAttributeValueException.class)
	public void serialize_shouldFailIfTheStringDoesNotMatchTheRegex() throws Exception {
		handler.serialize("spaces not allowed");
	}
	
}

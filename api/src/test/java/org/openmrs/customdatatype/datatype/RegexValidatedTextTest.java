package org.openmrs.customdatatype.datatype;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.customdatatype.InvalidCustomValueException;

public class RegexValidatedTextTest {
	
	RegexValidatedText datatype;
	
	@Before
	public void before() {
		datatype = new RegexValidatedText();
		datatype.setConfiguration("[a-z]+");
	}
	
	/**
	 * @see RegexValidatedText#validate(String)
	 * @verifies accept a string that matches the regex
	 */
	@Test
	public void validate_shouldAcceptAStringThatMatchesTheRegex() throws Exception {
		datatype.validate("thisisgood");
	}
	
	/**
	 * @see RegexValidatedText#validate(String)
	 * @verifies fail if the string does not match the regex
	 */
	@Test(expected = InvalidCustomValueException.class)
	public void validate_shouldFailIfTheStringDoesNotMatchTheRegex() throws Exception {
		datatype.validate("spaces not allowed");
	}
	
	/**
	 * @see RegexValidatedText#toReferenceString(String)
	 * @verifies fail if the string does not match the regex
	 */
	@Test(expected = InvalidCustomValueException.class)
	public void toPersistentString_shouldFailIfTheStringDoesNotMatchTheRegex() throws Exception {
		datatype.toReferenceString("spaces not allowed");
	}
}

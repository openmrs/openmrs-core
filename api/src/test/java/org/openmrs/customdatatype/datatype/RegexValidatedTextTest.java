package org.openmrs.customdatatype.datatype;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.customdatatype.InvalidCustomValueException;

public class RegexValidatedTextTest {
	
	RegexValidatedTextDatatype datatype;
	
	@Before
	public void before() {
		datatype = new RegexValidatedTextDatatype();
		datatype.setConfiguration("[a-z]+");
	}
	
	/**
	 * @see RegexValidatedTextDatatype#validate(String)
	 * @verifies accept a string that matches the regex
	 */
	@Test
	public void validate_shouldAcceptAStringThatMatchesTheRegex() throws Exception {
		datatype.validate("thisisgood");
	}
	
	/**
	 * @see RegexValidatedTextDatatype#validate(String)
	 * @verifies fail if the string does not match the regex
	 */
	@Test(expected = InvalidCustomValueException.class)
	public void validate_shouldFailIfTheStringDoesNotMatchTheRegex() throws Exception {
		datatype.validate("spaces not allowed");
	}
	
	/**
	 * @see RegexValidatedTextDatatype#save(String, String))
	 * @verifies fail if the string does not match the regex
	 */
	@Test(expected = InvalidCustomValueException.class)
	public void toPersistentString_shouldFailIfTheStringDoesNotMatchTheRegex() throws Exception {
		datatype.save("spaces not allowed", null);
	}
}

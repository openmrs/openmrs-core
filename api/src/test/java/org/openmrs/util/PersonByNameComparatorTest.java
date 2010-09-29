package org.openmrs.util;


import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.test.Verifies;

public class PersonByNameComparatorTest {
	/**
	 * @see {@link PersonByNameComparator#compare(Person,Person)}
	 * 
	 */
	@Test
	@Verifies(value = "should return negative if personName for person1 comes before that of person2", method = "compare(Person,Person)")
	public void compare_shouldReturnNegativeIfPersonNameForPerson1ComesBeforeThatOfPerson2()
			throws Exception {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleName", "familyName"));		
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleNamf", "familyName"));
		int actualValue = new PersonByNameComparator().compare(person1, person2);
		Assert.assertTrue("Expected a negative value but it was: "+actualValue, actualValue < 0);
	}

	/**
	 * @see {@link PersonByNameComparator#compare(Person,Person)}
	 * 
	 */
	@Test
	@Verifies(value = "should return positive if personName for person1 comes after that of person2", method = "compare(Person,Person)")
	public void compare_shouldReturnPositiveIfPersonNameForPerson1ComesAfterThatOfPerson2()
			throws Exception {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleNamf", "familyName"));		
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleName", "familyName"));
		int actualValue = new PersonByNameComparator().compare(person1, person2);
		Assert.assertTrue("Expected a positive value but it was: "+actualValue, actualValue > 0);
	}

	/**
	 * @see {@link PersonByNameComparator#compare(Person,Person)}
	 * 
	 */
	@Test
	@Verifies(value = "should return zero if the givenName middleName and familyName match", method = "compare(Person,Person)")
	public void compare_shouldReturnZeroIfTheGivenNameMiddleNameAndFamilyNameMatch()
			throws Exception {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleName", "familyName"));		
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleName", "familyName"));
		int actualValue = new PersonByNameComparator().compare(person1, person2);
		Assert.assertTrue("Expected zero but it was: "+actualValue, actualValue == 0);
	}
}
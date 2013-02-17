package org.openmrs.util;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;

public class ProviderByPersonNameComparatorTest {
	
	/**
	 * @see {@link PersonByNameComparator#comparePersonsByName(org.openmrs.Person, org.openmrs.Person)}
	 */
	@Test
	public void compareProvidersByPersonsName_shouldReturnNegativeIfPersonNameForProvider1ComesBeforeThatOfProvider2()
	        throws Exception {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleName", "familyName"));
		Provider provider1 = new Provider();
		provider1.setPerson(person1);
		
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleNamf", "familyName"));
		Provider provider2 = new Provider();
		provider2.setPerson(person2);
		
		int actualValue = new ProviderByPersonNameComparator().compare(provider1, provider2);
		Assert.assertTrue("Expected a negative value but it was: " + actualValue, actualValue < 0);
	}
	
	/**
	 * @see {@link PersonByNameComparator#comparePersonsByName(Person,Person)}
	 */
	@Test
	public void compareProvidersByPersonName_shouldReturnPositiveIfPersonNameForProvider1ComesAfterThatOfProvider2()
	        throws Exception {
		Person person1 = new Person();
		person1.addName(new PersonName("givenNamf", "middleName", "familyName"));
		Provider provider1 = new Provider();
		provider1.setPerson(person1);
		
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleName", "familyName"));
		Provider provider2 = new Provider();
		provider2.setPerson(person2);
		
		int actualValue = new ProviderByPersonNameComparator().compare(provider1, provider2);
		Assert.assertTrue("Expected a positive value but it was: " + actualValue, actualValue > 0);
	}
	
	/**
	 * @see {@link PersonByNameComparator#comparePersonsByName(Person,Person)}
	 */
	@Test
	public void compareProvidersByPersonName_shouldReturnZeroIfTheGivenNameMiddleNameAndFamilyNameMatch() throws Exception {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleName", "familyName"));
		Provider provider1 = new Provider();
		provider1.setPerson(person1);
		
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleName", "familyName"));
		Provider provider2 = new Provider();
		provider2.setPerson(person2);
		
		int actualValue = new ProviderByPersonNameComparator().compare(provider1, provider2);
		Assert.assertTrue("Expected zero but it was: " + actualValue, actualValue == 0);
	}
	
	@Test
	public void compareProvidersByPersonName_shouldNotFailIfProvider1HasNoAssociatedPerson() throws Exception {
		Provider provider1 = new Provider();
		
		Person person2 = new Person();
		person2.addName(new PersonName("givenName", "middleName", "familyName"));
		Provider provider2 = new Provider();
		provider2.setPerson(person2);
		
		int actualValue = new ProviderByPersonNameComparator().compare(provider1, provider2);
		Assert.assertTrue("Expected a positive value but it was: " + actualValue, actualValue > 0);
		;
	}
	
	@Test
	public void compareProvidersByPersonName_shouldNotFailIfProvider2HasNoAssociatedPerson() throws Exception {
		Person person1 = new Person();
		person1.addName(new PersonName("givenName", "middleName", "familyName"));
		Provider provider1 = new Provider();
		provider1.setPerson(person1);
		
		Provider provider2 = new Provider();
		
		int actualValue = new ProviderByPersonNameComparator().compare(provider1, provider2);
		Assert.assertTrue("Expected a negative value but it was: " + actualValue, actualValue < 0);
	}
	
	@Test
	public void compareProvidersByPersonName_shouldNotFailIfNeitherProviderHasAnAssociatedPerson() throws Exception {
		
		Provider provider1 = new Provider();
		Provider provider2 = new Provider();
		
		int actualValue = new ProviderByPersonNameComparator().compare(provider1, provider2);
		Assert.assertTrue("Expected zero but it was: " + actualValue, actualValue == 0);
	}
}

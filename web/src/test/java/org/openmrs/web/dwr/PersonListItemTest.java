/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.web.dwr;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

import java.util.Map;

/**
 * Tests the {@link PersonListItem} class.
 */
public class PersonListItemTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @see {@link PersonListItem#PersonListItem(Person, String)}
	 */
	@Test
	@Verifies(value = "should identify best matching name for the family name", method = "PersonListItem(Person, String)")
	public void PersonListItem_shouldIdentifyBestMatchingNameForTheFamilyName() throws Exception {
		
		PersonListItem listItem = new PersonListItem(Context.getPersonService().getPerson(2), "hornblower3");
		Assert.assertEquals("Hornblower3", listItem.getFamilyName());
		Assert.assertEquals("John", listItem.getGivenName());
		Assert.assertEquals("Peeter", listItem.getMiddleName());
	}
	
	/**
	 * @see {@link PersonListItem#PersonListItem(Person, String)}
	 */
	@Test
	@Verifies(value = "identify best matching name as preferred name even if other names match", method = "PersonListItem(Person, String)")
	public void PersonListItem_shouldIdentifyBestMatchingNameForTheGivenPreferredNameEvenIfOtherNamesMatch()
	        throws Exception {
		
		PersonListItem listItem = new PersonListItem(Context.getPersonService().getPerson(2), "Horatio");
		Assert.assertEquals("Hornblower", listItem.getFamilyName());
		Assert.assertEquals("Horatio", listItem.getGivenName());
		Assert.assertEquals("Test", listItem.getMiddleName());
	}
	
	/**
	 * @see {@link PersonListItem#PersonListItem(Person, String)}
	 */
	@Test
	@Verifies(value = "identify best matching name as other name for the middle name", method = "PersonListItem(Person, String)")
	public void PersonListItem_shouldIdentifyBestMatchingNameAsOtherNameForTheMiddleName() throws Exception {
		
		PersonListItem listItem = new PersonListItem(Context.getPersonService().getPerson(2), "Peeter");
		Assert.assertEquals("Hornblower2", listItem.getFamilyName());
		Assert.assertEquals("Horatio", listItem.getGivenName());
		Assert.assertEquals("Peeter", listItem.getMiddleName());
	}
	
	/**
	 * @see {@link PersonListItem#PersonListItem(Person, String)}
	 */
	@Test
	@Verifies(value = "identify best matching name as other name for the given name", method = "PersonListItem(Person, String)")
	public void PersonListItem_shouldIdentifyBestMatchingNameAsOtherNameForTheGivenName() throws Exception {
		
		PersonListItem listItem = new PersonListItem(Context.getPersonService().getPerson(2), "joh");
		Assert.assertEquals("Hornblower3", listItem.getFamilyName());
		Assert.assertEquals("John", listItem.getGivenName());
		Assert.assertEquals("Peeter", listItem.getMiddleName());
	}
	
	/**
	 * @see {@link PersonListItem#PersonListItem(Person, String)}
	 */
	@Test
	@Verifies(value = "identify best matching name in multiple search names", method = "PersonListItem(Person, String)")
	public void PersonListItem_shouldIdentifyBestMatchingNameInMultipleSearchNames() throws Exception {
		
		PersonListItem listItem = new PersonListItem(Context.getPersonService().getPerson(2), "Horn peet john");
		Assert.assertEquals("Hornblower3", listItem.getFamilyName());
		Assert.assertEquals("John", listItem.getGivenName());
		Assert.assertEquals("Peeter", listItem.getMiddleName());
	}
	
	/**
	 * @see {@link PersonListItem#PersonListItem(Person)}
	 */
	@Test
	@Verifies(value = "should put attribute toString value into attributes map", method = "PersonListItem(Person)")
	public void PersonListItem_shouldPutAttributeToStringValueIntoAttributesMap() throws Exception {
		PersonListItem listItem = PersonListItem.createBestMatch(Context.getPersonService().getPerson(2));
		
		for (Map.Entry<String, String> entry : listItem.getAttributes().entrySet()) {
			if (entry.getKey().equals("Civil Status")) {
				Assert.assertEquals("MARRIED", entry.getValue()); // should be string not conceptId
				return; // quit after we test the first one
			}
		}
		
		// make sure we found at least one attr
		Assert.fail("No civil status person attribute was defined");
	}
	
	/**
	 * @see {@link PersonListItem#createBestMatch(Person)}
	 */
	@Test
	@Verifies(value = "should return PatientListItem given patient parameter", method = "createBestMatch(Person)")
	@SuppressWarnings("unused")
	public void createBestMatch_shouldReturnPatientListItemGivenPatientParameter() throws Exception {
		PatientListItem listItem = (PatientListItem) PersonListItem.createBestMatch(Context.getPersonService().getPerson(2));
	}
	
	/**
	 * @see {@link PersonListItem#createBestMatch(Person)}
	 */
	@Test
	@Verifies(value = "should return PersonListItem given person parameter", method = "createBestMatch(Person)")
	public void createBestMatch_shouldReturnPersonListItemGivenPersonParameter() throws Exception {
		PersonListItem listItem = PersonListItem.createBestMatch(Context.getPersonService().getPerson(2));
		Assert.assertTrue(listItem instanceof PersonListItem);
	}
	
}

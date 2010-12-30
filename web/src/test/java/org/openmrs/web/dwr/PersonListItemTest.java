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

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;

/**
 * Tests the {@link PersonListItem} class.
 */
public class PersonListItemTest extends BaseWebContextSensitiveTest {
	
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

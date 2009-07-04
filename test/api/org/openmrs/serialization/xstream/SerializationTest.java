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
package org.openmrs.serialization.xstream;

import static org.junit.Assert.assertEquals;

import java.util.Set;
import org.junit.Test;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

public class SerializationTest extends BaseContextSensitiveTest {
	
	@Test
	public void shouldReturnSameClassName(){
		User admin = Context.getAuthenticatedUser();
		Set<PersonName> set = admin.getNames();
		/*
		 * You can see the "standardTestDataSet.xml" file, admin's personName only have one record
		 * and that recode's person_name_id is "9348".
		 * 
		 * Besides, both "person_id" and "creator" of that record are equal.
		 */
		for(PersonName pn : set){
			String classNameOfCreator = pn.getCreator().getClass().getName();
			String classNameOfPerson = pn.getPerson().getClass().getName();
			System.out.println(classNameOfCreator);
			System.out.println(classNameOfPerson);
			assertEquals(classNameOfCreator, classNameOfPerson);
		}
	}
}

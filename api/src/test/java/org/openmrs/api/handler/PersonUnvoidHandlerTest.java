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
package org.openmrs.api.handler;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.test.Verifies;

/**
 * Tests the {@link PersonUnvoidHandler} class.
 */
public class PersonUnvoidHandlerTest {
	
	/**
	 * @see {@link PersonUnvoidHandler#handle(Person,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should unset the personVoided bit", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldUnsetThePersonVoidedBit() throws Exception {
		UnvoidHandler<Person> handler = new PersonUnvoidHandler();
		Person person = new Person();
		person.setPersonVoided(true); // make sure isPersonVoided is set
		handler.handle(person, null, null, null);
		Assert.assertFalse(person.isPersonVoided());
	}
	
	/**
	 * @see {@link PersonUnvoidHandler#handle(Person,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should unset the personVoider", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldUnsetThePersonVoider() throws Exception {
		UnvoidHandler<Person> handler = new PersonUnvoidHandler();
		Person person = new Person();
		person.setPersonVoided(true);
		person.setPersonVoidedBy(new User(1));
		handler.handle(person, null, null, null);
		Assert.assertNull(person.getPersonVoidedBy());
	}
	
	/**
	 * @see {@link PersonUnvoidHandler#handle(Person,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should unset the personDateVoided", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldUnsetThePersonDateVoided() throws Exception {
		UnvoidHandler<Person> handler = new PersonUnvoidHandler();
		Person person = new Person();
		person.setPersonVoided(true);
		person.setPersonDateVoided(new Date());
		handler.handle(person, null, null, null);
		Assert.assertNull(person.getPersonDateVoided());
	}
	
	/**
	 * @see {@link PersonUnvoidHandler#handle(Person,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should unset the PersonVoidReason", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldUnsetThePersonVoidReason() throws Exception {
		UnvoidHandler<Person> handler = new PersonUnvoidHandler();
		Person person = new Person();
		person.setPersonVoided(true);
		person.setPersonVoidReason("SOME REASON");
		handler.handle(person, null, null, null);
		Assert.assertNull(person.getPersonVoidReason());
	}
	
	/**
	 * @see {@link PersonUnvoidHandler#handle(Person,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should only act on already personVoided objects", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldOnlyActOnAlreadyVoidedObjects() throws Exception {
		UnvoidHandler<Person> handler = new PersonUnvoidHandler();
		Person person = new Person();
		person.setPersonVoided(false);
		handler.handle(person, null, null, "SOME REASON");
		Assert.assertNull(person.getPersonVoidReason());
	}
	
	/**
	 * @see {@link PersonUnvoidHandler#handle(Person,User,Date,String)}
	 */
	@Test
	@Verifies(value = "should not act on objects with a different personPersonDateVoided", method = "handle(Voidable,User,Date,String)")
	public void handle_shouldNotActOnObjectsWithADifferentPersonDateVoided() throws Exception {
		Date d = new Date(new Date().getTime() - 1000); // a time that isn't right now
		
		UnvoidHandler<Person> handler = new PersonUnvoidHandler();
		Person person = new Person();
		person.setPersonVoided(true);
		person.setPersonDateVoided(d);
		
		handler.handle(person, null, new Date(), "SOME REASON");
		Assert.assertTrue(person.isPersonVoided());
	}
}

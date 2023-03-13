/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.handler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.User;

/**
 * Tests the {@link PersonUnvoidHandler} class.
 */
public class PersonUnvoidHandlerTest {
	
	/**
	 * @see PersonUnvoidHandler#handle(Person,User,Date,String)
	 */
	@Test
	public void handle_shouldUnsetThePersonVoidedBit() {
		UnvoidHandler<Person> handler = new PersonUnvoidHandler();
		Person person = new Person();
		person.setPersonVoided(true); // make sure personVoided is set
		handler.handle(person, null, null, null);
		assertFalse(person.getPersonVoided());
	}
	
	/**
	 * @see PersonUnvoidHandler#handle(Person,User,Date,String)
	 */
	@Test
	public void handle_shouldUnsetThePersonVoider() {
		UnvoidHandler<Person> handler = new PersonUnvoidHandler();
		Person person = new Person();
		person.setPersonVoided(true);
		person.setPersonVoidedBy(new User(1));
		handler.handle(person, null, null, null);
		assertNull(person.getPersonVoidedBy());
	}
	
	/**
	 * @see PersonUnvoidHandler#handle(Person,User,Date,String)
	 */
	@Test
	public void handle_shouldUnsetThePersonDateVoided() {
		UnvoidHandler<Person> handler = new PersonUnvoidHandler();
		Person person = new Person();
		person.setPersonVoided(true);
		person.setPersonDateVoided(new Date());
		handler.handle(person, null, null, null);
		assertNull(person.getPersonDateVoided());
	}
	
	/**
	 * @see PersonUnvoidHandler#handle(Person,User,Date,String)
	 */
	@Test
	public void handle_shouldUnsetThePersonVoidReason() {
		UnvoidHandler<Person> handler = new PersonUnvoidHandler();
		Person person = new Person();
		person.setPersonVoided(true);
		person.setPersonVoidReason("SOME REASON");
		handler.handle(person, null, null, null);
		assertNull(person.getPersonVoidReason());
	}
	
	/**
	 * @see PersonUnvoidHandler#handle(Person,User,Date,String)
	 */
	@Test
	public void handle_shouldOnlyActOnAlreadyVoidedObjects() {
		UnvoidHandler<Person> handler = new PersonUnvoidHandler();
		Person person = new Person();
		person.setPersonVoided(false);
		handler.handle(person, null, null, "SOME REASON");
		assertNull(person.getPersonVoidReason());
	}
	
	/**
	 * @see PersonUnvoidHandler#handle(Person,User,Date,String)
	 */
	@Test
	public void handle_shouldNotActOnObjectsWithADifferentPersonDateVoided() {
		Date d = new Date(new Date().getTime() - 1000); // a time that isn't right now
		
		UnvoidHandler<Person> handler = new PersonUnvoidHandler();
		Person person = new Person();
		person.setPersonVoided(true);
		person.setPersonDateVoided(d);
		
		handler.handle(person, null, new Date(), "SOME REASON");
		assertTrue(person.getPersonVoided());
	}
}

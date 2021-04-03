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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmrs.api.context.Context.getUserService;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Tests for the {@link PersonVoidHandler} class.
 */
public class PersonVoidHandlerTest extends BaseContextSensitiveTest {
	
	/**
	 * @see PersonVoidHandler#handle(Person,User,Date,String)
	 */
	@Test
	public void handle_shouldSetThePersonVoidedBit() {
		VoidHandler<Person> handler = new PersonVoidHandler();
		Person person = new Person();
		person.setPersonVoided(false); // make sure personVoided is false
		handler.handle(person, null, null, " ");
		assertTrue(person.getPersonVoided());
	}
	
	/**
	 * @see PersonVoidHandler#handle(Person,User,Date,String)
	 */
	@Test
	public void handle_shouldSetThePersonVoidReason() {
		VoidHandler<Person> handler = new PersonVoidHandler();
		Person person = new Person();
		handler.handle(person, null, null, "THE REASON");
		assertEquals("THE REASON", person.getPersonVoidReason());
	}
	
	/**
	 * @see PersonVoidHandler#handle(Person,User,Date,String)
	 */
	@Test
	public void handle_shouldSetPersonVoidedBy() {
		VoidHandler<Person> handler = new PersonVoidHandler();
		Person person = new Person();
		handler.handle(person, new User(2), null, " ");
		assertEquals(2, person.getPersonVoidedBy().getId().intValue());
	}
	
	/**
	 * @see PersonVoidHandler#handle(Person,User,Date,String)
	 */
	@Test
	public void handle_shouldNotSetPersonVoidedByIfNonNull() {
		VoidHandler<Person> handler = new PersonVoidHandler();
		Person person = new Person();
		person.setPersonVoidedBy(new User(3));
		handler.handle(person, new User(2), null, " ");
		assertEquals(3, person.getPersonVoidedBy().getId().intValue());
	}
	
	/**
	 * @see PersonVoidHandler#handle(Person,User,Date,String)
	 */
	@Test
	public void handle_shouldSetPersonDateVoided() {
		Date d = new Date();
		
		VoidHandler<Person> handler = new PersonVoidHandler();
		Person person = new Person();
		handler.handle(person, null, d, " ");
		assertEquals(d, person.getPersonDateVoided());
	}
	
	/**
	 * @see PersonVoidHandler#handle(Person,User,Date,String)
	 */
	@Test
	public void handle_shouldNotSetPersonDateVoidedIfNonNull() {
		Date d = new Date(new Date().getTime() - 1000); // a time that is not "now"
		
		VoidHandler<Person> handler = new PersonVoidHandler();
		Person person = new Person();
		person.setPersonDateVoided(d); // make personDateVoided non null
		
		handler.handle(person, null, new Date(), " ");
		assertEquals(d, person.getPersonDateVoided());
	}
	
	/**
	 * @see PersonVoidHandler#handle(Person,User,Date,String)
	 */
	@Test
	public void handle_shouldNotSetThePersonVoidReasonIfAlreadyPersonVoided() {
		VoidHandler<Person> handler = new PersonVoidHandler();
		Person person = new Person();
		person.setPersonVoided(true);
		
		handler.handle(person, null, null, "THE REASON");
		assertNull(person.getPersonVoidReason());
	}
	
	/**
	 * @see PersonVoidHandler#handle(Person,User,Date,String)
	 */
	@Test
	public void handle_shouldRetireUsers() {
		//given
		VoidHandler<Person> handler = new PersonVoidHandler();
		Person person = Context.getPersonService().getPerson(2);
		User user = new User(person);
		Context.getUserService().createUser(user, "Admin123");
		assertFalse(Context.getUserService().getUsersByPerson(person, false).isEmpty());
		
		//when
		handler.handle(person, null, null, "reason");
		
		//then
		assertThat(getUserService().getUsersByPerson(person, false), is(empty()));
	}
	
}

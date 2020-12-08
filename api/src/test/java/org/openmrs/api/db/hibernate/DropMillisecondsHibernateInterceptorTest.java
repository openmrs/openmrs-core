/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.sql.Time;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * We can't easily test the full behavior against different database versions, so we just verify that milliseconds are
 * zeroed out when saving or updating an item.
 */
public class DropMillisecondsHibernateInterceptorTest extends BaseContextSensitiveTest {

	@Autowired
	PersonService personService;
	
	@Autowired
	DropMillisecondsHibernateInterceptor dropMillisecondsHibernateInterceptor;

	@Test
	public void shouldClearMillisecondsWhenSavingANewObject() {
		Date dateWithMillisecond = new Date(567L);
		Date dateWithoutMillisecond = new Date(0L);

		Person person = new Person();
		person.addName(new PersonName("Alice", null, "Paul"));
		person.setGender("F");
		person.setBirthdate(dateWithMillisecond);

		personService.savePerson(person);
		Context.flushSession();

		assertThat(person.getBirthdate(), is(dateWithoutMillisecond));
	}

	@Test
	public void shouldClearMillisecondsWhenUpdatingAnExistingObject() {
		Date dateWithMillisecond = new Date(567L);
		Date dateWithoutMillisecond = new Date(0L);

		Person person = personService.getPerson(1);
		person.setBirthdate(dateWithMillisecond);

		personService.savePerson(person);
		Context.flushSession();

		assertThat(person.getBirthdate(), is(dateWithoutMillisecond));
	}
	
	@Test
	public void shouldNotChangeWhenInstanceOfTime() throws Exception {
		Time[] time = { Time.valueOf("17:00:00") };
		boolean anyChanges = dropMillisecondsHibernateInterceptor.onSave(null, null, time, null, null);
		assertFalse(anyChanges);
	}
	
	@Test
	public void shouldNotThrowUnsupportedOperationExceptionWhenInstanceOfSqlDate() throws Exception {
		Date[] sqlDate = {new java.sql.Date(567L)};
		boolean anyChanges = dropMillisecondsHibernateInterceptor.onSave(null, null, sqlDate, null, null);
		assertFalse(anyChanges);
	}
}

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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * We can't easily test the full behavior against different database versions, so we just verify that milliseconds are
 * zeroed out when saving or updating an item.
 */
public class DropMillisecondsHibernateInterceptorTest extends BaseContextSensitiveTest {

	@Autowired
	PersonService personService;

	@Test
	public void shouldClearMillisecondsWhenSavingANewObject() throws Exception {
		Date dateWithMillisecond = new Date(567l);
		Date dateWithoutMillisecond = new Date(0l);

		Person person = new Person();
		person.addName(new PersonName("Alice", null, "Paul"));
		person.setGender("F");
		person.setBirthdate(dateWithMillisecond);

		personService.savePerson(person);
		Context.flushSession();

		assertThat(person.getBirthdate(), is(dateWithoutMillisecond));
	}

	@Test
	public void shouldClearMillisecondsWhenUpdatingAnExistingObject() throws Exception {
		Date dateWithMillisecond = new Date(567l);
		Date dateWithoutMillisecond = new Date(0l);

		Person person = personService.getPerson(1);
		person.setBirthdate(dateWithMillisecond);

		personService.savePerson(person);
		Context.flushSession();

		assertThat(person.getBirthdate(), is(dateWithoutMillisecond));
	}
}
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

import java.util.Date;

import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;

/**
 * This class sets the personVoid* attributes on the given {@link Person} object when a void* method
 * is called with this class. This differs from the {@link BaseVoidHandler} because the Person
 * object contains personVoided* attributes instead of the normal voided attributes
 *
 * @see RequiredDataAdvice
 * @see UnvoidHandler
 * @since 1.5
 */
@Handler(supports = Person.class)
public class PersonVoidHandler implements VoidHandler<Person> {
	
	/**
	 * Sets all personVoid* attributes to the given parameters.
	 *
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 * @should set the personVoided bit
	 * @should set the personVoidReason
	 * @should set personVoidedBy
	 * @should not set personVoidedBy if non null
	 * @should set personDateVoided
	 * @should not set personDateVoided if non null
	 * @should not set the personVoidReason if already personVoided
	 * @should retire users
	 */
	@Override
	public void handle(Person person, User voidingUser, Date voidedDate, String voidReason) {
		
		// skip over all work if the object is already voided
		if (!person.getPersonVoided()) {
			if (person.getPersonId() != null) {
				// Skip if person is not persisted
				UserService us = Context.getUserService();
				for (User user : us.getUsersByPerson(person, false)) {
					us.retireUser(user, voidReason);
				}
			}
			
			person.setPersonVoided(true);
			person.setPersonVoidReason(voidReason);
			
			if (person.getPersonVoidedBy() == null) {
				person.setPersonVoidedBy(voidingUser);
			}
			if (person.getPersonDateVoided() == null) {
				person.setPersonDateVoided(voidedDate);
			}
		}
	}
	
}

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

/**
 * This class unsets the personVoid* attributes on the given {@link Person} object when an unvoid*
 * method is called with this class. This differs from the {@link BaseUnvoidHandler} because the
 * Person object contains personVoided* attributes instead of the normal voided attributes. <br>
 * 
 * @see RequiredDataAdvice
 * @see VoidHandler
 * @since 1.5
 */
@Handler(supports = Person.class)
public class PersonUnvoidHandler implements UnvoidHandler<Person> {
	
	/**
	 * Called around every unvoid* method to set everything to null.<br>
	 * <br>
	 * 
	 * @see org.openmrs.api.handler.RequiredDataHandler#handle(org.openmrs.OpenmrsObject,
	 *      org.openmrs.User, java.util.Date, java.lang.String)
	 * @should unset the personVoided bit
	 * @should unset the personVoider
	 * @should unset the personDateVoided
	 * @should unset the personVoidReason
	 * @should only act on already personVoided objects
	 * @should not act on objects with a different personDateVoided
	 */
	@Override
	public void handle(Person person, User unvoidingUser, Date origParentVoidedDate, String unused) {
		
		// only operate on voided objects
		if (person.getPersonVoided()
		        && (origParentVoidedDate == null || origParentVoidedDate.equals(person.getPersonDateVoided()))) {
			
			// only unvoid objects that were voided at the same time as the parent object
			person.setPersonVoided(false);
			person.setPersonVoidedBy(null);
			person.setPersonDateVoided(null);
			person.setPersonVoidReason(null);
		}
	}
	
}

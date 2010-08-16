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

import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;

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
	 */
	public void handle(Person person, User voidingUser, Date voidedDate, String voidReason) {
		
		// skip over all work if the object is already voided
		if (!person.isPersonVoided()) {
			
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

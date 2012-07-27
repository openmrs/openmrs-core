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
 * This class unsets the personVoid* attributes on the given {@link Person} object when an unvoid*
 * method is called with this class. This differs from the {@link BaseUnvoidHandler} because the
 * Person object contains personVoided* attributes instead of the normal voided attributes. <br/>
 * 
 * @see RequiredDataAdvice
 * @see VoidHandler
 * @since 1.5
 */
@Handler(supports = Person.class)
public class PersonUnvoidHandler implements UnvoidHandler<Person> {
	
	/**
	 * Called around every unvoid* method to set everything to null.<br/>
	 * <br/>
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
	public void handle(Person person, User unvoidingUser, Date origParentVoidedDate, String unused) {
		
		// only operate on voided objects
		if (person.isPersonVoided()) {
			
			// only unvoid objects that were voided at the same time as the parent object
			if (origParentVoidedDate == null || origParentVoidedDate.equals(person.getPersonDateVoided())) {
				person.setPersonVoided(false);
				person.setPersonVoidedBy(null);
				person.setPersonDateVoided(null);
				person.setPersonVoidReason(null);
			}
		}
	}
	
}

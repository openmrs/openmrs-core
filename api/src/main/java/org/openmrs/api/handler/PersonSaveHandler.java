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
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.APIException;
import org.springframework.util.StringUtils;

/**
 * This class deals with {@link Person} objects when they are saved via a save* method in an Openmrs
 * Service. This handler is automatically called by the {@link RequiredDataAdvice} AOP class. <br/>
 * 
 * @see RequiredDataHandler
 * @see SaveHandler
 * @see Person
 * @since 1.5
 */
@Handler(supports = Person.class)
public class PersonSaveHandler implements SaveHandler<Person> {
	
	/**
	 * @see org.openmrs.api.handler.SaveHandler#handle(org.openmrs.OpenmrsObject, org.openmrs.User,
	 *      java.util.Date, java.lang.String)
	 */
	public void handle(Person person, User creator, Date dateCreated, String other) {
		
		// only set the creator and date created if they weren't set by the developer already
		if (person.getPersonCreator() == null) {
			person.setPersonCreator(creator);
		}
		
		if (person.getPersonDateCreated() == null) {
			person.setPersonDateCreated(dateCreated);
		}
		
		// if there is an id already, we assume its been saved before and so set personChanged*
		boolean hasId;
		try {
			hasId = person.getId() != null;
		}
		catch (UnsupportedOperationException e) {
			hasId = true; // if no "id" to check, just go ahead and set them
		}
		if (hasId) {
			person.setPersonChangedBy(creator);
			person.setPersonDateChanged(dateCreated);
		}
		
		// address collection
		if (person.getAddresses() != null && person.getAddresses().size() > 0) {
			for (PersonAddress pAddress : person.getAddresses()) {
				pAddress.setPerson(person);
			}
		}
		
		// name collection
		if (person.getNames() != null && person.getNames().size() > 0) {
			for (PersonName pName : person.getNames()) {
				pName.setPerson(person);
			}
		}
		
		// attribute collection
		if (person.getAttributes() != null && person.getAttributes().size() > 0) {
			for (PersonAttribute pAttr : person.getAttributes()) {
				pAttr.setPerson(person);
			}
		}
		
		//if the patient was marked as dead and reversed, drop the cause of death
		if (!person.isDead() && person.getCauseOfDeath() != null)
			person.setCauseOfDeath(null);
		
		// do the checks for voided attributes (also in PersonVoidHandler)
		if (person.isPersonVoided()) {
			
			if (!StringUtils.hasLength(person.getPersonVoidReason()))
				throw new APIException(
				        "The voided bit was set to true, so a void reason is required at save time for person: " + person);
			
			if (person.getPersonVoidedBy() == null) {
				person.setPersonVoidedBy(creator);
			}
			if (person.getPersonDateVoided() == null) {
				person.setPersonDateVoided(dateCreated);
			}
		} else {
			// voided is set to false
			person.setPersonVoidedBy(null);
			person.setPersonDateVoided(null);
			person.setPersonVoidReason(null);
		}
	}
}

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

import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.annotation.Handler;

/**
 * This is called every time a {@link PersonName} object is saved. The current implemention just
 * trims out the whitespace from the beginning and end of the given/middle/familyname/familyName2
 * attributes
 */
@Handler(supports = PersonName.class)
public class PersonNameSaveHandler implements SaveHandler<PersonName> {
	
	/**
	 * @see org.openmrs.api.handler.SaveHandler#handle(org.openmrs.OpenmrsObject, org.openmrs.User,
	 *      java.util.Date, java.lang.String)
	 */
	public void handle(PersonName personName, User creator, Date dateCreated, String other) {
		if (personName.getGivenName() != null) {
			personName.setGivenName(personName.getGivenName().trim());
		}
		if (personName.getMiddleName() != null) {
			personName.setMiddleName(personName.getMiddleName().trim());
		}
		if (personName.getFamilyName() != null) {
			personName.setFamilyName(personName.getFamilyName().trim());
		}
		if (personName.getFamilyName2() != null) {
			personName.setFamilyName2(personName.getFamilyName2().trim());
		}
	}
	
}

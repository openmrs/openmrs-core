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

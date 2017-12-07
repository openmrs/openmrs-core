/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.openmrs.Person;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

/**
 * Allows for serializing/deserializing a Person object to a string so that Spring knows how to pass
 * a Person back and forth through an html form or other medium. <br>
 * <br>
 * In version 1.9, added ability for this to also retrieve Person objects by uuid
 *
 * @see Person
 */
public class PersonEditor extends PropertyEditorSupport {

	/**
	 * @should set using id
	 * @should set using uuid
	 *
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		PersonService ps = Context.getPersonService();
		if (StringUtils.hasText(text)) {
			try {
				Integer personId = Integer.valueOf(text);
				setValue(ps.getPerson(personId));
			}
			catch (NumberFormatException e) {
				Person person = ps.getPersonByUuid(text);
				setValue(person);
				if (person == null) {
					throw new IllegalArgumentException("Failed to find person for value [" + text + "]");
				}
			}
		} else {
			setValue(null);
		}
	}
	
	/**
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 */
	@Override
	public String getAsText() {
		Person t = (Person) getValue();
		if (t == null) {
			return "";
		} else {
			return (t.getPersonId() == null) ? "" : t.getPersonId().toString();
		}
	}
	
}

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
package org.openmrs.propertyeditor;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.springframework.util.StringUtils;

/**
 * Allows for serializing/deserializing a Person object to a string so that Spring knows how to pass
 * a Person back and forth through an html form or other medium
 * 
 * @see Person
 */
public class PersonEditor extends PropertyEditorSupport {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	@Override
    public void setAsText(String text) throws IllegalArgumentException {
		PersonService ps = Context.getPersonService();
		if (StringUtils.hasText(text)) {
			try {
				setValue(ps.getPerson(Integer.valueOf(text)));
			}
			catch (Exception ex) {
				log.error("Error setting text: " + text, ex);
				throw new IllegalArgumentException("Person not found: " + ex.getMessage());
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

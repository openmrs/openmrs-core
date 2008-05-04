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
package org.openmrs.reporting;

import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;

public class PersonAttributeFilter extends AbstractPatientFilter implements PatientFilter {

	private PersonAttributeType attribute;
	private String value;
	
	/**
	 * This currently only returns patients, although it's named for persons.
	 */
	public PersonAttributeFilter() { }
	
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Patients with ");
		sb.append(getAttribute() != null ? getAttribute().getName() : " any attribute");
		if (getValue() != null) {
			sb.append(" equal to ");
			sb.append(getValue());
		}
		return sb.toString();
	}
	
	public PatientSet filter(PatientSet input) {
		PatientSet ps = Context.getPatientSetService().getPatientsHavingPersonAttribute(getAttribute(), getValue());
		return input == null ? ps : input.intersect(ps);
	}

	public PatientSet filterInverse(PatientSet input) {
		PatientSet ps = Context.getPatientSetService().getPatientsHavingPersonAttribute(getAttribute(), getValue());
		return input.subtract(ps);
	}

	public boolean isReadyToRun() {
		// TODO Auto-generated method stub
		return true;
	}
	
	// getters and setters

	public PersonAttributeType getAttribute() {
		return attribute;
	}

	public void setAttribute(PersonAttributeType attribute) {
		this.attribute = attribute;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}

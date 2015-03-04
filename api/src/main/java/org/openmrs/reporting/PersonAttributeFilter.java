/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

import org.openmrs.Cohort;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.report.EvaluationContext;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class PersonAttributeFilter extends CachingPatientFilter {
	
	private PersonAttributeType attribute;
	
	private String value;
	
	/**
	 * This currently only returns patients, although it's named for persons.
	 */
	public PersonAttributeFilter() {
	}
	
	@Override
	public String getCacheKey() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName()).append(".");
		sb.append(getAttribute()).append(".");
		sb.append(getValue());
		return sb.toString();
	}
	
	public String getDescription() {
		MessageSourceService msa = Context.getMessageSourceService();
		StringBuilder sb = new StringBuilder();
		sb.append(msa.getMessage("reporting.patientsWith")).append(" ");
		sb.append(getAttribute() != null ? getAttribute().getName() : " " + msa.getMessage("reporting.anyAttribute"));
		if (getValue() != null) {
			sb.append(" ").append(msa.getMessage("reporting.equalTo")).append(" ");
			sb.append(getValue());
		}
		return sb.toString();
	}
	
	@Override
	public Cohort filterImpl(EvaluationContext context) {
		return Context.getPatientSetService().getPatientsHavingPersonAttribute(getAttribute(), getValue());
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

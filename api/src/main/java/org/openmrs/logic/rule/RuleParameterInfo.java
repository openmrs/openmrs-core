/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logic.rule;

/**
 * An argument passed to a logic Rule
 * 
 * @see org.openmrs.logic.Rule
 */
public class RuleParameterInfo {
	
	private Class parameterClass;
	
	private boolean required;
	
	private Object defaultValue;
	
	public RuleParameterInfo(Class parameterClass) {
		this(parameterClass, false, null);
	}
	
	public RuleParameterInfo(Class parameterClass, boolean required) {
		this(parameterClass, required, null);
	}
	
	public RuleParameterInfo(Class parameterClass, boolean required, Object defaultValue) {
		this.parameterClass = parameterClass;
		this.required = required;
		this.defaultValue = defaultValue;
	}
	
	public Class getParameterClass() {
		return parameterClass;
	}
	
	public void setParameterClass(Class parameterClass) {
		this.parameterClass = parameterClass;
	}
	
	public boolean isRquired() {
		return required;
	}
	
	public void setRequired(boolean required) {
		this.required = required;
	}
	
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
}

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
package org.openmrs.logic.rule;

/**
 * An argument passed to a logic Rule
 * 
 * @see org.openmrs.logic.rule.Rule
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

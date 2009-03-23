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
package org.openmrs.web.dwr;


public class EnteredField {
	
	private Integer fieldId;
	
	private String value;
	
	private String valueClass;
	
	private String dateTime;
	
	public EnteredField() {
	}
	
	public EnteredField(String s) {
		String[] temp = s.split("\\^");
		fieldId = Integer.valueOf(temp[0]);
		dateTime = temp[1];
		value = temp[2];
	}
	
	public boolean isEmpty() {
		return value == null || value.length() == 0;
	}
	
	public Double getValueAsDouble() {
		return value == null ? null : Double.valueOf(value);
	}
	
	public Integer getValueAsInteger() {
		return value == null ? null : Integer.valueOf(value);
	}
	
	public String getDateTime() {
		return dateTime;
	}
	
	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	
	public Integer getFieldId() {
		return fieldId;
	}
	
	public void setFieldId(Integer fieldId) {
		this.fieldId = fieldId;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValueClass() {
		return valueClass;
	}
	
	public void setValueClass(String valueClass) {
		this.valueClass = valueClass;
	}
	
}

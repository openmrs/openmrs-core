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
package org.openmrs.report;

import org.openmrs.logic.LogicCriteria;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Parameters are used in a ReportSchema, LogicCriteria, and CohortFromStrategy, etc If an object is
 * parameterizable, it is willing to accept a parameter like this. Examples of a parameter would be
 * "What start date do you want to use?" The value of the parameter would be plugged in when the
 * object is evaluated.
 * 
 * @see ReportModel
 * @see LogicCriteria
 * @see CohortFromStrategy
 * @see Parameterizable
 */
@Root(strict = false)
public class Parameter {
	
	private static final long serialVersionUID = 12020438439292929L;
	
	/**
	 * The fairly descriptive name to give to this parameter (the "key" part of "key-value pair")
	 */
	private String name;
	
	/**
	 * The text displayed to the user if input is needed
	 */
	private String label;
	
	/**
	 * Data type of this parameter. e.g. java.util.Date, java.lang.String, etc
	 */
	private Class<?> clazz;
	
	/**
	 * The value given to this parameter if the user does not provide any input
	 */
	private Object defaultValue;
	
	/**
	 * Default constructor
	 */
	public Parameter() {
	}
	
	/**
	 * Initialize this Parameter with the given values
	 * 
	 * @param name The defined descriptive name
	 * @param label The label to display to the user if value is needed
	 * @param clazz The data type of this parameter
	 * @param defaultValue The value to fill in if nothing provided by the user
	 */
	public Parameter(String name, String label, Class clazz, Object defaultValue) {
		super();
		this.name = name;
		this.label = label;
		this.clazz = clazz;
		this.defaultValue = defaultValue;
	}
	
	/**
	 * Whether or not this parameter "must" be filled in by a value
	 * 
	 * @return true/false whether the user has to give their input
	 */
	public boolean isRequired() {
		return defaultValue == null;
	}
	
	// getters and setters
	
	@Attribute(required = true)
	public Class<?> getClazz() {
		return clazz;
	}
	
	@Attribute(required = true)
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	
	@Element(required = false)
	public Object getDefaultValue() {
		return defaultValue;
	}
	
	@Element(required = false)
	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	@Element(data = true, required = false)
	public String getLabel() {
		return label;
	}
	
	@Element(data = true, required = false)
	public void setLabel(String label) {
		this.label = label;
	}
	
	@Element(data = true, required = true)
	public String getName() {
		return name;
	}
	
	@Element(data = true, required = true)
	public void setName(String name) {
		this.name = name;
	}
	
}

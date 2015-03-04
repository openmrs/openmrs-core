/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.report;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Parameters are used in a ReportSchema, LogicCriteria, and CohortFromStrategy, etc
 * <p>
 * If an object is parameterizable, it is willing to accept a parameter like this. Examples of a
 * parameter would be "What start date do you want to use?" The value of the parameter would be
 * plugged in when the object is evaluated.
 * 
 * @see LogicCriteria
 * @see Parameterizable
 * @deprecated see reportingcompatibility module
 */
@Root(strict = false)
@Deprecated
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
	public Parameter(String name, String label, Class<?> clazz, Object defaultValue) {
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

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
package org.openmrs.hl7;

public class HL7Segment {
	
	private HL7Message parent;
	
	private String line;
	
	private String[] fields;
	
	public HL7Segment(HL7Message parent, String line) {
		this.parent = parent;
		if (line == null)
			line = "";
		this.line = line;
		this.fields = line.split("\\" + parent.getFieldSeparator());
	}
	
	public String getId() {
		return getField(0);
	}
	
	/**
	 * Returns value of field within segment
	 * 
	 * @param index zero-based index
	 * @return value of field at given index
	 */
	public String getField(int index) {
		if (index < 0 || index > fields.length - 1)
			return "";
		return fields[index];
	}
	
	public String[] getComponents(int fieldIndex) {
		return getField(fieldIndex).split("\\" + parent.getComponentSeparator());
	}
	
	/**
	 * Returns value of component within a field
	 * 
	 * @param index index of field
	 * @param componentIndex one-based component of field
	 * @return value of sub-field (part) within specified field
	 */
	public String getComponent(int fieldIndex, int componentIndex) {
		String[] components = getComponents(fieldIndex);
		if (componentIndex < 1 || componentIndex > components.length)
			return "";
		return components[componentIndex - 1];
	}
	
	public String toString() {
		return line;
	}
}

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
package org.openmrs.arden;

import java.io.Writer;

/**
 *
 */
public class LogicAssignment {
	
	private String variableName = null;
	
	private String variableValue = null;
	
	public LogicAssignment(String variableName, String variableValue) {
		this.variableName = variableName;
		this.variableValue = variableValue;
	}
	
	public String getVariableName() {
		return variableName;
	}
	
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	
	public String getVariableValue() {
		return variableValue;
	}
	
	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}
	
	public void write(Writer w) {
		String name = getVariableName();
		String value = getVariableValue();
		try {
			w.append("\t\t\tuserVarMap.put(\"" + name + "\", \"" + value + "\");\n");
		}
		catch (Exception e) {}
	}
}

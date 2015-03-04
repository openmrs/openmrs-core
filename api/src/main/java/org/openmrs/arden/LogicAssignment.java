/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
			w.append("\t\t\t//preprocess any || operator ;\n");
			w.append("\t\t\tString val = doAction(\"" + value + "\", userVarMap, resultLookup);\n");
			//	w.append("\t\t\tuserVarMap.put(\"" + name + "\", \"" + value + "\");\n");
			w.append("\t\t\tuserVarMap.put(\"" + name + "\",  val);\n");
		}
		catch (Exception e) {}
	}
}

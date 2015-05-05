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
import java.util.ArrayList;

/**
 * This class translates the CALL function of an MLM into a context.eval() of a rule
 */
public class Call {
	
	private String callVar = null;
	
	private String callMethod = null;
	
	private ArrayList<String> parameters = null;
	
	public Call(String callVar, String callMethod) {
		this.callVar = callVar;
		this.callMethod = callMethod;
		this.parameters = new ArrayList<String>();
	}
	
	public String getCallVar() {
		return callVar;
	}
	
	public void setCallVar(String callVar) {
		this.callVar = callVar;
	}
	
	public String getCallMethod() {
		return callMethod;
	}
	
	public void setCallMethod(String callMethod) {
		this.callMethod = callMethod;
	}
	
	public void write(Writer w) {
		try {
			
			for (int i = 0; i < parameters.size(); i++) {
				String currParam = parameters.get(i);
				w.append("\t\t\t\tvarLen = " + "\"" + currParam + "\"" + ".length();\n");
				
				w.append("\t\t\t\tvalue=userVarMap.get(" + "\"" + currParam + "\"" + ");\n");
				w.append("\t\t\t\tif(value != null){\n");
				w.append("\t\t\t\t\tparameters.put(\"param" + (i + 1) + "\"," + "value);\n");
				w.append("\t\t\t\t}\n");
				
				w.append("\t\t\t\t// It must be a result value or date\n");
				w.append("\t\t\t\telse if(" + "\"" + currParam + "\"" + ".endsWith(\"_value\"))\n");
				w.append("\t\t\t\t{\n");
				w.append("\t\t\t\t\tvariable = " + "\"" + currParam + "\"" + ".substring(0, varLen-6); // -6 for _value\n");
				w.append("\t\t\t\t\tif (resultLookup.get(variable) != null){\n");
				w.append("\t\t\t\t\t\tvalue = resultLookup.get(variable).toString();\n");
				w.append("\t\t\t\t\t}\n");
				w.append("\t\t\t\t}\n");
				w.append("\t\t\t\telse if(" + "\"" + currParam + "\"" + ".endsWith(\"_date\"))\n");
				w.append("\t\t\t\t{\n");
				w.append("\t\t\t\t\tvariable = " + "\"" + currParam + "\"" + ".substring(0, varLen-5); // -5 for _date\n");
				w.append("\t\t\t\t\tif (resultLookup.get(variable) != null){\n");
				w.append("\t\t\t\t\t\tvalue = resultLookup.get(variable).getResultDate().toString();\n");
				w.append("\t\t\t\t\t}\n");
				w.append("\t\t\t\t}\n");
				w.append("\t\t\t\telse if(" + "\"" + currParam + "\"" + ".endsWith(\"_object\"))\n");
				w.append("\t\t\t\t{\n");
				w.append("\t\t\t\t\tvariable = " + "\"" + currParam + "\"" + ".substring(0, varLen-7); // -5 for _object\n");
				w.append("\t\t\t\t\tif (resultLookup.get(variable) != null){\n");
				w.append("\t\t\t\t\t\tvalue = resultLookup.get(variable);\n");
				w.append("\t\t\t\t\t}\n");
				w.append("\t\t\t\t}\n");
				w.append("\t\t\t\telse\n");
				w.append("\t\t\t\t{\n");
				w.append("\t\t\t\t\tif (resultLookup.get(" + "\"" + currParam + "\"" + ") != null){\n");
				w.append("\t\t\t\t\t\tvalue = resultLookup.get(" + "\"" + currParam + "\"" + ").toString();\n");
				w.append("\t\t\t\t\t}\n");
				w.append("\t\t\t\t}\n");
				w.append("\t\t\t\tif(value != null){\n");
				w.append("\t\t\t\t\tparameters.put(\"param" + (i + 1) + "\"," + "value);\n");
				w.append("\t\t\t\t}\n");
				
				w.append("\t\t\t\telse\n");
				w.append("\t\t\t\t{\n");
				w.append("\t\t\t\t\tparameters.put(\"param" + (i + 1) + "\",\"" + currParam + "\");\n");
				w.append("\t\t\t\t}\n");
				
			}
			
			w.append("\t\t\t\t");
			w.append("if (ruleProvider != null) {\n");
			w.append("\t\t\t\t\t");
			w.append("ruleProvider.getRule(\"" + getCallMethod() + "\");\n");
			w.append("\t\t\t\t");
			w.append("}\n");
			w.append("\t\t\t\t");
			if (getCallVar() != null && getCallVar().length() > 0) {
				w.append("Result " + getCallVar() + " = ");
			}
			
			w.append("context.eval(patient.getPatientId(), \"" + getCallMethod() + "\",parameters);\n");
			w.append("\t\t\t\t");
			if (getCallVar() != null && getCallVar().length() > 0) {
				w.append("resultLookup.put(\"" + getCallVar() + "\"," + getCallVar() + ");\n");
			}
		}
		catch (Exception e) {}
	}
	
	public void addParameter(String parameter) {
		this.parameters.add(parameter);
	}
}

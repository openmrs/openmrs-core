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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * 
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
				w.append("parameters.put(\"param" + (i+1) + "\",\"" + currParam
				        + "\");");
			}

			w.append("\t\t\t\t");
			if (getCallVar() != null && getCallVar().length() > 0) {
				w.append("Result " + getCallVar() + " = ");
			}
			w.append("logicService.eval(patient, \"" + getCallMethod()
			        + "\",parameters);");
			
		} catch (Exception e) {
		}
	}

	public void addParameter(String parameter) {
		this.parameters.add(parameter);
	}
}

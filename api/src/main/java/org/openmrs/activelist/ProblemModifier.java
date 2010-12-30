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
package org.openmrs.activelist;

/**
 *
 */
public enum ProblemModifier {
	RULE_OUT("Rule Out"), HISTORY_OF("History of");
	
	private String text;
	
	private ProblemModifier(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	public static ProblemModifier getValue(String arg) {
		try {
			return valueOf(arg.replaceAll(" ", "_").toUpperCase());
		}
		catch (Exception e) {
			return null;
		}
	}
}

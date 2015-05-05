/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.activelist;

/**
 *
 */
public enum ProblemModifier {
	RULE_OUT("Rule Out"),
	HISTORY_OF("History of");
	
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

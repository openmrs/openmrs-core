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

/**
 *
 */
public class Action {
	
	String actionString = null;
	
	String atVar = null;
	
	public Action(String actionString) {
		this.actionString = actionString;
	}
	
	public String getActionString() {
		return actionString;
	}
	
	public void setActionString(String actionString) {
		this.actionString = actionString;
	}
	
	public String getAtVar() {
		return atVar;
	}
	
	public void setAtVar(String atVar) {
		this.atVar = atVar;
	}
	
}

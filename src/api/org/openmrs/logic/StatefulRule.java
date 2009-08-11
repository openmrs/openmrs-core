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
package org.openmrs.logic;

import org.openmrs.logic.rule.ReferenceRule;

/**
 * Rules that implement this interface need to maintain their state across recreations. Rules are
 * usually pseudo-static in that they don't have instance variables and don't need to be
 * initialized. If a rule does need this, then it is a StatefulRule.
 * 
 * @see ReferenceRule
 */
public interface StatefulRule extends Rule {
	
	/**
	 * Convert this Rule's current state to a String so that the Rule can be recreated just as it
	 * was before.
	 * 
	 * @return a String representing the current state of the Rule
	 * @see #restoreFromString(String)
	 */
	public String saveToString();
	
	/**
	 * Recreate the current Rule's state from the given String
	 * 
	 * @param state the state to restore
	 */
	public void restoreFromString(String state);
}

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
package org.openmrs.logic.op;

/**
 * The Exists operator test whether a criteria will exist for person or not.<br /><br />
 * 
 * Example: <br />
 * - <code>logicService.parse("EncounterDataSource.ENCOUNTER_KEY").equals("ADULTRETURN").exists();</code><br />
 *   The above will give us a criteria to test whether ADULTRETURN encounter exists or not
 */
public class Exists implements TransformOperator {
	
	public String toString() {
		return "EXISTS";
	}
	
}

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
 * The Distinct operator will filter out duplicate results and return a set of distinct results.<br /><br />
 * 
 * Example: <br />
 * - <code>logicService.parse("EncounterDataSource.ENCOUNTER_KEY").distinct();</code><br />
 *   The above will give us a criteria to get all distinct encounter type from the system
 */
public class Distinct implements TransformOperator {
	
	public String toString() {
		return "DISTINCT";
	}
}

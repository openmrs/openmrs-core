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
 * The In operator test whether a value is in a collection of value or not.<br />
 * <br />
 * Example: <br />
 * <code>
 *  Collection answers = Collection.asList("ADULTRETURN");<br />
 *  logicService.parseString(EncounterDataSource.ENCOUNTER_KEY).in(typeNames);<br />
 * </code> The above criteria will test whether a patient have EncounterType in the list
 */
public class In implements ComparisonOperator {
	
	public String toString() {
		return "IN";
	}
	
}

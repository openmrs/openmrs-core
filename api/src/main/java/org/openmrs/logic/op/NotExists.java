/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logic.op;

/**
 * The NotExists operator test whether a criteria will exist for person or not.<br>
 * <br>
 * Example: <br>
 * -
 * <code>logicService.parse("EncounterDataSource.ENCOUNTER_KEY").equals("ADULTRETURN").notExists();</code>
 * <br>
 * The above will give us a criteria to test whether ADULTRETURN encounter exists or not
 */
public class NotExists implements TransformOperator {
	
	@Override
	public String toString() {
		return "NOT EXISTS";
	}
	
}

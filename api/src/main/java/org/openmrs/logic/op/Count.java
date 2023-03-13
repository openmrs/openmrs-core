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
 * The Count operator will return the number of results returned by the logic service<br>
 * <br>
 * Example: <br>
 * - <code>logicService.parse("EncounterDataSource.ENCOUNTER_KEY").count();</code><br>
 * The above will give us a criteria to get the number of encounter type we have in the system
 */
public class Count implements TransformOperator {
	
	@Override
	public String toString() {
		return "Count";
	}
	
}

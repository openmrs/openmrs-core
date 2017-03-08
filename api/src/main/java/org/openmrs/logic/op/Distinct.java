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
 * The Distinct operator will filter out duplicate results and return a set of distinct results.<br>
 * <br>
 * Example: <br>
 * - <code>logicService.parse("EncounterDataSource.ENCOUNTER_KEY").distinct();</code><br>
 * The above will give us a criteria to get all distinct encounter type from the system
 */
public class Distinct implements TransformOperator {
	
	@Override
	public String toString() {
		return "DISTINCT";
	}
}

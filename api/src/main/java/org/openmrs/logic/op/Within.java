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
 * The Within operator tests whether an expression will yield true for a certain time frame.<br />
 * <br />
 * Example: <br />
 * - <code>logicService.parse("'CD4 COUNT'").within(Duration.years(2));</code><br />
 * The above will give us a criteria to check if there's "CD4 COUNT" observations within the last 2
 * years
 */
public class Within implements ComparisonOperator {
	
	public String toString() {
		return "WITHIN";
	}
	
}

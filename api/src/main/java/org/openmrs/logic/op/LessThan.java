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
 * The LessThan operator will return result that have a less value than the operand.<br>
 * <br>
 * Example: <br>
 * - <code>logicService.parse("'CD4 COUNT'").gte(200);</code><br>
 * The above will give us a criteria to get the "CD4 COUNT" observations that has the value numeric
 * less than 200
 * 
 * @see GreaterThan
 * @see GreaterThanEquals
 * @see LessThanEquals
 */
public class LessThan implements ComparisonOperator {
	
	@Override
	public String toString() {
		return "LESS THAN";
	}
	
}

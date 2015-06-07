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

import org.openmrs.logic.LogicCriteria;

/**
 * The And operator is a conjunction operator to combine two or more {@link LogicCriteria} objects.<br />
 * <br />
 * Example: <br />
 * - <code>logicService.parse("'CD4 COUNT'").and(logicService.parse("'WEIGHT (KG)'"));</code><br />
 * The above will give us a criteria to check if there's "CD4 COUNT" and "WEIGHT (KG)" observations
 */
public class And implements LogicalOperator {
	
	public String toString() {
		return "AND";
	}
	
}

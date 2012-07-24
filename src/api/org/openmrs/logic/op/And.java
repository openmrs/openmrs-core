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

import org.openmrs.logic.LogicCriteria;

/**
 * The And operator is a conjunction operator to combine two or more {@link LogicCriteria} objects.<br /><br />
 * Example: <br />
 * - <code>logicService.parse("'CD4 COUNT'").and(logicService.parse("'WEIGHT (KG)'"));</code><br />
 *   The above will give us a criteria to check if there's "CD4 COUNT" and "WEIGHT (KG)" observations
 */
public class And implements LogicalOperator {
	
	public String toString() {
		return "AND";
	}
	
}

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
 * The LessThan operator will return result that have a less value than or equals to the operand.<br />
 * <br />
 * Example: <br />
 * - <code>logicService.parse("'CD4 COUNT'").lte(200);</code><br />
 * The above will give us a criteria to get the "CD4 COUNT" observations that has the value numeric
 * less than or equals to 200
 * 
 * @see GreaterThan
 * @see GreaterThanEquals
 * @see LessThan
 */
public class LessThanEquals implements ComparisonOperator {
	
	public String toString() {
		return "LESS THAN EQUALS";
	}
	
}

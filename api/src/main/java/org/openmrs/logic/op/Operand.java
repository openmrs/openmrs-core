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

import org.openmrs.logic.LogicExpression;

/**
 * This is a marker interface for things that can be operated on by an {@link Operator} in a
 * {@link LogicExpression}
 */
public interface Operand {
	
	/**
	 * Sanity check for this Operand vs a given operator. Would return true for {@link OperandDate}
	 * .supports("BEFORE") but {@link OperandConcept}.supports("BEFORE") returns false
	 * 
	 * @param operator The operator to test against this Operand
	 * @return true/false about whether this Operand supports this {@link ComparisonOperator}
	 */
	public boolean supports(ComparisonOperator operator);
	
}

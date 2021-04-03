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

import org.openmrs.logic.LogicExpression;

/**
 * This is a marker interface for things that can be operated on by an {@link Operator} in a
 * {@link LogicExpression}
 */
public interface Operand {
	
	/**
	 * Sanity check for this Operand vs a given operator.
	 * 
	 * @param operator The operator to test against this Operand
	 * @return true/false about whether this Operand supports this {@link ComparisonOperator}
	 */
	public boolean supports(ComparisonOperator operator);
	
}

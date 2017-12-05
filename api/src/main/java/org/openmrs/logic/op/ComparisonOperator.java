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
 * Marker interface to keep track of the ComparisonOperator sub type
 * 
 * @see Operator
 */
public interface ComparisonOperator extends Operator {
	
	// comparison operators
	ComparisonOperator CONTAINS = new Contains();
	
	ComparisonOperator EQUALS = new Equals();
	
	ComparisonOperator WITHIN = new Within();
	
	ComparisonOperator GT = new GreaterThan();
	
	ComparisonOperator GTE = new GreaterThanEquals();
	
	ComparisonOperator LT = new LessThan();
	
	ComparisonOperator LTE = new LessThanEquals();
	
	ComparisonOperator BEFORE = new Before();
	
	ComparisonOperator AFTER = new After();
	
	ComparisonOperator IN = new In();
	
}

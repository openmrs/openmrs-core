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
	public static final ComparisonOperator CONTAINS = new Contains();
	
	public static final ComparisonOperator EQUALS = new Equals();
	
	public static final ComparisonOperator WITHIN = new Within();
	
	public static final ComparisonOperator GT = new GreaterThan();
	
	public static final ComparisonOperator GTE = new GreaterThanEquals();
	
	public static final ComparisonOperator LT = new LessThan();
	
	public static final ComparisonOperator LTE = new LessThanEquals();
	
	public static final ComparisonOperator BEFORE = new Before();
	
	public static final ComparisonOperator AFTER = new After();
	
	public static final ComparisonOperator IN = new In();
	
}

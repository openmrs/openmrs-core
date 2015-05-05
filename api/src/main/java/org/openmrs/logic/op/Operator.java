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
 * An operator used within a logical expression.
 */
public interface Operator {
	
	// comparison operators
	public static final Operator CONTAINS = ComparisonOperator.CONTAINS;
	
	public static final Operator EQUALS = ComparisonOperator.EQUALS;
	
	public static final Operator WITHIN = ComparisonOperator.WITHIN;
	
	public static final Operator GT = ComparisonOperator.GT;
	
	public static final Operator GTE = ComparisonOperator.GTE;
	
	public static final Operator LT = ComparisonOperator.LT;
	
	public static final Operator LTE = ComparisonOperator.LTE;
	
	public static final Operator BEFORE = ComparisonOperator.BEFORE;
	
	public static final Operator AFTER = ComparisonOperator.AFTER;
	
	public static final Operator IN = ComparisonOperator.IN;
	
	// weird operator
	public static final Operator ASOF = new AsOf();
	
	// logical operators
	public static final Operator AND = LogicalOperator.AND;
	
	public static final Operator OR = LogicalOperator.OR;
	
	public static final Operator NOT = LogicalOperator.NOT;
	
	// transform operators
	public static final Operator LAST = TransformOperator.LAST;
	
	public static final Operator FIRST = TransformOperator.FIRST;
	
	public static final Operator DISTINCT = TransformOperator.DISTINCT;
	
	public static final Operator EXISTS = TransformOperator.EXISTS;
	
	public static final Operator NOT_EXISTS = TransformOperator.NOT_EXISTS;
	
	public static final Operator COUNT = TransformOperator.COUNT;
	
	public static final Operator AVERAGE = TransformOperator.AVERAGE;
	
}

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

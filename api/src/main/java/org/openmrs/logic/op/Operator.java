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
	Operator CONTAINS = ComparisonOperator.CONTAINS;
	
	Operator EQUALS = ComparisonOperator.EQUALS;
	
	Operator WITHIN = ComparisonOperator.WITHIN;
	
	Operator GT = ComparisonOperator.GT;
	
	Operator GTE = ComparisonOperator.GTE;
	
	Operator LT = ComparisonOperator.LT;
	
	Operator LTE = ComparisonOperator.LTE;
	
	Operator BEFORE = ComparisonOperator.BEFORE;
	
	Operator AFTER = ComparisonOperator.AFTER;
	
	Operator IN = ComparisonOperator.IN;
	
	// weird operator
	Operator ASOF = new AsOf();
	
	// logical operators
	Operator AND = LogicalOperator.AND;
	
	Operator OR = LogicalOperator.OR;
	
	Operator NOT = LogicalOperator.NOT;
	
	// transform operators
	Operator LAST = TransformOperator.LAST;
	
	Operator FIRST = TransformOperator.FIRST;
	
	Operator DISTINCT = TransformOperator.DISTINCT;
	
	Operator EXISTS = TransformOperator.EXISTS;
	
	Operator NOT_EXISTS = TransformOperator.NOT_EXISTS;
	
	Operator COUNT = TransformOperator.COUNT;
	
	Operator AVERAGE = TransformOperator.AVERAGE;
	
}

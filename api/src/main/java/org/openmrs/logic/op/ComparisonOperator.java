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

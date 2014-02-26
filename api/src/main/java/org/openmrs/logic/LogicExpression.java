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
package org.openmrs.logic;

import org.openmrs.logic.op.Operand;
import org.openmrs.logic.op.Operator;
import org.openmrs.logic.op.TransformOperator;

/**
 * LogicExpression is an internal representation of the LogicCriteria created through LogicService.
 * This internal representation will be processed by the LogicService backend engine to create
 * hibernate query.<br />
 * <br />
 * LogicExpression has two form, the binary and unary. Binary logic expression takes the form of <br />
 * <code>LogicExpressionBinary -- (Operand Operator Operand)</code><br />
 * <code>LogicExpressionUnary  -- (Operator Operand)</code>
 */
public interface LogicExpression extends Operand {
	
	/**
	 * Get the operator for the current LogicExpression
	 * 
	 * @return current operator of the LogicExpression
	 */
	public Operator getOperator();
	
	/**
	 * Method to get the root token of the current LogicCriteria.
	 * 
	 * @return the root token of the LogicExpression
	 * @see LogicCriteria#getRootToken()
	 */
	public String getRootToken();
	
	/**
	 * Get the right operand of the LogicExpression. Both LogicExpressionBinary and
	 * LogicExpressionUnary have right operand
	 * 
	 * @return right operand of the LogicExpression
	 */
	public Operand getRightOperand();
	
	/**
	 * Get the transformation expression applied to the LogicExpression
	 * 
	 * @return transformation expression of the LogicExpression
	 * @see TransformOperator
	 */
	public LogicTransform getTransform();
	
	/**
	 * Set the transformation expression applied to the LogicExpression
	 * 
	 * @see TransformOperator
	 */
	public void setTransform(LogicTransform transform);
}

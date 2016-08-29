/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logic;

import org.openmrs.logic.op.Operand;
import org.openmrs.logic.op.Operator;
import org.openmrs.logic.op.TransformOperator;

/**
 * LogicExpression is an internal representation of the LogicCriteria created through LogicService.
 * This internal representation will be processed by the LogicService backend engine to create
 * hibernate query.<br>
 * <br>
 * LogicExpression has two form, the binary and unary. Binary logic expression takes the form of <br>
 * <code>LogicExpressionBinary -- (Operand Operator Operand)</code><br>
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

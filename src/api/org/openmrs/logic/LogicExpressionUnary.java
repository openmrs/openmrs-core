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

import java.util.ArrayList;

import org.openmrs.logic.op.Operator;

/**
 *
 */
public class LogicExpressionUnary implements LogicExpression {
	
	private Object operand = null;
	
	private Operator operator = null;
	
	private LogicTransform transform = null;
	
	public LogicExpressionUnary(Object operand, Operator operator) {
		this.operand = operand;
		this.operator = operator;
	}
	
	public Operator getOperator() {
		return this.operator;
	}
	
	public String getRootToken() {
		
		if (operand != null) {
			if (operand instanceof LogicExpression) {
				return ((LogicExpression) operand).getRootToken();
			} else {
				return operand.toString();
			}
			
		}
		
		return null;
	}
	
	public String toString() {
		
		String result = "";
		
		if (this.transform != null) {
			result += transform.toString() + " {";
		}
		result += operator + " " + operand;
		if (this.transform != null) {
			result += transform.toString() + "}";
		}
		return "(" + result + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LogicExpressionUnary)) {
			return false;
		}
		
		LogicExpressionUnary compExpression = (LogicExpressionUnary) obj;
		
		if (!safeEquals(this.operator, compExpression.getOperator())) {
			return false;
		}
		
		if (!safeEquals(this.operand, compExpression.getOperand())) {
			return false;
		}
		if (!safeEquals(this.transform, compExpression.getTransform())) {
			return false;
		}
		
		return true;
	}
	
	public boolean safeEquals(Object a, Object b) {
		if (a == null && b == null)
			return true;
		if (a == null || b == null)
			return false;
		return a.equals(b);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((operator == null) ? 0 : operator.hashCode());
		result = prime * result + ((operand == null) ? 0 : operand.hashCode());
		result = prime * result + ((transform == null) ? 0 : transform.hashCode());
		
		return result;
	}
	
	public Object getOperand() {
		return operand;
	}
	
	/**
	 * @see org.openmrs.logic.LogicExpressionBinary#getOperands()
	 */
	public ArrayList<Object> getOperands() {
		ArrayList<Object> operands = new ArrayList<Object>();
		operands.add(this.operand);
		return operands;
	}
	
	/**
	 * @see org.openmrs.logic.LogicExpression#getRightOperand()
	 */
	public Object getRightOperand() {
		return this.operand;
	}
	
	/**
	 * @see org.openmrs.logic.LogicExpression#getTransform()
	 */
	public LogicTransform getTransform() {
		return transform;
	}
	
	/**
	 * @see org.openmrs.logic.LogicExpression#setTransform(org.openmrs.logic.LogicTransform)
	 */
	public void setTransform(LogicTransform transform) {
		this.transform = transform;
	}
}

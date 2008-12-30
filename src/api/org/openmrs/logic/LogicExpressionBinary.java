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
public class LogicExpressionBinary implements LogicExpression {
	
	private ArrayList<Object> operands = new ArrayList<Object>(2);
	
	private Operator operator = null;
	
	private LogicTransform transform = null;
	
	public LogicExpressionBinary(Object operand1, Object operand2, Operator operator) {
		this.operands.add(operand1);
		this.operands.add(operand2);
		this.operator = operator;
	}
	
	public Operator getOperator() {
		return this.operator;
	}
	
	public String getRootToken() {
		
		for (Object operand : operands) {
			if (operand != null) {
				if (operand instanceof LogicExpression) {
					return ((LogicExpression) operand).getRootToken();
				} else {
					return operand.toString();
				}
				
			}
		}
		
		return null;
	}
	
	public String toString() {
		
		String result = "";
		
		if (this.transform != null) {
			result += transform.toString() + " {";
		}
		result += this.operands.get(0) + " " + operator + " " + this.operands.get(1);
		if (this.transform != null) {
			result += "}";
		}
		return "(" + result + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LogicExpressionBinary)) {
			return false;
		}
		
		LogicExpressionBinary compExpression = (LogicExpressionBinary) obj;
		
		if (!safeEquals(this.operator, compExpression.getOperator())) {
			return false;
		}
		
		if (!safeEquals(this.transform, compExpression.getTransform())) {
			return false;
		}
		
		if (this.operands.size() != compExpression.getOperands().size()) {
			return false;
		}
		
		for (Object currOperand : this.operands) {
			if (!compExpression.getOperands().contains(currOperand)) {
				return false;
			}
		}
		
		for (Object currOperand : compExpression.getOperands()) {
			if (!this.operands.contains(currOperand)) {
				return false;
			}
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
	
	public ArrayList<Object> getOperands() {
		return operands;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((operator == null) ? 0 : operator.hashCode());
		result = prime * result + ((transform == null) ? 0 : transform.hashCode());
		for (Object currOperand : this.operands) {
			result = prime * result + ((currOperand == null) ? 0 : currOperand.hashCode());
		}
		return result;
	}
	
	/**
	 * @see org.openmrs.logic.LogicExpression#getRightOperand()
	 */
	public Object getRightOperand() {
		return this.operands.get(1);
	}
	
	public Object getLeftOperand() {
		return this.operands.get(0);
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

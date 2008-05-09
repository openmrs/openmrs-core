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

import java.util.Date;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.logic.op.Operator;

/**
 * 
 * Used to create a hierarchical representation of a criteria (e.g., similar to
 * a parse tree). Criteria can be generated through a series of method calls;
 * each method call returns another criteria object containing the prior
 * criteria and the newly added criteria.
 * 
 * This class has two purposes:
 * <ol>
 * <li>provide a mechanism for building criteria</li>
 * <li>provide a structure that can be passed to the DAO level for analysis
 * &amp; execution</li>
 * </ol>
 * 
 * In its simplest form, a criteria is equivalent to a token &mdash; e.g., the
 * following two methods should return the same result:
 * <ul>
 * <li><code>LogicService.eval(myPatient, "CD4 COUNT");</code></li>
 * <li><code>LogicService.eval(myPatient, new LogicCriteria("CD4 COUNT"));</code></li>
 * </ul>
 * 
 * However, when criteria or restrictions need to be placed on the token, then a
 * LogicCriteria can be used to define these restrictions, e.g.
 * <code>new LogicCriteria("CD4 COUNT").lt(200).within(Duration.months(6))</code>
 * 
 */

public class LogicCriteria {

    private Map<String, Object> logicParameters = null;

    private Operator operator = null;

    private Object leftOperand = null;

    private Object rightOperand = null;

    /**
     * Used for creating a simple token-based criteria, which can later be
     * refined by using LogicCriteria methods.
     * 
     * @param token
     */
    public LogicCriteria(String token) {
        rightOperand = token;
    }

    /**
     * Used for passing arguments to a rule
     * 
     * @param token
     * @param args
     */
    public LogicCriteria(String token, Map<String, Object> logicParameters) {
        rightOperand = token;
        this.logicParameters = logicParameters;
    }

    // shouldn't be used outside of Logic Service
    public LogicCriteria(Operator operator) {
        this.operator = operator;
    }
    
    private LogicCriteria(Object leftOperand, Operator operator, Object rightOperand)
    {
    	this.leftOperand = leftOperand;
    	this.operator = operator;
    	this.rightOperand = rightOperand;
    }
    
    private LogicCriteria createAndCriteria(Operator operator,Object rightOperand)
    {
    	return new LogicCriteria(this,Operator.AND,
                          new LogicCriteria(null, operator, rightOperand));
    }
    
    //--Logic Operators
    public LogicCriteria and(LogicCriteria logicCriteria)
    {
    	return new LogicCriteria(this,Operator.AND,logicCriteria);
    }
    
    public LogicCriteria or(LogicCriteria logicCriteria)
    {
    	return new LogicCriteria(this,Operator.OR,logicCriteria);
    }
    
    //--Transform Operators
    public LogicCriteria last() {
    	return new LogicCriteria(null,Operator.LAST,this);
    }
    
    public LogicCriteria first() {
    	return new LogicCriteria(null,Operator.FIRST,this);
    }
    
    public LogicCriteria exists() {
        return new LogicCriteria(null,Operator.EXISTS, this);
    }

    public LogicCriteria notExists() {
        return new LogicCriteria(null,Operator.NOT_EXISTS, this);
    }

    public LogicCriteria not() {
        return new LogicCriteria(null,Operator.NOT, this);
    }
    
    //--Comparison Operators
    public LogicCriteria asOf(Date value) {
        return createAndCriteria(Operator.ASOF, value);
    }

    public LogicCriteria before(Date value) {
        	
        return createAndCriteria(Operator.BEFORE,value);
    }

    public LogicCriteria after(Date value) {
        return createAndCriteria(Operator.AFTER, value);
    }

    public LogicCriteria contains(Integer value) {
        return createAndCriteria(Operator.CONTAINS, value);
    }

    public LogicCriteria contains(Float value) {
        return createAndCriteria(Operator.CONTAINS, value);
    }

    public LogicCriteria contains(String value) {
        return createAndCriteria(Operator.CONTAINS, value);
    }

    public LogicCriteria contains(Date value) {
        return createAndCriteria(Operator.CONTAINS, value);
    }

    public LogicCriteria contains(Concept value) {
        return createAndCriteria(Operator.CONTAINS, value);
    }

    public LogicCriteria equalTo(Integer value) {
        return createAndCriteria(Operator.EQUALS, value);
    }

    public LogicCriteria equalTo(Float value) {
        return createAndCriteria(Operator.EQUALS, value);
    }

    public LogicCriteria equalTo(String value) {
        return createAndCriteria(Operator.EQUALS, value);
    }

    public LogicCriteria equalTo(Date value) {
        return createAndCriteria(Operator.EQUALS, value);
    }

    public LogicCriteria equalTo(Concept value) {
        return createAndCriteria(Operator.EQUALS, value);
    }

    public LogicCriteria gte(Integer value) {
        return createAndCriteria(Operator.GTE, value);
    }

    public LogicCriteria gte(Float value) {
        return createAndCriteria(Operator.GTE, value);
    }

    public LogicCriteria gte(String value) {
        return createAndCriteria(Operator.GTE, value);
    }

    public LogicCriteria gte(Date value) {
        return createAndCriteria(Operator.GTE, value);
    }


    public LogicCriteria gt(Integer value) {
        return createAndCriteria(Operator.GT, value);
    }

    public LogicCriteria gt(Float value) {
        return createAndCriteria(Operator.GT, value);
    }

    public LogicCriteria gt(String value) {
        return createAndCriteria(Operator.GT, value);
    }

    public LogicCriteria gt(Date value) {
        return createAndCriteria(Operator.GT, value);
    }

    public LogicCriteria lt(Integer value) {
        return createAndCriteria(Operator.LT, value);
    }

    public LogicCriteria lt(Float value) {
        return createAndCriteria(Operator.LT, value);
    }

    public LogicCriteria lt(String value) {
        return createAndCriteria(Operator.LT, value);
    }

    public LogicCriteria lt(Date value) {
        return createAndCriteria(Operator.LT, value);
    }

    public LogicCriteria lte(Integer value) {
        return createAndCriteria(Operator.LTE, value);
    }

    public LogicCriteria lte(Float value) {
        return createAndCriteria(Operator.LTE, value);
    }

    public LogicCriteria lte(String value) {
        return createAndCriteria(Operator.LTE, value);
    }

    public LogicCriteria lte(Date value) {
        return createAndCriteria(Operator.LTE, value);
    }
    
    public LogicCriteria within(Duration duration) {
        return createAndCriteria(Operator.WITHIN, duration);
    }    

    public Map<String, Object> getLogicParameters() {
        return logicParameters;
    }

    public void setLogicParameters(Map<String, Object> logicParameters) {
        this.logicParameters = logicParameters;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public Object getLeftOperand() {
        return leftOperand;
    }

    public void setLeftOperand(Object leftOperand) {
        this.leftOperand = leftOperand;
    }

    public Object getRightOperand() {
        return rightOperand;
    }

    public void setRightOperand(Object rightOperand) {
        this.rightOperand = rightOperand;
    }

    public String toString() {
        return "[" + leftOperand + " " + operator + " " + rightOperand + "]";
    }
    
    public String getRootToken() {
		Operator currOperator = null;
		Object currLogicNode = this;

		//we are looking for the base string token for this Logic Criteria
		while (currLogicNode instanceof LogicCriteria) {
			currOperator = ((LogicCriteria) currLogicNode).getOperator();
			//keep going left down the logic criteria tree if
			//AND/OR is the operator
			if (currOperator == Operator.AND || currOperator == Operator.OR) {
				currLogicNode = ((LogicCriteria) currLogicNode).getLeftOperand();
			}else
			{
				//look right down the tree for all other operators
				currLogicNode = ((LogicCriteria) currLogicNode).getRightOperand();
			}
		}
		
		if(currLogicNode instanceof String)
		{
			return (String) currLogicNode;
		}

		return null;
	}
    
    

	/**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result
	            + ((leftOperand == null) ? 0 : leftOperand.hashCode());
	    result = prime * result
	            + ((operator == null) ? 0 : operator.hashCode());
	    result = prime * result
	            + ((logicParameters == null) ? 0 : logicParameters.hashCode());
	    result = prime * result
	            + ((rightOperand == null) ? 0 : rightOperand.hashCode());
	    return result;
    }

	/**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
    	
    	if(!(obj instanceof LogicCriteria))
    	{
    		return false;
    	}
    	
    	LogicCriteria compCriteria = (LogicCriteria) obj;
    	
    	//see if left operands are equal
    	if(leftOperand == null && 
    			compCriteria.getLeftOperand() != null)
    	{
    		return false;
    	}
    	if(!safeEquals(leftOperand,compCriteria.getLeftOperand()))
    	{
    		return false;
    	}
    	
    	//see if right operands are equal
    	if(rightOperand == null && 
    			compCriteria.getRightOperand() != null)
    	{
    		return false;
    	}
    	if(!safeEquals(rightOperand,compCriteria.getRightOperand()))
    	{
    		return false;
    	}
    	
    	//see if operators are equal
    	if(operator == null && 
    			compCriteria.getOperator() != null)
    	{
    		return false;
    	}
    	
    	if(!safeEquals(operator,compCriteria.getOperator()))
    	{
    		return false;
    	}
	    
	    return true;
    }
    
	private boolean safeEquals(Object a, Object b) {
		if (a == null && b == null)
			return true;
		if (a == null || b == null)
			return false;
		return a.equals(b);
	}

}
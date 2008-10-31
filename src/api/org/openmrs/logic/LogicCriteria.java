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

    private LogicExpression expression = null;

    /**
     * Used for creating a simple token-based criteria, which can later be
     * refined by using LogicCriteria methods.
     * 
     * @param token
     */
    public LogicCriteria(String token) {
    	this(null,token);
    }

    /**
     * Used for passing arguments to a rule
     * 
     * @param token
     * @param args
     */
    public LogicCriteria(String token, Map<String, Object> logicParameters) {
        this(token);
        this.logicParameters = logicParameters;
    }
    
    public LogicCriteria(Operator operator, Object operand)
    {       
        if(operator==Operator.NOT){
            this.expression = new LogicExpressionUnary(operand,operator);

        }else{
            this.expression = new LogicExpressionBinary(null, operand,operator);
        }
    }
    
    public LogicCriteria(Operator operator, Object operand,
    		Map<String, Object> logicParameters)
    {
    	this(operator,operand);
    	this.logicParameters = logicParameters;
    }
    
    public LogicCriteria appendExpression(Operator operator,Object operand) 
    {
        this.expression = new LogicExpressionBinary(this.expression,operand,operator);
       
    	return this;
    }
    
    private LogicCriteria appendExpression(Operator operator, LogicExpression expression) 
    {
    	if(expression != null){
    		this.expression = new LogicExpressionBinary(this.expression,expression,operator);
    	}else{
    		this.expression = new LogicExpressionUnary(this.expression,operator);
    	}
		
    	return this;
    }
    
    private LogicCriteria appendTransform(Operator operator,Integer numResults,String sortColumn) {

    	LogicTransform transform = new LogicTransform(operator);
    	if(numResults != null){
    		transform.setNumResults(numResults);
    	}
    	if(sortColumn != null){
    		transform.setSortColumn(sortColumn);
    	}
		this.expression.setTransform(transform);

		return this;
	}
    
    public LogicCriteria applyTransform(Operator operator)
    {
    	if (operator == Operator.LAST) {
			return last();
		} else if (operator == Operator.FIRST) {
			return first();
		} else if (operator == Operator.EXISTS) {
			return exists();
		} else if (operator == Operator.NOT_EXISTS) {
			return notExists();
		} else if (operator == Operator.COUNT) {
			return count();
		} else if (operator == Operator.AVERAGE){
			return average();
		}

		return this; // no valid transform
    	
     }
    
    // --Logic Operators joining criteria
    public LogicCriteria appendCriteria(Operator operator,
	        LogicCriteria logicCriteria) {
		return appendExpression( operator,logicCriteria.getExpression());
	}
    
    public LogicCriteria and(LogicCriteria logicCriteria)
    {
		return appendExpression( Operator.AND,logicCriteria.getExpression());
    }
    
    public LogicCriteria or(LogicCriteria logicCriteria)
    {
		return appendExpression( Operator.OR,logicCriteria.getExpression());
    }
    
    public LogicCriteria not() {
		return appendExpression( Operator.NOT,null);
    }
    
    //--Transform Operators
    public LogicCriteria count() {
    	return this.appendTransform(Operator.COUNT,null,null);
    }
    public LogicCriteria average() {
    	return this.appendTransform(Operator.AVERAGE,null,null);
    }
    public LogicCriteria last() {
    	return this.appendTransform(Operator.LAST,null,null);
    }
    public LogicCriteria last(Integer numResults) {
    	return this.appendTransform(Operator.LAST,numResults,null);
    }
    
    //TODO implement this method
    //after implementing switch to public
    private LogicCriteria last(String sortComponent) {
    	return this.appendTransform(Operator.LAST,null,sortComponent);
    }
    
    //TODO implement this method
    //after implementing switch to public
    private LogicCriteria last(Integer numResults,String sortComponent) {
    	return this.appendTransform(Operator.LAST,numResults,sortComponent);
    }
    
    public LogicCriteria first(){
    	return this.appendTransform(Operator.FIRST,null,null);
    }
    
    public LogicCriteria first(Integer numResults) {
    	return this.appendTransform(Operator.FIRST,numResults,null);
    }
    
    public LogicCriteria first(String sortComponent) {
    	return this.appendTransform(Operator.FIRST,null,sortComponent);
    }
    
    public LogicCriteria first(Integer numResults,String sortComponent) {
    	return this.appendTransform(Operator.FIRST,numResults,sortComponent);
    }
    
    public LogicCriteria distinct() {
    	return this.appendTransform(Operator.DISTINCT,null,null);
    }
    
    public LogicCriteria exists() {
        return this.appendTransform(Operator.EXISTS,null,null);
    }

    public LogicCriteria notExists() {
        return this.appendTransform(Operator.NOT_EXISTS,null,null);
    }
    
    //--Comparison Operators
    public LogicCriteria asOf(Date value) {
        return appendExpression(Operator.ASOF, value);
    }

    public LogicCriteria before(Date value) {
        	
        return appendExpression(Operator.BEFORE,value);
    }

    public LogicCriteria after(Date value) {
        return appendExpression(Operator.AFTER, value);
    }

    public LogicCriteria contains(Object value) {
        return appendExpression(Operator.CONTAINS, value);
    }
    
    public LogicCriteria contains(int value) {
        return appendExpression(Operator.CONTAINS, value);
    }

    public LogicCriteria contains(float value) {
        return appendExpression(Operator.CONTAINS, value);
    }
    public LogicCriteria contains(double value) {
        return appendExpression(Operator.CONTAINS, value);
    }

    public LogicCriteria equalTo(Object value) {
        return appendExpression(Operator.EQUALS, value);
    }
    
    public LogicCriteria equalTo(int value) {
        return appendExpression(Operator.EQUALS, value);
    }

    public LogicCriteria equalTo(float value) {
        return appendExpression(Operator.EQUALS, value);
    }
    
    public LogicCriteria equalTo(double value) {
        return appendExpression(Operator.EQUALS, value);
    }

    public LogicCriteria gte(Object value) {
        return appendExpression(Operator.GTE, value);
    }
    
    public LogicCriteria gte(int value) {
        return appendExpression(Operator.GTE, value);
    }

    public LogicCriteria gte(float value) {
        return appendExpression(Operator.GTE, value);
    }
    
    public LogicCriteria gte(double value) {
        return appendExpression(Operator.GTE, value);
    }

    public LogicCriteria gt(Object value) {
        return appendExpression(Operator.GT, value);
    }
    
    public LogicCriteria gt(int value) {
        return appendExpression(Operator.GT, value);
    }

    public LogicCriteria gt(float value) {
        return appendExpression(Operator.GT, value);
    }
    
    public LogicCriteria gt(double value) {
        return appendExpression(Operator.GT, value);
    }

    public LogicCriteria lt(Object value) {
        return appendExpression(Operator.LT, value);
    }
    
    public LogicCriteria lt(int value) {
        return appendExpression(Operator.LT, value);
    }

    public LogicCriteria lt(float value) {
        return appendExpression(Operator.LT, value);
    }
    
    public LogicCriteria lt(double value) {
        return appendExpression(Operator.LT, value);
    }

    public LogicCriteria lte(Object value) {
        return appendExpression(Operator.LTE, value);
    }
    
    public LogicCriteria lte(int value) {
        return appendExpression(Operator.LTE, value);
    }

    public LogicCriteria lte(float value) {
        return appendExpression(Operator.LTE, value);
    }
    
    public LogicCriteria lte(double value) {
        return appendExpression(Operator.LTE, value);
    }
    
    public LogicCriteria within(Duration duration)  {
        return appendExpression(Operator.WITHIN, duration);
    }    

    public Map<String, Object> getLogicParameters() {
        return logicParameters;
    }

    public void setLogicParameters(Map<String, Object> logicParameters) {
        this.logicParameters = logicParameters;
    }

    public String toString() {
    	return this.expression.toString();
    }
    
    public String getRootToken() {
		return this.expression.getRootToken();
	}
    
    /**
     * Parses a query string into a LogicCriteria object.  For example, a phrase like <em>"LAST
     * {CD4 COUNT} > 200"</em> is parsed into a LogicCriteria object equivalent to:
     * <code>new LogicCriteria("CD4 COUNT").gt(200).last()</code>.
     * 
     * This function will fail quietly.  If an exception occurs during parsing, then this method
     * will return a LogicCriteria constructed with the given query string without any parsing.
     * The actual work of parsing is performed by the LogicQueryParser class.
     * 
     * @param query a logic query to be parsed
     * @return the equivalent LogicCriteria to the given query string
     * @throws LogicException 
     * @see org.openmrs.logic.LogicQueryParser
     */
    public static LogicCriteria parse(String query) throws LogicException {
    	try {
    		return LogicQueryParser.parse(query);
    	} catch (LogicQueryParseException e) {
    		return new LogicCriteria(query);
    	}
    }

	/**
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		result = prime
		        * result
		        + ((this.expression == null) ? 0
		                : this.expression.hashCode());
		result = prime * result
		        + ((logicParameters == null) ? 0 : logicParameters.hashCode());
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
    	
    	if(!safeEquals(this.expression,
    	               compCriteria.getExpression())){
    		return false;
    	}
    	
    	if(!safeEquals(this.logicParameters,
    	               compCriteria.getLogicParameters())){
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

	public LogicExpression getExpression() {
    	return expression;
    }
}
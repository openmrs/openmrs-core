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

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.hibernate.criterion.Distinct;
import org.openmrs.logic.op.And;
import org.openmrs.logic.op.AsOf;
import org.openmrs.logic.op.Average;
import org.openmrs.logic.op.Count;
import org.openmrs.logic.op.First;
import org.openmrs.logic.op.GreaterThan;
import org.openmrs.logic.op.GreaterThanEquals;
import org.openmrs.logic.op.In;
import org.openmrs.logic.op.Last;
import org.openmrs.logic.op.LessThan;
import org.openmrs.logic.op.LessThanEquals;
import org.openmrs.logic.op.Operand;
import org.openmrs.logic.op.Operator;
import org.openmrs.logic.op.Or;
import org.openmrs.logic.op.TransformOperator;
import org.openmrs.logic.op.Within;

/**
 * Used to create a hierarchical representation of a criteria (e.g., similar to a parse tree).
 * Criteria can be generated through a series of method calls; each method call returns another
 * criteria object containing the prior criteria and the newly added criteria. This class has two
 * purposes:
 * <ol>
 * <li>provide a mechanism for building criteria</li>
 * <li>provide a structure that can be passed to the DAO level for analysis &amp; execution</li>
 * </ol>
 * In its simplest form, a criteria is equivalent to a token &mdash; e.g., the following two methods
 * should return the same result:
 * <ul>
 * <li><code>LogicService.eval(myPatient, "CD4 COUNT");</code></li>
 * <li><code>LogicService.eval(myPatient, new LogicCriteria("CD4 COUNT"));</code></li>
 * </ul>
 * However, when criteria or restrictions need to be placed on the token, then a LogicCriteria can
 * be used to define these restrictions, e.g.
 * <code>new LogicCriteriaImpl("CD4 COUNT").lt(200).within(Duration.months(6))</code>
 */
public interface LogicCriteria {
	
	/**
	 * Create a new LogicExpression using the <code>operand</code> and <code>operator</code> and
	 * then append them to the current LogicCriteria
	 * 
	 * @param operator one of the Operator object to be appended to the current LogicCriteria
	 * @param operand one of the Operand object
	 * @return a new LogicCriteria containing the existing and new LogicExpression
	 */
	public LogicCriteria appendExpression(Operator operator, Operand operand);
	
	/**
	 * @see LogicCriteria#appendExpression(Operator, Operand)
	 */
	public LogicCriteria appendExpression(Operator operator, String operand);
	
	/**
	 * @see LogicCriteria#appendExpression(Operator, Operand)
	 */
	public LogicCriteria appendExpression(Operator operator, double operand);
	
	/**
	 * Apply a transformation operator to a logic expression
	 * 
	 * @param operator type of the {@link TransformOperator}
	 * @return new logic criteria containing the {@link TransformOperator}
	 */
	public LogicCriteria applyTransform(Operator operator);
	
	// --Logic Operators joining criteria
	/**
	 * Append a LogicCriteria with another LogicCriteria using an operator
	 * 
	 * @param operator one type of {@link Operator}
	 * @param logicCriteria {@link LogicCriteria} to be appended
	 * @return new {@link LogicCriteria} containing existing and the new {@link LogicCriteria}
	 */
	public LogicCriteria appendCriteria(Operator operator, LogicCriteria logicCriteria);
	
	/**
	 * Append the LogicCriteria using the {@link And} operator
	 * 
	 * @param logicCriteria LogicCriteria to be appended
	 * @return LogicCriteria that is the combination of existing and the new LogicCriteria
	 * @see And
	 */
	public LogicCriteria and(LogicCriteria logicCriteria);
	
	/**
	 * Append the LogicCriteria using the {@link Or} operator
	 * 
	 * @param logicCriteria LogicCriteria to be appended
	 * @return LogicCriteria that is the combination of existing and the new LogicCriteria
	 * @see Or
	 */
	public LogicCriteria or(LogicCriteria logicCriteria);
	
	public LogicCriteria not();
	
	//--Transform Operators
	/**
	 * Apply the {@link Count} operator to the LogicCriteria
	 * 
	 * @return LogicCriteria with count applied
	 * @see Count
	 */
	public LogicCriteria count();
	
	/**
	 * Apply the {@link Average} operator to the LogicCriteria
	 * 
	 * @return LogicCriteria with Average applied
	 * @see Average
	 */
	public LogicCriteria average();
	
	/**
	 * Apply the {@link Last} operator to the LogicCriteria
	 * 
	 * @return LogicCriteria with Last applied
	 * @see Last
	 */
	public LogicCriteria last();
	
	/**
	 * @see LogicCriteria#last()
	 */
	public LogicCriteria last(Integer numResults);
	
	/**
	 * Apply the {@link First} operator to the LogicCriteria
	 * 
	 * @return LogicCriteria with First applied
	 * @see First
	 */
	public LogicCriteria first();
	
	/**
	 * @see LogicCriteria#first()
	 */
	public LogicCriteria first(Integer numResults);
	
	/**
	 * @see LogicCriteria#first()
	 */
	public LogicCriteria first(String sortComponent);
	
	/**
	 * @see LogicCriteria#first()
	 */
	public LogicCriteria first(Integer numResults, String sortComponent);
	
	/**
	 * Apply the {@link Distinct} operator to the LogicCriteria
	 * 
	 * @return LogicCriteria with Distinct operator in it
	 * @see org.openmrs.logic.op.Distinct
	 */
	public LogicCriteria distinct();
	
	public LogicCriteria exists();
	
	public LogicCriteria notExists();
	
	//--Comparison Operators
	/**
	 * Add a {@link AsOf} expression to the current LogicCriteria
	 * 
	 * @param value the operand for the AsOf operator
	 * @return LogicCriteria with AsOf expression
	 * @see AsOf
	 */
	public LogicCriteria asOf(Date value);
	
	/**
	 * Add a {@link Before} expression to the current LogicCriteria
	 * 
	 * @param value the operand for the Before operator
	 * @return LogicCriteria with Before expression
	 * @see Before
	 */
	public LogicCriteria before(Date value);
	
	/**
	 * Add a {@link In} expression to the current LogicCriteria
	 * 
	 * @param value the operand for the In operator
	 * @return LogicCriteria with In expression
	 * @see In
	 */
	public LogicCriteria after(Date value);
	
	/**
	 * Add a {@link After} expression to the current LogicCriteria
	 * 
	 * @param value the operand for the After operator
	 * @return LogicCriteria with After expression
	 * @see After
	 */
	public LogicCriteria in(Collection<?> value);
	
	/**
	 * @see LogicCriteria#contains(String)
	 */
	public LogicCriteria contains(Operand value);
	
	/**
	 * @see LogicCriteria#contains(String)
	 */
	public LogicCriteria contains(int value);
	
	/**
	 * @see LogicCriteria#contains(String)
	 */
	public LogicCriteria contains(float value);
	
	/**
	 * @see LogicCriteria#contains(String)
	 */
	public LogicCriteria contains(double value);
	
	/**
	 * Add a {@link Contains} expression to the current LogicCriteria
	 * 
	 * @param value the operand for the Contains operator
	 * @return LogicCriteria with Contains expression
	 * @see Contains
	 */
	public LogicCriteria contains(String value);
	
	/**
	 * @see LogicCriteria#equalTo(String)
	 */
	public LogicCriteria equalTo(Operand value);
	
	/**
	 * @see LogicCriteria#equalTo(String)
	 */
	public LogicCriteria equalTo(int value);
	
	/**
	 * @see LogicCriteria#equalTo(String)
	 */
	public LogicCriteria equalTo(float value);
	
	/**
	 * @see LogicCriteria#equalTo(String)
	 */
	public LogicCriteria equalTo(double value);
	
	/**
	 * Add a {@link Equals} expression to the current LogicCriteria
	 * 
	 * @param value the operand for the Equals operator
	 * @return LogicCriteria with Equals expression
	 * @see Equals
	 */
	public LogicCriteria equalTo(String value);
	
	/**
	 * @see LogicCriteria#gte(double)
	 */
	public LogicCriteria gte(Operand value);
	
	/**
	 * @see LogicCriteria#gte(double)
	 */
	public LogicCriteria gte(int value);
	
	/**
	 * @see LogicCriteria#gte(double)
	 */
	public LogicCriteria gte(float value);
	
	/**
	 * Add a {@link GreaterThanEquals} expression to the current LogicCriteria
	 * 
	 * @param value the operand for the GreaterThanEquals operator
	 * @return LogicCriteria with GreaterThanEquals expression
	 * @see GreaterThanEquals
	 */
	public LogicCriteria gte(double value);
	
	/**
	 * @see LogicCriteria#gt(double)
	 */
	public LogicCriteria gt(Operand value);
	
	/**
	 * @see LogicCriteria#gt(double)
	 */
	public LogicCriteria gt(int value);
	
	/**
	 * @see LogicCriteria#gt(double)
	 */
	public LogicCriteria gt(float value);
	
	/**
	 * Add a {@link GreaterThan} expression to the current LogicCriteria
	 * 
	 * @param value the operand for the GreaterThan operator
	 * @return LogicCriteria with GreaterThan expression
	 * @see GreaterThan
	 */
	public LogicCriteria gt(double value);
	
	/**
	 * @see LogicCriteria#lt(double)
	 */
	public LogicCriteria lt(Operand value);
	
	/**
	 * @see LogicCriteria#lt(double)
	 */
	public LogicCriteria lt(int value);
	
	/**
	 * @see LogicCriteria#lt(double)
	 */
	public LogicCriteria lt(float value);
	
	/**
	 * Add a {@link LessThan} expression to the current LogicCriteria
	 * 
	 * @param value the operand for the LessThan operator
	 * @return LogicCriteria with LessThan expression
	 * @see LessThan
	 */
	public LogicCriteria lt(double value);
	
	/**
	 * @see LogicCriteria#lte(double)
	 */
	public LogicCriteria lte(Operand value);
	
	/**
	 * @see LogicCriteria#lte(double)
	 */
	public LogicCriteria lte(int value);
	
	/**
	 * @see LogicCriteria#lte(double)
	 */
	public LogicCriteria lte(float value);
	
	/**
	 * Add a {@link LessThanEquals} expression to the current LogicCriteria
	 * 
	 * @param value the operand for the LessThanEquals operator
	 * @return LogicCriteria with LessThanEquals expression
	 * @see LessThanEquals
	 */
	public LogicCriteria lte(double value);
	
	/**
	 * Add a duration expression to the current LogicCriteria
	 * 
	 * @param duration the {@link Duration} object
	 * @return LogicCriteria containing duration expression
	 * @see Within
	 */
	public LogicCriteria within(Duration duration);
	
	/**
	 * @return
	 */
	public Map<String, Object> getLogicParameters();
	
	/**
	 * @param logicParameters
	 */
	public void setLogicParameters(Map<String, Object> logicParameters);
	
	/**
	 * Method to get the root token of the current LogicCriteria. <code>
	 * logicService.parseString("'CD4 COUNT'").getRootToken().equals("CD4 COUNT");
	 * </code>
	 * 
	 * @return the root token of the LogicCriteria
	 */
	public String getRootToken();
	
	/**
	 * Method to get the LogicExpression backing the current LogicCriteria
	 * 
	 * @return the LogicExpression of the current LogicCriteria
	 */
	public LogicExpression getExpression();
	
}

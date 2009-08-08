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
package org.openmrs.logic.util;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.logic.LogicCriteria;

/**
 *
 */
public class LogicCriteriaBuilder {
	
	private static Log log = LogFactory.getLog(LogicCriteriaBuilder.class);
	
	public static String CRITERION_PATTERN = "\\.";
	
	public static String TOKEN_PATTERN = "TOKEN(.*)";
	
	// TODO We need to be able to support other formats, but this should be the first 
	public static DateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
	
	/**
	 * Auto generated method comment
	 * 
	 * @param criteriaString
	 * @return
	 * @throws ParseException
	 */
	public static LogicCriteria serialize(String criteriaString) throws ParseException {
		LogicCriteria criteria = null;
		
		log.info("Criteria string: " + criteriaString);
		// Get atomic elements of a logic criteria string
		String[] elements = criteriaString.split(CRITERION_PATTERN);
		
		log.info("Elements: " + elements);
		
		//if (elements == null || elements.length < 1)
		//throw new ParseException("Logic criteria must contain at least one operand", 0);
		
		String token = extractOperand(elements[0]);
		
		log.info("Token: " + token);
		
		// Instantiate the logic criteria with the given token
		criteria = new LogicCriteria(token);
		
		// Iterate over the rest of the string to add logic criteria
		for (int i = 1; i < elements.length; i++) {
			
			// Get criterion as an upper case string with no spaces 
			String expression = elements[i].toUpperCase().trim();
			
			// Might be null or empty string
			String operand = extractOperand(expression);
			
			log.info("Expression: " + expression);
			log.info("Operand: " + operand);
			
			if (expression.toUpperCase().startsWith("AFTER")) {
				Date value = DATE_FORMATTER.parse(operand);
				criteria = criteria.after(value);
			} else if (expression.toUpperCase().startsWith("AND")) {
				throw new UnsupportedOperationException();
			} else if (expression.toUpperCase().startsWith("ASOF")) {
				Date value = DATE_FORMATTER.parse(operand);
				criteria = criteria.asOf(value);
			} else if (expression.toUpperCase().startsWith("BEFORE")) {
				Date value = DATE_FORMATTER.parse(operand);
				criteria = criteria.before(value);
			} else if (expression.toUpperCase().startsWith("CONTAINS")) {
				criteria = criteria.contains(operand);
			}
			// TODO 	equalTo() supports a few different objects, we only support String here
			else if (expression.toUpperCase().startsWith("EQUALS")) {
				criteria = criteria.equalTo(operand);
			} else if (expression.toUpperCase().startsWith("EXISTS")) {
				criteria = criteria.exists();
			} else if (expression.toUpperCase().startsWith("FIRST")) {
				criteria = criteria.first();
			}
			// TODO Assuming that GT is for numeric
			else if (expression.toUpperCase().startsWith("GT")) {
				Float value = (Float) DecimalFormat.getInstance().parse(operand);
				criteria = criteria.gt(value);
			} else if (expression.toUpperCase().startsWith("LAST")) {
				criteria = criteria.last();
			} else if (expression.toUpperCase().startsWith("LT")) {
				Float value = (Float) DecimalFormat.getInstance().parse(operand);
				criteria = criteria.lt(value);
			} else if (expression.toUpperCase().startsWith("NOT")) {
				throw new UnsupportedOperationException();
			} else if (expression.toUpperCase().startsWith("OR")) {
				throw new UnsupportedOperationException();
			} else if (expression.toUpperCase().startsWith("WITHIN")) {
				Float value = (Float) DecimalFormat.getInstance().parse(operand);
				criteria = criteria.lt(value);
			}
		}
		return criteria;
		
	}
	
	/**
	 * @param criteria
	 * @return
	 */
	public static String deserialize(LogicCriteria criteria) {
		return criteria.toString();
	}
	
	/**
	 * Parses an expression and returns the operator(operand).
	 * 
	 * @param expression
	 * @return
	 * @throws ParseException
	 */
	public static String extractOperand(String expression) throws ParseException {
		
		// Matches:  OPERATOR(OPERAND)
		Pattern pattern = Pattern.compile("(\\w*)\\((.*)\\)");
		
		if (expression == null || (expression.length() == 0))
			throw new ParseException("Expression must contain operator", 0);
		
		Matcher matcher = pattern.matcher(expression);
		while (matcher.find()) {
			// Get the second group clause 
			return matcher.group(2);
		}
		return "EMPTY";
	}
}

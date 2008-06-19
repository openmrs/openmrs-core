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
package org.openmrs.report;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openmrs.Cohort;
import org.openmrs.Location;
import org.openmrs.api.context.Context;

/**
 * The EvaluationContext provides the following capabilities:
 *  - A baseCohort, i.e. the universe of patients relevant to this context (defaults to all patients)
 *  - An in-memory cache which can be used to persist and retrieve objects. Note that this cache is cleared whenever any changes are made to baseCohort or any parameter values.
 *  - Capabilities to add, remove, and retrieve parameter values
 *  - Capabilities to evaluate parametric expressions, e.g. ${someDateParameterName+30d}
 */
public class EvaluationContext {
	
	protected static Logger log = Logger.getLogger(EvaluationContext.class);
	
	public static final String START_OF_EXPRESSION = "${";
	public static final String END_OF_EXPRESSION = "}";
	public static final Pattern DATE_OPERATION_PATTERN = Pattern.compile("(\\d{4}\\-\\d{2}\\-\\d{2}\\ \\d{2}:\\d{2}:\\d{2})(([+-])(\\d{1,})([dwmy]))?");
	
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private Cohort baseCohort;
	private Map<Parameterizable, Map<Parameter, Object>> parameterValues = new HashMap<Parameterizable, Map<Parameter, Object>>();
	private transient Map<String, Object> cache = new HashMap<String, Object>();
	
	public EvaluationContext() { }
	
	public static boolean isExpression(String s) {
		return s != null && s.startsWith(START_OF_EXPRESSION) && s.endsWith(END_OF_EXPRESSION);
	}
	
	/**
	 * Get the cache property
	 * @return Map<String, Object>
	 */
	public Map<String, Object> getCache() {
    	return cache;
    }

	/**
	 * Set the cache property
	 * @param cache
	 */
	public void setCache(Map<String, Object> cache) {
    	this.cache = cache;
    }
	
	/**
	 * Add a value to the cache with a given key
	 * @return Map<String, Object>
	 */
	public void addToCache(String key, Object value) {
		cache.put(key, value);
	}
	
	/**
	 * Remove an entry cached with the given key
	 * @param key
	 */
	public void removeFromCache(String key) {
		cache.remove(key);
	}
	
	/**
	 * Retrieve an entry from the  cached with the given key
	 * @param key
	 */
	public Object getFromCache(String key) {
		return cache.get(key);
	}
	
	/**
	 * Return true if a cache entry exists with the given key
	 * @param key
	 */
	public boolean isCached(String key) {
		return cache.get(key) != null;
	}
	
	/**
	 * Clear the entire cache
	 */
	public void clearCache() {
		cache.clear();
	}

	/**
	 * Add a parameter to the context with the given value with global scope
	 * @param param
	 * @param value
	 */
	public void addParameterValue(Parameter parameter, Object value) {
		addParameterValue(null, parameter, value);
	}
	
	/**
	 * Add a parameter to the context with the given value in the scope of the passed Parameterizable object
	 * @param param
	 * @param value
	 */
	public void addParameterValue(Parameterizable obj, Parameter parameter, Object value) {
		clearCache();
		Map<Parameter, Object> globalParams = parameterValues.get(obj);
		if (globalParams == null) {
			globalParams = new HashMap<Parameter, Object>();
			parameterValues.put(obj, globalParams);
		}
		globalParams.put(parameter, value);
	}
	
	/**
	 * Retrieve all parameter values
	 * @return Map
	 */
	public Map<Parameterizable, Map<Parameter, Object>> getParameterValues() {
    	return parameterValues;
    }
	
	/**
	 * Set all parameter values
	 * @param parameterValues
	 */
	public void setParameterValues(Map<Parameterizable, Map<Parameter, Object>> parameterValues) {
		clearCache();
    	this.parameterValues = parameterValues;
    }
	
	/**
	 * Retrieve a Parameter by Name.  If a parameterizable is passed in, it will
	 * check scope local to the Parameterizable first, and if not found, check global scope
	 * It will return null if not found in either scope
	 * @param obj
	 * @param parameterName
	 */
	public Parameter getParameter(Parameterizable obj, String parameterName) {
		Map<Parameter, Object> params = parameterValues.get(obj);
		if (params != null) {
			for (Parameter parameter: params.keySet()) {
				if (parameterName != null && parameterName.equals(parameter.getName())) {
					return parameter;
				}
				if (obj != null) {
					return getParameter(null, parameterName);
				}
			}
		}
		return null;
	}
	
	/**
	 * Retrieve Parameter from Global Scope only. It will return null if not found
	 * @param parameter
	 */
	public Object getParameter(String parameterName) {
		return getParameter(null, parameterName);
	}
	
	/**
	 * Retrieve parameter value by Parameter.  If a parameterizable is passed in, it will
	 * check scope local to the Parameterizable first, and if not found, check global scope
	 * It will return null if not found in either scope
	 * @param obj
	 * @param parameter
	 */
	public Object getParameterValue(Parameterizable obj, Parameter parameter) {
		Map<Parameter, Object> params = parameterValues.get(obj);
		if (params != null) {
			Object localParam = params.get(parameter);
			if (localParam != null) {
				return localParam;
			}
			if (obj != null) {
				return getParameterValue(null, parameter);
			}
		}
		return null;
	}
	
	/**
	 * Retrieve parameter value in Global Scope only. It will return null if not found
	 * @param parameter
	 */
	public Object getParameterValue(Parameter parameter) {
		return getParameterValue(null, parameter);
	}
	
	/**
	 * Retrieve parameter value by parameter name.  If a parameterizable is passed in, it will
	 * check scope local to the Parameterizable first, and if not found, check global scope
	 * It will return null if not found in either scope
	 * @param parameterName key of the parameter to look for
	 * @param value
	 * @return Object value of the parameter named by <code>parameterName</code>
	 */
	public Object getParameterValue(Parameterizable obj, String parameterName) {
		Parameter param = getParameter(obj, parameterName);
		if (param != null) {
			return getParameterValue(obj, param);
		}
		return null;
	}
	
	/**
	 * Retrieve global parameter value by name
	 * @param parameterName key of the parameter to look for
	 * @param value
	 * @return Object value of the parameter named by <code>parameterName</code>
	 */
	public Object getParameterValue(String parameterName) {
		return getParameterValue(null, parameterName);
	}
	
	
	/**
	 * This method will parse the passed expression and return a value based on the following criteria:
	 * - Any string that matches a parameter within the EvaluationContext will be replaced by the value of that parameter
	 *   ** CURRENTLY REPLACEMENT PARAMETERS MUST EXIST IN THE GLOBAL SCOPE 
	 * - If this date is followed by an expression, it will attempt to evaluate this by incrementing/decrementing days/weeks/months/years as specified
	 * 
	 * - Examples:
	 *   Given 2 parameters:
	 *     - report.startDate = java.util.Date with value of [2007-01-10]
	 *     - report.gender = "male"
	 *   The following should result:
	 *     evaluateExpression("${report.startDate}") -> "2007-01-10" as Date
	 *     evaluateExpression("${report.startDate+5d}") -> "2007-01-15" as Date
	 *     evaluateExpression("${report.startDate-1w}") -> "2007-01-03" as Date
	 *     evaluateExpression("${report.startDate+3m}") -> "2007-04-15" as Date
	 *     evaluateExpression("${report.startDate+1y}") -> "2008-01-10" as Date
	 * 
	 * @param expression
	 * @return
	 * @throws ParameterException
	 */
	public Object evaluateExpression(String expression) throws ParameterException {
		
		if (expression == null) {
			log.warn("evaluateExpression returning null.");
			return null;
		}
		
		log.debug("Starting expression: " + expression);
		boolean containsDate = false;
		
		while (expression.contains(START_OF_EXPRESSION) && expression.contains(END_OF_EXPRESSION)) {
			int startIndex = expression.indexOf(START_OF_EXPRESSION);
			int endIndex = expression.indexOf(END_OF_EXPRESSION);
			
			String toReplace = expression.substring(startIndex, endIndex + END_OF_EXPRESSION.length());
			log.debug("Found expression to replace: " + toReplace);
			String replacement = expression.substring(startIndex + START_OF_EXPRESSION.length(), endIndex);
			log.debug("Stripped this down to: " + replacement);
				
			boolean found = false;
			// Iterate through each parameter and replace where appropriate in the expression string
			Map<Parameter, Object> globalParameters = parameterValues.get(null);
			if (globalParameters != null) {
				log.debug("Starting parameters: " + globalParameters);
				for (Parameter parameter : globalParameters.keySet()) {
					if (replacement.contains(parameter.getName())) {
						found = true;
						Object value = globalParameters.get(parameter);
						if (value == null) {  // If parameter is required, but value is null, throw exception
							throw new ParameterException("Expression [" + replacement + "] requires parameter [" + parameter + "] which is null.");
						}
						log.debug("Starting evaluation of " + replacement + " with " + value);
						
						// Handle date parameters
						if (value instanceof Date) {
							containsDate = true;
							replacement = replacement.replace(parameter.getName(), df.format((Date)value));
							log.debug("Modified to: " + replacement);
							
							// Attempt to evaluate any date arithmetic
							Matcher m = DATE_OPERATION_PATTERN.matcher(replacement);
							Calendar cal = new GregorianCalendar();
							try {
								while (m.find()) {
									log.debug("Found date expression of: " + m.group());
									String foundDate = m.group(1);
									if (m.group(2) != null) {
										int num = ("-".equals(m.group(3)) ? -1 : 1) * Integer.parseInt(m.group(4));
										int field = Calendar.DATE;
										if ("w".equals(m.group(5))) {
											num *= 7;
										}
										else if ("m".equals(m.group(5))) {
											field = Calendar.MONTH;
										}
										else if ("y".equals(m.group(5))) {
											field = Calendar.YEAR;
										}
										cal.setTime(df.parse(foundDate));
										cal.add(field, num);
										foundDate = df.format(cal.getTime());
										log.debug("Calculated date of: " + foundDate);
									}
									replacement = replacement.replaceAll("\\Q"+m.group(0)+"\\E", foundDate);
									log.debug("Modified to: " + replacement);
								}
							}
							catch (Exception e) {
								log.debug(e.getMessage());
								throw new ParameterException("Error parsing dates in expression: " + replacement);
							}
						}
						else if (value instanceof Location) {
							replacement = replacement.replace(parameter.getName(), ((Location)value).getLocationId().toString());
						}
						// Handle default parameters
						else {
							replacement = replacement.replace(parameter.getName(), value.toString());
						}
						log.debug("Modified to: " + replacement);
						expression = expression.replace(toReplace, replacement);
						log.debug("Expression now: " + expression);
					}
				}
			}
			// By default, throw an exception if a parametric expression contains no parameters
			if (!found) {
				throw new ParameterException("Expression [" + expression + "] requires parameter [" + replacement + "] which is not found.");
			}
		}
		// If one of the parameters evaluated was a date, try casting this to a Date object if possible
		if (containsDate) {
			try {
				log.debug("Trying to parse back to a Date: " + expression);
				Date newDate = df.parse(expression);
				log.debug("Returning Date: " + newDate);
				return newDate;
			}
			catch (Exception e) {
				log.debug("Unable to parse into a Date.");
			}
		}
		log.debug("Returning String: " + expression);
		return expression;
	}

	public Cohort getBaseCohort() {
		if (baseCohort == null) {
			// Save this so we don't have to query the database next time. This doesn't clear the cache
			baseCohort = Context.getPatientSetService().getAllPatients();
		}
    	return baseCohort;
    }

	public void setBaseCohort(Cohort baseCohort) {
		clearCache();
    	this.baseCohort = baseCohort;
    }
}

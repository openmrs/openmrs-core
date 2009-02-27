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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses strings into a LogicCriteria object. This provides a convenience mechanism for logic
 * queries to be written out in a simplified, text-based query language rather than being built
 * manually in Java. It also provides a simpler mechanism for external code to send queries to the
 * logic service. A simple logic query follows the form:
 * <code>[ <aggregator> ] {TOKEN} [ <conditions> ]</code> Both the aggregator and the conditions are
 * optional. Token references are surrounded with curlie braces. Aggregator options are:
 * <table width="50%">
 * <col width="40%" /><col width="60%">
 * <tr>
 * <td>LAST</td>
 * <td>The last result in a list, typically the most recent (latest).</td>
 * </tr>
 * <tr>
 * <td><s>LAST n FROM</s></td>
 * <td>The last n result(s) in a list.</td>
 * </tr>
 * <tr>
 * <td>FIRST</td>
 * <td>The first result in a list, typically the oldest (earliest).</td>
 * </tr>
 * <tr>
 * <td><s>FIRST n FROM</s></td>
 * <td>The first n result(s) in a list.</td>
 * </tr>
 * <tr>
 * <td><s>MAX</s></td>
 * <td>The maximum result in a list.</td>
 * </tr>
 * <tr>
 * <td><s>MAX n FROM</s></td>
 * <td>The maximum n result(s) in a list.</td>
 * </tr>
 * <tr>
 * <td><s>MIN</s></td>
 * <td>The minimum result in a list.</td>
 * </tr>
 * <tr>
 * <td><s>MIN n FROM</s></td>
 * <td>The minimum n result(s) in a list.</td>
 * </tr>
 * </table>
 * Examples of conditions:
 * <table width="50%">
 * <tr>
 * <td>&gt; 200</td>
 * </tr>
 * <tr>
 * <td><s>WITHIN 6 MONTHS</s></td>
 * </tr>
 * <tr>
 * <td>BEFORE 2008-07-21&nsbp;<sup>*</sup></td>
 * </tr>
 * <tr>
 * <td>AFTER 2008-06-30&nsbp;<sup>*</sup></td>
 * </tr>
 * <tr>
 * <td><s>BETWEEN 2008-06-30 AND 2008-07-21</s>&nsbp;<sup>*</sup></td>
 * </tr>
 * </table>
 * <sup>*</sup>Dates must be in the format YYYY-MM-DD.<br />
 * <s>NOT YET IMPLEMENTED</s> (items with strikeout are not yet supported by LogicCriteria) For
 * example, the following two criteria would be functionally the same:
 * 
 * <pre>
 * LogicCriteria crit1 = LogicCriteria.parse(&quot;LAST {CD4 COUNT} &lt; 200&quot;);
 * LogicCriteria crit2 = new LogicCriteria(&quot;CD4 COUNT&quot;).lt(200).last();
 * </pre>
 * 
 * This class is used by the LogicCriteria.<em>parse(String)</em> method and should not be accessed
 * directly. <h5>Developer notes</h5>
 * <ul>
 * <li>Parentheticals are not yet supported, but could be used to control precedence.</li>
 * <li>We would like to evolve this class into a full parser (using ANTLR)</li>
 * </ul>
 * 
 * @see LogicCriteria#parse(String)
 */
public class LogicQueryParser {
	
	// Simple logic query syntax: [<aggregator>] {TOKEN} [<condition>]
	private static final Pattern queryPattern = regex("\\s*(([^\\{]+)\\s+)?\\{([^\\}]+)\\}(\\s+(.*?))?\\s*");
	
	// Used to recognized dates within condition statements
	private static final String datePattern = "(\\d{4}-\\d{1,2}-\\d{1,2}|TODAY)";
	
	// Internal class used to represent an aggregator phrase (FIRST, LAST, MIN,
	// MAX, EXIST, etc.) and to link a particular aggregator pattern to a method
	// that can properly adjust the criteria.
	static abstract class Aggregator {
		
		abstract LogicCriteria apply(LogicCriteria criteria, Matcher match) throws LogicQueryParseException;
	}
	
	// Stores all known aggregator patterns
	private static Map<Pattern, Aggregator> aggregators = new Hashtable<Pattern, Aggregator>();
	
	// Load aggregator patterns
	static {
		aggregators.put(regex("EXIST"), new Aggregator() {
			
			LogicCriteria apply(LogicCriteria criteria, Matcher match) throws LogicQueryParseException {
				return criteria.exists();
			}
		});
		aggregators.put(regex("SUM"), new Aggregator() {
			
			LogicCriteria apply(LogicCriteria criteria, Matcher match) throws LogicQueryParseException {
				throw new LogicQueryParseException("Logic query SUM not implemented");
			}
		});
		aggregators.put(regex("AVERAGE|AVG"), new Aggregator() {
			
			LogicCriteria apply(LogicCriteria criteria, Matcher match) throws LogicQueryParseException {
				throw new LogicQueryParseException("Logic query AVERAGE not implemented");
			}
		});
		aggregators.put(regex("MIN(IMUM)?(\\s+(\\d+) FROM)?"), new Aggregator() {
			
			LogicCriteria apply(LogicCriteria criteria, Matcher match) throws LogicQueryParseException {
				throw new LogicQueryParseException("Logic query MINIMUM not implemented");
			}
		});
		aggregators.put(regex("MAX(IMUM)?(\\s+(\\d+)\\s+FROM)?"), new Aggregator() {
			
			LogicCriteria apply(LogicCriteria criteria, Matcher match) throws LogicQueryParseException {
				throw new LogicQueryParseException("Logic query MAXIMUM not implemented");
			}
		});
		aggregators.put(regex("(LAST|LATEST)(\\s+(\\d+)\\s+FROM)?"), new Aggregator() {
			
			LogicCriteria apply(LogicCriteria criteria, Matcher match) throws LogicQueryParseException {
				if (match.group(3) != null) {
					Integer n = 0;
					try {
						n = Integer.parseInt(match.group(3));
					}
					catch (NumberFormatException e) {}
					if (n < 1)
						throw new LogicQueryParseException("Logic query invalid syntax for '" + match.group(1)
						        + " n FROM'. n must be positive whole number: " + match.group(3));
					// criteria = criteria.first(n);
					throw new LogicQueryParseException("Logic query feature not yet supported: " + match.group(1) + " " + n);
				}
				return criteria.last();
			}
		});
		aggregators.put(regex("(FIRST|EARLIEST)(\\s+(\\d+)\\s+FROM)?"), new Aggregator() {
			
			LogicCriteria apply(LogicCriteria criteria, Matcher match) throws LogicQueryParseException {
				if (match.group(3) != null) {
					Integer n = 0;
					try {
						n = Integer.parseInt(match.group(3));
					}
					catch (NumberFormatException e) {}
					if (n < 1)
						throw new LogicQueryParseException(
						        "Logic query invalid syntax for 'FIRST n FROM'. n must be positive whole number: "
						                + match.group(3));
					// criteria = criteria.first(n);
					throw new LogicQueryParseException("Logic query feature not yet supported: FIRST " + n);
				}
				return criteria.first();
			}
		});
	}
	
	// Internal class used to represent a conditional phrase (GT X, BEFORE
	// 2008-10-23, etc.) and to link a particular condition pattern to a method
	// that can properly adjust the criteria.
	static abstract class Condition {
		
		abstract LogicCriteria apply(LogicCriteria criteria, Matcher match) throws LogicQueryParseException;
	}
	
	// Stores all known condition patterns
	private static Map<Pattern, Condition> conditions = new Hashtable<Pattern, Condition>();
	
	// Load condition patterns
	static {
		// Accept both < and LT
		conditions.put(regex("(\\>|\\>=|=|==|\\<\\>|!=|\\<|\\<=|GT|GTE|EQ|NE|LT|LTE)\\s+(.*?)\\s*"), new Condition() {
			
			LogicCriteria apply(LogicCriteria criteria, Matcher match) throws LogicQueryParseException {
				return applyOperator(criteria, match.group(1), match.group(2));
			}
		});
		conditions.put(regex("WITHIN\\s+(\\d+)\\s+(SECONDS|MINUTES|HOURS|DAYS|WEEKS|MONTHS|YEARS)"), new Condition() {
			
			LogicCriteria apply(LogicCriteria criteria, Matcher match) throws LogicQueryParseException {
				return applyWithin(criteria, match.group(1), match.group(2));
			}
		});
		conditions.put(regex("BEFORE\\s+" + datePattern), new Condition() {
			
			LogicCriteria apply(LogicCriteria criteria, Matcher match) throws LogicQueryParseException {
				return criteria.before(parseDate(match.group(1)));
			}
		});
		conditions.put(regex("AFTER\\s+" + datePattern), new Condition() {
			
			LogicCriteria apply(LogicCriteria criteria, Matcher match) throws LogicQueryParseException {
				return criteria.after(parseDate(match.group(1)));
			}
		});
		conditions.put(regex("BETWEEN\\s+" + datePattern + "\\s+AND\\s+" + datePattern), new Condition() {
			
			LogicCriteria apply(LogicCriteria criteria, Matcher match) throws LogicQueryParseException {
				// return
				// criteria.between(parseDate(match.group(1)),parseDate(match
				// .group(1)));
				// until we have between, use before and after
				return criteria.before(parseDate(match.group(1))).after(parseDate(match.group(2)));
			}
		});
	}
	
	/**
	 * Parses a logic query into a matching LogicCriteria object.
	 * 
	 * @param query logic query to be parsed
	 * @return the equivalent LogicCriteria object for the given logic query string
	 * @throws LogicQueryParseException
	 */
	protected static LogicCriteria parse(String query) throws LogicQueryParseException {
		
		// First, we try to recognize the basic query format
		Matcher queryMatcher = queryPattern.matcher(query);
		if (!queryMatcher.matches())
			throw new LogicQueryParseException("Invalid or unsupported logic query: " + query);
		
		// Extract components of the query
		String aggregator = queryMatcher.group(2);
		String token = queryMatcher.group(3);
		String condition = queryMatcher.group(5);
		
		// Initial criteria with the query's token
		LogicCriteria criteria = new LogicCriteria(token);
		
		// Apply conditions
		if (condition != null) {
			boolean conditionMatched = false;
			for (Entry<Pattern, Condition> entry : conditions.entrySet()) {
				Matcher match = entry.getKey().matcher(condition);
				if (match.matches()) {
					conditionMatched = true;
					criteria = entry.getValue().apply(criteria, match);
					break; // only apply one condition
				}
			}
			if (!conditionMatched)
				// Condition included in query, but not recognized
				throw new LogicQueryParseException("Logic query unrecognized or invalid condition: " + condition);
		}
		
		// Apply aggregator
		if (aggregator != null) {
			boolean aggregatorMatched = false;
			for (Entry<Pattern, Aggregator> entry : aggregators.entrySet()) {
				Matcher match = entry.getKey().matcher(aggregator);
				if (match.matches()) {
					aggregatorMatched = true;
					criteria = entry.getValue().apply(criteria, match);
					break; // only apply one aggregator
				}
			}
			if (!aggregatorMatched)
				// Aggregator included in query, but not recognized
				throw new LogicQueryParseException("Logic query unrecognized or invalid aggregator: " + aggregator);
		}
		
		return criteria;
		
	}
	
	/**
	 * Internal method for applying operator conditions to a criteria. Only GT, LT, GTE, LTE, EQ,
	 * and NE are supported
	 */
	private static LogicCriteria applyOperator(LogicCriteria criteria, String op, String operand)
	                                                                                             throws LogicQueryParseException {
		Double operandValue = 0d;
		try {
			operandValue = Double.parseDouble(operand);
		}
		catch (Exception e) {
			throw new LogicQueryParseException("Unable to parse value for logic query condition: " + operand, e);
		}
		if (op.equals(">") || op.equals("GT"))
			return criteria.gt(operandValue);
		else if (op.equals("<") || op.equals("LT"))
			return criteria.lt(operandValue);
		else if (op.equals("=") || op.equals("=="))
			return criteria.equalTo(operandValue);
		else if (op.equals("<>") || op.equals("!="))
			return criteria.equalTo(operandValue).not();
		else if (op.equals(">=") || op.equals("GTE"))
			return criteria.gte(operandValue);
		else if (op.equals("<=") || op.equals("LTE"))
			return criteria.lte(operandValue);
		
		return criteria;
	}
	
	/**
	 * Internal method for applying WITHIN condition to a criteria.
	 */
	private static LogicCriteria applyWithin(LogicCriteria criteria, String duration, String units)
	                                                                                               throws LogicQueryParseException {
		Double durationValue = 0d;
		try {
			durationValue = Double.parseDouble(duration);
		}
		catch (Exception e) {
			throw new LogicQueryParseException("Unable to parse value for logic query within statement duration: "
			        + duration);
		}
		
		if (units.equalsIgnoreCase("SECONDS"))
			return criteria.within(Duration.seconds(durationValue));
		else if (units.equalsIgnoreCase("MINUTES"))
			return criteria.within(Duration.minutes(durationValue));
		else if (units.equalsIgnoreCase("HOURS"))
			return criteria.within(Duration.hours(durationValue));
		else if (units.equalsIgnoreCase("DAYS"))
			return criteria.within(Duration.days(durationValue));
		else if (units.equalsIgnoreCase("WEEKS"))
			return criteria.within(Duration.weeks(durationValue));
		else if (units.equalsIgnoreCase("MONTHS"))
			return criteria.within(Duration.months(durationValue));
		else if (units.equalsIgnoreCase("YEARS"))
			return criteria.within(Duration.years(durationValue));
		
		return criteria;
	}
	
	/**
	 * � Internal method for parsing date strings into an equivalent Java object. Accepts "TODAY"
	 * for today's date.
	 */
	private static Date parseDate(String dateText) {
		if (dateText.equalsIgnoreCase("TODAY"))
			return new Date();
		DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
		Date dateValue = null;
		try {
			dateValue = fmt.parse(dateText);
		}
		catch (Exception e) {}
		return dateValue;
	}
	
	/**
	 * Convenience method used internally to compile regular expressions
	 */
	private static Pattern regex(String s) {
		return Pattern.compile(s, Pattern.CASE_INSENSITIVE);
	}
}

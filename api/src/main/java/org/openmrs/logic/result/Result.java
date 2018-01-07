/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.logic.result;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicException;

/**
 * A result from the logic service. A result can be 0-to-n date-values pairs. You can treat the
 * result as a list or easily coerce it into a simple value as needed. <br>
 * <br>
 * When possible, results carry references to more complex objects so that code that deals with
 * results and has some prior knowledge of the objects returned by a particular rule can more easily
 * get to the full-featured objects instead of the simplified values in the date-value pairs.<br>
 * <br>
 * TODO: better support/handling of NULL_RESULT
 */
public class Result extends ArrayList<Result> {
	
	private static final long serialVersionUID = -5587574403423820797L;
	
	/**
	 * Core datatypes for a result. Each result is one of these datatypes, but can be easily coerced
	 * into the other datatypes. To promote flexibility and maximize re-usability of logic rules,
	 * the value of a result can be controlled individually for each datatype &mdash; i.e., specific
	 * datatype representations of a single result can be overridden. For example, a result could
	 * have a <em>numeric</em> value of 0.15 and its text value could be overridden to be
	 * "15 percent" or "Fifteen percent."
	 */
	public enum Datatype {
		/**
		 * Represents a true/false type of result
		 */
		BOOLEAN,
		/**
		 * Represents a Concept type of result
		 */
		CODED,
		/**
		 * Represents a date type of result
		 */
		DATETIME,
		/**
		 * Represents number (float, double, int) type of results
		 */
		NUMERIC,
		/**
		 * Represents string type of results
		 */
		TEXT
	}
	
	private Datatype datatype;
	
	private Date resultDatetime;
	
	private Boolean valueBoolean;
	
	private Concept valueCoded;
	
	private Date valueDatetime;
	
	private Double valueNumeric;
	
	private String valueText;
	
	private Object resultObject;
	
	private static final Result emptyResult = new EmptyResult();
	
	public Result() {
	}
	
	/**
	 * Builds result upon another result &mdash; the first step in create a result that contains a
	 * list of other results.
	 * 
	 * @param result the result that will be the sole member of the new result
	 * @should not fail with null result
	 */
	public Result(Result result) {
		if (result != null) {
			this.add(result);
		}
	}
	
	/**
	 * Builds a result from a list of results
	 * 
	 * @param list a list of results
	 * @should not fail with null list
	 * @should not fail with empty list
	 */
	public Result(List<Result> list) {
		if (!(list == null || list.isEmpty())) {
			this.addAll(list);
		}
	}
	
	/**
	 * Builds a boolean result with a result date of today
	 * 
	 * @param valueBoolean
	 */
	public Result(Boolean valueBoolean) {
		this(new Date(), valueBoolean, null);
	}
	
	/**
	 * Builds a boolean result with a specific result date
	 * 
	 * @param resultDate
	 * @param valueBoolean
	 */
	public Result(Date resultDate, Boolean valueBoolean, Object obj) {
		this(resultDate, Datatype.BOOLEAN, valueBoolean, null, null, null, null, obj);
	}
	
	/**
	 * Builds a coded result with a result date of today
	 * 
	 * @param valueCoded
	 */
	public Result(Concept valueCoded) {
		this(new Date(), valueCoded, null);
	}
	
	/**
	 * Builds a coded result with a specific result date
	 * 
	 * @param resultDate
	 * @param valueCoded
	 */
	public Result(Date resultDate, Concept valueCoded, Object obj) {
		this(resultDate, Datatype.CODED, null, valueCoded, null, null, null, obj);
	}
	
	/**
	 * Builds a coded result from an observation
	 * 
	 * @param obs
	 */
	public Result(Obs obs) {
		this(obs.getObsDatetime(), null, obs.getValueAsBoolean(), obs.getValueCoded(), obs.getValueDatetime(), obs
		        .getValueNumeric(), obs.getValueText(), obs);
		
		Concept concept = obs.getConcept();
		ConceptDatatype conceptDatatype;
		
		if (concept != null) {
			conceptDatatype = concept.getDatatype();
			
			if (conceptDatatype == null) {
				return;
			}
			if (conceptDatatype.isCoded()) {
				this.datatype = Datatype.CODED;
			} else if (conceptDatatype.isNumeric()) {
				this.datatype = Datatype.NUMERIC;
			} else if (conceptDatatype.isDate()) {
				this.datatype = Datatype.DATETIME;
			} else if (conceptDatatype.isText()) {
				this.datatype = Datatype.TEXT;
			} else if (conceptDatatype.isBoolean()) {
				this.datatype = Datatype.BOOLEAN;
			}
		}
	}
	
	/**
	 * Builds a datetime result with a result date of today
	 * 
	 * @param valueDatetime
	 */
	public Result(Date valueDatetime) {
		this(new Date(), valueDatetime, null);
	}
	
	/**
	 * Builds a datetime result with a specific result date
	 * 
	 * @param resultDate
	 * @param valueDatetime
	 */
	public Result(Date resultDate, Date valueDatetime, Object obj) {
		this(resultDate, Datatype.DATETIME, null, null, valueDatetime, null, null, obj);
	}
	
	/**
	 * Builds a numeric result with a result date of today
	 * 
	 * @param valueNumeric
	 */
	public Result(Double valueNumeric) {
		this(new Date(), valueNumeric, null);
	}
	
	/**
	 * Builds a numeric result with a specific result date
	 * 
	 * @param resultDate
	 * @param valueNumeric
	 */
	public Result(Date resultDate, Double valueNumeric, Object obj) {
		this(resultDate, Datatype.NUMERIC, null, null, null, valueNumeric, null, obj);
	}
	
	/**
	 * Builds a numeric result with a result date of today
	 * 
	 * @param valueNumeric
	 */
	public Result(Integer valueNumeric) {
		this(new Date(), valueNumeric, null);
	}
	
	/**
	 * Builds a numeric result with a specific result date
	 * 
	 * @param resultDate
	 * @param valueNumeric
	 */
	public Result(Date resultDate, Integer valueNumeric, Object obj) {
		this(resultDate, Datatype.NUMERIC, null, null, null, valueNumeric.doubleValue(), null, obj);
	}
	
	/**
	 * Builds a text result with a result date of today
	 * 
	 * @param valueText
	 */
	public Result(String valueText) {
		this(new Date(), valueText, null);
	}
	
	/**
	 * Builds a text result with a specific result date
	 * 
	 * @param resultDate
	 * @param valueText
	 */
	public Result(Date resultDate, String valueText, Object obj) {
		this(resultDate, Datatype.TEXT, null, null, null, null, valueText, obj);
	}
	
	/**
	 * Builds a result date with specific (overloaded) values &mdash; i.e., instead of simply
	 * accepting the default translation of one datatype into another (e.g., a date translated
	 * automatically into string format), this contructor allows the various datatype
	 * representations of the result to be individually controlled. Any values set to <em>null</em>
	 * will yield the natural translation of the default datatype. For example,
	 * 
	 * <pre>
	 * Result result = new Result(new Date(), 2.5);
	 * assertEqualtes(&quot;2.5&quot;, result.toString());
	 * 
	 * Result result = new Result(new Date(), Result.Datatype.NUMERIC, 2.5, null, null, &quot;Two and a half&quot;, null);
	 * assertEquals(&quot;Two and a half&quot;, result.toString());
	 * </pre>
	 * 
	 * @param resultDate
	 * @param datatype
	 * @param valueBoolean
	 * @param valueCoded
	 * @param valueDatetime
	 * @param valueNumeric
	 * @param valueText
	 * @param object
	 */
	public Result(Date resultDate, Datatype datatype, Boolean valueBoolean, Concept valueCoded, Date valueDatetime,
	    Double valueNumeric, String valueText, Object object) {
		this.resultDatetime = resultDate;
		this.valueNumeric = valueNumeric;
		this.valueDatetime = valueDatetime;
		this.valueCoded = valueCoded;
		this.valueText = valueText;
		this.valueBoolean = valueBoolean;
		this.datatype = datatype;
		this.resultObject = object;
	}
	
	/**
	 * @return null/empty result
	 */
	public static final Result emptyResult() {
		return emptyResult;
	}
	
	/**
	 * Returns the datatype of the result. If the result is a list of other results, then the
	 * datatype of the first element is returned
	 * 
	 * @return datatype of the result
	 */
	public Datatype getDatatype() {
		if (isSingleResult()) {
			return this.datatype;
		}
		// TODO: better option than defaulting to first element's datatype?
		return this.get(0).getDatatype();
	}
	
	/**
	 * Changes the result date time &mdash; not to be confused with a value that is a date. The
	 * result date time is typically the datetime that the observation was recorded.
	 * 
	 * @param resultDatetime
	 */
	public void setResultDate(Date resultDatetime) {
		this.resultDatetime = resultDatetime;
	}
	
	/**
	 * Changes the default datatype of the result
	 * 
	 * @param datatype
	 */
	public void setDatatype(Datatype datatype) {
		this.datatype = datatype;
	}
	
	/**
	 * Overrides the boolean representation of ths result without changing the default datatype
	 * 
	 * @param valueBoolean
	 */
	public void setValueBoolean(Boolean valueBoolean) {
		this.valueBoolean = valueBoolean;
	}
	
	/**
	 * Overrides the coded representation of ths result without changing the default datatype
	 * 
	 * @param valueCoded
	 */
	public void setValueCoded(Concept valueCoded) {
		this.valueCoded = valueCoded;
	}
	
	/**
	 * Overrides the datetime representation of ths result without changing the default datatype
	 * 
	 * @param valueDatetime
	 */
	public void setValueDatetime(Date valueDatetime) {
		this.valueDatetime = valueDatetime;
	}
	
	/**
	 * Overrides the numeric representation of ths result without changing the default datatype
	 * 
	 * @param valueNumeric
	 */
	public void setValueNumeric(Integer valueNumeric) {
		this.valueNumeric = valueNumeric.doubleValue();
	}
	
	/**
	 * Overrides the numeric representation of ths result without changing the default datatype
	 * 
	 * @param valueNumeric
	 */
	public void setValueNumeric(Double valueNumeric) {
		this.valueNumeric = valueNumeric;
	}
	
	/**
	 * Overrides the text representation of ths result without changing the default datatype
	 * 
	 * @param valueText
	 */
	public void setValueText(String valueText) {
		this.valueText = valueText;
	}
	
	/**
	 * Returns the data of the result (not to be confused with a data value). For example, if a
	 * result represents an observation like DATE STARTED ON HIV TREATMENT, the <em>result date</em>
	 * (returned by this method) would be the date the observation was recorded while the
	 * <em>toDatetime()</em> method would be used to get the actual answer (when the patient started
	 * their treatment).
	 * 
	 * @return date of the result (usually the date the result was recorded or observed)
	 * @see #toDatetime()
	 */
	public Date getResultDate() {
		if (isSingleResult()) {
			return resultDatetime;
		}
		return this.get(0).getResultDate();
	}
	
	/**
	 * Get the result object
	 * 
	 * @return the underlying result object
	 */
	public Object getResultObject() {
		return this.resultObject;
	}
	
	/**
	 * Set the result object
	 * 
	 * @param object
	 */
	public void setResultObject(Object object) {
		this.resultObject = object;
	}
	
	/**
	 * @return boolean representation of the result. For non-boolean results, this will either be
	 *         the overridden boolean value (if specifically defined) or a boolean representation of
	 *         the default datatype. If the result is a list, then return false only if all members
	 *         are false
	 *         <table summary="Return logic">
	 *         <tr>
	 *         <th>Datatype</th>
	 *         <th>Returns</th>
	 *         </tr>
	 *         <tr>
	 *         <td>CODED</td>
	 *         <td>false for concept FALSE<br>
	 *         true for all others</td>
	 *         </tr>
	 *         <tr>
	 *         <td>DATETIME</td>
	 *         <td>true for any date value<br>
	 *         false if the date is null</td>
	 *         </tr>
	 *         <tr>
	 *         <td>NUMERIC</td>
	 *         <td>true for any non-zero number<br>
	 *         false for zero</td>
	 *         </tr>
	 *         <tr>
	 *         <td>TEXT</td>
	 *         <td>true for any non-blank value<br>
	 *         false if blank or null</td>
	 *         </tr>
	 *         </table>
	 */
	public Boolean toBoolean() {
		
		if (isSingleResult()) {
			
			if (datatype == null) {
				return valueBoolean;
			}
			
			switch (datatype) {
				case BOOLEAN:
					return (valueBoolean == null ? false : valueBoolean);
				case CODED:
					return (valueCoded != null); // TODO: return
					// false for "FALSE"
					// concept
				case DATETIME:
					return (valueDatetime != null);
				case NUMERIC:
					return (valueNumeric != null && valueNumeric != 0);
				case TEXT:
					return (valueText != null && valueText.length() >= 1);
				default:
					return valueBoolean;
			}
		}
		for (Result r : this) {
			if (!r.toBoolean()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @return concept for result. For non-concept results, returns the concept value if it was
	 *         overridden (specifically defined for the result), otherwise returns <em>null</em>. If
	 *         the result is a list, then the concept for the first member is returned.
	 */
	public Concept toConcept() {
		if (isSingleResult()) {
			return valueCoded;
		}
		return this.get(0).toConcept();
	}
	
	/**
	 * @return the datetime representation of the result <em>value</em> (not to be confused with the
	 *         result's own datetime). For non-datetime results, this will return the overridden
	 *         datetime value (if specifically defined) or datetime representation of the default
	 *         datatype. If the result is a list, then the datetime representation of the first
	 *         member is returned.
	 *         <table summary="Return logic">
	 *         <tr>
	 *         <th>Datatype</th>
	 *         <th>Returns</th>
	 *         </tr>
	 *         <tr>
	 *         <td>BOOLEAN</td>
	 *         <td>null</td>
	 *         </tr>
	 *         <tr>
	 *         <td>CODED</td>
	 *         <td>null</td>
	 *         </tr>
	 *         <tr>
	 *         <td>NUMERIC</td>
	 *         <td>null</td>
	 *         </tr>
	 *         <tr>
	 *         <td>TEXT</td>
	 *         <td>If the text can be parsed into a date, then that value is returned;<br>
	 *         otherwise returns <em>null</em></td>
	 *         </tr>
	 *         </table>
	 */
	public Date toDatetime() {
		if (isSingleResult()) {
			if (valueDatetime != null) {
				return valueDatetime;
			}
			if (datatype == Datatype.TEXT && valueText != null) {
				try {
					return Context.getDateFormat().parse(valueText);
				}
				catch (Exception e) {}
			}
			return valueDatetime;
		}
		return this.get(0).toDatetime();
	}
	
	/**
	 * @return numeric representation of the result. For non-numeric results, this will either be
	 *         the overridden numeric value (if specifically defined) or a numeric representation of
	 *         the default datatype. If the result is a list, then the value of the first element is
	 *         returned.
	 *         <table summary="Return logic">
	 *         <tr>
	 *         <th>Datatype</th>
	 *         <th>Returns</th>
	 *         </tr>
	 *         <tr>
	 *         <td>BOOLEAN</td>
	 *         <td>1 for true<br>
	 *         0 for false</td>
	 *         </tr>
	 *         <tr>
	 *         <td>CODED</td>
	 *         <td>zero (0)</td>
	 *         </tr>
	 *         <tr>
	 *         <tr>
	 *         <td>DATETIME</td>
	 *         <td>Number of milliseconds since Java's epoch</td>
	 *         </tr>
	 *         <tr>
	 *         <td>TEXT</td>
	 *         <td>numeric value of text if it can be parsed into a number<br>
	 *         otherwise zero (0)</td> </tr>
	 *         </table>
	 */
	public Double toNumber() {
		if (isSingleResult()) {
			if (datatype == null) {
				return valueNumeric;
			}
			switch (datatype) {
				
				case BOOLEAN:
					return (valueBoolean == null || !valueBoolean ? 0D : 1D);
				case CODED:
					return 0D;
				case DATETIME:
					return (valueDatetime == null ? 0 : Long.valueOf(valueDatetime.getTime()).doubleValue());
				case NUMERIC:
					return (valueNumeric == null ? 0D : valueNumeric);
				case TEXT:
					try {
						return Double.parseDouble(valueText);
					}
					catch (Exception e) {
						return 0D;
					}
				default:
					return valueNumeric;
			}
		}
		return this.get(0).toNumber();
	}
	
	/**
	 * @return string representation of the result. For non-text results, this will either be the
	 *         overridden text value (if specifically defined) or a string representation of the
	 *         default datatype value. If the result is a list, then the string representation of
	 *         all members a joined with commas.
	 */
	@Override
	public String toString() {
		if (isSingleResult()) {
			if (datatype == null) {
				return valueText == null ? "" : valueText;
			}
			
			switch (datatype) {
				case BOOLEAN:
					return (valueBoolean ? "true" : "false");
				case CODED:
					return (valueCoded == null ? "" : valueCoded.getName(Context.getLocale()).getName());
				case DATETIME:
					return (valueDatetime == null ? "" : Context.getDateFormat().format(valueDatetime));
				case NUMERIC:
					return (valueNumeric == null ? "" : String.valueOf(valueNumeric));
				case TEXT:
					return (valueText == null ? "" : valueText);
				default:
					return valueText;
			}
		}
		StringBuilder s = new StringBuilder();
		for (Result r : this) {
			if (s.length() > 0) {
				s.append(",");
			}
			s.append(r.toString());
		}
		return s.toString();
	}
	
	/**
	 * @return the object associated with the result (generally, this is used internally or for
	 *         advanced rule design)
	 * @should return resultObject for single results
	 * @should return all results for result list
	 */
	public Object toObject() {
		if (isSingleResult()) {
			return resultObject;
		}
		if (this.size() == 1) {
			return this.get(0).toObject();
		}
		throw new LogicException("This result represents more than one result, you cannot call toObject on multiple results");
	}
	
	/**
	 * @return true if result is empty
	 */
	public boolean isNull() {
		return false; //EmptyResult has its own implementation
		//that should return true
	}
	
	/**
	 * @return true if the result has any non-zero, non-empty value
	 */
	public boolean exists() {
		if (isSingleResult()) {
			return ((valueBoolean != null && valueBoolean) || valueCoded != null || valueDatetime != null
			        || (valueNumeric != null && valueNumeric != 0) || (valueText != null && valueText.length() > 0));
		}
		for (Result r : this) {
			if (r.exists()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean contains(Concept concept) {
		return containsConcept(concept.getConceptId());
	}
	
	/**
	 * @return all results greater than the given value
	 */
	public Result gt(Integer value) {
		if (isSingleResult()) {
			if (valueNumeric == null || valueNumeric <= value) {
				return emptyResult;
			}
			return this;
		}
		List<Result> matches = new ArrayList<>();
		for (Result r : this) {
			if (!r.gt(value).isEmpty()) {
				matches.add(r);
			}
		}
		if (matches.size() < 1) {
			return emptyResult;
		}
		return new Result(matches);
	}
	
	/**
	 * @return true if result contains a coded value with the given concept id (if the result is a
	 *         list, then returns true if <em>any</em> member has a matching coded value)
	 */
	public boolean containsConcept(Integer conceptId) {
		if (isSingleResult()) {
			return (valueCoded != null && valueCoded.getConceptId().equals(conceptId));
		}
		for (Result r : this) {
			if (r.containsConcept(conceptId)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return true if the result is equal to the given result or is a list containing a member
	 *         equal to the given result
	 */
	public boolean contains(Result result) {
		if (isSingleResult()) {
			return this.equals(result);
		}
		for (Result r : this) {
			if (r.contains(result)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return a result with all duplicates removed
	 */
	public Result unique() {
		if (isSingleResult()) {
			return this;
		}
		Integer something = 1;
		Map<Result, Integer> map = new HashMap<>();
		for (Result r : this) {
			map.put(r, something);
		}
		List<Result> uniqueList = new ArrayList<>(map.keySet());
		return new Result(uniqueList);
	}
	
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Result)) {
			return false;
		}
		Result r = (Result) obj;
		
		if (EmptyResult.class.isAssignableFrom(r.getClass()) && this.isEmpty()) {
			return true;
		}
		
		if (EmptyResult.class.isAssignableFrom(this.getClass()) && r.isEmpty()) {
			return true;
		}
		
		if (isSingleResult() && r.isSingleResult()) {
			
			if (datatype == null) {
				return false;
			}
			// both are single results
			switch (datatype) {
				case BOOLEAN:
					return (valueBoolean.equals(r.valueBoolean));
				case CODED:
					return (valueCoded.equals(r.valueCoded));
				case DATETIME:
					return (valueDatetime.equals(r.valueDatetime));
				case NUMERIC:
					return (valueNumeric.equals(r.valueNumeric));
				case TEXT:
					return (valueText.equals(r.valueText));
				default:
					return false;
			}
		}
		if (isSingleResult() || r.isSingleResult()) {
			// we already know they're not both single results, so if one is
			// single, it's not a match
			return false;
		}
		if (this.size() != r.size()) {
			return false;
		}
		// at this point, we have two results that are lists, so members must
		// match exactly
		for (int i = 0; i < this.size(); i++) {
			if (!this.get(i).equals(r.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		if (isSingleResult()) {
			return new HashCodeBuilder().append(datatype).hashCode();
		} else {
			return super.hashCode();
		}
	}
	
	/**
	 * @return the <em>index</em> element of a list. If the result is not a list, then this will
	 *         return the result only if <em>index</em> is equal to zero (0); otherwise, returns an
	 *         empty result
	 * @see java.util.List#get(int)
	 * @should get empty result for indexes out of range
	 */
	@Override
	public Result get(int index) {
		if (isSingleResult()) {
			return (index == 0 ? this : emptyResult);
		}
		
		if (index >= this.size()) {
			return emptyResult;
		}
		return super.get(index);
	}
	
	/**
	 * @return the chronologically (based on result date) first result
	 * @should get the first result given multiple results
	 * @should get the result given a single result
	 * @should get an empty result given an empty result
	 * @should not get the result with null result date given other results
	 * @should get one result with null result dates for all results
	 */
	public Result earliest() {
		if (isSingleResult()) {
			return this;
		}
		
		Result first = emptyResult();
		
		// default the returned result to the first item
		// in case all resultDates are null
		if (size() > 0) {
			first = get(0);
		}
		
		for (Result r : this) {
			if (r != null && r.getResultDate() != null
			        && (first.getResultDate() == null || r.getResultDate().before(first.getResultDate()))) {
				first = r;
			}
		}
		return first;
	}
	
	/**
	 * @return the chronologically (based on result date) last result
	 * @should get the most recent result given multiple results
	 * @should get the result given a single result
	 * @should get an empty result given an empty result
	 * @should get the result with null result date
	 */
	public Result latest() {
		if (isSingleResult()) {
			return this;
		}
		Result last = emptyResult();
		
		// default the returned result to the first item
		// in case all resultDates are null
		if (size() > 0) {
			last = get(0);
		}
		
		for (Result r : this) {
			if ((last.getResultDate() == null || (r.getResultDate() != null && r.getResultDate().after(last.getResultDate())))) {
				last = r;
			}
		}
		return last;
	}
	
	/**
	 * Convenience method to know if this Result represents multiple results or not
	 * 
	 * @return true/false whether this is just one Result or more than one
	 */
	private boolean isSingleResult() {
		return (this.size() < 1);
	}
	
}

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
package org.openmrs.reporting;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PatientSetService.BooleanOperator;
import org.openmrs.cohort.CohortDefinition;
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.cohort.CohortUtil;
import org.openmrs.report.EvaluationContext;
import org.openmrs.report.Parameter;
import org.openmrs.util.OpenmrsUtil;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * This class represents a search for a set of patients, as entered from a user interface. There are
 * different types of searches:
 * <ul>
 * <li>a composition, e.g. "1 and (2 or 3)"
 * <li>a reference to a saved filter, expressed as the database integer pk.
 * <li>a reference to a saved cohort, expressed as the database integer pk.
 * <li>a regular search, which describes a PatientFilter subclass and a list of bean-style
 * properties to set.
 * </ul>
 * Composition filters:<br/>
 * When isComposition() returns true, then this represents something like "1 and (2 or 3)", which
 * must be evaluated in the context of a search history.
 * <p>
 * Saved filters:<br/>
 * When isSavedFilterReference() returns true, then this represents something like "saved filter #8"
 * <br/>
 * When isSavedCohortReference() returns true, then this represents something like "saved cohort #3"
 * <p>
 * Regular filters:<br/>
 * Otherwise this search describes a PatientFilter subclass and a list of bean-style properties to
 * set, so that it can be turned into a PatientFilter with the utility method
 * OpenmrsUtil.toPatientFilter(PatientSearch). But it can also be left as-is for better
 * version-compatibility if PatientFilter classes change, or to avoid issues with xml-encoding
 * hibernate proxies.
 * 
 * @deprecated see reportingcompatibility module
 */
@Root(strict = false)
@Deprecated
public class PatientSearch implements CohortDefinition {
	
	private static final long serialVersionUID = -8913742497675209159L;
	
	protected static transient final Log log = LogFactory.getLog(PatientSearch.class);
	
	private static Set<String> andWords = new HashSet<String>();
	
	private static Set<String> orWords = new HashSet<String>();
	
	private static Set<String> notWords = new HashSet<String>();
	
	private static Set<String> openParenthesesWords = new HashSet<String>();
	
	private static Set<String> closeParenthesesWords = new HashSet<String>();
	static {
		andWords.add("and");
		andWords.add("intersection");
		andWords.add("*");
		orWords.add("or");
		orWords.add("union");
		orWords.add("+");
		notWords.add("not");
		notWords.add("!");
		openParenthesesWords.add("(");
		openParenthesesWords.add("[");
		openParenthesesWords.add("{");
		closeParenthesesWords.add(")");
		closeParenthesesWords.add("]");
		closeParenthesesWords.add("}");
	}
	
	private Class<PatientFilter> filterClass;
	
	private List<SearchArgument> arguments;
	
	private List<Object> parsedComposition;
	
	private Integer savedSearchId;
	
	private Integer savedFilterId;
	
	private Integer savedCohortId;
	
	// Temporary storage for user-specified parameter values. This is a bit of a hack.  
	private transient Map<String, String> parameterValues = new HashMap<String, String>();
	
	// static factory methods:
	public static PatientSearch createSavedSearchReference(int id) {
		PatientSearch ps = new PatientSearch();
		ps.setSavedSearchId(id);
		return ps;
	}
	
	public static PatientSearch createSavedFilterReference(int id) {
		PatientSearch ps = new PatientSearch();
		ps.setSavedFilterId(id);
		return ps;
	}
	
	public static PatientSearch createSavedCohortReference(int id) {
		PatientSearch ps = new PatientSearch();
		ps.setSavedCohortId(id);
		return ps;
	}
	
	public static PatientSearch createCompositionSearch(String description) {
		// TODO This is a rewrite of the code in CohortSearchHistory.createCompositionFilter(String). That method should probably delegate to this one in some way.
		// TODO use open/closeParenthesesWords declared above
		List<Object> tokens = new ArrayList<Object>();
		try {
			StreamTokenizer st = new StreamTokenizer(new StringReader(description));
			st.ordinaryChar('(');
			st.ordinaryChar(')');
			while (st.nextToken() != StreamTokenizer.TT_EOF) {
				if (st.ttype == StreamTokenizer.TT_NUMBER) {
					Integer thisInt = new Integer((int) st.nval);
					if (thisInt < 1) {
						log.error("number < 1");
						return null;
					}
					tokens.add(thisInt);
				} else if (st.ttype == '(') {
					tokens.add("(");
				} else if (st.ttype == ')') {
					tokens.add(")");
				} else if (st.ttype == StreamTokenizer.TT_WORD) {
					String str = st.sval.toLowerCase();
					tokens.add(str);
				}
			}
			return createCompositionSearch(tokens);
		}
		catch (Exception ex) {
			log.error("Error in description string: " + description, ex);
			return null;
		}
	}
	
	public static PatientSearch createCompositionSearch(Object[] tokens) {
		return createCompositionSearch(Arrays.asList(tokens));
	}
	
	public static PatientSearch createCompositionSearch(List<Object> tokens) {
		// TODO This is a rewrite of the code in CohortSearchHistory.createCompositionFilter(String). That method should probably delegate to this one in some way.
		List<Object> currentLine = new ArrayList<Object>();
		
		try {
			Stack<List<Object>> stack = new Stack<List<Object>>();
			for (Object token : tokens) {
				if (token instanceof String) {
					String s = (String) token;
					s = s.toLowerCase();
					if (andWords.contains(s)) {
						currentLine.add(PatientSetService.BooleanOperator.AND);
					} else if (orWords.contains(s)) {
						currentLine.add(PatientSetService.BooleanOperator.OR);
					} else if (notWords.contains(s)) {
						currentLine.add(PatientSetService.BooleanOperator.NOT);
					} else if (openParenthesesWords.contains(s)) {
						stack.push(currentLine);
						currentLine = new ArrayList<Object>();
					} else if (closeParenthesesWords.contains(s)) {
						List<Object> l = stack.pop();
						l.add(currentLine);
						currentLine = l;
					} else {
						throw new IllegalArgumentException("Unrecognized string token: " + s);
					}
				} else if (token instanceof Integer) {
					currentLine.add(token);
				} else if (token instanceof PatientSearch) {
					currentLine.add(token);
				} else if (token instanceof PatientFilter) {
					currentLine.add(token);
				} else {
					throw new IllegalArgumentException("Unknown class in token list: " + token.getClass());
				}
			}
		}
		catch (Exception ex) {
			log.error("Error in token list", ex);
			return null;
		}
		
		PatientSearch ret = new PatientSearch();
		ret.setParsedComposition(currentLine);
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static PatientSearch createFilterSearch(Class filterClass) {
		PatientSearch ps = new PatientSearch();
		ps.setFilterClass(filterClass);
		ps.setArguments(new ArrayList<SearchArgument>());
		return ps;
	}
	
	// constructors and instance methods
	
	public PatientSearch() {
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PatientSearch");
		if (getSavedCohortId() != null)
			sb.append(" savedCohortId=" + getSavedCohortId());
		if (getSavedFilterId() != null)
			sb.append(" savedFilterId=" + getSavedFilterId());
		if (getSavedSearchId() != null)
			sb.append(" savedSearchId=" + getSavedSearchId());
		if (getFilterClass() != null) {
			sb.append(" filterClass=" + getFilterClass());
			if (getArguments() != null)
				for (SearchArgument sa : getArguments())
					sb.append(" (" + sa.getPropertyClass() + ")" + sa.getName() + "=" + sa.getValue());
		}
		if (getParsedComposition() != null) {
			sb.append(" parsedComposition=");
			for (Object o : getParsedComposition())
				sb.append("\n" + o);
		}
		if (parameterValues != null)
			for (Map.Entry<String, String> e : parameterValues.entrySet())
				sb.append(" paramValue:" + e.getKey() + "=" + e.getValue());
		return sb.toString();
	}
	
	public boolean isComposition() {
		return parsedComposition != null;
	}
	
	public String getCompositionString() {
		if (parsedComposition == null)
			return null;
		else
			return compositionStringHelper(parsedComposition);
	}
	
	/**
	 * Convenience method so that a PatientSearch object can be created from a string of
	 * compositions
	 * 
	 * @param specification
	 */
	@Element(data = true, name = "specification", required = false)
	public void setSpecificationString(String specification) {
		PatientSearch temp = (PatientSearch) CohortUtil.parse(specification);
		if (temp == null)
			throw new IllegalArgumentException("Couldn't parse: " + specification);
		this.setParsedComposition(temp.getParsedComposition());
		this.setSavedSearchId(temp.getSavedSearchId());
		this.setSavedFilterId(temp.getSavedFilterId());
		this.setSavedCohortId(temp.getSavedCohortId());
		this.setFilterClass(temp.getFilterClass());
		if (temp.getArguments() != null)
			this.setArguments(new ArrayList<SearchArgument>(temp.getArguments()));
		else
			this.setArguments(null);
	}
	
	@Element(data = true, name = "specification", required = false)
	public String getSpecificationString() {
		return "Not Yet Implemented";
	}
	
	@SuppressWarnings("unchecked")
	private String compositionStringHelper(List list) {
		StringBuilder ret = new StringBuilder();
		for (Object o : list) {
			if (ret.length() > 0)
				ret.append(" ");
			if (o instanceof List)
				ret.append("(" + compositionStringHelper((List) o) + ")");
			else
				ret.append(o);
		}
		return ret.toString();
	}
	
	/**
	 * @return Whether this search requires a history against which to evaluate it
	 */
	public boolean requiresHistory() {
		if (isComposition()) {
			return requiresHistoryHelper(parsedComposition);
		} else
			return false;
	}
	
	private boolean requiresHistoryHelper(List<Object> list) {
		for (Object o : list) {
			if (o instanceof Integer)
				return true;
			else if (o instanceof PatientSearch)
				return ((PatientSearch) o).requiresHistory();
			else if (o instanceof List) {
				if (requiresHistoryHelper((List<Object>) o))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Creates a copy of this PatientSearch that doesn't depend on history, replacing references
	 * with actual PatientSearch elements from the provided history. The PatientSearch object
	 * returned is only a copy when necessary to detach it from history. This method does NOT do a
	 * clone.
	 */
	public PatientSearch copyAndDetachFromHistory(CohortSearchHistory history) {
		if (isComposition() && requiresHistory()) {
			PatientSearch copy = new PatientSearch();
			copy.setParsedComposition(copyAndDetachHelper(parsedComposition, history));
			return copy;
		} else
			return this;
	}
	
	@SuppressWarnings("unchecked")
	private List<Object> copyAndDetachHelper(List<Object> list, CohortSearchHistory history) {
		List<Object> ret = new ArrayList<Object>();
		for (Object o : list) {
			if (o instanceof PatientSearch) {
				ret.add(((PatientSearch) o).copyAndDetachFromHistory(history));
			} else if (o instanceof Integer) {
				PatientSearch ps = history.getSearchHistory().get(((Integer) o) - 1);
				ret.add(ps.copyAndDetachFromHistory(history));
			} else if (o instanceof List) {
				ret.add(copyAndDetachHelper((List) o, history));
			} else
				ret.add(o);
		}
		return ret;
	}
	
	/**
	 * Deep-copies this.parsedComposition, and converts to filters, in the context of history
	 */
	public CohortHistoryCompositionFilter cloneCompositionAsFilter(CohortSearchHistory history) {
		return cloneCompositionAsFilter(history, null);
	}
	
	/**
	 * Deep-copies this.parsedComposition, and converts to filters, in the context of history
	 */
	public CohortHistoryCompositionFilter cloneCompositionAsFilter(CohortSearchHistory history, EvaluationContext evalContext) {
		List<Object> list = cloneCompositionHelper(parsedComposition, history, evalContext);
		CohortHistoryCompositionFilter pf = new CohortHistoryCompositionFilter();
		pf.setParsedCompositionString(list);
		pf.setHistory(history);
		return pf;
	}
	
	@SuppressWarnings("unchecked")
	private List<Object> cloneCompositionHelper(List<Object> list, CohortSearchHistory history, EvaluationContext evalContext) {
		List<Object> ret = new ArrayList<Object>();
		for (Object o : list) {
			if (o instanceof List)
				ret.add(cloneCompositionHelper((List) o, history, evalContext));
			else if (o instanceof Integer)
				ret.add(history.ensureCachedFilter((Integer) o - 1));
			else if (o instanceof BooleanOperator)
				ret.add(o);
			else if (o instanceof PatientFilter)
				ret.add(o);
			else if (o instanceof PatientSearch)
				ret.add(OpenmrsUtil.toPatientFilter((PatientSearch) o, history, evalContext));
			else
				throw new RuntimeException("Programming Error: forgot to handle: " + o.getClass());
		}
		return ret;
	}
	
	public boolean isSavedReference() {
		return isSavedSearchReference() || isSavedFilterReference() || isSavedCohortReference();
	}
	
	public boolean isSavedSearchReference() {
		return savedSearchId != null;
	}
	
	public boolean isSavedFilterReference() {
		return savedFilterId != null;
	}
	
	public boolean isSavedCohortReference() {
		return savedCohortId != null;
	}
	
	/**
	 * Call this to notify this composition search that the _i_th element of the search history has
	 * been removed, and the search potentially needs to renumber its constituent parts. Examples,
	 * assuming this search is "1 and (4 or
	 * 5)": * removeFromHistoryNotify(1) -> This search becomes "1 and (3 or 4)" and the method
	 * return false * removeFromHistoryNotify(3) -> This search becomes invalid, and the method
	 * returns true * removeFromHistoryNotify(9) -> This search is unaffected, and the method
	 * returns false
	 * 
	 * @return whether or not this search itself should be removed (because it directly references
	 *         the removed history element
	 */
	public boolean removeFromHistoryNotify(int i) {
		if (!isComposition())
			throw new IllegalArgumentException("Can only call this method on a composition search");
		return removeHelper(parsedComposition, i);
	}
	
	@SuppressWarnings("unchecked")
	private boolean removeHelper(List<Object> list, int i) {
		boolean ret = false;
		for (ListIterator<Object> iter = list.listIterator(); iter.hasNext();) {
			Object o = iter.next();
			if (o instanceof List)
				ret |= removeHelper((List<Object>) o, i);
			else if (o instanceof Integer) {
				Integer ref = (Integer) o;
				if (ref == i) {
					ret = true;
					iter.set("-1");
				} else if (ref > i)
					iter.set(ref - 1);
			}
		}
		return ret;
	}
	
	/**
	 * Looks up an argument value, accounting for parameterValues
	 * 
	 * @param name
	 * @return the <code>String</code> value for the specified argument
	 */
	public String getArgumentValue(String name) {
		if (parameterValues.containsKey(name))
			return parameterValues.get(name);
		for (SearchArgument sa : arguments)
			if (sa.getName().equals(name))
				return sa.getValue();
		return null;
	}
	
	@ElementList(required = false)
	public List<SearchArgument> getArguments() {
		return arguments;
	}
	
	@ElementList(required = false)
	public void setArguments(List<SearchArgument> arguments) {
		this.arguments = arguments;
	}
	
	/**
	 * Returns all SearchArgument values that match
	 * {@link org.openmrs.report.EvaluationContext#parameterValues}
	 * 
	 * @return <code>List&lt;Parameter></code> of all parameters in the arguments
	 */
	public List<Parameter> getParameters() {
		List<Parameter> parameters = new ArrayList<Parameter>();
		if (arguments != null) {
			for (SearchArgument a : arguments) {
				String value = parameterValues.get(a.getName());
				if (value == null)
					value = a.getValue();
				if (EvaluationContext.isExpression(value)) {
					parameters.add(new Parameter(a.getName(), a.getName(), a.getPropertyClass(), value));
				}
			}
		}
		return parameters;
	}
	
	@SuppressWarnings("unchecked")
	@Attribute(required = false)
	public Class getFilterClass() {
		return filterClass;
	}
	
	@SuppressWarnings("unchecked")
	@Attribute(required = false)
	public void setFilterClass(Class clazz) {
		if (clazz != null && !PatientFilter.class.isAssignableFrom(clazz))
			throw new IllegalArgumentException(clazz + " is not an org.openmrs.PatientFilter");
		this.filterClass = clazz;
	}
	
	@SuppressWarnings("unchecked")
	public void addArgument(String name, String value, Class clz) {
		addArgument(new SearchArgument(name, value, clz));
	}
	
	public void addArgument(SearchArgument sa) {
		if (arguments == null)
			arguments = new ArrayList<SearchArgument>();
		arguments.add(sa);
	}
	
	/**
	 * Adds a SearchArgument as a Parameter where the SearchArgument name is set to the Parameter
	 * label and SearchArgument value is set to the Parameter name and SearchArgument propertyClass
	 * is set to the Parameter clazz
	 * 
	 * @param parameter
	 */
	public void addParameter(Parameter parameter) {
		addArgument(parameter.getLabel(), parameter.getName(), parameter.getClazz());
	}
	
	//@ElementList(required=false)
	public List<Object> getParsedComposition() {
		return parsedComposition;
	}
	
	/**
	 * Elements in this list can be: an Integer, indicating a 1-based index into a search history a
	 * BooleanOperator (AND, OR, NOT) a PatientFilter a PatientSearch another List of the same form,
	 * which indicates a parenthetical expression
	 */
	//@ElementList(required=false)
	public void setParsedComposition(List<Object> parsedComposition) {
		this.parsedComposition = parsedComposition;
	}
	
	@Attribute(required = false)
	public Integer getSavedSearchId() {
		return savedSearchId;
	}
	
	@Attribute(required = false)
	public void setSavedSearchId(Integer savedSearchId) {
		this.savedSearchId = savedSearchId;
	}
	
	@Attribute(required = false)
	public Integer getSavedFilterId() {
		return savedFilterId;
	}
	
	@Attribute(required = false)
	public void setSavedFilterId(Integer savedFilterId) {
		this.savedFilterId = savedFilterId;
	}
	
	@Attribute(required = false)
	public Integer getSavedCohortId() {
		return savedCohortId;
	}
	
	@Attribute(required = false)
	public void setSavedCohortId(Integer savedCohortId) {
		this.savedCohortId = savedCohortId;
	}
	
	public void setParameterValue(String name, String value) {
		parameterValues.put(name, value);
	}
	
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.openmrs.api.PatientSetService.BooleanOperator;
import org.openmrs.cohort.CohortHistoryCompositionFilter;
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.util.OpenmrsUtil;

/**
 * This class represents a search for a set of patients, as entered from a user interface.
 * There are different types of searches:
 *     * a composition, e.g. "1 and (2 or 3)"
 *     * a reference to a saved filter, expressed as the database integer pk.
 *     * a reference to a saved cohort, expressed as the database integer pk.
 *     * a regular search, which describes a PatientFilter subclass and a list of bean-style properties to set.
 * 
 * Composition filters:
 * When isComposition() returns true, then this represents something like "1 and (2 or 3)", which must be evaluated in the context of a search history.
 * 
 * Saved filters:
 * When isSavedFilterReference() returns true, then this represents something like "saved filter #8"
 * When isSavedCohortReference() returns true, then this represents something like "saved cohort #3"
 * 
 * Regular filters:
 * Otherwise this search describes a PatientFilter subclass and a list of bean-style properties to set, so that it can be turned into
 * a PatientFilter with the utility method OpenmrsUtil.toPatientFilter(PatientSearch).
 * But it can also be left as-is for better version-compatibility if PatientFilter classes change, or to avoid
 * issues with xml-encoding hibernate proxies.
 */
public class PatientSearch {

	private Class filterClass;
	private List<SearchArgument> arguments;
	private List<Object> parsedComposition;
	private Integer savedSearchId;
	private Integer savedFilterId;
	private Integer savedCohortId;

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
	
	public static PatientSearch createCompositionSearch(String s) {
		// TODO: implement this
		return null;
	}
	
	// constructors and instance methods
	
	public PatientSearch() { }

	public String toString() {
		return "PatientSearch( filterClass=" + filterClass + ", arguments=" + arguments + " )";
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
		}
		return false;
	}

	/**
	 * Creates a copy of this PatientSearch that doesn't depend on history, replacing references with actual PatientSearch
	 * elements from the provided history.
	 * The PatientSearch object returned is only a copy when necessary to detach it from history. This method does NOT do a clone. 
	 */
	public PatientSearch copyAndDetachFromHistory(CohortSearchHistory history) {
		if (isComposition() && requiresHistory()) {
			PatientSearch copy = new PatientSearch();
			copy.setParsedComposition(copyAndDetachHelper(parsedComposition, history));
			return copy;
		} else
			return this;
	}
	
	private List<Object> copyAndDetachHelper(List<Object> list, CohortSearchHistory history) {
		List<Object> ret = new ArrayList<Object>();
		for (Object o : list) {
			if (o instanceof PatientSearch) {
				ret.add(((PatientSearch) o).copyAndDetachFromHistory(history));
			} else if (o instanceof Integer) {
				PatientSearch ps = history.getSearchHistory().get( ((Integer) o) - 1);
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
		List<Object> list = cloneCompositionHelper(parsedComposition, history);
		CohortHistoryCompositionFilter pf = new CohortHistoryCompositionFilter();
		pf.setParsedCompositionString(list);
		pf.setHistory(history);
		return pf;
	}
	
	private List<Object> cloneCompositionHelper(List<Object> list, CohortSearchHistory history) {
		List<Object> ret = new ArrayList<Object>();
		for (Object o : list) {
			if (o instanceof List)
				ret.add(cloneCompositionHelper((List) o, history));
			else if (o instanceof Integer)
				ret.add(history.ensureCachedFilter((Integer) o - 1));
			else if (o instanceof BooleanOperator)
				ret.add(o);
			else if (o instanceof PatientFilter)
				ret.add(o);
			else if (o instanceof PatientSearch)
				ret.add(OpenmrsUtil.toPatientFilter((PatientSearch) o, history));
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
	 * Call this to notify this composition search that the _i_th element of the search history
	 * has been removed, and the search potentially needs to renumber its constituent parts.
	 * Examples, assuming this search is "1 and (4 or 5)":
	 *     * removeFromHistoryNotify(1) -> This search becomes "1 and (3 or 4)" and the method return false
	 *     * removeFromHistoryNotify(3) -> This search becomes invalid, and the method returns true
	 *     * removeFromHistoryNotify(9) -> This search is unaffected, and the method returns false  
	 * @return whether or not this search itself should be removed (because it directly references the removed history element
	 */
	public boolean removeFromHistoryNotify(int i) {
		if (!isComposition())
			throw new IllegalArgumentException("Can only call this method on a composition search");
		return removeHelper(parsedComposition, i);
	}
	
	private boolean removeHelper(List<Object> list, int i) {
		boolean ret = false;
		for (ListIterator<Object> iter = list.listIterator(); iter.hasNext(); ) {
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

	// getters and setters
	
	public List<SearchArgument> getArguments() {
    	return arguments;
    }

	public void setArguments(List<SearchArgument> arguments) {
    	this.arguments = arguments;
    }

	public Class getFilterClass() {
    	return filterClass;
    }

	public void setFilterClass(Class clazz) {
		if (clazz != null && !PatientFilter.class.isAssignableFrom(clazz))
			throw new IllegalArgumentException(clazz + " is not an org.openmrs.PatientFilter");
    	this.filterClass = clazz;
    }
	
	public void addArgument(String name, String value, Class clz) {
		addArgument(new SearchArgument(name, value, clz));
	}
	
	public void addArgument(SearchArgument sa) {
		if (arguments == null)
			arguments = new ArrayList<SearchArgument>();
		arguments.add(sa);
	}

	public List<Object> getParsedComposition() {
    	return parsedComposition;
    }

	/**
	 * Elements in this list can be:
	 *  an Integer, indicating a 1-based index into a search history
	 *  a BooleanOperator (AND, OR, NOT)
	 *  a PatientFilter
	 *  a PatientSearch
	 *  another List of the same form, which indicates a parenthetical expression
	 */
	public void setParsedComposition(List<Object> parsedComposition) {
    	this.parsedComposition = parsedComposition;
    }

	public Integer getSavedSearchId() {
    	return savedSearchId;
    }

	public void setSavedSearchId(Integer savedSearchId) {
    	this.savedSearchId = savedSearchId;
    }

	public Integer getSavedFilterId() {
    	return savedFilterId;
    }

	public void setSavedFilterId(Integer savedFilterId) {
    	this.savedFilterId = savedFilterId;
    }

	public Integer getSavedCohortId() {
    	return savedCohortId;
    }

	public void setSavedCohortId(Integer savedCohortId) {
    	this.savedCohortId = savedCohortId;
    }

}

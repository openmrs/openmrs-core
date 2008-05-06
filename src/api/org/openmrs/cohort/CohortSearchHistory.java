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
package org.openmrs.cohort;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.PatientSetService;
import org.openmrs.api.PatientSetService.BooleanOperator;
import org.openmrs.api.context.Context;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSet;
import org.openmrs.reporting.ReportObject;
import org.openmrs.util.OpenmrsUtil;

public class CohortSearchHistory extends AbstractReportObject {
	
	protected transient final Log log = LogFactory.getLog(getClass());
	
	public class CohortSearchHistoryItemHolder {
		private PatientSearch search; 
		private PatientFilter filter;
		private String name;
		private String description;
		private Boolean saved;
		private PatientSet cachedResult;
		private Date cachedResultDate;
		public CohortSearchHistoryItemHolder() { }
		public PatientSet getCachedResult() {
			return cachedResult;
		}
		public void setCachedResult(PatientSet cachedResult) {
			this.cachedResult = cachedResult;
		}
		public Date getCachedResultDate() {
			return cachedResultDate;
		}
		public void setCachedResultDate(Date cachedResultDate) {
			this.cachedResultDate = cachedResultDate;
		}
		public PatientSearch getSearch() {
        	return search;
        }
		public void setSearch(PatientSearch search) {
        	this.search = search;
        }
		public PatientFilter getFilter() {
        	return filter;
        }
		public void setFilter(PatientFilter filter) {
        	this.filter = filter;
        }
		public String getDescription() {
        	return description;
        }
		public void setDescription(String description) {
        	this.description = description;
        }
		public String getName() {
        	return name;
        }
		public void setName(String name) {
        	this.name = name;
        }
		public Boolean getSaved() {
        	return saved;
        }
		public void setSaved(Boolean saved) {
        	this.saved = saved;
        }
	}
	
	private List<PatientSearch> searchHistory;
	private volatile List<PatientFilter> cachedFilters;
	private volatile List<PatientSet> cachedResults;
	private volatile List<Date> cachedResultDates;
	
	public CohortSearchHistory() {
		super.setType("Search History");
		super.setSubType("Search History");
		searchHistory = new ArrayList<PatientSearch>();
		cachedFilters = new ArrayList<PatientFilter>();
		cachedResults = new ArrayList<PatientSet>();
		cachedResultDates = new ArrayList<Date>();
	}
	
	public synchronized List<CohortSearchHistoryItemHolder> getItems() {
		checkArrayLengths();
		List<CohortSearchHistoryItemHolder> ret = new ArrayList<CohortSearchHistoryItemHolder>();
		for (int i = 0; i < searchHistory.size(); ++i) {
			CohortSearchHistoryItemHolder item = new CohortSearchHistoryItemHolder();
			PatientSearch search = searchHistory.get(i);
			item.setSearch(search);
			ensureCachedFilter(i);
			PatientFilter filter = cachedFilters.get(i);
			item.setFilter(filter);
			if (search.isSavedFilterReference()) {
				ReportObject ro = Context.getReportService().getReportObject(search.getSavedFilterId());
				item.setName(ro.getName());
				item.setDescription(ro.getDescription());
			} else if (search.isSavedCohortReference()) {
				Cohort c = Context.getCohortService().getCohort(search.getSavedCohortId());
				item.setName(c.getName());
				item.setDescription(c.getDescription());
			} else if (search.isSavedSearchReference()) {
				ReportObject ro = Context.getReportService().getReportObject(search.getSavedSearchId());
				item.setName(ro.getName());
				item.setDescription(ro.getDescription());
			} else if (search.isComposition()) {
				item.setName(search.getCompositionString());
			} else {
				item.setName(filter.getName());
				item.setDescription(filter.getDescription());
			}
			item.setSaved(search.isSavedReference());
			item.setCachedResult(cachedResults.get(i));
			item.setCachedResultDate(cachedResultDates.get(i));
			ret.add(item);
		}
		return ret;
	}
	
	public List<PatientSearch> getSearchHistory() {
		return searchHistory;
	}

	public void setSearchHistory(List<PatientSearch> searchHistory) {
		this.searchHistory = searchHistory;
		cachedFilters = new ArrayList<PatientFilter>();
		cachedResults = new ArrayList<PatientSet>();
		cachedResultDates = new ArrayList<Date>();
		for (int i = 0; i < searchHistory.size(); ++i) {
			cachedFilters.add(null);
			cachedResults.add(null);
			cachedResultDates.add(null);
		}
	}

	public List<Date> getCachedResultDates() {
		return cachedResultDates;
	}

	public List<PatientSet> getCachedResults() {
		return cachedResults;
	}
	
	public int size() {
		return searchHistory.size();
	}
	
	public int getSize() {
		return size();
	}
	
	public synchronized void addSearchItem(PatientSearch ps) {
		checkArrayLengths();
		searchHistory.add(ps);
		cachedFilters.add(null);
		cachedResults.add(null);
		cachedResultDates.add(null);
	}
	
	public synchronized void removeSearchItem(int i) {
		checkArrayLengths();
		List<Integer> toDelete = new ArrayList<Integer>();
		toDelete.add(i);
		while (toDelete.size() > 0) {
			int index = toDelete.remove(0);
			List<Integer> toCascade = removeSearchItemHelper(index);
			searchHistory.remove(index);
			cachedFilters.remove(index);
			cachedResults.remove(index);
			cachedResultDates.remove(index);
			toDelete.addAll(toCascade);
		}
	}
	
	/**
	 * @return zero-based indices that should also be removed due to cascading. (These will already have had 1 subtracted from them, since we know that a search from above is being deleted) 
	 */
	private synchronized List<Integer> removeSearchItemHelper(int i) {
		// 1. Decrement any number in a CohortHistoryCompositionFilter that's greater than i.
		// 2. If any CohortHistoryCompositionFilter references search i, we'll have to cascade delete it
		List<Integer> ret = new ArrayList<Integer>();
		for (int j = i + 1; j < searchHistory.size(); ++j) {
			PatientSearch ps = searchHistory.get(j);
			if (ps.isComposition()) {
				cachedFilters.set(i, null); // this actually only needs to happen if the filter is affected
				// note that i is zero-based, but in a composition filter it would be one-based
				boolean removeMeToo = ps.removeFromHistoryNotify(i + 1);
				if (removeMeToo)
					ret.add(j - 1);
			}
		}
		return ret;
	}
	
	public synchronized PatientFilter ensureCachedFilter(int i) {
		if (cachedFilters.get(i) == null)
			cachedFilters.set(i, OpenmrsUtil.toPatientFilter(searchHistory.get(i), this));
		return cachedFilters.get(i);
	}

	/**
	 * @param i
	 * @return patient set resulting from the i_th filter in the search history. (cached if possible)
	 */
	public PatientSet getPatientSet(int i) {
		return getPatientSet(i, true);
	}
	
	/**
	 * @param i
	 * @param useCache whether to use a cached result, if available
	 * @return patient set resulting from the i_th filter in the search history
	 */
    public PatientSet getPatientSet(int i, boolean useCache) {
		checkArrayLengths();
		PatientSet ret = null;
		if (useCache)
			ret = cachedResults.get(i);
		if (ret == null) {
			ensureCachedFilter(i);
			PatientFilter pf = cachedFilters.get(i); 
			PatientSet everyone = Context.getPatientSetService().getAllPatients();
			ret = pf.filter(everyone);
			cachedFilters.set(i, pf);
			cachedResults.set(i, ret);
			cachedResultDates.set(i, new Date());
		}
		return ret;
	}

    //TODO: figure out whether to return empty paitentset or all patients when history is empty
    public PatientSet getLastPatientSet() {
    	if (searchHistory.size() > 0)
    		return getPatientSet(searchHistory.size() - 1);
    	else
    		//return Context.getPatientSetService().getAllPatients();
    		return new PatientSet();
    }
    
    public PatientSet getPatientSetCombineWithAnd() {
    	Set<Integer> current = null;
    	for (int i = 0; i < searchHistory.size(); ++i) {
    		PatientSet ps = getPatientSet(i);
    		if (current == null)
    			current = new HashSet<Integer>(ps.getPatientIds());
    		else
    			current.retainAll(ps.getPatientIds());
    	}
    	if (current == null)
    		return Context.getPatientSetService().getAllPatients();
    	else {
    		List<Integer> ret = new ArrayList<Integer>(current);
    		Collections.sort(ret);
    		PatientSet ps = new PatientSet();
    		ps.setPatientIds(ret);
    		return ps;
    	}
    }
    
    public PatientSet getPatientSetCombineWithOr() {
    	Set<Integer> ret = new HashSet<Integer>();
    	for (int i = 0; i < searchHistory.size(); ++i) {
    		ret.addAll(getPatientSet(i).getPatientIds());
    	}
    	return new PatientSet().copyPatientIds(ret);
    }
	
	// Just in case someone has modified the searchHistory list directly. Maybe I should make that getter return an unmodifiable list.
	// TODO: this isn't actually good enough. Use the unmodifiable list method instead
	private synchronized void checkArrayLengths() {
		int n = searchHistory.size();
		while (cachedFilters.size() > n)
			cachedFilters.remove(n);
		while (cachedResults.size() > n)
			cachedResults.remove(n);
		while (cachedResultDates.size() > n)
			cachedResultDates.remove(n);
		while (cachedFilters.size() < n)
			cachedFilters.add(null);
		while (cachedResults.size() < n)
			cachedResults.add(null);
		while (cachedResultDates.size() < n)
			cachedResultDates.add(null);
	}

	public PatientSearch createCompositionFilter(String description) {
		Set<String> andWords = new HashSet<String>();
		Set<String> orWords = new HashSet<String>();
		Set<String> notWords = new HashSet<String>();
		andWords.add("and");
		andWords.add("intersection");
		andWords.add("*");
		orWords.add("or");
		orWords.add("union");
		orWords.add("+");
		notWords.add("not");
		notWords.add("!");

		List<Object> currentLine = new ArrayList<Object>();

		try {
			StreamTokenizer st = new StreamTokenizer(new StringReader(description));
			st.ordinaryChar('(');
			st.ordinaryChar(')');
			Stack<List<Object>> stack = new Stack<List<Object>>();
			while (st.nextToken() != StreamTokenizer.TT_EOF) {
				if (st.ttype == StreamTokenizer.TT_NUMBER) {
					Integer thisInt = new Integer((int) st.nval);
					if (thisInt < 1 || thisInt > searchHistory.size()) {
						log.error("number < 1 or > search history size");
						return null;
					}
					currentLine.add(thisInt);
				} else if (st.ttype == '(') {
					stack.push(currentLine);
					currentLine = new ArrayList<Object>();
				} else if (st.ttype == ')') {
					List<Object> l = stack.pop();
					l.add(currentLine);
					currentLine = l;
				} else if (st.ttype == StreamTokenizer.TT_WORD) {
					String str = st.sval.toLowerCase();
					if (andWords.contains(str))
						currentLine.add(PatientSetService.BooleanOperator.AND);
					else if (orWords.contains(str))
						currentLine.add(PatientSetService.BooleanOperator.OR);
					else if (notWords.contains(str))
						currentLine.add(PatientSetService.BooleanOperator.NOT);
					else
						throw new IllegalArgumentException("Don't recognize " + st.sval);
				}
			}
		} catch (Exception ex) {
			log.error("Error in description string: " + description, ex);
			return null; 
		}

		if (!testCompositionList(currentLine)) {
			log.error("Description string failed test: " + description);
			return null;
		}
		
		//return toPatientFilter(currentLine);
		PatientSearch ret = new PatientSearch();
		ret.setParsedComposition(currentLine);
		return ret;
	}
	
	private static boolean testCompositionList(List<Object> list) {
		// if length > 2, make sure there's at least one operator
		// make sure NOT is always followed by something
		// make sure not everything is a logical operator
		// can't have two logical operators in a row (unless the second is a NOT)
		boolean anyNonOperator = false;
		boolean anyOperator = false;
		boolean lastIsNot = false;
		boolean lastIsOperator = false;
		boolean childrenOkay = true;
		for (Object o : list) {
			if (o instanceof List) {
				childrenOkay &= testCompositionList((List<Object>) o);
				anyNonOperator = true;
			} else if (o instanceof BooleanOperator) {
				if (lastIsOperator && (BooleanOperator) o != BooleanOperator.NOT)
					return false;
				anyOperator = true;
			} else if (o instanceof Integer) {
				anyNonOperator = true;
			} else {
				throw new RuntimeException("Programming error! unexpected class " + o.getClass());
			}
			lastIsNot = ( (o instanceof BooleanOperator) && (((BooleanOperator) o) == BooleanOperator.NOT) );
			lastIsOperator = o instanceof BooleanOperator;
		}
		if (list.size() > 2 && !anyOperator)
			return false;
		if (lastIsNot)
			return false;
		if (!anyNonOperator)
			return false;
		return true;
	}
	
}

package org.openmrs.cohort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.openmrs.api.context.Context;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.CompoundPatientFilter;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSet;

public class CohortSearchHistory extends AbstractReportObject {
	
	public class CohortSearchHistoryItemHolder {
		private PatientFilter filter;
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
		public PatientFilter getFilter() {
			return filter;
		}
		public void setFilter(PatientFilter filter) {
			this.filter = filter;
		}
	}
	
	private List<PatientFilter> searchHistory;
	private volatile List<PatientSet> cachedResults;
	private volatile List<Date> cachedResultDates;
	
	public CohortSearchHistory() {
		super.setType("org.openmrs.cohort.CohortSearchHistory");
		searchHistory = new ArrayList<PatientFilter>();
		cachedResults = new ArrayList<PatientSet>();
		cachedResultDates = new ArrayList<Date>();
	}
	
	public synchronized List<CohortSearchHistoryItemHolder> getItems() {
		checkArrayLengths();
		List<CohortSearchHistoryItemHolder> ret = new ArrayList<CohortSearchHistoryItemHolder>();
		for (int i = 0; i < searchHistory.size(); ++i) {
			CohortSearchHistoryItemHolder item = new CohortSearchHistoryItemHolder();
			item.setFilter(searchHistory.get(i));
			item.setCachedResult(cachedResults.get(i));
			item.setCachedResultDate(cachedResultDates.get(i));
			ret.add(item);
		}
		return ret;
	}
	
	public List<PatientFilter> getSearchHistory() {
		return searchHistory;
	}

	public void setSearchHistory(List<PatientFilter> searchHistory) {
		this.searchHistory = searchHistory;
		cachedResults = new ArrayList<PatientSet>();
		cachedResultDates = new ArrayList<Date>();
		for (int i = 0; i < searchHistory.size(); ++i) {
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
	
	public synchronized void addSearchItem(PatientFilter pf) {
		checkArrayLengths();
		searchHistory.add(pf);
		cachedResults.add(null);
		cachedResultDates.add(null);
	}
	
	public synchronized void removeSearchItem(int i) {
		checkArrayLengths();
		searchHistory.remove(i);
		cachedResults.remove(i);
		cachedResultDates.remove(i);
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
			PatientFilter pf = searchHistory.get(i);
			PatientSet everyone = Context.getPatientSetService().getAllPatients();
			ret = pf.filter(everyone);
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
		while (cachedResults.size() > n)
			cachedResults.remove(n);
		while (cachedResultDates.size() > n)
			cachedResultDates.remove(n);
		while (cachedResults.size() < n)
			cachedResults.add(null);
		while (cachedResultDates.size() < n)
			cachedResultDates.add(null);
	}
	
	/**
	 * Currently a hack version: any list of numbers and one case-insensitive instance of the word AND or OR (e.g. "and 1 2 3")
	 * Eventually this should support something like "1 and (2 or 3)"
	 * @param description
	 * @return
	 */
	public PatientFilter createCompositionFilter(String description) {
		CompoundPatientFilter ret = null;
		CompoundPatientFilter.Operator op = CompoundPatientFilter.Operator.AND;
		List<PatientFilter> toCompose = new ArrayList<PatientFilter>();
		for (StringTokenizer st = new StringTokenizer(description); st.hasMoreTokens(); ) {
			String s = st.nextToken();
			s = s.toLowerCase();
			if ("or".equals(s))
				op = CompoundPatientFilter.Operator.OR;
			else if ("and".equals(s))
				op = CompoundPatientFilter.Operator.AND;
			else {
				try {
					int i = Integer.parseInt(s);
					toCompose.add(searchHistory.get(i - 1));
				} catch (Exception ex) { }
			}
		}
		if (toCompose.size() > 0) {
			ret = new CompoundPatientFilter(op, toCompose);
			// TODO: this won't actually work right
			ret.setDescription(description);
		}
		return ret;
	}
	
}

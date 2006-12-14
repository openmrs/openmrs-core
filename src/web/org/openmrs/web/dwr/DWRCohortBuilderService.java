package org.openmrs.web.dwr;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSet;

public class DWRCohortBuilderService {

	protected final Log log = LogFactory.getLog(getClass());
	
	public Integer getResultCountForFilterId(Integer filterId) {
		PatientFilter pf = Context.getReportService().getPatientFilterById(filterId);
		if (pf == null)
			return null;
		PatientSet everyone = Context.getPatientSetService().getAllPatients();
		PatientSet filtered = pf.filter(everyone);
		return filtered.size();
	}
	
	/**
	 * @param index
	 * @return the number of patients in the resulting PatientSet
	 */
	public Integer getResultCountForSearch(int index) {
		CohortSearchHistory history = (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
		PatientSet ps = history.getPatientSet(index);
		return ps.size();
	}
	
	public PatientSet getResultForSearch(int index) {
		CohortSearchHistory history = (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
		PatientSet ps = history.getPatientSet(index);
		return ps;
	}
	
	public PatientSet getResultCombineWithAnd() {
		CohortSearchHistory history = (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
		PatientSet ps = history.getPatientSetCombineWithAnd();
		return ps;
	}
	
	public PatientSet getResultCombineWithOr() {
		CohortSearchHistory history = (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
		PatientSet ps = history.getPatientSetCombineWithOr();
		return ps;
	}
	
	public PatientSet getLastResult() {
		CohortSearchHistory history = (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
		PatientSet ps = history.getLastPatientSet();
		return ps;
	}
	
	public List<ListItem> getSearchHistories() {
		List<ListItem> ret = new ArrayList<ListItem>();
		List<CohortSearchHistory> histories = Context.getReportService().getSearchHistories();
		for (CohortSearchHistory h : histories) {
			ListItem li = new ListItem();
			li.setId(h.getReportObjectId());
			li.setName(h.getName());
			li.setDescription(h.getDescription());
			ret.add(li);
		}
		return ret;
	}
	
	public void saveSearchHistory(String name, String description) {
		CohortSearchHistory history = (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
		if (history.getReportObjectId() != null)
			throw new RuntimeException("Re-saving search history Not Yet Implemented");
		history.setName(name);
		history.setDescription(description);
		Context.getReportService().createSearchHistory(history);
	}
	
	public void loadSearchHistory(Integer id) {
		Context.setVolatileUserData("CohortBuilderSearchHistory", Context.getReportService().getSearchHistory(id));
	}
	
	/**
	 * This isn't really useful because most of the properties don't have DWR converters.
	 * I'm leaving it here in case I get to work on it later.
	 */
	public CohortSearchHistory getUserSearchHistory() {
		return (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
	}
	
}

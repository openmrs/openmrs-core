package org.openmrs.web.dwr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortHistoryCompositionFilter;
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSet;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

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
	
	public List<ListItem> getSavedFilters() {
		List<ListItem> ret = new ArrayList<ListItem>();
		List<PatientFilter> savedFilters = Context.getReportService().getAllPatientFilters();
		for (PatientFilter pf : savedFilters) {
			ListItem li = new ListItem();
			li.setId(pf.getReportObjectId());
			li.setName(pf.getName());
			li.setDescription(pf.getDescription());
			ret.add(li);
		}
		return ret;
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
	 * Saves an element from the search history as a PatientFilter 
	 * @param name The name to give the saved filter
	 * @param description The description to give the saved filter
	 * @param indexInHistory The index into the authenticated user's search history
	 */
	public Boolean saveHistoryElement(String name, String description, Integer indexInHistory) {
		CohortSearchHistory history = (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
		try {
			PatientFilter pf = history.getSearchHistory().get(indexInHistory);
			if (pf == null)
				return false;
			// we can't just save a CohortHistoryCompositionFilter because it depends on its history
			if (pf instanceof CohortHistoryCompositionFilter)
				pf = ((CohortHistoryCompositionFilter) pf).toCohortDefinition();
			AbstractReportObject aro = (AbstractReportObject) pf;
			aro.setName(name);
			aro.setDescription(description);
			aro.setReportObjectId(null); // if this is already a saved object, we resave it as a new one
			aro.setType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTFILTER);
			aro.setSubType("CohortDefinition");
			Context.getReportService().createReportObject(aro);
			return true;
		} catch (Exception ex) {
			log.error("Exception", ex);
			return false;
		}
	}
	
	public void saveCohort(String name, String description, String commaSeparatedIds) {
		Set<Integer> ids = new HashSet<Integer>(OpenmrsUtil.delimitedStringToIntegerList(commaSeparatedIds, ","));
		Cohort cohort = new Cohort();
		cohort.setName(name);
		cohort.setDescription(description);
		cohort.setMemberIds(ids);
		Context.getCohortService().createCohort(cohort);
	}
	
	/**
	 * This isn't really useful because most of the properties don't have DWR converters.
	 * I'm leaving it here in case I get to work on it later.
	 */
	public CohortSearchHistory getUserSearchHistory() {
		return (CohortSearchHistory) Context.getVolatileUserData("CohortBuilderSearchHistory");
	}
	
}

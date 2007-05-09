package org.openmrs.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openmrs.api.APIException;
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.reporting.db.ReportDAO;
import org.openmrs.reporting.db.ReportObjectDAO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ReportService {

	public void setReportDAO(ReportDAO dao);

	public void setReportObjectDAO(ReportObjectDAO dao);

	@Transactional(readOnly=true)
	public Set<Report> getAllReports();

	@Transactional(readOnly=true)
	public Report getReport(Integer reportId) throws APIException;

	public void createReport(Report report) throws APIException;

	public void deleteReport(Report report) throws APIException;

	public void updateReport(Report report) throws APIException;

	@Transactional(readOnly=true)
	public List<AbstractReportObject> getAllReportObjects();

	@Transactional(readOnly=true)
	public AbstractReportObject getReportObject(Integer reportObjectId)
			throws APIException;

	@Transactional(readOnly=true)
	public List<AbstractReportObject> getReportObjectsByType(
			String reportObjectType) throws APIException;

	@Transactional(readOnly=true)
	public PatientFilter getPatientFilterById(Integer filterId)
			throws APIException;

	@Transactional(readOnly=true)
	public PatientFilter getPatientFilterByName(String filterName)
			throws APIException;

	@Transactional(readOnly=true)
	public ArrayList<PatientFilter> getAllPatientFilters() throws APIException;

	public void createReportObject(AbstractReportObject reportObject)
			throws APIException;

	public void deleteReport(AbstractReportObject reportObject)
			throws APIException;

	public void updateReportObject(AbstractReportObject reportObject)
			throws APIException;

	@Transactional(readOnly=true)
	public Set<String> getReportObjectTypes();

	@Transactional(readOnly=true)
	public Set<String> getReportObjectSubTypes(String type);

	public boolean isSubTypeOfType(String type, String subType);

	@Transactional(readOnly=true)
	public String getReportObjectClassBySubType(String subType);

	@Transactional(readOnly=true)
	public Set<String> getAllReportObjectClasses();

	@Transactional(readOnly=true)
	public String getReportObjectValidatorByClass(String currentClassName);

	@Transactional(readOnly=true)
	public String getDefaultReportObjectValidator();
	
	public void createSearchHistory(CohortSearchHistory history);
	
	public void deleteSearchHistory(CohortSearchHistory history);
	
	// public void updateSearchHistory(CohortSearchHistory history);
	
	@Transactional(readOnly=true)
	public CohortSearchHistory getSearchHistory(Integer id);
	
	@Transactional(readOnly=true)
	public List<CohortSearchHistory> getSearchHistories();

}
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
package org.openmrs.reporting.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.reporting.Report;
import org.openmrs.reporting.ReportObjectFactory;
import org.openmrs.reporting.ReportService;
import org.openmrs.reporting.db.ReportDAO;
import org.openmrs.reporting.db.ReportObjectDAO;
import org.openmrs.util.OpenmrsConstants;

public class ReportServiceImpl implements ReportService {

	private Log log = LogFactory.getLog(this.getClass());
	
	private ReportDAO dao;
	
	private ReportObjectDAO objectDAO;
	
	private ReportObjectFactory reportObjectFactory;
	
	public ReportServiceImpl() {
		new ReportObjectFactory(); // instantiate reportObjectFactory
		this.reportObjectFactory = ReportObjectFactory.getInstance();
	}
	
	private ReportDAO getReportDAO() {
		return dao;
	}
	
	public void setReportDAO(ReportDAO dao) {
		this.dao = dao;
	}
	
	private ReportObjectDAO getReportObjectDAO() {
		return this.objectDAO;
	}
	
	public void setReportObjectDAO(ReportObjectDAO dao) {
		this.objectDAO = dao;
	}

	public Set<Report> getAllReports() {
		return getReportDAO().getAllReports();
	}
	
	public Report getReport(Integer reportId) throws APIException {
		return getReportDAO().getReport(reportId);
	}
	
	public void createReport(Report report) throws APIException {
		getReportDAO().createReport(report);
	}
	
	public void deleteReport(Report report) throws APIException {
		getReportDAO().deleteReport(report);
	}
	
	public void updateReport(Report report) throws APIException {
		getReportDAO().updateReport(report);
	}

	public List<AbstractReportObject> getAllReportObjects() {
		return getReportObjectDAO().getAllReportObjects();
	}
		
	public AbstractReportObject getReportObject(Integer reportObjectId) throws APIException {
		return getReportObjectDAO().getReportObject(reportObjectId);
	}
	
	public List<AbstractReportObject> getReportObjectsByType(String reportObjectType) throws APIException {
		return getReportObjectDAO().getReportObjectsByType(reportObjectType);
	}
	
	public PatientSearch getPatientSearch(Integer searchId) throws APIException {
		return ((PatientSearchReportObject) getReportObject(searchId)).getPatientSearch();
	}
	
	public List<PatientSearch> getAllPatientSearches() throws APIException {
		List<PatientSearch> allSearches = new ArrayList<PatientSearch>();
		List<AbstractReportObject> allMatchingObjects = getReportObjectsByType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH);
		if ( allMatchingObjects != null ) {
			for ( AbstractReportObject aro : allMatchingObjects ) {
				allSearches.add( ((PatientSearchReportObject) aro).getPatientSearch() );
			}
		}
		return allSearches;
	}
	
	public PatientSearch getPatientSearch(String name) throws APIException {
		List<AbstractReportObject> allMatchingObjects = getReportObjectsByType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH);
		if ( allMatchingObjects != null ) {
			for ( AbstractReportObject aro : allMatchingObjects ) {
				if (aro.getName().equals(name))
					return ((PatientSearchReportObject) aro).getPatientSearch();
			}
		}
		return null;
	}

	public PatientFilter getPatientFilterById(Integer filterId) throws APIException {
		return (PatientFilter)getReportObject(filterId);
	}
	
	public PatientFilter getPatientFilterByName(String filterName) throws APIException {
		// TODO: push this into the DAO layer
		for (PatientFilter pf : getAllPatientFilters()) {
			AbstractReportObject o = (AbstractReportObject) pf;
			if (filterName.equals(o.getName())) {
				return pf;
			}
		}
		return null;
	}
	
	public ArrayList<PatientFilter> getAllPatientFilters() throws APIException {
		ArrayList<PatientFilter> allPatientFilters = new ArrayList<PatientFilter>();
		List<AbstractReportObject> allMatchingObjects = getReportObjectsByType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTFILTER);
		if ( allMatchingObjects != null ) {
			for ( AbstractReportObject aro : allMatchingObjects ) {
				allPatientFilters.add((PatientFilter)aro);
			}
		}
		return allPatientFilters;
	}

	public Integer createReportObject(AbstractReportObject reportObject) throws APIException {
		return getReportObjectDAO().createReportObject(reportObject);
	}
	
	public void deleteReport(AbstractReportObject reportObject) throws APIException {
		getReportObjectDAO().deleteReportObject(reportObject);
	}
	
	public void updateReportObject(AbstractReportObject reportObject) throws APIException {
		getReportObjectDAO().updateReportObject(reportObject);
	}

	public Set<String> getReportObjectTypes() {
		Set<String> availableTypes = this.reportObjectFactory.getReportObjectTypes();
		return availableTypes;
	}
	
	public Set<String> getReportObjectSubTypes(String type) {
		Set<String> availableTypes = this.reportObjectFactory.getReportObjectSubTypes(type);
		return availableTypes;
	}

	public boolean isSubTypeOfType(String type, String subType) {
		return this.reportObjectFactory.isSubTypeOfType(type, subType);
	}

	public String getReportObjectClassBySubType(String subType) {
		String className = this.reportObjectFactory.getReportObjectClassBySubType(subType);
		return className;
	}

	public Set<String> getAllReportObjectClasses() {
		Set<String> allClasses = this.reportObjectFactory.getAllReportObjectClasses();
		return allClasses;
	}

	public String getReportObjectValidatorByClass(String currentClassName) {
		String validatorClass = this.reportObjectFactory.getReportObjectValidatorByClass(currentClassName);
		return validatorClass;
	}
	
	public String getDefaultReportObjectValidator() {
		String defaultValidator = this.reportObjectFactory.getDefaultValidator();
		return defaultValidator;
	}
	
	public void createSearchHistory(CohortSearchHistory history) {
		createReportObject(history);
	}
	
	public void deleteSearchHistory(CohortSearchHistory history) {
		deleteReport(history); // TODO: check if this is right, and if so rename it to deleteReportObject
	}
	
	public CohortSearchHistory getSearchHistory(Integer reportObjectId) {
		return (CohortSearchHistory) getReportObject(reportObjectId);
	}
	
	public List<CohortSearchHistory> getSearchHistories() {
		List<AbstractReportObject> temp = getReportObjectsByType("org.openmrs.cohort.CohortSearchHistory");
		List<CohortSearchHistory> ret = new ArrayList<CohortSearchHistory>();
		for (AbstractReportObject o : temp)
			ret.add((CohortSearchHistory) o);
		return ret;
	}

}

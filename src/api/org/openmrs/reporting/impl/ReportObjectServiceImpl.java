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

import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.reporting.PatientSearchReportObject;
import org.openmrs.reporting.ReportObjectFactory;
import org.openmrs.reporting.ReportObjectService;
import org.openmrs.reporting.db.ReportObjectDAO;
import org.openmrs.util.OpenmrsConstants;

/**
 *
 */
public class ReportObjectServiceImpl extends BaseOpenmrsService implements ReportObjectService {
	
	private ReportObjectDAO reportObjectDAO;
	
	private ReportObjectFactory reportObjectFactory;
	
	/**
	 * Default constructor
	 */
	public ReportObjectServiceImpl() {
		new ReportObjectFactory(); // instantiate reportObjectFactory
		this.reportObjectFactory = ReportObjectFactory.getInstance();
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#setReportObjectDAO(org.openmrs.reporting.db.ReportObjectDAO)
	 */
	public void setReportObjectDAO(ReportObjectDAO dao) {
		this.reportObjectDAO = dao;
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getAllReportObjects()
	 */
	public List<AbstractReportObject> getAllReportObjects() {
		return reportObjectDAO.getAllReportObjects();
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getReportObject(java.lang.Integer)
	 */
	public AbstractReportObject getReportObject(Integer reportObjectId) throws APIException {
		return reportObjectDAO.getReportObject(reportObjectId);
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getReportObjectsByType(java.lang.String)
	 */
	public List<AbstractReportObject> getReportObjectsByType(String reportObjectType) throws APIException {
		return reportObjectDAO.getReportObjectsByType(reportObjectType);
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getPatientSearch(java.lang.Integer)
	 */
	public PatientSearch getPatientSearch(Integer searchId) throws APIException {
		return ((PatientSearchReportObject) getReportObject(searchId)).getPatientSearch();
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getAllPatientSearches()
	 */
	public List<PatientSearch> getAllPatientSearches() throws APIException {
		List<PatientSearch> allSearches = new ArrayList<PatientSearch>();
		List<AbstractReportObject> allMatchingObjects = getReportObjectsByType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH);
		if (allMatchingObjects != null) {
			for (AbstractReportObject aro : allMatchingObjects) {
				allSearches.add(((PatientSearchReportObject) aro).getPatientSearch());
			}
		}
		return allSearches;
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getPatientSearch(java.lang.String)
	 */
	public PatientSearch getPatientSearch(String name) throws APIException {
		List<AbstractReportObject> allMatchingObjects = getReportObjectsByType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTSEARCH);
		if (allMatchingObjects != null) {
			for (AbstractReportObject aro : allMatchingObjects) {
				if (aro.getName().equals(name))
					return ((PatientSearchReportObject) aro).getPatientSearch();
			}
		}
		return null;
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getPatientFilterById(java.lang.Integer)
	 */
	public PatientFilter getPatientFilterById(Integer filterId) throws APIException {
		return (PatientFilter) getReportObject(filterId);
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getPatientFilterByName(java.lang.String)
	 */
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
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getAllPatientFilters()
	 */
	public List<PatientFilter> getAllPatientFilters() throws APIException {
		ArrayList<PatientFilter> allPatientFilters = new ArrayList<PatientFilter>();
		List<AbstractReportObject> allMatchingObjects = getReportObjectsByType(OpenmrsConstants.REPORT_OBJECT_TYPE_PATIENTFILTER);
		if (allMatchingObjects != null) {
			for (AbstractReportObject aro : allMatchingObjects) {
				allPatientFilters.add((PatientFilter) aro);
			}
		}
		return allPatientFilters;
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#createReportObject(org.openmrs.reporting.AbstractReportObject)
	 * @deprecated
	 */
	public Integer createReportObject(AbstractReportObject reportObject) throws APIException {
		saveReportObject(reportObject);
		return reportObject.getReportObjectId();
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#saveReportObject(org.openmrs.reporting.AbstractReportObject)
	 */
	public AbstractReportObject saveReportObject(AbstractReportObject reportObject) throws APIException {
		return reportObjectDAO.saveReportObject(reportObject);
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#deleteReport(org.openmrs.reporting.AbstractReportObject)
	 * @deprecated
	 */
	public void deleteReport(AbstractReportObject reportObject) throws APIException {
		purgeReportObject(reportObject);
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#purgeReportObject(org.openmrs.reporting.AbstractReportObject)
	 */
	public void purgeReportObject(AbstractReportObject reportObject) throws APIException {
		reportObjectDAO.deleteReportObject(reportObject);
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#updateReportObject(org.openmrs.reporting.AbstractReportObject)
	 * @deprecated
	 */
	public void updateReportObject(AbstractReportObject reportObject) throws APIException {
		saveReportObject(reportObject);
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getReportObjectTypes()
	 */
	public List<String> getReportObjectTypes() {
		List<String> availableTypes = this.reportObjectFactory.getReportObjectTypes();
		return availableTypes;
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getReportObjectSubTypes(java.lang.String)
	 */
	public List<String> getReportObjectSubTypes(String type) {
		List<String> availableTypes = this.reportObjectFactory.getReportObjectSubTypes(type);
		return availableTypes;
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#isSubTypeOfType(java.lang.String,
	 *      java.lang.String)
	 */
	public boolean isSubTypeOfType(String type, String subType) {
		return this.reportObjectFactory.isSubTypeOfType(type, subType);
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getReportObjectClassBySubType(java.lang.String)
	 */
	public String getReportObjectClassBySubType(String subType) {
		String className = this.reportObjectFactory.getReportObjectClassBySubType(subType);
		return className;
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getAllReportObjectClasses()
	 */
	public List<String> getAllReportObjectClasses() {
		List<String> allClasses = this.reportObjectFactory.getAllReportObjectClasses();
		return allClasses;
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getReportObjectValidatorByClass(java.lang.String)
	 */
	public String getReportObjectValidatorByClass(String currentClassName) {
		String validatorClass = this.reportObjectFactory.getReportObjectValidatorByClass(currentClassName);
		return validatorClass;
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getDefaultReportObjectValidator()
	 */
	public String getDefaultReportObjectValidator() {
		String defaultValidator = this.reportObjectFactory.getDefaultValidator();
		return defaultValidator;
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#createSearchHistory(org.openmrs.cohort.CohortSearchHistory)
	 * @deprecated
	 */
	public void createSearchHistory(CohortSearchHistory history) {
		saveSearchHistory(history);
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#saveSearchHistory(org.openmrs.cohort.CohortSearchHistory)
	 */
	public CohortSearchHistory saveSearchHistory(CohortSearchHistory history) {
		return (CohortSearchHistory) saveReportObject(history);
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#deleteSearchHistory(org.openmrs.cohort.CohortSearchHistory)
	 */
	public void deleteSearchHistory(CohortSearchHistory history) {
		purgeSearchHistory(history);
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#purgeSearchHistory(org.openmrs.cohort.CohortSearchHistory)
	 */
	public void purgeSearchHistory(CohortSearchHistory history) {
		purgeReportObject(history);
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getSearchHistory(java.lang.Integer)
	 */
	public CohortSearchHistory getSearchHistory(Integer reportObjectId) {
		return (CohortSearchHistory) getReportObject(reportObjectId);
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getSearchHistories()
	 * @deprecated
	 */
	public List<CohortSearchHistory> getSearchHistories() {
		return getAllSearchHistories();
	}
	
	/**
	 * @see org.openmrs.reporting.ReportObjectService#getAllSearchHistories()
	 */
	public List<CohortSearchHistory> getAllSearchHistories() {
		List<AbstractReportObject> temp = getReportObjectsByType("Search History"); // TODO make this a constant
		List<CohortSearchHistory> ret = new ArrayList<CohortSearchHistory>();
		for (AbstractReportObject o : temp)
			ret.add((CohortSearchHistory) o);
		return ret;
	}
}

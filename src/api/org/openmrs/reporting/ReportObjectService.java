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

import java.util.List;

import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.reporting.db.ReportObjectDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * @deprecated see reportingcompatibility module
 */
@Transactional
@Deprecated
public interface ReportObjectService extends OpenmrsService {
	
	/**
	 * Used by spring to set the data access class to use
	 * 
	 * @param dao
	 */
	public void setReportObjectDAO(ReportObjectDAO dao);
	
	/**
	 * Get all report objects stored in the system
	 * 
	 * @return List<AbstractReportObject> of all report objects stored in the system
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<AbstractReportObject> getAllReportObjects() throws APIException;
	
	/**
	 * Get the report object by internal id
	 * 
	 * @param reportObjectId
	 * @return AbstractReportObject by internal id
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public AbstractReportObject getReportObject(Integer reportObjectId) throws APIException;
	
	/**
	 * Get report objects by type that was stored. Report objects are basically just xml blobs, so
	 * this parameter lets you specify which type of blob to get Current possibilities are
	 * "Search History", Patient Search, Data Export, etc
	 * 
	 * @param reportObjectType String representing the type of the object
	 * @return List<AbstractReportObject> of a specific type
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<AbstractReportObject> getReportObjectsByType(String reportObjectType) throws APIException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @param filterId
	 * @return PatientFilter by filter ID
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public PatientFilter getPatientFilterById(Integer filterId) throws APIException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @param filterName
	 * @return PatientFilter by filter name
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public PatientFilter getPatientFilterByName(String filterName) throws APIException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @return List<PatientFilter> of all Patient Filters
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<PatientFilter> getAllPatientFilters() throws APIException;
	
	/**
	 * @deprecated use {@link #saveReportObject(AbstractReportObject)}
	 */
	public Integer createReportObject(AbstractReportObject reportObject) throws APIException;
	
	/**
	 * @deprecated use {@link #purgeReportObject(AbstractReportObject)}
	 */
	public void deleteReport(AbstractReportObject reportObject) throws APIException;
	
	/**
	 * Completely delete the given reportObject from the database
	 * 
	 * @param reportObject
	 * @throws APIException
	 */
	public void purgeReportObject(AbstractReportObject reportObject) throws APIException;
	
	/**
	 * @deprecated use {@link #saveReportObject(AbstractReportObject)}
	 */
	public void updateReportObject(AbstractReportObject reportObject) throws APIException;
	
	/**
	 * Save the given report object to the database
	 * 
	 * @param reportObject
	 * @return AbstractReportObject that was saved
	 * @throws APIException
	 */
	public AbstractReportObject saveReportObject(AbstractReportObject reportObject) throws APIException;
	
	/**
	 * Get the current list of different reportObjectTypes stored in the system
	 * 
	 * @return List<String> of different reportObjectTypes stored in the system
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<String> getReportObjectTypes() throws APIException;
	
	/**
	 * Get the current list of different sub types stored in the system
	 * 
	 * @return <List> String of different sub types stored in the system
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<String> getReportObjectSubTypes(String type) throws APIException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @param type
	 * @param subType
	 * @return true if the subType is from a particular type
	 * @throws APIException
	 */
	public boolean isSubTypeOfType(String type, String subType) throws APIException;
	
	@Transactional(readOnly = true)
	public String getReportObjectClassBySubType(String subType) throws APIException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @return List<String> of all ReportObjectClasses
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<String> getAllReportObjectClasses() throws APIException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @param currentClassName
	 * @return String of Reported Object Validator by the currentClassName
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public String getReportObjectValidatorByClass(String currentClassName) throws APIException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @return String with the default Report Object Validator
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public String getDefaultReportObjectValidator() throws APIException;
	
	/**
	 * @deprecated use {@link #saveSearchHistory(CohortSearchHistory)}
	 */
	public void createSearchHistory(CohortSearchHistory history) throws APIException;
	
	/**
	 * TODO: why is this method in this service?
	 * 
	 * @param history
	 * @return CohortSearchHistory of the saved Search History of a Cohort
	 * @throws APIException
	 */
	public CohortSearchHistory saveSearchHistory(CohortSearchHistory history) throws APIException;
	
	/**
	 * @deprecated use {@link #purgeSearchHistory(CohortSearchHistory)}
	 */
	public void deleteSearchHistory(CohortSearchHistory history) throws APIException;
	
	/**
	 * Completely delete this method from the database
	 * 
	 * @param history
	 * @throws APIException
	 */
	public void purgeSearchHistory(CohortSearchHistory history) throws APIException;
	
	/**
	 * Get a CohortSearchHistory by internal id or null if none found
	 * 
	 * @param id
	 * @return CohortSearchHistory by internal id. null if not found
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public CohortSearchHistory getSearchHistory(Integer id) throws APIException;
	
	/**
	 * @deprecated use {@link #getAllSearchHistories()}
	 */
	@Transactional(readOnly = true)
	public List<CohortSearchHistory> getSearchHistories() throws APIException;
	
	/**
	 * Get all search histories stored in the database
	 * 
	 * @return List<CohortSearchHistory> of all search histories stored in the database
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<CohortSearchHistory> getAllSearchHistories() throws APIException;
	
	/**
	 * Get patient search object by internal id or null if none found
	 * 
	 * @param searchId
	 * @return PatientSearch by internal id. null if not found
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public PatientSearch getPatientSearch(Integer searchId) throws APIException;
	
	/**
	 * Get all patient searches in the database
	 * 
	 * @return List<PatientSearch> of all patient searches on the database
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public List<PatientSearch> getAllPatientSearches() throws APIException;
	
	/**
	 * Get a patient search matching the name exactly or null if none found
	 * 
	 * @param name
	 * @return PatientSearch by name. null if not found
	 * @throws APIException
	 */
	@Transactional(readOnly = true)
	public PatientSearch getPatientSearch(String name) throws APIException;
	
}

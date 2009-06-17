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
package org.openmrs.api.db;

import java.util.Collection;
import java.util.List;

import org.openmrs.GlobalProperty;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.Report;

/**
 * Database methods for the AdministrationService
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.AdministrationService
 */
public interface AdministrationDAO {
	
	/**
	 * Create a new Report
	 * 
	 * @param r Report to create
	 * @deprecated see reportingcompatibility module
	 * @throws DAOException
	 */
	@Deprecated
	public void createReport(Report r) throws DAOException;
	
	/**
	 * Update Report
	 * 
	 * @param r Report to update
	 * @deprecated see reportingcompatibility module
	 * @throws DAOException
	 */
	@Deprecated
	public void updateReport(Report r) throws DAOException;
	
	/**
	 * Delete Report
	 * 
	 * @param r Report to delete
	 * @deprecated see reportingcompatibility module
	 * @throws DAOException
	 */
	@Deprecated
	public void deleteReport(Report r) throws DAOException;
	
	/**
	 * Create a new Report Object
	 * 
	 * @param ro AbstractReportObject to create
	 * @deprecated see reportingcompatibility module
	 * @throws DAOException
	 */
	@Deprecated
	public void createReportObject(AbstractReportObject ro) throws DAOException;
	
	/**
	 * Update Report Object
	 * 
	 * @param ro AbstractReportObject to update
	 * @deprecated see reportingcompatibility module
	 * @throws DAOException
	 */
	@Deprecated
	public void updateReportObject(AbstractReportObject ro) throws DAOException;
	
	/**
	 * Delete Report Object
	 * 
	 * @deprecated see reportingcompatibility module
	 * @param reportObjectId Internal identifier for report object to delete
	 * @throws DAOException
	 */
	@Deprecated
	public void deleteReportObject(Integer reportObjectId) throws DAOException;
	
	/**
	 * @see org.openmrs.api.AdministrationService#mrnGeneratorLog(java.lang.String,
	 *      java.lang.Integer, java.lang.Integer)
	 */
	public void mrnGeneratorLog(String site, Integer start, Integer count) throws DAOException;
	
	/**
	 * @see org.openmrs.api.AdministrationService#getMRNGeneratorLog()
	 */
	public Collection<?> getMRNGeneratorLog() throws DAOException;
	
	/**
	 * @see org.openmrs.api.AdministrationService#getGlobalProperty(String)
	 */
	public String getGlobalProperty(String propertyName) throws DAOException;
	
	/**
	 * @see org.openmrs.api.AdministrationService#getGlobalPropertyObject(java.lang.String)
	 */
	public GlobalProperty getGlobalPropertyObject(String propertyName);
	
	/**
	 * @see org.openmrs.api.AdministrationService#getAllGlobalProperties()
	 */
	public List<GlobalProperty> getAllGlobalProperties() throws DAOException;
	
	public GlobalProperty getGlobalPropertyByUuid(String uuid) throws DAOException;
	
	/**
	 * @see org.openmrs.api.AdministrationService#getGlobalPropertiesByPrefix(java.lang.String)
	 */
	public List<GlobalProperty> getGlobalPropertiesByPrefix(String prefix);
	
	/**
	 * @see org.openmrs.api.AdministrationService#getGlobalPropertiesBySuffix(java.lang.String)
	 */
	public List<GlobalProperty> getGlobalPropertiesBySuffix(String suffix);
	
	/**
	 * @see org.openmrs.api.AdministrationService#purgeGlobalProperty(org.openmrs.GlobalProperty)
	 */
	public void deleteGlobalProperty(GlobalProperty gp) throws DAOException;
	
	/**
	 * @see org.openmrs.api.AdministrationService#saveGlobalProperty(org.openmrs.GlobalProperty)
	 */
	public GlobalProperty saveGlobalProperty(GlobalProperty gp) throws DAOException;
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#executeSQL(java.lang.String, boolean)
	 */
	public List<List<Object>> executeSQL(String sql, boolean selectOnly) throws DAOException;
	
}

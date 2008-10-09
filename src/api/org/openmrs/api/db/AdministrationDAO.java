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
import java.util.Date;
import java.util.List;

import org.openmrs.DataEntryStatistic;
import org.openmrs.GlobalProperty;
import org.openmrs.ImplementationId;
import org.openmrs.Tribe;
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
	 * Create a new Tribe
	 * @param Tribe to create
	 * @throws DAOException
	 */
	public void createTribe(Tribe tribe) throws DAOException;

	/**
	 * Update Tribe
	 * @param Tribe to update
	 * @throws DAOException
	 */
	public void updateTribe(Tribe tribe) throws DAOException;

	/**
	 * Delete Tribe
	 * @param Tribe to delete
	 * @throws DAOException
	 */
	public void deleteTribe(Tribe tribe) throws DAOException;	
	
	/**
	 * Retire Tribe
	 * @param Tribe to retire
	 * @throws DAOException
	 */
	public void retireTribe(Tribe tribe) throws DAOException;	

	/**
	 * Unretire Tribe
	 * @param Tribe to unretire
	 * @throws DAOException
	 */
	public void unretireTribe(Tribe tribe) throws DAOException;	
	
	/**
	 * Create a new Report
	 * @param Report to create
	 * @throws DAOException
	 */
	public void createReport(Report r) throws DAOException;

	/**
	 * Update Report
	 * @param Report to update
	 * @throws DAOException
	 */
	public void updateReport(Report r) throws DAOException;

	/**
	 * Delete Report
	 * @param Report to delete
	 * @throws DAOException
	 */
	public void deleteReport(Report r) throws DAOException;
	
	/**
	 * Create a new Report Object
	 * @param Report Object to create
	 * @throws DAOException
	 */
	public void createReportObject(AbstractReportObject ro) throws DAOException;

	/**
	 * Update Report Object
	 * @param Report Object to update
	 * @throws DAOException
	 */
	public void updateReportObject(AbstractReportObject ro) throws DAOException;

	/**
	 * Delete Report Object
	 * @param Report Objectto delete
	 * @throws DAOException
	 */
	public void deleteReportObject(Integer reportObjectId) throws DAOException;

	/**
	 * @see org.openmrs.api.AdministrationService#mrnGeneratorLog(java.lang.String, java.lang.Integer, java.lang.Integer)
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
     * @see org.openmrs.api.AdministrationService#getAllGlobalProperties()
     */
    public List<GlobalProperty> getAllGlobalProperties() throws DAOException;

	/**
     * @see org.openmrs.api.AdministrationService#purgeGlobalProperty(org.openmrs.GlobalProperty)
     */
    public void deleteGlobalProperty(GlobalProperty gp) throws DAOException;
	
	/**
     * @see org.openmrs.api.AdministrationService#saveGlobalProperty(org.openmrs.GlobalProperty)
     */
    public GlobalProperty saveGlobalProperty(GlobalProperty gp) throws DAOException;

	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getDataEntryStatistics(java.util.Date, java.util.Date, java.lang.String, java.lang.String, java.lang.String)
	 */
	public List<DataEntryStatistic> getDataEntryStatistics(Date fromDate, Date toDate, String encounterUserColumn, String orderUserColumn, String groupBy) throws DAOException;

	/**
	 * @see org.openmrs.api.db.AdministrationDAO#executeSQL(java.lang.String, boolean)
	 */
	public List<List<Object>> executeSQL(String sql, boolean selectOnly) throws DAOException;
	
    /**
     * @see org.openmrs.api.AdministrationService#getImplementation()
     */
    public ImplementationId getImplementationId();
}
/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting.db;

import java.util.List;

import org.openmrs.api.db.DAOException;
import org.openmrs.reporting.AbstractReportObject;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public interface ReportObjectDAO {
	
	/**
	 * Auto generated method comment
	 * 
	 * @return List<AbstractReportObject> of all reported objects on the system
	 */
	public List<AbstractReportObject> getAllReportObjects() throws DAOException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @param reportObjId
	 * @return AbstractReportObject by a internal report object ID
	 * @throws DAOException
	 */
	public AbstractReportObject getReportObject(Integer reportObjId) throws DAOException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @param reportObj
	 * @throws DAOException
	 */
	public void deleteReportObject(AbstractReportObject reportObj) throws DAOException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @param reportObj
	 * @return AbstractReportObject that was saved
	 * @throws DAOException
	 */
	public AbstractReportObject saveReportObject(AbstractReportObject reportObj) throws DAOException;
	
	/**
	 * Auto generated method comment
	 * 
	 * @param reportObjectType
	 * @return List<AbstractReportObject> of all the Reported Objects of a certain type
	 * @throws DAOException
	 */
	public List<AbstractReportObject> getReportObjectsByType(String reportObjectType) throws DAOException;
}

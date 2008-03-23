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
package org.openmrs.reporting.db;

import java.util.List;

import org.openmrs.api.db.DAOException;
import org.openmrs.reporting.AbstractReportObject;

public interface ReportObjectDAO {
	List<AbstractReportObject> getAllReportObjects();
	AbstractReportObject getReportObject(Integer reportObjId) throws DAOException;
	Integer createReportObject(AbstractReportObject reportObj) throws DAOException;
	void deleteReportObject(AbstractReportObject reportObj) throws DAOException;
	void updateReportObject(AbstractReportObject reportObj) throws DAOException;
	List<AbstractReportObject> getReportObjectsByType(String reportObjectType) throws DAOException;
}

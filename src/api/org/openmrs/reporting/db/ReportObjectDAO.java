package org.openmrs.reporting.db;

import java.util.Set;

import org.openmrs.api.db.DAOException;
import org.openmrs.reporting.AbstractReportObject;

public interface ReportObjectDAO {
	Set<AbstractReportObject> getAllReportObjects();
	AbstractReportObject getReportObject(Integer reportObjId) throws DAOException;
	void createReportObject(AbstractReportObject reportObj) throws DAOException;
	void deleteReportObject(AbstractReportObject reportObj) throws DAOException;
	void updateReportObject(AbstractReportObject reportObj) throws DAOException;
	Set<AbstractReportObject> getReportObjectsByType(String reportObjectType) throws DAOException;
}

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

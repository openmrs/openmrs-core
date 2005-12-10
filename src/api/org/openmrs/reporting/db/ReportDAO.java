package org.openmrs.reporting.db;

import org.openmrs.api.db.DAOException;
import org.openmrs.reporting.Report;

public interface ReportDAO {

	java.util.Set<Report> getAllReports();
	Report getReport(Integer reportId) throws DAOException;
	void createReport(Report report) throws DAOException;
	void deleteReport(Report report) throws DAOException;
	void updateReport(Report report) throws DAOException;

}

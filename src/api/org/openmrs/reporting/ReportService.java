package org.openmrs.reporting;

import org.openmrs.api.db.APIException;

public interface ReportService {

	java.util.Set<Report> getAllReports();
	Report getReport(Integer reportId) throws APIException;
	void createReport(Report report) throws APIException;
	void deleteReport(Report report) throws APIException;
	void updateReport(Report report) throws APIException;
}

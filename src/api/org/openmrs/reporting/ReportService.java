package org.openmrs.reporting;

public interface ReportService {

	java.util.Set<Report> getAllReports();
	Report getReport(Integer reportId);
	
}

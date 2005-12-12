package org.openmrs.reporting;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

public class ReportService {
	
	private Context context;
	
	public ReportService(Context c) {
		this.context = c;
	}

	public java.util.Set<Report> getAllReports() {
		return context.getDAOContext().getReportDAO().getAllReports();
	}
	
	public Report getReport(Integer reportId) throws APIException {
		return context.getDAOContext().getReportDAO().getReport(reportId);
	}
	
	public void createReport(Report report) throws APIException {
		context.getDAOContext().getReportDAO().createReport(report);
	}
	
	public void deleteReport(Report report) throws APIException {
		context.getDAOContext().getReportDAO().deleteReport(report);
	}
	
	public void updateReport(Report report) throws APIException {
		context.getDAOContext().getReportDAO().updateReport(report);
	}
}

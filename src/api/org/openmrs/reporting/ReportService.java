package org.openmrs.reporting;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOContext;
import org.openmrs.reporting.db.ReportDAO;

public class ReportService {
	
	private Context context;
	private DAOContext daoContext;
	
	public ReportService(Context c, DAOContext d) {
		this.context = c;
		this.daoContext = d;
	}
	
	private ReportDAO dao() {
		return daoContext.getReportDAO();
	}

	public java.util.Set<Report> getAllReports() {
		return dao().getAllReports();
	}
	
	public Report getReport(Integer reportId) throws APIException {
		return dao().getReport(reportId);
	}
	
	public void createReport(Report report) throws APIException {
		dao().createReport(report);
	}
	
	public void deleteReport(Report report) throws APIException {
		dao().deleteReport(report);
	}
	
	public void updateReport(Report report) throws APIException {
		dao().updateReport(report);
	}
}

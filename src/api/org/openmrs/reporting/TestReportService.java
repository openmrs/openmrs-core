package org.openmrs.reporting;

import java.util.HashSet;
import java.util.Set;

import org.openmrs.api.APIException;
import org.openmrs.reporting.db.ReportDAO;

public class TestReportService implements ReportDAO {

	Set<Report> reports;
	
	public TestReportService() {
		reports = new java.util.HashSet<Report>();

		Report r = new Report();
		r.setName("Low CD4 counts");
		r.setDescription("List names and accompagnateurs of all patients with CD4 counts below 350.");
		r.setReportId(new Integer(1));
		reports.add(r);
		
		r = new Report();
		r.setName("Enrollment by month");
		r.setDescription("Enrollment by month, subdivided by location");
		r.setReportId(new Integer(2));
		reports.add(r);

		r = new Report();
		r.setName("Outcomes");
		r.setDescription("Pie charts of outcomes, subdivided by Gender and Age(<15 vs >=15)");
		r.setReportId(new Integer(3));
		reports.add(r);
		
	}
	
	public Set<Report> getAllReports() {
		return new HashSet<Report>(reports);
	}

	public Report getReport(Integer reportId) {
		for (Report r : reports) {
			if (reportId.equals(r.getReportId())) {
				return r;
			}
		}
		return null;
	}

	public void createReport(Report report) throws APIException {
		reports.add(report);
	}

	public void deleteReport(Report report) throws APIException {
		reports.remove(report);
	}

	public void updateReport(Report report) throws APIException {
		Report toDelete = null;
		for (Report r : reports) {
			if (r.getReportId() == report.getReportId()) {
				toDelete = r;
				break;
			}
		}
		if (toDelete != null) {
			reports.remove(toDelete);
			reports.add(report);
		} else {
			throw new APIException("no report with id " + report.getReportId());
		}
	}

}

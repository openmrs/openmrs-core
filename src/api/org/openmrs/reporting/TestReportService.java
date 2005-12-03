package org.openmrs.reporting;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TestReportService implements ReportService {

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
		for (Iterator<Report> i = reports.iterator(); i.hasNext(); ) {
			Report r = i.next();
			if (reportId.equals(r.getReportId())) {
				return r;
			}
		}
		return null;
	}

}

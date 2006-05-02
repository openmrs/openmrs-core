package org.openmrs.reporting;

import java.util.HashSet;
import java.util.Set;

public class ReportObjectList {

	Set<AbstractReportObject> reportObjects;
	
	public ReportObjectList() {
		reportObjects = new HashSet<AbstractReportObject>();
	}

	public ReportObjectList( Set<AbstractReportObject> objList ) {
		this.reportObjects = objList;
	}

	public int getSize() {
		return reportObjects.size();
	}

	public Set<AbstractReportObject> getReportObjects() {
		return reportObjects;
	}

	public void setReportObjects(Set<AbstractReportObject> reportObjects) {
		this.reportObjects = reportObjects;
	}
}

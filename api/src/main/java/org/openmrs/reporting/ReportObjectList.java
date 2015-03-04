/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.reporting;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public class ReportObjectList {
	
	Set<AbstractReportObject> reportObjects;
	
	public ReportObjectList() {
		reportObjects = new HashSet<AbstractReportObject>();
	}
	
	public ReportObjectList(Collection<AbstractReportObject> objList) {
		this.reportObjects = new HashSet<AbstractReportObject>();
		this.reportObjects.addAll(objList);
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

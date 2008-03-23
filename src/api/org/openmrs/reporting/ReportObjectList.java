/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.reporting;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ReportObjectList {

	Set<AbstractReportObject> reportObjects;
	
	public ReportObjectList() {
		reportObjects = new HashSet<AbstractReportObject>();
	}

	public ReportObjectList( Collection<AbstractReportObject> objList ) {
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

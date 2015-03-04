/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.summary.web;

import java.util.List;
import java.util.Properties;

public class PatientSummarySpecification {
	
	private static PatientSummarySpecification singleton;
	
	public static PatientSummarySpecification getInstance() {
		return singleton;
	}
	
	private List<Properties> specification;
	
	public PatientSummarySpecification() {
		singleton = this;
	}
	
	public List<Properties> getSpecification() {
		return specification;
	}
	
	public void setSpecification(List<Properties> specification) {
		this.specification = specification;
	}
	
}

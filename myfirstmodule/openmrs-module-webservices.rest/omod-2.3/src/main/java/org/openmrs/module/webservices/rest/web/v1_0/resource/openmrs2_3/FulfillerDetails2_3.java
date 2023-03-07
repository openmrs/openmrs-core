/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_3;

import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_2.FulfillerDetails2_2;

/**
 * Backing object for FulfillerDetailsResource2_3, expands FulfillerDetails2_2 to add Accession
 * Number
 */
public class FulfillerDetails2_3 extends FulfillerDetails2_2 {
	
	private String accessionNumber;
	
	public String getAccessionNumber() {
		return accessionNumber;
	}
	
	public void setAccessionNumber(String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}
}

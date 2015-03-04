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

import java.util.Date;

import org.openmrs.User;

/**
 * @deprecated see reportingcompatibility module
 */
@Deprecated
public abstract class AbstractPatientDataProducer extends AbstractReportObject {
	
	public AbstractPatientDataProducer() {
		// do nothing
		super.setType("Patient Data Producer");
	}
	
	public AbstractPatientDataProducer(Integer reportObjectId, String name, String description, String type, String subType,
	    User creator, Date dateCreated, User changedBy, Date dateChanged, Boolean voided, User voidedBy, Date dateVoided,
	    String voidReason) {
		super(reportObjectId, name, description, type, subType, creator, dateCreated, changedBy, dateChanged, voided,
		        voidedBy, dateVoided, voidReason);
	}
}

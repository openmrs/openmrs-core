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

import java.util.Date;

import org.openmrs.User;

public abstract class AbstractPatientDataProducer extends AbstractReportObject {

	public AbstractPatientDataProducer()
	{
		// do nothing
		super.setType("Patient Data Producer");
	}

	public AbstractPatientDataProducer(Integer reportObjectId, String name, String description, String type, String subType, 
			User creator, Date dateCreated, User changedBy, Date dateChanged, Boolean voided, User voidedBy,
			Date dateVoided, String voidReason )
	{
		super(reportObjectId, name, description, type, subType, creator, dateCreated, changedBy, dateChanged, voided, voidedBy,
				dateVoided, voidReason);
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;

@Resource(name = RestConstants.VERSION_1 + "/patient", supportedClass = Patient.class, supportedOpenmrsVersions = { "1.9.* - 9.*" })
public class PatientResource1_9 extends PatientResource1_8 {
	
	@Override
	public void delete(Patient patient, String reason, RequestContext context) throws ResponseException {
		if (patient.isVoided()) {
			// DELETE is idempotent, so we return success here
			return;
		}
		
		VisitService visitService = Context.getVisitService();
		List<Visit> visits = Context.getVisitService().getVisitsByPatient(patient);
		for (Visit visit : visits) {
			visitService.voidVisit(visit, "Patient deleted");
		}
		
		super.delete(patient, reason, context);
	}
	
}

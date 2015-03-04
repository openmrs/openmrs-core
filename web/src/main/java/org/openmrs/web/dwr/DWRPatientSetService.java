/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.dwr;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

public class DWRPatientSetService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public Vector<PatientListItem> getPatients(String patientIds) {
		Vector<PatientListItem> ret = new Vector<PatientListItem>();
		List<Integer> ptIds = new ArrayList<Integer>();
		for (String s : patientIds.split(",")) {
			try {
				ptIds.add(Integer.valueOf(s));
			}
			catch (Exception ex) {
				log.error("Error during adding integer into list ptIds", ex);
			}
		}
		List<Patient> patients = Context.getPatientSetService().getPatients(ptIds);
		for (Patient patient : patients) {
			ret.add(new PatientListItem(patient));
		}
		return ret;
	}
	
}

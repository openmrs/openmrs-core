/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.annotation.Handler;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Collection;

/**
 * Validates {@link Cohort} objects.
 * @since 2.1.0
 */
@Handler(supports = {Cohort.class}, order=50)
public class CohortValidator implements Validator {

	private static final Log log = LogFactory.getLog(Cohort.class);

	public boolean supports(Class c) {
		return Cohort.class.isAssignableFrom(c);
	}

	public void validate(Object obj, Errors errors) {
		if (obj == null || !(obj instanceof Cohort)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type"
					+ Cohort.class);
		}



		Cohort cohort = (Cohort) obj;
		Collection<CohortMembership> members = cohort.getMembers();
		if (!CollectionUtils.isEmpty(members)) {
			for (CohortMembership member : members) {
				Patient p = member.getPatient();
				if (p.getVoided() && !member.getVoided()) {
					String eMessage = "Patient " + p.getPatientId() + " is voided, cannot add voided members to a cohort";
					errors.rejectValue("members", "Cohort.patientAndMemberShouldBeVoided", eMessage);
				}
			}
		}
	}
}

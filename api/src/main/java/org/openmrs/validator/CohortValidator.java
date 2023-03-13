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

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Cohort;
import org.openmrs.CohortMembership;
import org.openmrs.Patient;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Validates {@link Cohort} objects.
 * @since 2.1.0
 */
@Handler(supports = {Cohort.class}, order=50)
public class CohortValidator implements Validator {

	@Override
	public boolean supports(Class<?> c) {
		return Cohort.class.isAssignableFrom(c);
	}

	@Override
	public void validate(Object obj, Errors errors) {
		if (obj == null || !(obj instanceof Cohort)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type"
					+ Cohort.class);
		}



		Cohort cohort = (Cohort) obj;
		if (!cohort.getVoided()) {
			Collection<CohortMembership> members = cohort.getMemberships();
			if (!CollectionUtils.isEmpty(members)) {
				for (CohortMembership member : members) {
					Patient p = Context.getPatientService().getPatient(member.getPatientId());
					int dateCompare = OpenmrsUtil.compareWithNullAsLatest(member.getStartDate(), member.getEndDate());
					if (p != null && p.getVoided() && !member.getVoided()) {
						String message = "Patient " + p.getPatientId()
								+ " is voided, cannot add voided members to a cohort";
						errors.rejectValue("memberships", "Cohort.patientAndMemberShouldBeVoided", message);
					}
					if (dateCompare == 1) {
						String message = "Start date is null or end date is before start date";
						errors.rejectValue("memberships", "Cohort.startDateShouldNotBeNullOrBeforeEndDate", message);
					}
				}
			}
		}
	}
}

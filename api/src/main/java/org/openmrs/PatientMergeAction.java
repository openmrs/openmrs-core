/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import org.openmrs.Patient;

/**
 * {@link org.openmrs.module.emrapi.adt.AdtService#mergePatients(org.openmrs.Patient, org.openmrs.Patient)}
 * will invoke all Spring beans that implement this within the same transaction
 * that it uses to merge patients.
 */
public interface PatientMergeAction {
	
	/**
	 * This method will be called before calling the underlying OpenMRS
	 * {@link org.openmrs.api.PatientService#mergePatients(org.openmrs.Patient, org.openmrs.Patient)}
	 * method, but in the same transaction. Any thrown exception will cancel the
	 * merge
	 *
	 * @param preferred
	 * @param notPreferred
	 */
	void beforeMergingPatients(Patient preferred, Patient notPreferred);
	
	/**
	 * This method will be called after calling the underlying OpenMRS
	 * {@link org.openmrs.api.PatientService#mergePatients(org.openmrs.Patient, org.openmrs.Patient)}
	 * method, but in the same transaction.
	 *
	 * @param preferred
	 * @param notPreferred
	 */
	void afterMergingPatients(Patient preferred, Patient notPreferred);
}

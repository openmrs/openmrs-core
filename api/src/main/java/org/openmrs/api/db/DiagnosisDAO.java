/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.util.Date;
import java.util.List;

import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Patient;

/**
 * This interface defines database methods for diagnosis objects.
 *
 * @since 2.2
 */
public interface DiagnosisDAO {
	
	/**
	 * Saves the diagnosis
	 *
	 * @param diagnosis the diagnosis to save
	 * @return the saved diagnosis
	 * @throws DAOException exception thrown if error occurs while saving the diagnosis
	 */
	Diagnosis saveDiagnosis(Diagnosis diagnosis) throws DAOException;
	
	/**
	 * Gets a diagnosis from database using the diagnosis id
	 *
	 * @param diagnosisId the id of the diagnosis to look for
	 * @return the diagnosis with the given diagnosis id
	 * @throws DAOException exception thrown if error occurs while getting the diagnosis
	 */
	Diagnosis getDiagnosisById(Integer diagnosisId) throws DAOException;

	/**
	 * Gets the diagnosis attached to the specified UUID.
	 *
	 * @param uuid the uuid to search for in the database.
	 * @return the diagnosis associated with the UUID.
	 */
	Diagnosis getDiagnosisByUuid(String uuid);

	/**
	 * Deletes a diagnosis
	 *
	 * @param diagnosis the diagnosis to delete
	 * @throws DAOException exception thrown if error occurs while deleting the diagnosis
	 */
	void deleteDiagnosis(Diagnosis diagnosis) throws DAOException;
	
	/**
	 * Gets all diagnoses for a given encounter
	 *
	 * @param encounter the specific encounter to get the diagnoses for.
	 * @return list of diagnoses for an encounter
	 */
	List<Diagnosis> getDiagnoses(Encounter encounter);

	/**
	 * Gets primary diagnoses for a given encounter
	 *
	 * @param encounter the specific encounter to get the primary diagnoses for.
	 * @return list of primary diagnoses for an encounter
	 */
	List<Diagnosis> getPrimaryDiagnoses(Encounter encounter);

	/**
	 * Gets all active diagnoses related to the specified patient.
	 *
	 * @param patient the patient whose active diagnoses are being queried.
	 * @param fromDate the start date for the check of active diagnosis
	 *                    
	 * @return all active diagnoses associated with the specified patient.
	 */
	List<Diagnosis> getActiveDiagnoses(Patient patient, Date fromDate);
}

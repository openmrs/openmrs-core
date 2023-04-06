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

import org.openmrs.MedicationDispense;
import org.openmrs.parameter.MedicationDispenseCriteria;

import java.util.List;

/**
 * This interface defines database methods for the MedicationDispense domain
 * @since 2.6.0
 */
public interface MedicationDispenseDAO {

	/**
	 * Gets a MedicationDispense by id
	 * @param medicationDispenseId the id of the MedicationDispense to retrieve
	 * @return the MedicationDispense with the given id, or null if none exists
	 */
	MedicationDispense getMedicationDispense(Integer medicationDispenseId);

	/**
	 * Gets a MedicationDispense based on the uuid
	 * @param uuid - uuid of the MedicationDispense to be returned
	 * @return the MedicationDispense
	 */
	MedicationDispense getMedicationDispenseByUuid(String uuid);

	/**
	 * Gets all MedicationDispense results that match the given criteria
	 * @param criteria - the criteria for the returned MedicationDispense results
	 * @return a list of MedicationDispenses
	 */
	List<MedicationDispense> getMedicationDispenseByCriteria(MedicationDispenseCriteria criteria);

	/**
	 * Saves a MedicationDispense
	 * @param medicationDispense - the MedicationDispense to be saved
	 */
	MedicationDispense saveMedicationDispense(MedicationDispense medicationDispense);

	/**
	 * Remove a MedicationDispense from the database.
	 * @param medicationDispense the MedicationDispense to be purged
	 */
	void deleteMedicationDispense(MedicationDispense medicationDispense);
}

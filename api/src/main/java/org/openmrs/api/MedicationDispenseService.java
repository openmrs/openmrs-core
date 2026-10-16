/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.util.List;

import org.openmrs.MedicationDispense;
import org.openmrs.parameter.MedicationDispenseCriteria;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * This interface defines an API for interacting with MedicationDispense objects.
 *
 * @since 2.6.0
 */
public interface MedicationDispenseService extends OpenmrsService {

	/**
	 * Gets a MedicationDispense by id
	 *
	 * @param medicationDispenseId the id of the MedicationDispense to retrieve
	 * @return the MedicationDispense with the given id, or null if none exists
	 * @throws org.springframework.security.access.AccessDeniedException if the current user lacks
	 *             permission
	 */
	@PreAuthorize("hasPermission(#medicationDispenseId, 'MedicationDispense', '" + PrivilegeConstants.GET_MEDICATION_DISPENSE
	        + "')")
	MedicationDispense getMedicationDispense(Integer medicationDispenseId);

	/**
	 * Gets a MedicationDispense based on the uuid
	 *
	 * @param uuid - uuid of the MedicationDispense to be returned
	 * @return the MedicationDispense
	 * @throws org.springframework.security.access.AccessDeniedException if the current user lacks
	 *             permission
	 */
	@PreAuthorize("hasPermission(#uuid, 'MedicationDispense', '" + PrivilegeConstants.GET_MEDICATION_DISPENSE + "')")
	MedicationDispense getMedicationDispenseByUuid(String uuid);

	/**
	 * Gets all MedicationDispense results that match the given criteria
	 *
	 * @param criteria - the criteria for the returned MedicationDispense results
	 * @return a list of MedicationDispenses
	 * @throws org.springframework.security.access.AccessDeniedException if the current user lacks
	 *             permission
	 */
	@PreAuthorize("hasPermission(#criteria?.patient, '" + PrivilegeConstants.GET_MEDICATION_DISPENSE + "')")
	@PostFilter("hasPermission(filterObject, '" + PrivilegeConstants.GET_MEDICATION_DISPENSE + "')")
	List<MedicationDispense> getMedicationDispenseByCriteria(MedicationDispenseCriteria criteria);

	/**
	 * Saves a MedicationDispense
	 *
	 * @param medicationDispense - the MedicationDispense to be saved
	 * @throws org.springframework.security.access.AccessDeniedException if the current user lacks
	 *             permission
	 */
	@PreAuthorize("hasPermission(#medicationDispense, '" + PrivilegeConstants.EDIT_MEDICATION_DISPENSE + "')")
	MedicationDispense saveMedicationDispense(MedicationDispense medicationDispense);

	/**
	 * Voids a MedicationDispense
	 *
	 * @param medicationDispense the MedicationDispense to be voided
	 * @param reason the reason for voiding the MedicationDispense
	 * @throws org.springframework.security.access.AccessDeniedException if the current user lacks
	 *             permission
	 */
	@PreAuthorize("hasPermission(#medicationDispense, '" + PrivilegeConstants.EDIT_MEDICATION_DISPENSE + "')")
	MedicationDispense voidMedicationDispense(MedicationDispense medicationDispense, String reason);

	/**
	 * Un-void a previously voided MedicationDispense
	 *
	 * @param medicationDispense MedicationDispense to un-void
	 * @throws org.springframework.security.access.AccessDeniedException if the current user lacks
	 *             permission
	 */
	@PreAuthorize("hasPermission(#medicationDispense, '" + PrivilegeConstants.EDIT_MEDICATION_DISPENSE + "')")
	MedicationDispense unvoidMedicationDispense(MedicationDispense medicationDispense);

	/**
	 * Completely remove a MedicationDispense from the database. This should typically not be called
	 * because we don't want to ever lose data. The data really <i>should</i> be voided and then it is
	 * not seen in interface any longer (see #voidMedicationDispense(MedicationDispense) for that one)
	 * If other data references this medicationDispense, an error will be thrown.
	 *
	 * @param medicationDispense the MedicationDispense to be purged
	 * @throws org.springframework.security.access.AccessDeniedException if the current user lacks
	 *             permission
	 */
	@PreAuthorize("hasPermission(#medicationDispense, '" + PrivilegeConstants.DELETE_MEDICATION_DISPENSE + "')")
	void purgeMedicationDispense(MedicationDispense medicationDispense);
}

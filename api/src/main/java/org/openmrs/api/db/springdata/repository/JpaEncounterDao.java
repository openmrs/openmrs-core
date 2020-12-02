/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.springdata.repository;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.api.db.DAOException;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Encounter-related database functions
 */
@Repository
public interface JpaEncounterDao extends JpaRepository<Encounter, Integer>, CrudRepository<Encounter, Integer> {
	/**
	 * Get encounter by internal identifier
	 *
	 * @param encounterId encounter id
	 * @return encounter with given internal identifier
	 * @throws DAOException
	 */
	public Encounter getEncounter(Integer encounterId) throws DAOException;

	/**
	 * @param patientId
	 * @return all encounters for the given patient identifier
	 * @throws DAOException
	 */
	public List<Encounter> getEncountersByPatientId(Integer patientId) throws DAOException;

	/**
	 * @see org.openmrs.api.EncounterService#getEncounters(org.openmrs.parameter.EncounterSearchCriteria)
	 */
	public List<Encounter> getEncounters(EncounterSearchCriteria encounterSearchCriteria);

	/**
	 * Save an Encounter Type
	 *
	 * @param encounterType
	 */
	public EncounterType saveEncounterType(EncounterType encounterType);


}

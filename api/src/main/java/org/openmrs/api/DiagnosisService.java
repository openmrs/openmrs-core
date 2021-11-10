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

import org.openmrs.DiagnosisAttribute;
import org.openmrs.DiagnosisAttributeType;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Diagnosis;
import org.openmrs.Visit;
import org.openmrs.annotation.Authorized;
import org.openmrs.util.PrivilegeConstants;


import java.util.Date;
import java.util.List;

/**
 * <pre>
 * API methods for managing diagnoses
 * </pre>
 * 
 *  @since 2.2
 */
public interface DiagnosisService extends OpenmrsService {

	/**
	 * Saves a diagnosis
	 *
	 * @param diagnosis - the diagnosis to be saved
	 * @return the diagnosis
	 */
	@Authorized({ PrivilegeConstants.EDIT_DIAGNOSES })
	Diagnosis save(Diagnosis diagnosis);

	/**
	 * Voids a diagnosis
	 *
	 * @param diagnosis - the diagnosis to be voided
	 * @param voidReason - the reason for voiding the diagnosis
	 * @return the diagnosis that was voided
	 */
	@Authorized({ PrivilegeConstants.EDIT_DIAGNOSES })
	Diagnosis voidDiagnosis(Diagnosis diagnosis, String voidReason);

	/**
	 * Gets a diagnosis based on the uuid
	 *
	 * @param uuid - uuid of the diagnosis to be returned
	 * @return diagnosis matching the given uuid
	 */
	@Authorized({ PrivilegeConstants.GET_DIAGNOSES })
	Diagnosis getDiagnosisByUuid(String uuid);

	/**
	 * Gets diagnoses since date, sorted in reverse chronological order
	 *
	 * @param patient the patient whose diagnosis we are to get
	 * @param fromDate the date used to filter diagnosis which happened from this date and later
	 * @return the list of diagnoses for the given patient and starting from the given date
	 */
	@Authorized({ PrivilegeConstants.GET_DIAGNOSES })
	List<Diagnosis> getDiagnoses(Patient patient, Date fromDate);

	/**
	 * Gets diagnoses for an Encounter.
	 *
	 * @param encounter the encounter for which to fetch diagnoses
	 * @param primaryOnly whether to return only primary diagnoses
	 * @param confirmedOnly whether to return only confirmed diagnoses
	 * @return the list of diagnoses for the given encounter
	 * 
	 * @since 2.5.0
	 */
	@Authorized({ PrivilegeConstants.GET_DIAGNOSES })
	List<Diagnosis> getDiagnosesByEncounter(Encounter encounter, boolean primaryOnly, boolean confirmedOnly);
	
	/**
	 * Gets diagnoses for a Visit.
	 *
	 * @param visit the visit for which to fetch diagnoses
	 * @param primaryOnly whether to return only primary diagnoses
	 * @param confirmedOnly whether to return only confirmed diagnoses
	 * @return the list of diagnoses for the given visit
	 * 
	 * @since 2.5.0
	 */
	@Authorized({ PrivilegeConstants.GET_DIAGNOSES })
	List<Diagnosis> getDiagnosesByVisit(Visit visit, boolean primaryOnly, boolean confirmedOnly);


	/**
	 * Finds the primary diagnoses for a given encounter
	 *
	 * @deprecated since 2.5.0, use {@link #getDiagnosesByEncounter}
	 * 
	 * @param encounter the encounter whose diagnoses we are to get
	 * @return the list of diagnoses in the given encounter
	 */
	@Deprecated
	List<Diagnosis> getPrimaryDiagnoses(Encounter encounter);

	/**
	 * Gets unique diagnoses since date, sorted in reverse chronological order
	 *
	 * @param patient the patient whose diagnosis we are to get
	 * @param fromDate the date used to filter diagnosis which happened from this date and later
	 * @return the list of diagnoses
	 */
	List<Diagnosis> getUniqueDiagnoses(Patient patient, Date fromDate);


	/**
	 * Gets a diagnosis by id.
	 *
	 * @param diagnosisId - id of the diagnosis to be returned
	 * @return diagnosis matching the given id
	 */
	@Authorized({ PrivilegeConstants.GET_DIAGNOSES })
	Diagnosis getDiagnosis(Integer diagnosisId);

	/**
	 * Revive a diagnosis (pull a Lazarus)
	 *
	 * @param diagnosis diagnosis to unvoid
	 * @throws APIException
	 * <strong>Should</strong> unset voided bit on given diagnosis
	 * @return the unvoided diagnosis
	 */
	@Authorized(PrivilegeConstants.EDIT_DIAGNOSES)
	Diagnosis unvoidDiagnosis(Diagnosis diagnosis) throws APIException;

	/**
	 * Completely remove a diagnosis from the database. This should typically not be called
	 * because we don't want to ever lose data. The data really <i>should</i> be voided and then it
	 * is not seen in interface any longer (see #voidDiagnosis(Diagnosis) for that one) If other things link to
	 * this diagnosis, an error will be thrown.
	 *
	 * @param diagnosis diagnosis to remove from the database
	 * @throws APIException
	 * @see #purgeDiagnosis(Diagnosis) 
	 * <strong>Should</strong> delete the given diagnosis from th e database
	 */
	@Authorized(PrivilegeConstants.DELETE_DIAGNOSES)
	void purgeDiagnosis(Diagnosis diagnosis) throws APIException;

	/**
	 * Fetches all diagnosis attribute types including retired ones.
	 *
	 * @return all {@link DiagnosisAttributeType}s
	 * @since 2.5.0
	 * <strong>Should</strong> return all diagnosis attribute types including retired ones.
	 */
	@Authorized(PrivilegeConstants.GET_DIAGNOSES_ATTRIBUTE_TYPES)
	List<DiagnosisAttributeType> getAllDiagnosisAttributeTypes() throws APIException;

	/**
	 * Fetches a given diagnosis attribute type using the provided id
	 *
	 * @param id the id of the diagnosis attribute type to fetch
	 * @return the {@link DiagnosisAttributeType} with the given id
	 * @since 2.5.0
	 * <strong>Should</strong> return the diagnosis attribute type with the given id
	 * <strong>Should</strong> return null if no diagnosis attribute type exists with the given id
	 */
	@Authorized(PrivilegeConstants.GET_DIAGNOSES_ATTRIBUTE_TYPES)
	DiagnosisAttributeType getDiagnosisAttributeTypeById(Integer id) throws APIException;

	/**
	 * Fetches a given diagnosis attribute type using the provided uuid
	 * 
	 * @param uuid the uuid of the diagnosis attribute type to fetch
	 * @return the {@link DiagnosisAttributeType} with the given uuid
	 * @since 2.5.0
	 * <strong>Should</strong> return the diagnosis attribute type with the given uuid
	 * <strong>Should</strong> return null if no diagnosis attribute type exists with the given uuid
	 */
	@Authorized(PrivilegeConstants.GET_DIAGNOSES_ATTRIBUTE_TYPES)
	DiagnosisAttributeType getDiagnosisAttributeTypeByUuid(String uuid) throws APIException;

	/**
	 * Creates or updates the given diagnosis attribute type in the database
	 *
	 * @param diagnosisAttributeType the diagnosis attribute type to save or update
	 * @return the DiagnosisAttributeType created/saved
	 * @since 2.5.0
	 * <strong>Should</strong> create a new diagnosis attribute type
	 * <strong>Should</strong> edit an existing diagnosis attribute type
	 */
	@Authorized(PrivilegeConstants.EDIT_DIAGNOSES)
	DiagnosisAttributeType saveDiagnosisAttributeType(DiagnosisAttributeType diagnosisAttributeType) throws APIException;

	/**
	 * Retires the given diagnosis attribute type in the database
	 *
	 * @param diagnosisAttributeType the diagnosis attribute type to retire
	 * @param reason the reason why the diagnosis attribute type is being retired
	 * @return the diagnosisAttributeType retired
	 * @since 2.5.0
	 * <strong>Should</strong> retire a diagnosis attribute type
	 */
	@Authorized(PrivilegeConstants.EDIT_DIAGNOSES)
	DiagnosisAttributeType retireDiagnosisAttributeType(DiagnosisAttributeType diagnosisAttributeType, String reason) throws APIException;

	/**
	 * Restores a diagnosis attribute type that was previously retired
	 *
	 * @param diagnosisAttributeType the diagnosis attribute type to unretire.
	 * @return the DiagnosisAttributeType unretired
	 * @since 2.5.0
	 * <strong>Should</strong> unretire a retired diagnosis attribute type
	 */
	@Authorized(PrivilegeConstants.EDIT_DIAGNOSES)
	DiagnosisAttributeType unretireDiagnosisAttributeType(DiagnosisAttributeType diagnosisAttributeType) throws APIException;

	/**
	 * Completely removes a diagnosis attribute type from the database
	 *
	 * @param diagnosisAttributeType the diagnosis attribute type to purge
	 * @since 2.5.0
	 * <strong>Should</strong> completely remove a diagnosis attribute type
	 */
	@Authorized(PrivilegeConstants.DELETE_DIAGNOSES)
	void purgeDiagnosisAttributeType(DiagnosisAttributeType diagnosisAttributeType) throws APIException;

	/**
	 * Fetches a given diagnosis attribute using the provided uuid
	 *
	 * @param uuid the uuid of the diagnosis attribute to fetch
	 * @return the {@link DiagnosisAttribute} with the given uuid
	 * @since 2.5.0
	 * <strong>Should</strong> get the diagnosis attribute with the given uuid
	 * <strong>Should</strong> return null if no diagnosis attribute has the given uuid
	 */
	@Authorized(PrivilegeConstants.GET_DIAGNOSES)
	DiagnosisAttribute getDiagnosisAttributeByUuid(String uuid) throws APIException;
}

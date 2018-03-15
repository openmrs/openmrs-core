/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import org.openmrs.CodedOrFreeText;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.DiagnosisService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DiagnosisDAO;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Transactional
public class DiagnosisServiceImpl extends BaseOpenmrsService implements DiagnosisService {
	
	private DiagnosisDAO diagnosisDAO;

	/**
	 * Saves a diagnosis
	 *
	 * @param diagnosis - the diagnosis to be saved
	 * @return the diagnosis
	 */
	@Override
	public Diagnosis save(Diagnosis diagnosis) {
		diagnosisDAO.saveDiagnosis(diagnosis);
		return diagnosis;
	}

	/**
	 * Voids a diagnosis
	 *
	 * @param diagnosis  - the diagnosis to be voided
	 * @param voidReason - the reason for voiding the diagnosis
	 * @return the diagnosis that was voided
	 */
	@Override
	public Diagnosis voidDiagnosis(Diagnosis diagnosis, String voidReason) {
		return Context.getDiagnosisService().save(diagnosis);
	}

	/**
	 * Gets a diagnosis based on the uuid
	 *
	 * @param uuid - uuid of the diagnosis to be returned
	 * @return diagnosis matching the given uuid
	 */
	@Override
	public Diagnosis getDiagnosisByUuid(String uuid) {
		return diagnosisDAO.getDiagnosisByUuid(uuid);
	}

	/**
	 * Gets diagnoses since date, sorted in reverse chronological order
	 *
	 * @param patient  the patient whose diagnosis we are to get
	 * @param fromDate the date used to filter diagnosis which happened from this date and later
	 * @return the list of diagnoses for the given patient and starting from the given date
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Diagnosis> getDiagnoses(Patient patient, Date fromDate) {
		return diagnosisDAO.getActiveDiagnoses(patient, fromDate);
	}

	/**
	 * Finds the primary diagnoses for a given encounter
	 * The diagnosis order is identified using the integer rank value. The diagnosis rank is thus:
	 * 1 - PRIMARY (Primary diagnosis)
	 * 2 - SECONDARY (Secondary diagnosis)
	 *
	 * @param encounter the encounter whose diagnoses we are to get
	 * @return the list of diagnoses in the given encounter whose rank is 1 (Primary diagnosis)
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Diagnosis> getPrimaryDiagnoses(Encounter encounter) {
		return diagnosisDAO.getPrimaryDiagnoses(encounter);
	}

	/**
	 * Gets unique diagnoses since date, sorted in reverse chronological order
	 *
	 * @param patient  the patient whose diagnosis we are to get
	 * @param fromDate the date used to filter diagnosis which happened from this date and later
	 * @return the list of diagnoses
	 */
	@Override
	@Transactional(readOnly = true)
	public List<Diagnosis> getUniqueDiagnoses(Patient patient, Date fromDate) {
		List<Diagnosis> diagnoses = getDiagnoses(patient, fromDate);
		Set<CodedOrFreeText> answers = new HashSet<CodedOrFreeText>();
		Iterator<Diagnosis> iterator = diagnoses.iterator();
		while(iterator.hasNext()) {
			Diagnosis diagnosis = iterator.next();

			if (!answers.add(diagnosis.getDiagnosis())) {
				iterator.remove();
			}
		}
		return diagnoses;
	}

	/**
	 * Gets a diagnosis by id.
	 *
	 * @param diagnosisId - id of the diagnosis to be returned
	 * @return diagnosis matching the given id
	 */
	@Override
	@Transactional(readOnly = true)
	public Diagnosis getDiagnosis(Integer diagnosisId) {
		return diagnosisDAO.getDiagnosisById(diagnosisId);
	}

	/**
	 * Revive a diagnosis (pull a Lazarus)
	 *
	 * @param diagnosis diagnosis to unvoid
	 * @return the unvoided diagnosis
	 * @throws APIException
	 * @should unset voided bit on given diagnosis
	 */
	@Override
	public Diagnosis unvoidDiagnosis(Diagnosis diagnosis) {
		return Context.getDiagnosisService().save(diagnosis);
	}

	/**
	 * Completely remove a diagnosis from the database. This should typically not be called
	 * because we don't want to ever lose data. The data really <i>should</i> be voided and then it
	 * is not seen in interface any longer (see #voidDiagnosis(Diagnosis) for that one) If other things link to
	 * this diagnosis, an error will be thrown.
	 *
	 * @param diagnosis diagnosis to remove from the database
	 * @throws APIException
	 * @should delete the given diagnosis from th e database
	 * @see #purgeDiagnosis(Diagnosis)
	 */
	@Override
	public void purgeDiagnosis(Diagnosis diagnosis) {
		diagnosisDAO.deleteDiagnosis(diagnosis);
	}

	/**
	 * Gets the diagnosis data access object
	 * 
	 * @return the diagnosis data access object
	 */
	public DiagnosisDAO getDiagnosisDAO() {
		return diagnosisDAO;
	}

	/**
	 * Sets the diagnosis data access object
	 * 
	 * @param diagnosisDAO
	 */
	public void setDiagnosisDAO(DiagnosisDAO diagnosisDAO) {
		this.diagnosisDAO = diagnosisDAO;
	}
}

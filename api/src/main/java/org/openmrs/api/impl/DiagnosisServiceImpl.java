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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.DiagnosisService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DiagnosisDAO;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;
/**
 * API functionality for Diagnoses.
 *
 * @since 2.2
 */
public class DiagnosisServiceImpl extends BaseOpenmrsService implements DiagnosisService {

	private DiagnosisDAO diagnosisDAO;

	public DiagnosisDAO getDiagnosisDAO() {
		return diagnosisDAO;
	}

	public void setDiagnosisDAO(DiagnosisDAO dao) {
		this.diagnosisDAO = dao;
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#save(Diagnosis)
	 */
	public Diagnosis save(Diagnosis diagnosis) {
		return diagnosisDAO.saveDiagnosis(diagnosis);
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#voidDiagnosis(Diagnosis, String)
	 */
	public Diagnosis voidDiagnosis(Diagnosis diagnosis, String voidReason) {
		return Context.getDiagnosisService().save(diagnosis);
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#getDiagnosisByUuid(String)
	 */
	public Diagnosis getDiagnosisByUuid(String uuid) {
		return diagnosisDAO.getDiagnosisByUuid(uuid);
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#getDiagnoses(Patient, Date)
	 */
	public List<Diagnosis> getDiagnoses(Patient patient, Date fromDate) {

		EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder().setPatient(patient)
			.setFromDate(fromDate).createEncounterSearchCriteria();
		List<Encounter> encounters = Context.getEncounterService().getEncounters(encounterSearchCriteria);

		List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();

		for (Encounter encounter : encounters) {
			diagnoses.addAll(encounter.getDiagnoses());
		}
		return diagnoses;
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#getPrimaryDiagnoses(Encounter)
	 */
	public List<Diagnosis> getPrimaryDiagnoses(Encounter encounter) {
		List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
		if (encounter != null && !encounter.getVoided()) {

			for (Diagnosis diagnosis : encounter.getDiagnoses()) {
				// check if the diagnosis is given the highest importance. 
				if (diagnosis.getRank() == 1) {
					diagnoses.add(diagnosis);
				}
			}
		}
		return diagnoses;
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#hasDiagnosis(Encounter, Diagnosis)
	 */
	public boolean hasDiagnosis(Encounter encounter, Diagnosis diagnosis) {

		for (Diagnosis diagnosisFound : encounter.getDiagnoses()) {
			if (diagnosis.equals(diagnosisFound) && !diagnosisFound.getVoided()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#getUniqueDiagnoses(Patient, Date)
	 */
	public List<Diagnosis> getUniqueDiagnoses(Patient patient, Date fromDate) {

		Set<Diagnosis> uniqueDiagnoses = new HashSet<Diagnosis>();
		uniqueDiagnoses.addAll(getDiagnoses(patient, fromDate));
		return new ArrayList(uniqueDiagnoses);
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#unvoidDiagnosis(Diagnosis)
	 */
	public Diagnosis unvoidDiagnosis(Diagnosis diagnosis) throws APIException {
		return Context.getDiagnosisService().save(diagnosis);
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#purgeDiagnosis(Diagnosis)
	 */
	public void purgeDiagnosis(Diagnosis diagnosis) throws APIException {
		diagnosisDAO.deleteDiagnosis(diagnosis);
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#getDiagnosis(Integer)
	 */
	public Diagnosis getDiagnosis(Integer diagnosisId) {
		return diagnosisDAO.getDiagnosisById(diagnosisId);
	}
}

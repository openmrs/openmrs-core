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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Condition;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.ConditionService;
import org.openmrs.api.DiagnosisService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Unit tests for methods that are specific to the {@link DiagnosisServiceImpl}. General tests that
 * would span implementations should go on the {@link org.openmrs.api.DiagnosisService}.
 */
public class DiagnosisServiceImplTest extends BaseContextSensitiveTest {

	protected static final String DIAGNOSIS_XML = "org/openmrs/api/include/DiagnosisServiceImplTest-SetupDiagnosis.xml";
	
	private DiagnosisService diagnosisService;
	private PatientService patientService;
	private EncounterService encounterService;
	private ConditionService conditionService;

	@BeforeEach
	public void setUp (){
		if(diagnosisService == null){
			diagnosisService = Context.getDiagnosisService();
		}
		if(patientService == null){
			patientService = Context.getPatientService();
		}
		if(conditionService == null){
			conditionService = Context.getConditionService();
		}
		if(encounterService == null){
			encounterService = Context.getEncounterService();
		}
		executeDataSet(DIAGNOSIS_XML);
	}

	/**
	 * @see DiagnosisService#save(Diagnosis) 
	 */
	@Test
	public void saveDiagnosis_shouldSaveNewDiagnosis(){
		String uuid = "a303bbfb-w5w4-25d1-9f11-4f33f99d456r";
		Condition condition = conditionService.getConditionByUuid("2cc6880e-2c46-15e4-9038-a6c5e4d22fb7");
		Encounter encounter = encounterService.getEncounterByUuid("y403fafb-e5e4-42d0-9d11-4f52e89d123r");
		Patient patient = patientService.getPatient(2);
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setUuid(uuid);
		diagnosis.setEncounter(encounter);
		diagnosis.setCondition(condition);
		diagnosis.setCertainty(ConditionVerificationStatus.CONFIRMED);
		diagnosis.setPatient(patient);
		diagnosis.setRank(2);
		
		final String NAMESPACE = "namespace";
		final String FORMFIELD_PATH = "formFieldPath";
		diagnosis.setFormField(NAMESPACE, FORMFIELD_PATH);
		
		diagnosisService.save(diagnosis);
		
		Diagnosis savedDiagnosis = diagnosisService.getDiagnosisByUuid(uuid);
		
		assertEquals(uuid, savedDiagnosis.getUuid());
		assertEquals(condition, savedDiagnosis.getCondition());
		assertEquals(encounter, savedDiagnosis.getEncounter());
		assertEquals(patient, savedDiagnosis.getPatient());
		assertEquals(ConditionVerificationStatus.CONFIRMED, savedDiagnosis.getCertainty());
		assertEquals(new Integer(2), savedDiagnosis.getRank());
		assertEquals(NAMESPACE + "^" + FORMFIELD_PATH, savedDiagnosis.getFormNamespaceAndPath());
	}

	/**
	 * @see DiagnosisService#getDiagnosisByUuid(String) (String)
	 */
	@Test
	public void getDiagnosisByUuid_shouldFindDiagnosisGivenValidUuid() {
		String uuid="68802cce-6880-17e4-6880-a68804d22fb7";
		Diagnosis diagnosis = diagnosisService.getDiagnosisByUuid(uuid);
		assertEquals(uuid, diagnosis.getUuid());
	}

	/**
	 * @see DiagnosisService#getDiagnosis(Integer)
	 */
	@Test
	public void getDiagnosisById_shouldFindDiagnosifGivenId() {
		Integer diagnosisId = 1;
		Diagnosis diagnosis = diagnosisService.getDiagnosis(diagnosisId);
		assertEquals("68802cce-6880-17e4-6880-a68804d22fb7", diagnosis.getUuid());
		assertEquals(ConditionVerificationStatus.CONFIRMED, diagnosis.getCertainty());
		assertEquals(diagnosisId, diagnosis.getDiagnosisId());
	}

	/**
	 * @see DiagnosisService#getPrimaryDiagnoses(Encounter) 
	 */
	@Test
	public void getPrimaryDiagnoses_shouldGetPrimaryDiagnoses(){
		Encounter encounter = encounterService.getEncounterByUuid("y403fafb-e5e4-42d0-9d11-4f52e89d123r");
		List<Diagnosis> diagnoses = diagnosisService.getPrimaryDiagnoses(encounter);
		assertEquals(1, diagnoses.size());
		assertEquals("68802cce-6880-17e4-6880-a68804d22fb7", diagnoses.get(0).getUuid());
		assertEquals(ConditionVerificationStatus.CONFIRMED, diagnoses.get(0).getCertainty());
		assertEquals(new Integer(1), diagnoses.get(0).getDiagnosisId());
	}

	/**
	 * @see DiagnosisService#getDiagnoses(Patient, Date) 
	 */
	@Test
	public void getDiagnoses_shouldGetDiagnosesOfPatientWithDate(){
		Calendar calendar = new GregorianCalendar(2015, 12, 1, 0, 0, 0);
		Patient patient = patientService.getPatient(2);
		List<Diagnosis> diagnoses = diagnosisService.getDiagnoses(patient, calendar.getTime());
		assertEquals(2, diagnoses.size());
		assertEquals("68802cce-6880-17e4-6880-a68804d22fb7", diagnoses.get(0).getUuid());
		assertEquals("688804ce-6880-8804-6880-a68804d88047", diagnoses.get(1).getUuid());
	}

	/**
	 * @see DiagnosisService#getDiagnoses(Patient, Date)
	 */
	@Test
	public void getDiagnoses_shouldGetDiagnosesOfPatientWithDifferentDate(){
		Calendar calendar = new GregorianCalendar(2016, 12, 1, 0, 0, 0);
		Patient patient = patientService.getPatient(2);
		List<Diagnosis> diagnoses = diagnosisService.getDiagnoses(patient, calendar.getTime());
		assertEquals(1, diagnoses.size());
		assertEquals("68802cce-6880-17e4-6880-a68804d22fb7", diagnoses.get(0).getUuid());
	}

	/**
	 * @see DiagnosisService#getDiagnoses(Patient, Date)
	 */
	@Test
	public void getDiagnoses_shouldGetDiagnosesOfPatientWithoutDate(){
		Patient patient = patientService.getPatient(2);
		List<Diagnosis> diagnoses = diagnosisService.getDiagnoses(patient, null);
		assertEquals(3, diagnoses.size());
		assertEquals("68802cce-6880-17e4-6880-a68804d22fb7", diagnoses.get(0).getUuid());
		assertEquals("688804ce-6880-8804-6880-a68804d88047", diagnoses.get(1).getUuid());
		assertEquals("88042cce-8804-17e4-8804-a68804d22fb7", diagnoses.get(2).getUuid());
	}

	/**
	 * @see DiagnosisService#getUniqueDiagnoses(Patient, Date) 
	 */
	@Test
	public void getUniqueDiagnoses_shouldGetUniqueDiagnosesOfPatient(){
		Patient patient = patientService.getPatient(2);
		List<Diagnosis> diagnoses = diagnosisService.getUniqueDiagnoses(patient, new Date(0));
		
		assertEquals("68802cce-6880-17e4-6880-a68804d22fb7", diagnoses.get(0).getUuid());
		assertEquals(ConditionVerificationStatus.CONFIRMED, diagnoses.get(0).getCertainty());
		assertEquals(new Integer(1), diagnoses.get(0).getDiagnosisId());
		assertEquals(new Integer(2), diagnoses.get(0).getPatient().getPatientId());
		assertEquals(1, diagnoses.size());
	}

	/**
	 * @see DiagnosisService#voidDiagnosis(Diagnosis, String)
	 */
	@Test
	public void voidDiagnosis_shouldVoidDiagnosisSuccessfully(){
		String voidReason = "void reason";
		String uuid = "688804ce-6880-8804-6880-a68804d88047";
		Diagnosis nonVoidedDiagnosis = diagnosisService.getDiagnosisByUuid(uuid);
		assertFalse(nonVoidedDiagnosis.getVoided());
		assertNull(nonVoidedDiagnosis.getVoidedBy());
		assertNull(nonVoidedDiagnosis.getVoidReason());
		diagnosisService.voidDiagnosis(nonVoidedDiagnosis, voidReason);
		Diagnosis voidedDiagnosis = diagnosisService.getDiagnosisByUuid(uuid);
		assertTrue(voidedDiagnosis.getVoided());
		assertNotNull(voidedDiagnosis.getVoidedBy());
		assertNotNull(voidedDiagnosis.getDateVoided());
		assertEquals(voidReason, voidedDiagnosis.getVoidReason());
		assertEquals(Context.getAuthenticatedUser(), voidedDiagnosis.getVoidedBy());
	}

	/**
	 * @see DiagnosisService#unvoidDiagnosis(Diagnosis)
	 */
	@Test
	public void unvoidDiagnosis_shouldUnvoidDiagnosisSuccessfully(){
		String uuid = "77009cce-8804-17e4-8804-a68804d22fb7";
		Diagnosis voidedDiagnosis = diagnosisService.getDiagnosisByUuid(uuid);
		assertTrue(voidedDiagnosis.getVoided());
		assertNotNull(voidedDiagnosis.getVoidReason());
		assertNotNull(voidedDiagnosis.getDateVoided());
		assertNotNull(voidedDiagnosis.getVoidedBy());
		
		diagnosisService.unvoidDiagnosis(voidedDiagnosis);
		
		Diagnosis unVoidedDiagnosis= diagnosisService.getDiagnosisByUuid(uuid);
		assertEquals(ConditionVerificationStatus.PROVISIONAL, unVoidedDiagnosis.getCertainty());
		assertEquals(uuid, unVoidedDiagnosis.getUuid());
		assertFalse(unVoidedDiagnosis.getVoided());
		assertNull(unVoidedDiagnosis.getVoidReason());
		assertNull(unVoidedDiagnosis.getDateVoided());
		assertNull(unVoidedDiagnosis.getVoidedBy());
	}

	/**
	 * @see DiagnosisService#purgeDiagnosis(Diagnosis) 
	 */
	@Test
	public void purgeDiagnosis_shouldPurgeDiagnosis() {
		String uuid = "77009cce-8804-17e4-8804-a68804d22fb7";
		Diagnosis diagnosis = diagnosisService.getDiagnosisByUuid(uuid);
		assertNotNull(diagnosis);
		diagnosisService.purgeDiagnosis(diagnosis);
		Diagnosis purgedDiagnosis = diagnosisService.getDiagnosisByUuid(uuid);
		assertNull(purgedDiagnosis);
	}
	
}

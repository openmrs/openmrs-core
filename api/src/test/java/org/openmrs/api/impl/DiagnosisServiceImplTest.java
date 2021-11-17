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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
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
import org.openmrs.DiagnosisAttribute;
import org.openmrs.DiagnosisAttributeType;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.Patient;
import org.openmrs.api.ConditionService;
import org.openmrs.api.DiagnosisService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.FreeTextDatatype;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * Unit tests for methods that are specific to the {@link DiagnosisServiceImpl}. General tests that
 * would span implementations should go on the {@link org.openmrs.api.DiagnosisService}.
 */
public class DiagnosisServiceImplTest extends BaseContextSensitiveTest {
	protected static final String DIAGNOSIS_XML = "org/openmrs/api/include/DiagnosisServiceImplTest-SetupDiagnosis.xml";
	protected static final String DIAGNOSIS_ATTRIBUTES_XML = "org/openmrs/api/include/DiagnosisServiceImplTest-DiagnosisAttributes.xml";
	private DiagnosisService diagnosisService;
	private VisitService visitService;
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
		if (visitService == null) {
			visitService = Context.getVisitService();
		}
		executeDataSet(DIAGNOSIS_XML);
		executeDataSet(DIAGNOSIS_ATTRIBUTES_XML);
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
	 * @see DiagnosisService#getDiagnosesByEncounter(Encounter, boolean, boolean)
	 */
	@Test
	public void getDiagnosesByEncounter_shouldGetDiagnosesForEncounter() {
		Encounter encounter = encounterService.getEncounter(5);
		List<Diagnosis> diagnoses = diagnosisService.getDiagnosesByEncounter(encounter, false, false);

		assertEquals(2, diagnoses.size());
		assertEquals("88042cce-8804-17e4-8804-a68804d22fb7", diagnoses.get(0).getUuid());
		assertEquals(ConditionVerificationStatus.CONFIRMED, diagnoses.get(0).getCertainty());
		assertEquals("77009cce-8804-17e4-8804-a68804d22fb7", diagnoses.get(1).getUuid());
		assertEquals(ConditionVerificationStatus.PROVISIONAL, diagnoses.get(1).getCertainty());
	}

	/**
	 * @see DiagnosisService#getDiagnosesByEncounter(Encounter, boolean, boolean)
	 */
	@Test
	public void getDiagnosesByEncounter_shouldGetPrimaryDiagnosesForEncounter() {
		Encounter encounter = encounterService.getEncounter(6);
		List<Diagnosis> diagnoses = diagnosisService.getDiagnosesByEncounter(encounter, true, false);

		assertEquals(1, diagnoses.size());
		assertEquals("68802cce-6880-17e4-6880-a68804d22fb7", diagnoses.get(0).getUuid());
		assertEquals(ConditionVerificationStatus.CONFIRMED, diagnoses.get(0).getCertainty());
		assertEquals(1, diagnoses.get(0).getRank());
	}

	/**
	 * @see DiagnosisService#getDiagnosesByEncounter(Encounter, boolean, boolean)
	 */
	@Test
	public void getDiagnosesByEncounter_shouldGetConfirmedDiagnosesForEncounter() {
		Encounter encounter = encounterService.getEncounter(5);
		List<Diagnosis> diagnoses = diagnosisService.getDiagnosesByEncounter(encounter, false, true);

		assertEquals(1, diagnoses.size());
		assertEquals("88042cce-8804-17e4-8804-a68804d22fb7", diagnoses.get(0).getUuid());
		assertEquals(ConditionVerificationStatus.CONFIRMED, diagnoses.get(0).getCertainty());
	}

	/**
	 * @see DiagnosisService#getDiagnosesByVisit(Visit, boolean, boolean)
	 */
	@Test
	public void getDiagnosesByVisit_shouldGetDiagnosesForEncounter() {
		Visit visit = visitService.getVisit(7);
		List<Diagnosis> diagnoses = diagnosisService.getDiagnosesByVisit(visit, false, false);

		assertEquals(2, diagnoses.size());
		assertEquals("88042cce-8804-17e4-8804-a68804d22fb7", diagnoses.get(0).getUuid());
		assertEquals(ConditionVerificationStatus.CONFIRMED, diagnoses.get(0).getCertainty());
		assertEquals("77009cce-8804-17e4-8804-a68804d22fb7", diagnoses.get(1).getUuid());
		assertEquals(ConditionVerificationStatus.PROVISIONAL, diagnoses.get(1).getCertainty());

	}

	/**
	 * @see DiagnosisService#getDiagnosesByVisit(Visit, boolean, boolean)
	 */
	@Test
	public void getDiagnosesByVisit_shouldGetPrimaryDiagnosesForEncounter() {
		Visit visit = visitService.getVisit(8);
		List<Diagnosis> diagnoses = diagnosisService.getDiagnosesByVisit(visit, true, false);

		assertEquals(1, diagnoses.size());
		assertEquals("68802cce-6880-17e4-6880-a68804d22fb7", diagnoses.get(0).getUuid());
		assertEquals(ConditionVerificationStatus.CONFIRMED, diagnoses.get(0).getCertainty());
		assertEquals(1, diagnoses.get(0).getRank());
	}

	/**
	 * @see DiagnosisService#getDiagnosesByVisit(Visit, boolean, boolean)
	 */
	@Test
	public void getDiagnosesByVisit_shouldGetConfirmedDiagnosesForEncounter() {
		Visit visit = visitService.getVisit(7);
		List<Diagnosis> diagnoses = diagnosisService.getDiagnosesByVisit(visit, false, true);

		assertEquals(1, diagnoses.size());
		assertEquals("88042cce-8804-17e4-8804-a68804d22fb7", diagnoses.get(0).getUuid());
		assertEquals(ConditionVerificationStatus.CONFIRMED, diagnoses.get(0).getCertainty());
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

	/**
	 * @see org.openmrs.api.DiagnosisService#getAllDiagnosisAttributeTypes()
	 */
	@Test
	public void getAllDiagnosisAttributeTypes_shouldReturnAllDiagnosisAttributeTypesIncludingRetiredOnes() {
		assertThat(diagnosisService.getAllDiagnosisAttributeTypes(), hasSize(3));
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#getDiagnosisAttributeTypeById(Integer) 
	 */
	@Test
	public void getDiagnosisAttributeTypeById_shouldReturnTheDiagnosisAttributeTypeUsingTheProvidedId() {
		assertEquals("Differential Diagnosis", diagnosisService.getDiagnosisAttributeTypeById(1).getName());
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#getDiagnosisAttributeTypeById(Integer) 
	 */
	@Test
	public void getDiagnosisAttributeType_shouldReturnNullIfNoDiagnosisAttributeTypeExistsWithTheProvidedId() {
		assertNull(diagnosisService.getDiagnosisAttributeTypeById(2021));
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#getDiagnosisAttributeTypeByUuid(String)
	 */
	@Test
	public void getDiagnosisAttributeTypeByUuid_shouldReturnTheDiagnosisAttributeTypeWithTheProvidedUuid() {
		assertEquals("Pattern Recognition", diagnosisService.getDiagnosisAttributeTypeByUuid("96fc46dc-edd3-4f27-83b6-4a9f7b1f0a48").getName());
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#getDiagnosisAttributeTypeByUuid(String)
	 */
	@Test
	public void getDiagnosisAttributeTypeByUuid_shouldReturnNullIfNoDiagnosisAttributeTypeExistsWithTheProvidedUuid() {
		assertNull(diagnosisService.getDiagnosisAttributeTypeByUuid("077e8b7a-caa7-4882-bd99-8296829a661c"));
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#purgeDiagnosisAttributeType(DiagnosisAttributeType)
	 */
	@Test
	public void purgeDiagnosisAttributeType_shouldCompletelyRemoveTheDiagnosisAttributeType() {
		final int ORIGINAL_COUNT = diagnosisService.getAllDiagnosisAttributeTypes().size();
		assertNotNull(diagnosisService.getDiagnosisAttributeTypeById(2));
		diagnosisService.purgeDiagnosisAttributeType(diagnosisService.getDiagnosisAttributeTypeById(2));
		assertNull(diagnosisService.getDiagnosisAttributeTypeById(2));
		assertEquals(ORIGINAL_COUNT - 1, diagnosisService.getAllDiagnosisAttributeTypes().size());
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#retireDiagnosisAttributeType(DiagnosisAttributeType, String)
	 */
	@Test
	public void retireDiagnosisAttributeType_shouldRetireADiagnosisAttributeType() {
		DiagnosisAttributeType diagnosisAttributeType = diagnosisService.getDiagnosisAttributeTypeById(1);
		assertFalse(diagnosisAttributeType.getRetired());
		assertNull(diagnosisAttributeType.getRetiredBy());
		assertNull(diagnosisAttributeType.getRetireReason());
		assertNull(diagnosisAttributeType.getDateRetired());
		diagnosisService.retireDiagnosisAttributeType(diagnosisAttributeType, "Test Retire");
		diagnosisAttributeType = diagnosisService.getDiagnosisAttributeTypeById(1);
		assertTrue(diagnosisAttributeType.getRetired());
		assertNotNull(diagnosisAttributeType.getRetiredBy());
		assertEquals("Test Retire", diagnosisAttributeType.getRetireReason());
		assertNotNull(diagnosisAttributeType.getDateRetired());
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#unretireDiagnosisAttributeType(DiagnosisAttributeType)
	 */
	@Test
	public void unretireDiagnosisAttributeType_shouldUnretireARetiredDiagnosisAttributeType() {
		DiagnosisAttributeType diagnosisAttributeType = diagnosisService.getDiagnosisAttributeTypeById(2);
		assertTrue(diagnosisAttributeType.getRetired());
		assertNotNull(diagnosisAttributeType.getRetiredBy());
		assertNotNull(diagnosisAttributeType.getDateRetired());
		assertNotNull(diagnosisAttributeType.getRetireReason());
		diagnosisService.unretireDiagnosisAttributeType(diagnosisAttributeType);
		assertFalse(diagnosisAttributeType.getRetired());
		assertNull(diagnosisAttributeType.getRetiredBy());
		assertNull(diagnosisAttributeType.getDateRetired());
		assertNull(diagnosisAttributeType.getRetireReason());
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#saveDiagnosisAttributeType(DiagnosisAttributeType)
	 */
	@Test
	public void saveDiagnosisAttributeType_shouldSaveTheProvidedDiagnosisAttributeTypeToTheDatabase() {
		final Diagnosis diagnosis = Context.getDiagnosisService().getDiagnosis(1);
		final int ORIGINAL_COUNT = diagnosisService.getAllDiagnosisAttributeTypes().size();
		DiagnosisAttributeType diagnosisAttributeType = new DiagnosisAttributeType();
		diagnosisAttributeType.setName("Clinical Decision Support System");
		diagnosisAttributeType.setMinOccurs(1);
		diagnosisAttributeType.setMaxOccurs(5);
		diagnosisAttributeType.setDatatypeClassname(FreeTextDatatype.class.getName());
		diagnosisAttributeType.setCreator(diagnosis.getCreator());
		diagnosisAttributeType.setDateCreated(diagnosis.getDateCreated());
		diagnosisAttributeType.setRetired(false);
		diagnosisAttributeType.setUuid("353af72e-bb6e-4ed9-a1bf-0d8106ac2c15");
		diagnosisService.saveDiagnosisAttributeType(diagnosisAttributeType);
		assertNotNull(diagnosisAttributeType.getDiagnosisAttributeTypeId(), "Newly Saved Diagnosis Attribute Type");
		assertEquals(ORIGINAL_COUNT + 1, diagnosisService.getAllDiagnosisAttributeTypes().size());
		DiagnosisAttributeType savedDiagnosisAttributeType = diagnosisService.getDiagnosisAttributeTypeByUuid("353af72e-bb6e-4ed9-a1bf-0d8106ac2c15");
		assertEquals("Clinical Decision Support System", savedDiagnosisAttributeType.getName());
		assertEquals(1, savedDiagnosisAttributeType.getMinOccurs());
		assertEquals(5, savedDiagnosisAttributeType.getMaxOccurs());
		assertEquals(diagnosis.getCreator(), savedDiagnosisAttributeType.getCreator());
		assertEquals(diagnosis.getDateCreated(), savedDiagnosisAttributeType.getDateCreated());
		assertEquals("353af72e-bb6e-4ed9-a1bf-0d8106ac2c15", savedDiagnosisAttributeType.getUuid());
		assertEquals(false, savedDiagnosisAttributeType.getRetired());
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#saveDiagnosisAttributeType(DiagnosisAttributeType)
	 */
	@Test
	public void saveDiagnosisAttributeType_shouldEditTheExistingDiagnosisAttributeType() {
		DiagnosisAttributeType diagnosisAttributeType = diagnosisService.getDiagnosisAttributeTypeById(3);
		assertEquals("Diagnostic Criteria", diagnosisAttributeType.getName());
		diagnosisAttributeType.setName("Diagnosis Orientation");
		diagnosisService.saveDiagnosisAttributeType(diagnosisAttributeType);
		assertThat(diagnosisService.getDiagnosisAttributeTypeById(diagnosisAttributeType.getId()).getName(), is("Diagnosis Orientation"));
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#getDiagnosisAttributeByUuid(String)
	 */
	@Test
	public void getDiagnosisAttributeByUuid_shouldGetTheDiagnosisAttributeWithTheProvidedUuid() {
		DiagnosisAttribute diagnosisAttribute = diagnosisService.getDiagnosisAttributeByUuid("31f7c3cd-699b-4ed3-af10-563e024cae76");
		assertEquals("Testing Reference", diagnosisAttribute.getValueReference());
		assertEquals(1, diagnosisAttribute.getId());
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#getDiagnosisAttributeByUuid(String)
	 */
	@Test
	public void getDiagnosisAttributeByUuid_shouldReturnNullIfNoDiagnosisAttributeHasTheProvidedUuid() {
		assertNull(diagnosisService.getDiagnosisAttributeByUuid("001612a5-9f2a-4a05-9384-755679f75647"));
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#save(Diagnosis)
	 */
	@Test
	public void saveDiagnosis_shouldSaveTheDiagnosisWithTheProvidedAttributes() {
		final String NAMESPACE = "namespace";
		final String FORMFIELD_PATH = "formFieldPath";
		final DiagnosisAttributeType DIAGNOSIS_ATTRIBUTE_TYPE = Context.getDiagnosisService()
				.getDiagnosisAttributeTypeByUuid("949daf5b-a83e-4b65-b914-502a553243d3");
		DiagnosisAttribute diagnosisAttribute = new DiagnosisAttribute();
		diagnosisAttribute.setAttributeType(DIAGNOSIS_ATTRIBUTE_TYPE);
		diagnosisAttribute.setCreator(Context.getUserService().getUser(1));
		diagnosisAttribute.setVoided(false);
		diagnosisAttribute.setValueReferenceInternal("Diagnosis Attribute Reference");
		final String UUID = "3c6422d8-7bdd-4e20-bd1b-a590f084db08";
		Condition condition = conditionService.getConditionByUuid("2cc6880e-2c46-15e4-9038-a6c5e4d22fb7");
		Encounter encounter = encounterService.getEncounterByUuid("y403fafb-e5e4-42d0-9d11-4f52e89d123r");
		Patient patient = patientService.getPatient(2);
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setUuid(UUID);
		diagnosis.setEncounter(encounter);
		diagnosis.setCondition(condition);
		diagnosis.setCertainty(ConditionVerificationStatus.CONFIRMED);
		diagnosis.setPatient(patient);
		diagnosis.setRank(1);
		diagnosis.setVoided(false);
		diagnosis.setFormField(NAMESPACE, FORMFIELD_PATH);
		diagnosis.addAttribute(diagnosisAttribute);
		diagnosisService.save(diagnosis);
		assertNotNull(diagnosis.getId(), "Successfully Saved Diagnosis");
		Diagnosis savedDiagnosis = diagnosisService.getDiagnosisByUuid(UUID);
		assertEquals(condition, savedDiagnosis.getCondition());
		assertEquals(encounter, savedDiagnosis.getEncounter());
		assertEquals(patient, savedDiagnosis.getPatient());
		assertEquals(ConditionVerificationStatus.CONFIRMED, savedDiagnosis.getCertainty());
		assertEquals(1, savedDiagnosis.getRank());
		assertEquals(false, savedDiagnosis.getVoided());
		assertEquals(NAMESPACE + "^" + FORMFIELD_PATH, savedDiagnosis.getFormNamespaceAndPath());
		assertThat(savedDiagnosis.getAttributes(), hasItem(diagnosisAttribute));
	}

	/**
	 * @see org.openmrs.api.DiagnosisService#save(Diagnosis)
	 */
	@Test
	public void saveDiagnosis_shouldEditTheExistingDiagnosisRemovingTheAssociatedAttributesWhenRequired() {
		final DiagnosisAttribute DIAGNOSIS_ATTRIBUTE = Context.getDiagnosisService()
				.getDiagnosisAttributeByUuid("31f7c3cd-699b-4ed3-af10-563e024cae76");
		Diagnosis diagnosis = diagnosisService.getDiagnosis(1);
		assertThat(diagnosis.getAttributes(), hasItem(DIAGNOSIS_ATTRIBUTE));
		diagnosis.getAttributes().remove(DIAGNOSIS_ATTRIBUTE);
		diagnosisService.save(diagnosis);
		Diagnosis editedDiagnosis = diagnosisService.getDiagnosis(1);
		assertThat(editedDiagnosis.getAttributes(), not(hasItem(DIAGNOSIS_ATTRIBUTE)));
	}
}

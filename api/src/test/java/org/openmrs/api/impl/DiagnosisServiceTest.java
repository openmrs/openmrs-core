
package org.openmrs.api.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.DiagnosisService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Test methods for {@link DiagnosisServiceImpl}
 */
public class DiagnosisServiceTest extends BaseContextSensitiveTest {

	protected static final String DIAGNOSIS_XML = "org/openmrs/api/include/DiagnosisServiceImplTest-SetupDiagnosis.xml";

	private DiagnosisService diagnosisService;

	private EncounterService encounterService;

	private PatientService patientService;

	@Before
	public void setup() {
		if (diagnosisService == null) {
			diagnosisService = Context.getDiagnosisService();
		}
		if (encounterService == null) {
			encounterService = Context.getEncounterService();
		}
		if (patientService == null) {
			patientService = Context.getPatientService();
		}
		executeDataSet(DIAGNOSIS_XML);
	}

	/**
	 * Creates a diganosis object.
	 *
	 * @return diagnosis the diagnosis created
	 */
	private Diagnosis buildDiagnosis() {
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setEncounter(encounterService.getEncounter(1));
		diagnosis.setRank(1);
		diagnosis.setCertainty(ConditionVerificationStatus.PROVISIONAL);
		diagnosis.setUuid("2cd6780e-2a46-11e4-9038-a6c5e4d26fc2");
		diagnosis.setPatient(new Patient(2));
		return diagnosis;
	}

	/**
	 * @see DiagnosisService#save(Diagnosis)
	 */
	@Test
	public void save_shouldSaveDiagnosisWithBasicDetails() {
		Diagnosis diagnosis = buildDiagnosis();
		diagnosisService.save(diagnosis);
		Diagnosis newDiagnosis = diagnosisService.getDiagnosis(diagnosis.getId());
		Integer expectedRank = 1;
		Integer expectedPatientId = 2;

		assertNotNull("The saved diagnosis should have an id", newDiagnosis.getId());
		assertNotNull("We expect a diagnosis object to be returned", newDiagnosis);
		Assert.assertEquals(expectedRank, newDiagnosis.getId());
		Assert.assertEquals(encounterService.getEncounter(1), newDiagnosis.getEncounter());
		Assert.assertEquals(ConditionVerificationStatus.PROVISIONAL, newDiagnosis.getCertainty());
		Assert.assertEquals(expectedPatientId, newDiagnosis.getPatient().getPatientId());
	}

	/**
	 * @see DiagnosisService#save(Diagnosis)
	 */
	@Test
	public void save_shouldUpdateDiagnosisSuccessfully() {
		Diagnosis diagnosis = buildDiagnosis();
		Integer rank = 2;
		Encounter encounter = encounterService.getEncounter(2);
		ConditionVerificationStatus verificationStatus = ConditionVerificationStatus.CONFIRMED;

		diagnosis.setEncounter(encounter);
		diagnosis.setRank(rank);
		diagnosis.setCertainty(verificationStatus);

		diagnosisService.save(diagnosis);

		Diagnosis editedDiagnosis = diagnosisService.getDiagnosis(diagnosis.getId());

		Assert.assertEquals(rank, editedDiagnosis.getRank());
		Assert.assertEquals(encounter, editedDiagnosis.getEncounter());
		Assert.assertEquals(verificationStatus, editedDiagnosis.getCertainty());
	}

	/**
	 * @see DiagnosisService#voidDiagnosis(Diagnosis, String)
	 */
	@Test
	public void voidDiagnosis_shouldVoidDiagnosisSuccessfully() {
		Diagnosis diagnosis = buildDiagnosis();
		diagnosisService.save(diagnosis);
		String voidReason = "Test was contaminated";
		diagnosisService.voidDiagnosis(diagnosis, voidReason);
		Diagnosis voidedDiagnosis = diagnosisService.getDiagnosis(diagnosis.getId());

		assertFalse(voidedDiagnosis.getVoided());
	}

	/**
	 * @see DiagnosisService#getDiagnosisByUuid(String)
	 */
	@Test
	public void getDiagnosisByUuid_shouldGetDiagnosisByUuid() {
		String uuid = "2cc6880e-2c46-15e4-9038-a6c5e4d22fb7";

		Assert.assertEquals(uuid, diagnosisService.getDiagnosisByUuid(uuid).getUuid());
	}

	/**
	 * @see DiagnosisService#getDiagnoses(Patient, Date)
	 */
	@Test
	public void getDiagnoses_shouldGetDiagnoses() throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date fromDate = dateFormat.parse("2014-01-12 00:00:00");
		List<Diagnosis> diagnoses = diagnosisService.getDiagnoses(patientService.getPatient(2), fromDate);

		Assert.assertEquals(3, diagnoses.size());
	}

	/**
	 * @see DiagnosisService#getPrimaryDiagnoses(Encounter)
	 */
	@Test
	public void getPrimaryDiagnoses_shouldGetPrimaryDiagnoses() {
		List<Diagnosis> diagnoses = diagnosisService.getPrimaryDiagnoses(encounterService.getEncounter(2));

		Assert.assertEquals(2, diagnoses.size());
	}

	/**
	 * @see DiagnosisService#hasDiagnosis(Encounter, Diagnosis)
	 */
	@Test
	public void hasDiagnosis_shouldReturnBoolean() {
		Encounter encounter = encounterService.getEncounter(2);
		Diagnosis diagnosis = diagnosisService.getDiagnosis(2);

		Assert.assertTrue(diagnosisService.hasDiagnosis(encounter, diagnosis));
	}

	/**
	 * @see DiagnosisService#getUniqueDiagnoses(Patient, Date)
	 */
	@Test
	public void getUniqueDiagnoses_shouldGetUniqueDiagnoses() throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date fromDate = dateFormat.parse("2015-01-12 00:00:00");
		List<Diagnosis> diagnoses = diagnosisService.getDiagnoses(patientService.getPatient(2), fromDate);

		Assert.assertEquals(3, diagnoses.size());
	}

	/**
	 * @see DiagnosisService#unvoidDiagnosis(Diagnosis)
	 */
	@Test
	public void unvoidDiagnosis_shouldUnvoidDiagnosisSuccessfully() {
		Diagnosis diagnosis = buildDiagnosis();
		diagnosisService.save(diagnosis);
		diagnosisService.unvoidDiagnosis(diagnosis);
		Diagnosis voidedDiagnosis = diagnosisService.getDiagnosis(diagnosis.getId());

		assertFalse(voidedDiagnosis.getVoided());
	}

	/**
	 * @see DiagnosisService#purgeDiagnosis(Diagnosis)
	 */
	@Test
	public void purgeDiagnosis_shouldPurgeDiagnosisSuccessfully() {
		Diagnosis diagnosis = diagnosisService.getDiagnosis(2);
		diagnosisService.purgeDiagnosis(diagnosis);

		assertNull(diagnosisService.getDiagnosis(diagnosis.getId()));
	}
}

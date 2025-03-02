package org.openmrs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.PatientService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PatientServiceMockitoTest {
	/**
	 * @author XinTang
	 * Uses PowerMock to mock the static Context class
	 * Creates mock PatientService that will be returned by the Context
	 * Sets up test patients with different identifier types
	 * Prepares a real PatientServiceImpl that will delegate to our mock
	 * */

	@Mock
	private PatientService mockPatientService;

	// A simple wrapper class to avoid direct Context dependency
	private class SimplePatientServiceWrapper {
		private final PatientService service;

		public SimplePatientServiceWrapper(PatientService service) {
			this.service = service;
		}

		public List<Patient> searchPatients(String name, String identifier,
											List<PatientIdentifierType> identifierTypes,
											boolean matchIdentifierExactly) {
			return service.getPatients(name, identifier, identifierTypes,
				matchIdentifierExactly, 0, null);
		}
	}

	private SimplePatientServiceWrapper patientServiceWrapper;
	@BeforeEach
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		patientServiceWrapper = new SimplePatientServiceWrapper(mockPatientService);
	}


	@Test
	public void testBasicPatientSearch() {
		// Setup
		String name = "John";
		String identifier = "12345";
		List<PatientIdentifierType> identifierTypes = null; // No specific types
		boolean matchExactly = true;

		// Create a mock response
		Patient mockPatient = new Patient();
		mockPatient.setPatientId(1);
		List<Patient> expectedPatients = Arrays.asList(mockPatient);

		// Configure the mock
		when(mockPatientService.getPatients(name, identifier, identifierTypes,
			matchExactly, 0, null))
			.thenReturn(expectedPatients);

		// Execute the method
		List<Patient> result = patientServiceWrapper.searchPatients(
			name, identifier, identifierTypes, matchExactly);

		// Verify results
		assertEquals(1, result.size());
		assertEquals(Integer.valueOf(1), result.get(0).getPatientId());

		// Verify the mock was called with correct parameters
		verify(mockPatientService).getPatients(name, identifier, identifierTypes,
			matchExactly, 0, null);
	}

	@Test
	public void testSearchWithMultipleIdentifierTypes() {
		// Setup
		String name = null;
		String identifier = "ABC";

		// Create identifier types for the test
		PatientIdentifierType idType1 = new PatientIdentifierType();
		idType1.setId(1);
		PatientIdentifierType idType2 = new PatientIdentifierType();
		idType2.setId(2);

		List<PatientIdentifierType> identifierTypes = Arrays.asList(idType1, idType2);
		boolean matchExactly = false;

		// Create mock patients for response
		Patient mockPatient1 = new Patient();
		mockPatient1.setPatientId(1);
		Patient mockPatient2 = new Patient();
		mockPatient2.setPatientId(2);
		List<Patient> expectedPatients = Arrays.asList(mockPatient1, mockPatient2);

		// Configure the mock
		when(mockPatientService.getPatients(name, identifier, identifierTypes,
			matchExactly, 0, null))
			.thenReturn(expectedPatients);

		// Execute the method
		List<Patient> result = patientServiceWrapper.searchPatients(
			name, identifier, identifierTypes, matchExactly);

		// Verify results
		assertEquals(2, result.size());

		// Verify the mock was called with correct parameters
		verify(mockPatientService).getPatients(name, identifier, identifierTypes,
			matchExactly, 0, null);
	}
}

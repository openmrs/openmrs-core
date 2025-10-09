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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.DuplicateIdentifierException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.InsufficientIdentifiersException;
import org.openmrs.api.LocationService;
import org.openmrs.api.MissingRequiredIdentifierException;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientServiceTest;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.UserService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.UserContext;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.test.jupiter.BaseContextMockTest;

/**
 * This class tests org.openmrs.{@link PatientServiceImpl}
 * without using the application context and mocking the identifier validator and Context.
 *
 * If you need an integration test with application context and DB, have a look at @see org.openmrs.api.{@link PatientServiceTest}
 */
public class PatientServiceImplTest extends BaseContextMockTest {

	@Mock
	private PersonService personService;

	@Mock
	private VisitService visitService;

	@Mock
	private EncounterService encounterService;

	@Mock
	private ObsService obsService;

	@Mock
	private ProgramWorkflowService programWorkflowService;

	@Mock
	private OrderService orderService;

	@Mock
	private MessageSourceService messageSourceService;

	@Mock
	private UserService userService;

	@Mock
	private AdministrationService administrationService;

	@Mock
	private ConceptService conceptService;

	@Mock
	private LocationService locationService;

	@Mock
	private PatientDAO patientDaoMock;

	private PatientServiceImpl patientService;

	@BeforeEach
	public void before() {
		patientService = new PatientServiceImpl(
			patientDaoMock,
			personService,
			visitService,
			encounterService,
			obsService,
			programWorkflowService,
			orderService,
			messageSourceService,
			userService,
			administrationService,
			conceptService,
			locationService,
			new java.util.HashMap<>()
		);
		this.contextMockHelper.setPatientService(patientService);
	}

	@Test
	public void checkPatientIdentifiers_shouldThrowMissingRequiredIdentifierGivenRequiredIdentifierTypeMissing()
		throws Exception {
		// given
		final PatientIdentifierType requiredIdentifierType = new PatientIdentifierType(12345);
		requiredIdentifierType.setUuid("some type uuid");
		requiredIdentifierType.setName("NameOfRequiredIdentifierType");
		final PatientIdentifierType patientIdentifierType = new PatientIdentifierType(6789);
		patientIdentifierType.setUuid("another type uuid");
		patientIdentifierType.setName("NameOfPatientIdentifierType");

		final List<PatientIdentifierType> requiredTypes = new ArrayList<>();
		requiredTypes.add(requiredIdentifierType);
		when(patientDaoMock.getPatientIdentifierTypes(any(), any(), any(), any()))
			.thenReturn(requiredTypes);

		final Patient patientWithIdentifiers = new Patient();
		patientWithIdentifiers
			.addIdentifier(new PatientIdentifier("some identifier", patientIdentifierType, mock(Location.class)));

		try {
			// when
			patientService.checkPatientIdentifiers(patientWithIdentifiers);
			fail();
			// then
		}
		catch (MissingRequiredIdentifierException e) {
			assertTrue(e.getMessage().contains("required"));
			assertTrue(e.getMessage().contains("NameOfRequiredIdentifierType"));
		}
		catch (Exception e) {
			fail("Expecting MissingRequiredIdentifierException");
		}

	}

	@Test
	public void checkPatientIdentifiers_shouldNotThrowMissingRequiredIdentifierGivenRequiredIdentifierTypesArePresent()
		throws Exception {
		// given
		final String typeUuid = "equal type uuid";
		final PatientIdentifierType requiredIdentifierType = new PatientIdentifierType(12345);
		requiredIdentifierType.setUuid(typeUuid);
		final PatientIdentifierType patientIdentifierType = new PatientIdentifierType(12345);
		patientIdentifierType.setUuid(typeUuid);

		final List<PatientIdentifierType> requiredTypes = new ArrayList<>();
		requiredTypes.add(requiredIdentifierType);
		when(patientDaoMock.getPatientIdentifierTypes(any(), any(), any(), any()))
			.thenReturn(requiredTypes);

		final Patient patientWithIdentifiers = new Patient();
		patientWithIdentifiers
			.addIdentifier(new PatientIdentifier("some identifier", patientIdentifierType, mock(Location.class)));
		final PatientIdentifierType anotherPatientIdentifier = new PatientIdentifierType(2345);
		anotherPatientIdentifier.setUuid("another type uuid");
		patientWithIdentifiers
			.addIdentifier(new PatientIdentifier("some identifier", anotherPatientIdentifier, mock(Location.class)));

		// when
		patientService.checkPatientIdentifiers(patientWithIdentifiers);

		// then no exception
		assertNotNull(patientWithIdentifiers);
	}

	@Test
	public void checkPatientIdentifiers_shouldThrowDuplicateIdentifierGivenDuplicateIdentifiers() throws Exception {
		// given
		final Integer equalIdentifierTypeId = 12345;
		final String equalIdentifierTypeName = "TypeName";
		final String equalIdentifier = "Identifier1";

		final PatientIdentifierType identifierType = new PatientIdentifierType(equalIdentifierTypeId);
		identifierType.setName(equalIdentifierTypeName);
		final PatientIdentifierType sameIdentifierType = new PatientIdentifierType(equalIdentifierTypeId);
		sameIdentifierType.setName(equalIdentifierTypeName);

		final Patient patientWithIdentifiers = new Patient();
		final PatientIdentifier patientIdentifier = new PatientIdentifier("some identifier", identifierType,
			mock(Location.class));

		patientIdentifier.setIdentifier(equalIdentifier);
		patientWithIdentifiers.addIdentifier(patientIdentifier);
		final PatientIdentifier patientIdentifierSameType = new PatientIdentifier("some identifier", sameIdentifierType,
			mock(Location.class));
		patientIdentifierSameType.setIdentifier(equalIdentifier);
		patientWithIdentifiers.addIdentifier(patientIdentifierSameType);

		DuplicateIdentifierException thrown = assertThrows(DuplicateIdentifierException.class,
			() -> patientService.checkPatientIdentifiers(patientWithIdentifiers));
		assertNotNull(thrown.getPatientIdentifier());
		assertThat(thrown.getMessage(), containsString("Identifier1 id type #: 12345"));
	}

	@Test
	public void checkPatientIdentifiers_shouldThrowInsufficientIdentifiersErrorGivenPatientHasNoActiveIdentifiers()
		throws Exception {
		// given
		Patient patient = new Patient();
		patient.setVoided(false);
		patient.addIdentifier(createVoidedPatientIdentifier());

		// when
		assertThrows(InsufficientIdentifiersException.class, () -> patientService.checkPatientIdentifiers(patient));

		// this patient only has a voided identifier, so saving is not allowed > exception
	}

	@Test
	public void checkPatientIdentifiers_shouldIgnoreAbsenceOfActiveIdentifiersGivenPatientIsVoided() throws Exception {
		// given
		Patient patient = new Patient();
		patient.setVoided(true);
		patient.addIdentifier(createVoidedPatientIdentifier());

		// when
		patientService.checkPatientIdentifiers(patient);

		// no exception
	}

	@Test
	public void getDuplicatePatientsByAttributes_shouldThrowErrorGivenEmptyAttributes() throws Exception {
		assertThrows(APIException.class, () -> patientService.getDuplicatePatientsByAttributes(Collections.emptyList()));
	}

	@Test
	public void getDuplicatePatientsByAttributes_shouldThrowErrorGivenNoAttributes() throws Exception {
		assertThrows(APIException.class, () -> patientService.getDuplicatePatientsByAttributes(null));
	}

	@Test
	public void getDuplicatePatientsByAttributes_shouldCallDaoGivenAttributes() throws Exception {
		when(patientDaoMock.getDuplicatePatientsByAttributes(anyList())).thenReturn(
			Collections.singletonList(mock(Patient.class)));
		final List<Patient> duplicatePatients = patientService
			.getDuplicatePatientsByAttributes(Arrays.asList("some attribute", "another attribute"));
		verify(patientDaoMock, times(1)).getDuplicatePatientsByAttributes(anyList());
		assertEquals(duplicatePatients.size(), 1);
	}

	@Test
	public void getPatientIdentifierTypes_shouldReturnPatientIdentifierTypesFromDao() {

		// given
		final List<PatientIdentifierType> expectedIdentifierTypes = new ArrayList<>();
		expectedIdentifierTypes.add(new PatientIdentifierType(12345));
		when(patientDaoMock.getPatientIdentifierTypes(any(), any(), any(), any()))
			.thenReturn(expectedIdentifierTypes);

		// when
		final List<PatientIdentifierType> actualIdentifierTypes = patientService
			.getPatientIdentifierTypes("a name", "a format", true, false);

		// then
		verify(patientDaoMock, times(1)).getPatientIdentifierTypes("a name", "a format", true, false);
		assertEquals(expectedIdentifierTypes.get(0).getPatientIdentifierTypeId(),
			actualIdentifierTypes.get(0).getPatientIdentifierTypeId());
	}

	@Test
	public void getPatientIdentifierTypes_shouldReturnEmptyListGivenDaoReturnsNull() {

		// given
		when(patientDaoMock.getPatientIdentifierTypes(any(), any(), any(), any()))
			.thenReturn(null);

		// when
		final List<PatientIdentifierType> actualIdentifierTypes = patientService
			.getPatientIdentifierTypes("a name", "a format", true, false);

		// then
		verify(patientDaoMock, times(1)).getPatientIdentifierTypes("a name", "a format", true, false);
		assertEquals(0, actualIdentifierTypes.size());
	}

	@Test
	public void processDeath_shouldThrowAPIExceptionIfPatientIsNull() throws Exception {
		assertThrows(APIException.class, () -> patientService.processDeath(null, new Date(), new Concept(), "unknown"));
	}

	@Test
	public void processDeath_shouldThrowAPIExceptionIfDateDiedIsNull() throws Exception {
		assertThrows(APIException.class, () -> patientService.processDeath(new Patient(), null, new Concept(), "unknown"));
	}

	@Test
	public void processDeath_shouldThrowAPIExceptionIfCauseOfDeathIsNull() throws Exception {
		assertThrows(APIException.class, () -> patientService.processDeath(new Patient(), new Date(), null, "unknown"));
	}

	@Test
	public void processDeath_shouldMapValuesAndSavePatient() throws Exception {
		// given
		final Date dateDied = new Date();
		final Concept causeOfDeath = new Concept(2);

		when(conceptService.getConcept((String)any())).thenReturn(new Concept());
		when(locationService.getDefaultLocation()).thenReturn(new Location());

		UserContext userContext = mock(UserContext.class);
		this.contextMockHelper.setUserContext(userContext);
		when(userContext.hasPrivilege(anyString())).thenReturn(true);

		ArgumentCaptor<Patient> argumentCaptor = ArgumentCaptor.forClass(Patient.class);
		when(patientDaoMock.savePatient(argumentCaptor.capture())).thenReturn(new Patient());

		// when
		final Patient patient = new Patient();
		patient.addIdentifier(new PatientIdentifier("an identifier", new PatientIdentifierType(1234), new Location()));
		patientService.processDeath(patient, dateDied, causeOfDeath, "unknown");

		// then
		final Patient savedPatient = argumentCaptor.getValue();
		assertTrue(savedPatient.getDead());
		assertEquals(dateDied, savedPatient.getDeathDate());
		assertEquals(causeOfDeath, savedPatient.getCauseOfDeath());
	}

	private PatientIdentifier createVoidedPatientIdentifier() {
		PatientIdentifier patientIdentifier = new PatientIdentifier();
		patientIdentifier.setIdentifierType(mock(PatientIdentifierType.class));
		patientIdentifier.setVoided(true);
		patientIdentifier.setVoidedBy(mock(User.class));
		patientIdentifier.setVoidReason("Testing whether voided identifiers are ignored");
		return patientIdentifier;
	}
}

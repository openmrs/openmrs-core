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

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.User;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.api.impl.PatientServiceImpl;
import org.openmrs.validator.PatientIdentifierValidator;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class tests methods in the PatientService class
 * without using the application context.
 * If you need an integration test with application context and DB, have a look at @see org.openmrs.api.{@link PatientServiceTest}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(PatientIdentifierValidator.class)
public class PatientServiceImplUnitTest {

    private PatientServiceImpl patientService;
    private PatientDAO patientDaoMock;

    @Before
    public void before() {
        patientService = new PatientServiceImpl();
        patientDaoMock = mock(PatientDAO.class);
        patientService.setPatientDAO(patientDaoMock);
        PowerMockito.mockStatic(PatientIdentifierValidator.class);
    }

    @Test(expected = APIException.class)
    public void getDuplicatePatientsByAttributes_shouldThrowErrorGivenEmptyAttributes() throws Exception {
        patientService.getDuplicatePatientsByAttributes(Arrays.asList());
    }

    @Test(expected = APIException.class)
    public void getDuplicatePatientsByAttributes_shouldThrowErrorGivenNoAttributes() throws Exception {
        patientService.getDuplicatePatientsByAttributes(null);
    }

    @Test
    public void getDuplicatePatientsByAttributes_shouldCallDaoGivenAttributes() throws Exception {
        when(patientDaoMock.getDuplicatePatientsByAttributes(anyList())).thenReturn(Arrays.asList(mock(Patient.class)));
        final List<Patient> duplicatePatients = patientService.getDuplicatePatientsByAttributes(Arrays.asList("some attribute", "another attribute"));
        verify(patientDaoMock, times(1)).getDuplicatePatientsByAttributes(anyList());
        Assert.assertEquals(duplicatePatients.size(), 1);
    }

    @Test
    public void checkPatientIdentifiers_shouldThrowMissingRequiredIdentifierGivenRequiredIdentifierTypeMissing() throws Exception {
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
        patientWithIdentifiers.addIdentifier(new PatientIdentifier("some identifier", patientIdentifierType, mock(Location.class)));

        try {
            // when
            patientService.checkPatientIdentifiers(patientWithIdentifiers);
            fail();
            // then
        } catch(MissingRequiredIdentifierException e) {
            assertTrue(e.getMessage().contains("required"));
            assertTrue(e.getMessage().contains("NameOfRequiredIdentifierType"));
        } catch(Exception e) {
            fail("Expecting MissingRequiredIdentifierException");
        }

    }

    @Test
    public void checkPatientIdentifiers_shouldNotThrowMissingRequiredIdentifierGivenRequiredIdentifierTypesArePresent() throws Exception {
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
        patientWithIdentifiers.addIdentifier(new PatientIdentifier("some identifier", patientIdentifierType, mock(Location.class)));
        final PatientIdentifierType anotherPatientIdentifier = new PatientIdentifierType(2345);
        anotherPatientIdentifier.setUuid("another type uuid");
        patientWithIdentifiers.addIdentifier(new PatientIdentifier("some identifier", anotherPatientIdentifier, mock(Location.class)));

        // when
        patientService.checkPatientIdentifiers(patientWithIdentifiers);

        // then no exception
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

        when(patientDaoMock.getPatientIdentifierTypes(any(), any(), any(), any()))
                .thenReturn(new ArrayList<>());

        final Patient patientWithIdentifiers = new Patient();
        final PatientIdentifier patientIdentifier = new PatientIdentifier("some identifier", identifierType, mock(Location.class));

        patientIdentifier.setIdentifier(equalIdentifier);
        patientWithIdentifiers.addIdentifier(patientIdentifier);
        final PatientIdentifier patientIdentifierSameType = new PatientIdentifier("some identifier", sameIdentifierType, mock(Location.class));
        patientIdentifierSameType.setIdentifier(equalIdentifier);
        patientWithIdentifiers.addIdentifier(patientIdentifierSameType);

        // when
        try {
            patientService.checkPatientIdentifiers(patientWithIdentifiers);
            // then
            fail();
        } catch (DuplicateIdentifierException e) {
            assertNotNull(e.getPatientIdentifier());
            assertTrue(e.getMessage().contains("Identifier1 id type #: 12345"));
        }

    }

    @Test(expected = InsufficientIdentifiersException.class)
    public void checkPatientIdentifiers_shouldThrowInsufficientIdentifiersErrorGivenPatientHasNoActiveIdentifiers() throws Exception {
        // given
        Patient patient = new Patient();
        patient.setVoided(false);
        patient.addIdentifier(createVoidedPatientIdentifier());

        // when
        patientService.checkPatientIdentifiers(patient);

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
    public void getPatientIdentifierTypes_shouldReturnPatientIdentifierTypesFromDao() {

        // given
        final List<PatientIdentifierType> expectedIdentifierTypes = new ArrayList<>();
        expectedIdentifierTypes.add(new PatientIdentifierType(12345));
        when(patientDaoMock.getPatientIdentifierTypes(any(), any(), any(), any()))
                .thenReturn(expectedIdentifierTypes);

        // when
        final List<PatientIdentifierType> actualIdentifierTypes = patientService.getPatientIdentifierTypes("a name", "a format", true, false);

        // then
        verify(patientDaoMock, times(1)).getPatientIdentifierTypes("a name", "a format", true, false);
        assertEquals(expectedIdentifierTypes.get(0).getPatientIdentifierTypeId(), actualIdentifierTypes.get(0).getPatientIdentifierTypeId());
    }

    @Test
    public void getPatientIdentifierTypes_shouldReturnEmptyListGivenDaoReturnsNull() {

        // given
        when(patientDaoMock.getPatientIdentifierTypes(any(), any(), any(), any()))
                .thenReturn(null);

        // when
        final List<PatientIdentifierType> actualIdentifierTypes = patientService.getPatientIdentifierTypes("a name", "a format", true, false);

        // then
        verify(patientDaoMock, times(1)).getPatientIdentifierTypes("a name", "a format", true, false);
        assertEquals(0, actualIdentifierTypes.size());
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

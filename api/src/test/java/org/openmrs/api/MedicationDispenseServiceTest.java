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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.DrugOrder;
import org.openmrs.MedicationDispense;
import org.openmrs.api.builder.MedicationDispenseBuilder;
import org.openmrs.api.db.hibernate.HibernateMedicationDispenseDAOTest;
import org.openmrs.parameter.MedicationDispenseCriteria;
import org.openmrs.parameter.MedicationDispenseCriteriaBuilder;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the MedicationDispenseService
 */
public class MedicationDispenseServiceTest extends BaseContextSensitiveTest {
	
	@Autowired
	MedicationDispenseService medicationDispenseService;
	
	@Autowired
	PatientService patientService;

	@Autowired
	EncounterService encounterService;

	@Autowired
	OrderService orderService;
	
	@Autowired @Qualifier("adminService")
	AdministrationService administrationService;

	@BeforeEach
	public void setUp() {
		executeDataSet("org/openmrs/api/include/MedicationDispenseServiceTest-initialData.xml");
		updateSearchIndex();
	}

	/**
	 * @see MedicationDispenseService#getMedicationDispense(Integer) 
	 */
	@Test
	public void getMedicationDispense_shouldGetExistingMedicationDispense() {
		MedicationDispense existing = medicationDispenseService.getMedicationDispense(1);
		HibernateMedicationDispenseDAOTest.testMedicationDispense1(existing);
	}

	/**
	 * @see MedicationDispenseService#getMedicationDispenseByUuid(String)
	 */
	@Test
	public void getMedicationDispenseByUuid_shouldGetExistingMedicationDispense() {
		String uuid = "b75c5c9e-b66c-11ec-8065-0242ac110002";
		MedicationDispense existing = medicationDispenseService.getMedicationDispenseByUuid(uuid);
		HibernateMedicationDispenseDAOTest.testMedicationDispense1(existing);
	}

	/**
	 * @see MedicationDispenseService#getMedicationDispenseByCriteria(MedicationDispenseCriteria)
	 */
	@Test
	public void getMedicationDispenseByCriteria_shouldGetByPatient() {
		MedicationDispenseCriteriaBuilder b = new MedicationDispenseCriteriaBuilder();
		b.setPatient(patientService.getPatient(2));
		List<MedicationDispense> l = medicationDispenseService.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(1));
		HibernateMedicationDispenseDAOTest.testMedicationDispense1(l.get(0));

		b.setPatient(patientService.getPatient(7));
		l = medicationDispenseService.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(2));

		b.setPatient(null);
		l = medicationDispenseService.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(3));
	}

	/**
	 * @see MedicationDispenseService#getMedicationDispenseByCriteria(MedicationDispenseCriteria)
	 */
	@Test
	public void getMedicationDispenseByCriteria_shouldGetByEncounter() {
		MedicationDispenseCriteriaBuilder b = new MedicationDispenseCriteriaBuilder();
		b.setEncounter(encounterService.getEncounter(6));
		List<MedicationDispense> l = medicationDispenseService.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(1));
		HibernateMedicationDispenseDAOTest.testMedicationDispense1(l.get(0));

		b.setEncounter(encounterService.getEncounter(3));
		l = medicationDispenseService.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(1));

		b.setEncounter(null);
		l = medicationDispenseService.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(3));
	}

	/**
	 * @see MedicationDispenseService#getMedicationDispenseByCriteria(MedicationDispenseCriteria)
	 */
	@Test
	public void getMedicationDispenseByCriteria_shouldGetByDrugOrder() {
		MedicationDispenseCriteriaBuilder b = new MedicationDispenseCriteriaBuilder();
		b.setDrugOrder((DrugOrder)orderService.getOrder(2));
		List<MedicationDispense> l = medicationDispenseService.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(1));
		HibernateMedicationDispenseDAOTest.testMedicationDispense1(l.get(0));

		b.setDrugOrder((DrugOrder)orderService.getOrder(1));
		l = medicationDispenseService.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(1));

		b.setDrugOrder(null);
		l = medicationDispenseService.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(3));
	}

	/**
	 * @see MedicationDispenseService#getMedicationDispenseByCriteria(MedicationDispenseCriteria)
	 */
	@Test
	public void getMedicationDispenseByCriteria_shouldIncludedVoided() {
		MedicationDispenseCriteriaBuilder b = new MedicationDispenseCriteriaBuilder();
		List<MedicationDispense> l = medicationDispenseService.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(3));
		b.setIncludeVoided(true);
		l = medicationDispenseService.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(4));
	}

	/**
	 * @see MedicationDispenseService#saveMedicationDispense(MedicationDispense)
	 */
	@Test
	public void saveMedicationDispense_shouldSaveNewMedicationDispense() {
		MedicationDispenseBuilder b = new MedicationDispenseBuilder();
		b.withPatient(7).withConcept(88).withStatus(11111, 11210);
		MedicationDispense dispense = medicationDispenseService.saveMedicationDispense(b.build());
		assertNotNull(dispense.getId());
	}

	/**
	 * @see MedicationDispenseService#saveMedicationDispense(MedicationDispense)
	 */
	@Test
	public void saveMedicationDispense_shouldUpdateExistingMedicationDispense() {
		MedicationDispenseCriteria criteria = new MedicationDispenseCriteriaBuilder().build();
		List<MedicationDispense> allBefore = medicationDispenseService.getMedicationDispenseByCriteria(criteria);
		MedicationDispense existing = medicationDispenseService.getMedicationDispense(1);
		existing.setFormNamespaceAndPath("newNamespace^newPath");
		MedicationDispense saved = medicationDispenseService.saveMedicationDispense(existing);
		assertThat(saved.getFormNamespaceAndPath(), is("newNamespace^newPath"));
		List<MedicationDispense> allAfter = medicationDispenseService.getMedicationDispenseByCriteria(criteria);
		assertThat(allAfter.size(), is(allBefore.size()));
	}

	/**
	 * @see MedicationDispenseService#voidMedicationDispense(MedicationDispense, String)
	 */
	@Test
	public void voidMedicationDispense_shouldVoidIfNotAlreadyVoided() {
		Date now = new Date();
		MedicationDispense existing = medicationDispenseService.getMedicationDispense(1);
		assertFalse(existing.getVoided());
		existing = medicationDispenseService.voidMedicationDispense(existing, "A void reason");
		assertTrue(existing.getVoided());
		assertTrue(existing.getDateVoided().compareTo(now) >= 0);
		assertThat(existing.getVoidReason(), is("A void reason"));
	}

	/**
	 * @see MedicationDispenseService#voidMedicationDispense(MedicationDispense, String)
	 */
	@Test
	public void voidMedicationDispense_shouldNotChangeVoidedPropertiesIfAlreadyVoided() {
		MedicationDispense existing = medicationDispenseService.getMedicationDispense(2);
		assertTrue(existing.getVoided());
		assertThat(existing.getDateVoided().toString(), is("2008-08-19 10:00:00.0"));
		assertThat(existing.getVoidReason(), is("Testing"));
		existing = medicationDispenseService.voidMedicationDispense(existing, "A new reason");
		assertTrue(existing.getVoided());
		assertThat(existing.getDateVoided().toString(), is("2008-08-19 10:00:00.0"));
		assertThat(existing.getVoidReason(), is("Testing"));
	}

	/**
	 * @see MedicationDispenseService#voidMedicationDispense(MedicationDispense, String)
	 */
	@Test
	public void voidMedicationDispense_shouldFailIfVoidReasonIsNull() {
		MedicationDispense existing = medicationDispenseService.getMedicationDispense(1);
		assertFalse(existing.getVoided());
		assertThrows(IllegalArgumentException.class, () -> 
			medicationDispenseService.voidMedicationDispense(existing, null)
		);
	}

	/**
	 * @see MedicationDispenseService#voidMedicationDispense(MedicationDispense, String)
	 */
	@Test
	public void voidMedicationDispense_shouldFailIfVoidReasonIsEmpty() {
		MedicationDispense existing = medicationDispenseService.getMedicationDispense(1);
		assertFalse(existing.getVoided());
		assertThrows(IllegalArgumentException.class, () ->
			medicationDispenseService.voidMedicationDispense(existing, " ")
		);
	}

	/**
	 * @see MedicationDispenseService#unvoidMedicationDispense(MedicationDispense)
	 */
	@Test
	public void unvoidMedicationDispense_shouldUnvoidSuccessfully() {
		MedicationDispense existing = medicationDispenseService.getMedicationDispense(2);
		assertTrue(existing.getVoided());
		assertThat(existing.getDateVoided().toString(), is("2008-08-19 10:00:00.0"));
		assertThat(existing.getVoidReason(), is("Testing"));
		existing = medicationDispenseService.unvoidMedicationDispense(existing);
		assertFalse(existing.getVoided());
		assertNull(existing.getDateVoided());
		assertNull(existing.getVoidedBy());
		assertNull(existing.getVoidReason());
	}

	/**
	 * @see MedicationDispenseService#purgeMedicationDispense(MedicationDispense)
	 */
	@Test
	public void purgeMedicationDispense_shouldRemoveFromDatabase() {
		MedicationDispense existing = medicationDispenseService.getMedicationDispense(1);
		assertNotNull(existing);
		medicationDispenseService.purgeMedicationDispense(existing);
		existing = medicationDispenseService.getMedicationDispense(1);
		assertNull(existing);
	}
}

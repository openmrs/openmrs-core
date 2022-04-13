/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.MedicationDispense;
import org.openmrs.Patient;
import org.openmrs.api.builder.MedicationDispenseBuilder;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.api.db.MedicationDispenseDAO;
import org.openmrs.parameter.MedicationDispenseCriteria;
import org.openmrs.parameter.MedicationDispenseCriteriaBuilder;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the HibernateMedicationDispenseDAO
 */
public class HibernateMedicationDispenseDAOTest extends BaseContextSensitiveTest {
	
	@Autowired
	MedicationDispenseDAO medicationDispenseDAO;
	
	@Autowired
	AdministrationDAO administrationDAO;

	@BeforeEach
	public void setUp() {
		executeDataSet("org/openmrs/api/include/MedicationDispenseServiceTest-initialData.xml");
		updateSearchIndex();
	}

	/**
	 * @see MedicationDispenseDAO#getMedicationDispense(Integer) 
	 */
	@Test
	public void getMedicationDispense_shouldGetExistingMedicationDispense() {
		MedicationDispense existing = medicationDispenseDAO.getMedicationDispense(1);
		testMedicationDispense1(existing);
	}

	/**
	 * @see MedicationDispenseDAO#getMedicationDispenseByUuid(String)
	 */
	@Test
	public void getMedicationDispenseByUuid_shouldGetExistingMedicationDispense() {
		String uuid = "b75c5c9e-b66c-11ec-8065-0242ac110002";
		MedicationDispense existing = medicationDispenseDAO.getMedicationDispenseByUuid(uuid);
		testMedicationDispense1(existing);
	}

	/**
	 * @see MedicationDispenseDAO#getMedicationDispenseByCriteria(MedicationDispenseCriteria)
	 */
	@Test
	public void getMedicationDispenseByCriteria_shouldGetByPatient() {
		MedicationDispenseCriteriaBuilder b = new MedicationDispenseCriteriaBuilder();
		b.setPatient(new Patient(2));
		List<MedicationDispense> l = medicationDispenseDAO.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(1));
		testMedicationDispense1(l.get(0));
		
		b.setPatient(new Patient(7));
		l = medicationDispenseDAO.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(2));

		b.setPatient(null);
		l = medicationDispenseDAO.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(3));
	}

	/**
	 * @see MedicationDispenseDAO#getMedicationDispenseByCriteria(MedicationDispenseCriteria)
	 */
	@Test
	public void getMedicationDispenseByCriteria_shouldGetByEncounter() {
		MedicationDispenseCriteriaBuilder b = new MedicationDispenseCriteriaBuilder();
		b.setEncounter(new Encounter(6));
		List<MedicationDispense> l = medicationDispenseDAO.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(1));
		testMedicationDispense1(l.get(0));

		b.setEncounter(new Encounter(3));
		l = medicationDispenseDAO.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(1));
		
		b.setEncounter(null);
		l = medicationDispenseDAO.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(3));
	}

	/**
	 * @see MedicationDispenseDAO#getMedicationDispenseByCriteria(MedicationDispenseCriteria)
	 */
	@Test
	public void getMedicationDispenseByCriteria_shouldGetByDrugOrder() {
		MedicationDispenseCriteriaBuilder b = new MedicationDispenseCriteriaBuilder();
		b.setDrugOrder(new DrugOrder(2));
		List<MedicationDispense> l = medicationDispenseDAO.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(1));
		testMedicationDispense1(l.get(0));

		b.setDrugOrder(new DrugOrder(1));
		l = medicationDispenseDAO.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(1));

		b.setDrugOrder(null);
		l = medicationDispenseDAO.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(3));
	}

	/**
	 * @see MedicationDispenseDAO#getMedicationDispenseByCriteria(MedicationDispenseCriteria)
	 */
	@Test
	public void getMedicationDispenseByCriteria_shouldIncludedVoided() {
		MedicationDispenseCriteriaBuilder b = new MedicationDispenseCriteriaBuilder();
		List<MedicationDispense> l = medicationDispenseDAO.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(3));
		b.setIncludeVoided(true);
		l = medicationDispenseDAO.getMedicationDispenseByCriteria(b.build());
		assertThat(l.size(), is(4));
	}

	/**
	 * @see MedicationDispenseDAO#saveMedicationDispense(MedicationDispense)
	 */
	@Test
	public void saveMedicationDispense_shouldSaveNewMedicationDispense() {
		MedicationDispenseBuilder b = new MedicationDispenseBuilder();
		b.withPatient(7).withConcept(88).withStatus(11111, 11210);
		MedicationDispense dispense = medicationDispenseDAO.saveMedicationDispense(b.build());
		assertNotNull(dispense.getId());
	}

	/**
	 * @see MedicationDispenseDAO#saveMedicationDispense(MedicationDispense)
	 */
	@Test
	public void saveMedicationDispense_shouldUpdateExistingMedicationDispense() {
		MedicationDispenseCriteria criteria = new MedicationDispenseCriteriaBuilder().build();
		List<MedicationDispense> allBefore = medicationDispenseDAO.getMedicationDispenseByCriteria(criteria);
		MedicationDispense existing = medicationDispenseDAO.getMedicationDispense(1);
		existing.setFormNamespaceAndPath("newNamespace^newPath");
		MedicationDispense saved = medicationDispenseDAO.saveMedicationDispense(existing);
		assertThat(saved.getFormNamespaceAndPath(), is("newNamespace^newPath"));
		List<MedicationDispense> allAfter = medicationDispenseDAO.getMedicationDispenseByCriteria(criteria);
		assertThat(allAfter.size(), is(allBefore.size()));
	}

	/**
	 * @see MedicationDispenseDAO#deleteMedicationDispense(MedicationDispense)
	 */
	@Test
	public void deleteMedicationDispense_shouldRemoveFromDatabase() {
		MedicationDispense existing = medicationDispenseDAO.getMedicationDispense(1);
		testMedicationDispense1(existing);
		medicationDispenseDAO.deleteMedicationDispense(existing);
		existing = medicationDispenseDAO.getMedicationDispense(1);
		assertNull(existing);
	}
	
	public static void testMedicationDispense1(MedicationDispense existing) {
		assertThat(existing.getUuid(), is("b75c5c9e-b66c-11ec-8065-0242ac110002"));
		assertThat(existing.getPatient().getPatientId(), is(2));
		assertThat(existing.getEncounter().getEncounterId(), is(6));
		assertThat(existing.getDrugOrder().getOrderId(), is(2));
		assertThat(existing.getConcept().getConceptId(), is(792));
		assertThat(existing.getDrug().getDrugId(), is(2));
		assertThat(existing.getLocation().getLocationId(), is(2));
		assertThat(existing.getDispenser().getProviderId(), is(1));
		assertThat(existing.getStatus().getConceptId(), is(11112));
		assertNull(existing.getStatusReason());
		assertThat(existing.getType().getConceptId(), is(11310));
		assertThat(existing.getQuantity(), is(1.0));
		assertThat(existing.getQuantityUnits().getConceptId(), is(51));
		assertThat(existing.getDose(), is(1.0));
		assertThat(existing.getDoseUnits().getConceptId(), is(51));
		assertThat(existing.getRoute().getConceptId(), is(22));
		assertThat(existing.getFrequency().getOrderFrequencyId(), is(1));
		assertThat(existing.getAsNeeded(), is(Boolean.FALSE));
		assertThat(existing.getDosingInstructions(), is("Take as directed"));
		assertThat(existing.getDatePrepared().toString(), is("2008-08-19 10:00:00.0"));
		assertThat(existing.getDateHandedOver().toString(), is("2008-08-19 10:22:00.0"));
		assertThat(existing.getWasSubstituted(), is(Boolean.FALSE));
		assertNull(existing.getSubstitutionType());
		assertNull(existing.getSubstitutionReason());
		assertThat(existing.getFormNamespaceAndPath(), is("formNamespace^formPath"));
	}
}

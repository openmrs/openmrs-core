/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;
 
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RequiredValuesConstructorsTest {
	@Test
	void shouldCreateConceptWithRequiredValues() {
		ConceptDatatype datatype = new ConceptDatatype();
		ConceptClass conceptClass = new ConceptClass();

		Concept concept = new Concept(datatype, conceptClass);

		assertEquals(datatype, concept.getDatatype());
		assertEquals(conceptClass, concept.getConceptClass());
	}

	@Test
	void shouldCreateEncounterWithRequiredValues() {
		Patient patient = new Patient();
		EncounterType encounterType = new EncounterType();
		Date encounterDatetime = new Date();
		Location location = new Location("Mengo");

		Encounter encounter = new Encounter(patient, encounterType, encounterDatetime, location);

		assertEquals(patient, encounter.getPatient());
		assertEquals(encounterType, encounter.getEncounterType());
		assertEquals(encounterDatetime, encounter.getEncounterDatetime());
		assertEquals(location, encounter.getLocation());
	}

	@Test
	void shouldCreateLocationWithRequiredValues() {
		String name = "Kampala";
		Location location = new Location(name);

		assertEquals(name, location.getName());
	}

	@Test
	void shouldCreateProgramWithRequiredValues() {
		Concept concept = new Concept(new ConceptDatatype(), new ConceptClass());
		String name = "HIV Program";

		Program program = new Program(name, concept);

		assertEquals(name, program.getName());
		assertEquals(concept, program.getConcept());
	}

	@Test
	void shouldCreateUserWithRequiredValues() {
		Person person = new Person();
		String username = "testuser";

		User user = new User(person, username);

		assertEquals(person, user.getPerson());
		assertEquals(username, user.getUsername());
	}	
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test.jupiter;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Narrows {@link #getIndexedTypes()} the way several service tests do. When the one-time
 * standard-data setup runs on behalf of such a class, searches on types it leaves out must still
 * work (TRUNK-6814).
 */
public class BaseSetupSearchIndexTest extends BaseContextSensitiveTest {

	@Override
	public Class<?>[] getIndexedTypes() {
		return new Class<?>[] { PersonName.class };
	}

	/**
	 * @see BaseContextSensitiveNonTransactionalTest#getBaseSetupIndexedTypes()
	 */
	@Test
	public void getBaseSetupIndexedTypes_shouldIncludeTheDefaultTypesWhenGetIndexedTypesIsNarrowed() {
		assertTrue(getBaseSetupIndexedTypes().containsAll(
		    Arrays.asList(ConceptName.class, Drug.class, PersonName.class, PersonAttribute.class, PatientIdentifier.class)));
	}

	/**
	 * @see BaseContextSensitiveNonTransactionalTest#getBaseSetupIndexedTypes()
	 */
	@Test
	public void getBaseSetupIndexedTypes_shouldIncludeTypesAddedByGetIndexedTypes() {
		Set<Class<?>> types = new ExtendedIndexedTypes().getBaseSetupIndexedTypes();

		assertTrue(types.contains(Location.class));
		assertTrue(types.contains(ConceptName.class));
	}

	@Test
	public void baseSetup_shouldIndexConceptsAndDrugsWhenGetIndexedTypesIsNarrowed() throws SQLException {
		// The standard data is set up once per JVM, usually by an earlier test class, so force it to run
		// again on behalf of this one. Reindexing the empty database first drops anything an earlier
		// setup left in the concept and drug indexes.
		deleteAllData();
		Context.updateSearchIndexForType(ConceptName.class);
		Context.updateSearchIndexForType(Drug.class);
		baseSetupWithStandardDataAndAuthentication();

		ConceptService conceptService = Context.getConceptService();

		assertFalse(conceptService.getConcepts("CD4 COUNT", new Locale("en", "GB"), true).isEmpty());
		assertFalse(conceptService.getDrugs("ASPIRIN", null, true, true, true, 0, 100).isEmpty());
	}

	static class ExtendedIndexedTypes extends BaseContextSensitiveTest {

		@Override
		public Class<?>[] getIndexedTypes() {
			return new Class<?>[] { PersonName.class, Location.class };
		}
	}
}

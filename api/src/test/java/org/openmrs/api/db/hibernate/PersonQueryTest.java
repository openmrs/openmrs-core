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

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PersonQueryTest {

	@Test
	public void singleCharacterTerms_shouldReturnSingleCharacterTermsBesideALongerTerm() {
		assertEquals(Collections.singletonList("j"), PersonQuery.singleCharacterTerms("joshua j"));
		assertEquals(Collections.singletonList("J"), PersonQuery.singleCharacterTerms("J Johnson"));
		assertEquals(Arrays.asList("j", "d"), PersonQuery.singleCharacterTerms("  j  d   smith "));
	}

	@Test
	public void singleCharacterTerms_shouldReturnNullWhenNoTermHasASingleCharacter() {
		assertNull(PersonQuery.singleCharacterTerms("joshua johnson"));
		assertNull(PersonQuery.singleCharacterTerms("joshua jo"));
	}

	@Test
	public void singleCharacterTerms_shouldReturnEveryTermWhenAllHaveASingleCharacter() {
		assertEquals(Collections.singletonList("j"), PersonQuery.singleCharacterTerms("j"));
		assertEquals(Arrays.asList("j", "d"), PersonQuery.singleCharacterTerms("j d"));
	}

	@Test
	public void singleCharacterTerms_shouldReturnInitialsFollowedByADotWithoutTheDot() {
		assertEquals(Collections.singletonList("J"), PersonQuery.singleCharacterTerms("J. Doe"));
		assertEquals(Arrays.asList("j", "d"), PersonQuery.singleCharacterTerms("j. d."));
		assertNull(PersonQuery.singleCharacterTerms("j.d. smith"));
	}

	@Test
	public void singleCharacterTerms_shouldReturnSingleCharacterTermsBesideHyphenatedNames() {
		assertEquals(Collections.singletonList("d"), PersonQuery.singleCharacterTerms("jean-claude d"));
		assertEquals(Collections.singletonList("j"), PersonQuery.singleCharacterTerms("j smith-jones"));
	}

	@Test
	public void singleCharacterTerms_shouldReturnNullForQueriesUsingQuerySyntax() {
		assertNull(PersonQuery.singleCharacterTerms("\"John D Smith\""));
		assertNull(PersonQuery.singleCharacterTerms("joshua j*"));
		assertNull(PersonQuery.singleCharacterTerms("joshua | j"));
		assertNull(PersonQuery.singleCharacterTerms("joshua +j"));
		assertNull(PersonQuery.singleCharacterTerms("joshua -j"));
		assertNull(PersonQuery.singleCharacterTerms("(joshua j)"));
		assertNull(PersonQuery.singleCharacterTerms("joshua~ j"));
	}

	@Test
	public void singleCharacterTerms_shouldReturnNullForNullQuery() {
		assertNull(PersonQuery.singleCharacterTerms(null));
	}
}

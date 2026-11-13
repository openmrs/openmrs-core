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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PersonQueryTest {

	@Test
	public void prefixSingleCharacterTerms_shouldTurnSingleCharacterTermsIntoPrefixQueries() {
		assertEquals("joshua j*", PersonQuery.prefixSingleCharacterTerms("joshua j"));
		assertEquals("j* johnson", PersonQuery.prefixSingleCharacterTerms("j johnson"));
		assertEquals("j* d* smith", PersonQuery.prefixSingleCharacterTerms("j d smith"));
	}

	@Test
	public void prefixSingleCharacterTerms_shouldLeaveLongerTermsAndQuerySyntaxUnchanged() {
		assertEquals("joshua johnson", PersonQuery.prefixSingleCharacterTerms("joshua johnson"));
		assertEquals("joshua jo", PersonQuery.prefixSingleCharacterTerms("joshua jo"));
		assertEquals("joshua j*", PersonQuery.prefixSingleCharacterTerms("joshua j*"));
		assertEquals("joshua -", PersonQuery.prefixSingleCharacterTerms("joshua -"));
	}

	@Test
	public void prefixSingleCharacterTerms_shouldLeaveTermsInsideQuotedPhrasesUnchanged() {
		assertEquals("\"John D Smith\"", PersonQuery.prefixSingleCharacterTerms("\"John D Smith\""));
		assertEquals("\"John D\" s*", PersonQuery.prefixSingleCharacterTerms("\"John D\" s"));
		assertEquals("j* \"D Smith\"", PersonQuery.prefixSingleCharacterTerms("j \"D Smith\""));
	}

	@Test
	public void prefixSingleCharacterTerms_shouldCollapseSurroundingAndRepeatedWhitespace() {
		assertEquals("joshua j*", PersonQuery.prefixSingleCharacterTerms("  joshua   j "));
	}

	@Test
	public void prefixSingleCharacterTerms_shouldReturnNullForNullQuery() {
		assertNull(PersonQuery.prefixSingleCharacterTerms(null));
	}
}

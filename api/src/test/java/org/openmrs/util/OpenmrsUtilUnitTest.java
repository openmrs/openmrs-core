/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

/**
 * Unit tests for {@link OpenmrsUtil}.
 * 
 */
public class OpenmrsUtilUnitTest {


	@Test
	public void parseParameterList_shouldReturnKeyPairsGivenParametersSeparatedByPipe() {

		Map<String, String> actual = OpenmrsUtil.parseParameterList("role_id=role|person_id=person");
		
		assertThat(actual, hasEntry("role_id", "role"));
		assertThat(actual, hasEntry("person_id", "person"));
		assertEquals(2, actual.size());
	}

	@Test
	public void parseParameterList_shouldReturnKeyPairsGivenOnePairAndOmitTrailingPipe() {

		Map<String, String> actual = OpenmrsUtil.parseParameterList("role_id=role|");
		
		assertThat(actual, hasEntry("role_id", "role"));
		assertEquals(1, actual.size());
	}

	@Test
	public void parseParameterList_shouldReturnKeyPairsGivenOnePairWithoutValue() {

		Map<String, String> actual = OpenmrsUtil.parseParameterList("role_id=");
		
		assertThat(actual, hasEntry("role_id", ""));
		assertEquals(1, actual.size());
	}
	
	@Test
	public void parseParameterList_shouldReturnEmptyMapGivenNull() {

		assertThat(OpenmrsUtil.parseParameterList(null), equalTo(Collections.EMPTY_MAP));
	}
	
	@Test
	public void parseParameterList_shouldReturnEmptyMapGivenEmptyString() {

		assertThat(OpenmrsUtil.parseParameterList(""), equalTo(Collections.EMPTY_MAP));
	}
	
	@Test
	public void parseParameterList_shouldReturnEmptyMapGivenOnlyPipesAndNoWhitespace() {

		assertThat(OpenmrsUtil.parseParameterList("||||"), equalTo(Collections.EMPTY_MAP));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void parseParameterList_shouldFailGivenOnlyWhitespaces() {

		OpenmrsUtil.parseParameterList("   ");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void parseParameterList_shouldFailGivenOnlyAKey() {
		
		OpenmrsUtil.parseParameterList("role_id");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void parseParameterList_shouldFailGivenOnlyKeysSeparatedByPipes() {

		OpenmrsUtil.parseParameterList("role_id|patient_id");
	}
}

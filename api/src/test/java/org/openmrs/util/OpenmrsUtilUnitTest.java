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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link OpenmrsUtil}.
 * 
 */
public class OpenmrsUtilUnitTest {

	private List<String> moduleList;

	@Before
	public void setUp() {
		moduleList = Arrays.asList("module1", "module2");
	}

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

	@Test
	public void join_shouldReturnEmptyStringGivenNullForCollection() {
		
		assertThat(OpenmrsUtil.join(null, ""), is(""));
	}
	
	@Test
	public void join_shouldReturnEmptyStringGivenEmptyCollection() {

		assertThat(OpenmrsUtil.join(Collections.EMPTY_LIST, ""), is(""));
	}

	@Test
	public void join_shouldReturnListElementsJoinedBySeparatorWithoutLeadingOrTrailingSeparator() {

		assertThat(OpenmrsUtil.join(moduleList, ","), is("module1,module2"));
	}
	
	@Test
	public void join_shouldReturnListElementsJoinedBySeparatorIfGivenCollectionContainsNull() {

		List<String> listWithNull = Arrays.asList("module1", null, "module2");

		assertThat(OpenmrsUtil.join(listWithNull, ","), is("module1,null,module2"));
	}

	@Test
	public void join_shouldReturnListElementsJoinedBySeparatorIfGivenSeparatorIsEmptyString() {

		assertThat(OpenmrsUtil.join(moduleList, ""), is("module1module2"));
	}

	@Test
	public void join_shouldReturnListElementsJoinedBySeparatorIfGivenSeparatorIsNull() {

		assertThat(OpenmrsUtil.join(moduleList, null), is("module1nullmodule2"));
	}
}

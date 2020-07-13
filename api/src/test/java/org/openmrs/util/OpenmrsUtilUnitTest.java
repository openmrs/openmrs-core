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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.equalTo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Collection;
import java.util.Iterator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.ConceptNumeric;

/**
 * Unit tests for {@link OpenmrsUtil}.
 * 
 */
public class OpenmrsUtilUnitTest {

	private List<String> moduleList;

	@BeforeEach
	public void setUp() {
		moduleList = Arrays.asList("module1", "module2");
	}

	@AfterEach
	public void clearSystemProperty() {
		System.clearProperty("FUNCTIONAL_TEST_MODE");
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
	
	@Test
	public void parseParameterList_shouldFailGivenOnlyWhitespaces() {

		assertThrows(IllegalArgumentException.class, () -> OpenmrsUtil.parseParameterList("   "));
	}
	
	@Test
	public void parseParameterList_shouldFailGivenOnlyAKey() {
		assertThrows(IllegalArgumentException.class, () -> OpenmrsUtil.parseParameterList("role_id"));
	}
	
	@Test
	public void parseParameterList_shouldFailGivenOnlyKeysSeparatedByPipes() {

		assertThrows(IllegalArgumentException.class, () -> OpenmrsUtil.parseParameterList("role_id|patient_id"));
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

	@Test
	public void isStringInArray_shouldReturnTrueIfStringIsInArray() {

		String[] array = new String[]{"element1","element2","element3"};

		assertTrue(OpenmrsUtil.isStringInArray("element1", array));
	}

	@Test
	public void isStringInArray_shouldReturnFalseIfStringIsNotInArray() {

		String[] array = new String[]{"element1","element2","element3"};

		assertFalse(OpenmrsUtil.isStringInArray("element4", array));
	}

	@Test
	public void isInNormalNumericRange_shouldReturnFalseIfHiNormalIsNull() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiNormal(null);

		assertFalse(OpenmrsUtil.isInNormalNumericRange(5.67f, concept));
	}

	@Test
	public void isInNormalNumericRange_shouldReturnFalseIfLowNormalIsNull() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setLowNormal(null);

		assertFalse(OpenmrsUtil.isInNormalNumericRange(5.67f, concept));
	}

	@Test
	public void isInNormalNumericRange_shouldReturnTrueIfValueIsInRange() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiNormal(10.34d);
		concept.setLowNormal(3.67d);

		assertTrue(OpenmrsUtil.isInNormalNumericRange(5.67f, concept));
	}

	@Test
	public void isInNormalNumericRange_shouldReturnFalseIfValueIsAboveRange() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiNormal(4.97d);
		concept.setLowNormal(3.67d);

		assertFalse(OpenmrsUtil.isInNormalNumericRange(5.67f, concept));
	}

	@Test
	public void isInNormalNumericRange_shouldReturnFalseIfValueIsBelowRange() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiNormal(10.34d);
		concept.setLowNormal(6.67d);

		assertFalse(OpenmrsUtil.isInNormalNumericRange(5.67f, concept));
	}

	@Test
	public void isInNormalNumericRange_shouldReturnTrueIfValueIsEqualToHiRangeLimit() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiNormal(5.67);
		concept.setLowNormal(2.67);

		assertFalse(OpenmrsUtil.isInNormalNumericRange(5.67f, concept));
	}

	@Test
	public void isInNormalNumericRange_shouldReturnTrueIfValueIsEqualToLowRangeLimit() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiNormal(10.34d);
		concept.setLowNormal(5.64d);

		assertFalse(OpenmrsUtil.isInNormalNumericRange(5.64f, concept));
	}

	@Test
	public void isInCriticalNumericRange_shouldReturnFalseIfHiCriticalIsNull() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiCritical(null);

		assertFalse(OpenmrsUtil.isInCriticalNumericRange(5.67f, concept));
	}

	@Test
	public void isInCriticalNumericRange_shouldReturnFalseIfLowCriticalIsNull() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setLowCritical(null);

		assertFalse(OpenmrsUtil.isInCriticalNumericRange(5.67f, concept));
	}

	@Test
	public void isInCriticalNumericRange_shouldReturnTrueIfValueIsInRange() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiCritical(10.34d);
		concept.setLowCritical(3.67d);

		assertTrue(OpenmrsUtil.isInCriticalNumericRange(5.67f, concept));
	}

	@Test
	public void isInCriticalNumericRange_shouldReturnFalseIfValueIsAboveRange() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiCritical(4.34d);
		concept.setLowCritical(3.67d);

		assertFalse(OpenmrsUtil.isInCriticalNumericRange(5.67f, concept));
	}

	@Test
	public void isInCriticalNumericRange_shouldReturnFalseIfValueIsBelowRange() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiCritical(10.34d);
		concept.setLowCritical(3.67d);

		assertFalse(OpenmrsUtil.isInCriticalNumericRange(2.67f, concept));
	}

	@Test
	public void isInCriticalNumericRange_shouldReturnTrueIfValueIsEqualToHiRangeLimit() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiCritical(5.67d);
		concept.setLowCritical(3.67d);

		assertFalse(OpenmrsUtil.isInCriticalNumericRange(5.67f, concept));
	}

	@Test
	public void isInCriticalNumericRange_shouldReturnTrueIfValueIsEqualToLowRangeLimit() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiCritical(10.67d);
		concept.setLowCritical(5.64d);

		assertFalse(OpenmrsUtil.isInCriticalNumericRange(5.64f, concept));
	}

	@Test
	public void isInAbsoluteNumericRange_shouldReturnFalseIfHiAbsoluteIsNull() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiAbsolute(null);

		assertFalse(OpenmrsUtil.isInAbsoluteNumericRange(5.67f, concept));
	}

	@Test
	public void isInAbsoluteNumericRange_shouldReturnFalseIfLowAbsoluteIsNull() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setLowAbsolute(null);

		assertFalse(OpenmrsUtil.isInAbsoluteNumericRange(5.67f, concept));
	}

	@Test
	public void isInAbsoluteNumericRange_shouldReturnTrueIfValueIsInRange() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiAbsolute(10.34d);
		concept.setLowAbsolute(3.67d);

		assertTrue(OpenmrsUtil.isInAbsoluteNumericRange(5.67f, concept));
	}

	@Test
	public void isInAbsoluteNumericRange_shouldReturnFalseIfValueIsAboveRange() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiCritical(4.34d);
		concept.setLowCritical(3.67d);

		assertFalse(OpenmrsUtil.isInAbsoluteNumericRange(5.67f, concept));
	}

	@Test
	public void isInAbsoluteNumericRange_shouldReturnFalseIfValueIsBelowRange() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiCritical(10.34d);
		concept.setLowCritical(6.67d);

		assertFalse(OpenmrsUtil.isInAbsoluteNumericRange(5.67f, concept));
	}

	@Test
	public void isInAbsoluteNumericRange_shouldReturnTrueIfValueIsEqualToHiRangeLimit() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiCritical(4.34d);
		concept.setLowCritical(3.67d);

		assertFalse(OpenmrsUtil.isInAbsoluteNumericRange(4.34f, concept));
	}

	@Test
	public void isInAbsoluteNumericRange_shouldReturnTrueIfValueIsEqualToLowRangeLimit() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiCritical(14.34d);
		concept.setLowCritical(4.34d);

		assertFalse(OpenmrsUtil.isInAbsoluteNumericRange(4.34f, concept));
	}

	@Test
	public void isValidNumericValue_shouldReturnTrueIfHiAbsoluteIsNull() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiAbsolute(null);

		assertTrue(OpenmrsUtil.isValidNumericValue(5.67f, concept));
	}

	@Test
	public void isValidNumericValue_shouldReturnTrueIfLowAbsoluteIsNull() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setLowAbsolute(null);

		assertTrue(OpenmrsUtil.isValidNumericValue(5.67f, concept));
	}

	@Test
	public void isValidNumericValue_shouldReturnTrueIfValueIsInRange() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiAbsolute(10.34d);
		concept.setLowAbsolute(3.67d);

		assertTrue(OpenmrsUtil.isValidNumericValue(5.67f, concept));
	}

	@Test
	public void isValidNumericValue_shouldReturnFalseIfValueIsAboveRange() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiAbsolute(10.34d);
		concept.setLowAbsolute(6.67d);

		assertFalse(OpenmrsUtil.isValidNumericValue(5.67f, concept));
	}

	@Test
	public void isValidNumericValue_shouldReturnFalseIfValueIsBelowRange() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiAbsolute(10.34d);
		concept.setLowAbsolute(6.67d);

		assertFalse(OpenmrsUtil.isValidNumericValue(15.67f, concept));
	}

	@Test
	public void isValidNumericValue_shouldReturnTrueIfValueIsEqualToHiRangeLimit() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiCritical(4.34d);
		concept.setLowCritical(3.67d);

		assertFalse(OpenmrsUtil.isInAbsoluteNumericRange(4.34f, concept));
	}

	@Test
	public void isValidNumericValue_shouldReturnTrueIfValueIsEqualToLowRangeLimit() {

		ConceptNumeric concept = new ConceptNumeric();
		concept.setHiCritical(14.34d);
		concept.setLowCritical(4.34d);

		assertFalse(OpenmrsUtil.isInAbsoluteNumericRange(4.34f, concept));
	}
	
	@Test
	public void isTestMode_shouldReturnTrueIfFunctionalTestModeIsEnabled() {

		System.setProperty("FUNCTIONAL_TEST_MODE", "true");

		assertTrue(OpenmrsUtil.isTestMode());
	}
	
	@Test
	public void isTestMode_shouldReturnFalseIfFunctionalTestModeIsDisabled() {

		System.setProperty("FUNCTIONAL_TEST_MODE", "false");

		assertFalse(OpenmrsUtil.isTestMode());
	}

	@Test
	public void compareLists_shouldPassIfMapOfDifferencesIsCorrect() {

		LinkedList<Integer> origList = new LinkedList<>(Arrays.asList(1,2,3));
		LinkedList<Integer> newList = new LinkedList<>(Arrays.asList(2,3,4));

		Collection<Collection<Integer>> result = OpenmrsUtil.compareLists(origList, newList);
		Iterator<Collection<Integer>> it = result.iterator();
		
		assertThat(result.size(), is(2));
		assertThat(it.next(), contains(4));
		assertThat(it.next(), contains(1));
	}
	
	@Test
	public void getRuntimePropertiesFilePathName_shouldReturnNullIfPropertyFileNotExist() {

		assertNull(OpenmrsUtil.getRuntimePropertiesFilePathName("app-openmrs"));
	}

}

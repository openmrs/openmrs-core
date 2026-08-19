/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.layout.address;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.serialization.SerializationException;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AddressTemplateTest extends BaseContextSensitiveTest {

	private AddressTemplate addressTemplate;

	@BeforeEach
	public void setUp() {
		// the same shape as OpenmrsConstants.DEFAULT_ADDRESS_TEMPLATE
		addressTemplate = new AddressTemplate("address1");
		// a real ArrayList rather than an Arrays.asList view: the latter is a JDK-internal type that
		// XStream can only handle reflectively, which the module system blocks on Java 11 and later
		addressTemplate.setLineByLineFormat(
		    new ArrayList<>(Arrays.asList("address1", "address2", "cityVillage stateProvince country postalCode")));

		Map<String, String> nameMappings = new HashMap<>();
		Map<String, String> sizeMappings = new HashMap<>();
		for (String element : Arrays.asList("address1", "address2", "cityVillage", "stateProvince", "country",
		    "postalCode")) {
			nameMappings.put(element, "Location." + element);
			sizeMappings.put(element, "40");
		}
		addressTemplate.setNameMappings(nameMappings);
		addressTemplate.setSizeMappings(sizeMappings);
	}

	@Test
	public void getLines_shouldTokenizeTheDefaultAddressFormatOnceAndReuseTheResult() {
		List<List<Map<String, String>>> lines = addressTemplate.getLines();

		assertEquals(Arrays.asList("", "address1"), codeNames(lines.get(0)));
		assertEquals(Arrays.asList("", "address2"), codeNames(lines.get(1)));
		assertEquals(Arrays.asList("", "cityVillage", " ", "stateProvince", " ", "country", " ", "postalCode"),
		    codeNames(lines.get(2)));
		assertEquals(8, addressTemplate.getMaxTokens());

		assertSame(lines, addressTemplate.getLines());
	}

	@Test
	public void serialize_shouldNotPersistTheTokenizationCacheAndShouldRoundTripUnchanged() throws SerializationException {
		// populate the cache before serializing, so a non-transient cache would show up in the xml
		List<List<String>> before = codeNamesOfLines(addressTemplate.getLines());

		String xml = Context.getSerializationService().getDefaultSerializer().serialize(addressTemplate);

		assertFalse(xml.contains("tokenizedLines"), "tokenization cache leaked into the serialized template: " + xml);

		AddressTemplate deserialized = Context.getSerializationService().getDefaultSerializer().deserialize(xml,
		    AddressTemplate.class);

		assertEquals(before, codeNamesOfLines(deserialized.getLines()));
	}

	/**
	 * Reduces a tokenized line to the codeName of each token and the display text of each non-token,
	 * which together capture everything the tokenizer decided about that line.
	 */
	private List<String> codeNames(List<Map<String, String>> line) {
		List<String> ret = new ArrayList<>(line.size());
		for (Map<String, String> entry : line) {
			boolean isToken = addressTemplate.getLayoutToken().equals(entry.get("isToken"));
			ret.add(isToken ? entry.get("codeName") : entry.get("displayText"));
		}
		return ret;
	}

	private List<List<String>> codeNamesOfLines(List<List<Map<String, String>>> lines) {
		List<List<String>> ret = new ArrayList<>(lines.size());
		for (List<Map<String, String>> line : lines) {
			ret.add(codeNames(line));
		}
		return ret;
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.layout;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.serialization.SerializationException;
import org.openmrs.serialization.SimpleXStreamSerializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exercises the tokenization and its cache directly, via a stub LayoutSupport, so that no OpenMRS
 * Context is needed. NameTemplate and AddressTemplate add nothing to that logic beyond their token
 * markers and their support singleton.
 */
public class LayoutTemplateTest {

	private static final List<String> NAME_TOKENS = Arrays.asList("prefix", "givenName", "middleName", "familyNamePrefix",
	    "familyNameSuffix", "familyName2", "familyName", "degree");

	private static final List<String> ADDRESS_TOKENS = Arrays.asList("address1", "address2", "address3", "address4",
	    "address5", "address6", "cityVillage", "countyDistrict", "stateProvince", "country", "latitude", "longitude",
	    "postalCode", "startDate", "endDate");

	private StubLayoutSupport support;

	private StubLayoutTemplate template;

	@BeforeEach
	public void setUp() {
		support = new StubLayoutSupport();
		support.setSpecialTokens(NAME_TOKENS);
		template = newTemplate(support, "givenName", "familyName,");
	}

	@Test
	public void getLines_shouldTokenizeOnceAndReuseTheResult() {
		List<List<Map<String, String>>> first = template.getLines();
		List<List<Map<String, String>>> second = template.getLines();

		assertSame(first, second);
		assertEquals(1, support.specialTokenReads.get());
	}

	@Test
	public void getLines_shouldTokenizeMultiTokenAndSingleTokenLines() {
		template = newTemplate(support, "givenName", "familyName,", "\"middleName\"",
		    "familyNamePrefix familyName familyName2 familyNameSuffix");

		List<List<Map<String, String>>> lines = template.getLines();

		// a line holding a single token still gets a leading empty non-token, to match the shape
		// produced for lines that mix tokens and literal text
		assertEquals(Arrays.asList("N:", "T:givenName"), project(lines.get(0)));
		assertEquals(Arrays.asList("N:", "T:familyName", "N:,"), project(lines.get(1)));
		assertEquals(Arrays.asList("N:\"", "T:middleName", "N:\""), project(lines.get(2)));
		assertEquals(Arrays.asList("N:", "T:familyNamePrefix", "N: ", "T:familyName", "N: ", "T:familyName2", "N: ",
		    "T:familyNameSuffix"), project(lines.get(3)));
	}

	@Test
	public void getLines_shouldTokenizeTheDefaultAddressFormat() {
		support.setSpecialTokens(ADDRESS_TOKENS);
		template = newTemplate(support, "address1", "address2", "cityVillage stateProvince country postalCode");

		List<List<Map<String, String>>> lines = template.getLines();

		assertEquals(Arrays.asList("N:", "T:address1"), project(lines.get(0)));
		assertEquals(Arrays.asList("N:", "T:address2"), project(lines.get(1)));
		assertEquals(
		    Arrays.asList("N:", "T:cityVillage", "N: ", "T:stateProvince", "N: ", "T:country", "N: ", "T:postalCode"),
		    project(lines.get(2)));
	}

	@Test
	public void getLines_shouldPopulateDisplayTextAndSizeFromTheMappings() {
		Map<String, String> token = template.getLines().get(0).get(1);

		assertEquals(StubLayoutTemplate.LAYOUT_TOKEN_MARKER, token.get("isToken"));
		assertEquals("givenName", token.get("codeName"));
		assertEquals("PersonName.givenName", token.get("displayText"));
		assertEquals("30", token.get("displaySize"));
	}

	@Test
	public void getLines_shouldReturnNullWhenThereIsNoLineByLineFormat() {
		StubLayoutTemplate empty = new StubLayoutTemplate(support);

		assertNull(empty.getLines());
		assertNull(empty.getLines());
		// with nothing to tokenize, the support is never consulted at all
		assertEquals(0, support.specialTokenReads.get());
	}

	@Test
	public void getLines_shouldReturnStructuresThatCallersCannotMutate() {
		List<List<Map<String, String>>> lines = template.getLines();

		assertThrows(UnsupportedOperationException.class, () -> lines.remove(0));
		assertThrows(UnsupportedOperationException.class, () -> lines.get(0).clear());
		assertThrows(UnsupportedOperationException.class, () -> lines.get(0).get(0).put("isToken", "tampered"));
	}

	@Test
	public void getLines_shouldRetokenizeWhenTheLineByLineFormatChanges() {
		template.getLines();

		template.setLineByLineFormat(Collections.singletonList("middleName"));

		assertEquals(Collections.singletonList(Arrays.asList("N:", "T:middleName")), projectLines(template.getLines()));
		assertEquals(2, support.specialTokenReads.get());
	}

	@Test
	public void getLines_shouldRetokenizeWhenTheNameMappingsChange() {
		template.getLines();

		template.setNameMappings(Collections.singletonMap("givenName", "First name"));

		assertEquals("First name", template.getLines().get(0).get(1).get("displayText"));
		assertEquals(2, support.specialTokenReads.get());
	}

	@Test
	public void getLines_shouldRetokenizeWhenTheSizeMappingsChange() {
		template.getLines();

		template.setSizeMappings(Collections.singletonMap("givenName", "10"));

		assertEquals("10", template.getLines().get(0).get(1).get("displaySize"));
		assertEquals(2, support.specialTokenReads.get());
	}

	@Test
	public void getLines_shouldRetokenizeWhenMaxTokensIsSet() {
		List<List<Map<String, String>>> first = template.getLines();

		template.setMaxTokens(9);

		assertNotSame(first, template.getLines());
		assertEquals(2, support.specialTokenReads.get());
	}

	@Test
	public void getLines_shouldRetokenizeWhenTheSpecialTokensChange() {
		template.getLines();

		// dropping givenName from the special tokens means it is no longer recognised as a token
		support.setSpecialTokens(Collections.singletonList("familyName"));

		assertEquals(Arrays.asList("N:givenName"), project(template.getLines().get(0)));
		assertEquals(2, support.specialTokenReads.get());
	}

	@Test
	public void getLines_shouldNotRetokenizeWhenAnUnrelatedSupportIsReconfigured() {
		List<List<Map<String, String>>> first = template.getLines();

		// the version is per support, so reconfiguring one layout subsystem must not invalidate the
		// templates of another
		new StubLayoutSupport().setSpecialTokens(ADDRESS_TOKENS);

		assertSame(first, template.getLines());
		assertEquals(1, support.specialTokenReads.get());
	}

	@Test
	public void getMaxTokens_shouldReturnTheLongestLineWithoutAPriorGetLinesCall() {
		template = newTemplate(support, "givenName", "familyNamePrefix familyName familyName2 familyNameSuffix");

		assertEquals(8, template.getMaxTokens());
	}

	@Test
	public void getMaxTokens_shouldBeRecomputedWhenTheFormatChanges() {
		assertEquals(3, template.getMaxTokens());

		template.setLineByLineFormat(Arrays.asList("familyNamePrefix familyName familyName2 familyNameSuffix"));

		assertEquals(8, template.getMaxTokens());

		// the computed value is derived fresh from each format rather than kept as a high-water mark,
		// so a format with shorter lines lowers it again
		template.setLineByLineFormat(Arrays.asList("givenName"));

		assertEquals(2, template.getMaxTokens());
	}

	@Test
	public void getMaxTokens_shouldTreatAnExplicitlyConfiguredValueAsAFloor() {
		template.setMaxTokens(9);

		assertEquals(9, template.getMaxTokens());

		template.setMaxTokens(0);

		assertEquals(3, template.getMaxTokens());
	}

	@Test
	public void getLines_shouldOnlyEverPublishFullyBuiltLinesToConcurrentReaders() throws Exception {
		int threads = 8;
		List<List<String>> expected = projectLines(template.getLines());
		template.setLineByLineFormat(template.getLineByLineFormat());

		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch finished = new CountDownLatch(threads);
		List<Throwable> failures = new CopyOnWriteArrayList<>();

		for (int i = 0; i < threads; i++) {
			Thread thread = new Thread(() -> {
				try {
					start.await();
					for (int j = 0; j < 2000; j++) {
						assertEquals(expected, projectLines(template.getLines()));
					}
				} catch (Throwable t) {
					failures.add(t);
				} finally {
					finished.countDown();
				}
			});
			thread.setDaemon(true);
			thread.start();
		}

		start.countDown();
		assertTrue(finished.await(30, TimeUnit.SECONDS), "concurrent readers did not finish in time");
		assertTrue(failures.isEmpty(), () -> "concurrent readers saw inconsistent lines: " + failures);
	}

	@Test
	public void tokenizedLinesField_shouldBeTransientSoSerializersIgnoreIt() throws Exception {
		assertTrue(Modifier.isTransient(LayoutTemplate.class.getDeclaredField("tokenizedLines").getModifiers()));
	}

	@Test
	public void serialize_shouldNotIncludeTheCacheAndShouldRoundTripUnchanged() throws SerializationException {
		List<List<String>> before = projectLines(template.getLines());

		SimpleXStreamSerializer serializer = new SimpleXStreamSerializer();
		serializer.getXstream().allowTypeHierarchy(LayoutTemplate.class);
		String xml = serializer.serialize(template);

		assertFalse(xml.contains("tokenizedLines"), "cache leaked into the serialized form: " + xml);

		StubLayoutTemplate deserialized = serializer.deserialize(xml, StubLayoutTemplate.class);
		deserialized.support = support;

		assertEquals(before, projectLines(deserialized.getLines()));
	}

	/**
	 * Renders a tokenized line as "T:codeName" / "N:displayText" entries, so that a whole line can be
	 * compared in one assertion.
	 */
	private static List<String> project(List<Map<String, String>> line) {
		List<String> ret = new ArrayList<>(line.size());
		for (Map<String, String> entry : line) {
			boolean isToken = StubLayoutTemplate.LAYOUT_TOKEN_MARKER.equals(entry.get("isToken"));
			ret.add(isToken ? "T:" + entry.get("codeName") : "N:" + entry.get("displayText"));
		}
		return ret;
	}

	private static List<List<String>> projectLines(List<List<Map<String, String>>> lines) {
		List<List<String>> ret = new ArrayList<>(lines.size());
		for (List<Map<String, String>> line : lines) {
			ret.add(project(line));
		}
		return ret;
	}

	private static StubLayoutTemplate newTemplate(StubLayoutSupport support, String... lineByLineFormat) {
		StubLayoutTemplate template = new StubLayoutTemplate(support);
		template.setLineByLineFormat(new ArrayList<>(Arrays.asList(lineByLineFormat)));

		Map<String, String> nameMappings = new HashMap<>();
		Map<String, String> sizeMappings = new HashMap<>();
		// read the field rather than the getter, which counts tokenization passes
		for (String token : support.specialTokens) {
			nameMappings.put(token, "PersonName." + token);
			sizeMappings.put(token, "30");
		}
		template.setNameMappings(nameMappings);
		template.setSizeMappings(sizeMappings);
		return template;
	}

	public static class StubLayoutSupport extends LayoutSupport<StubLayoutTemplate> {

		/** counts tokenization passes: tokenizing reads the special tokens exactly once */
		private final AtomicInteger specialTokenReads = new AtomicInteger();

		@Override
		public List<String> getSpecialTokens() {
			specialTokenReads.incrementAndGet();
			return super.getSpecialTokens();
		}

		@Override
		public String getDefaultLayoutFormat() {
			return defaultLayoutFormat;
		}
	}

	public static class StubLayoutTemplate extends LayoutTemplate {

		public static final String LAYOUT_TOKEN_MARKER = "IS_STUB_TOKEN";

		private transient StubLayoutSupport support;

		public StubLayoutTemplate() {
		}

		public StubLayoutTemplate(StubLayoutSupport support) {
			this.support = support;
		}

		@Override
		public String getLayoutToken() {
			return LAYOUT_TOKEN_MARKER;
		}

		@Override
		public String getNonLayoutToken() {
			return "IS_NOT_STUB_TOKEN";
		}

		@Override
		public LayoutSupport<?> getLayoutSupportInstance() {
			return support;
		}
	}
}

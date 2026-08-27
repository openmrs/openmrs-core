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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;

/**
 * Tests how {@link Security#encodeStringArgon2(String)} derives its work factors from the
 * security.argon2.* runtime properties.
 * <p>
 * Deliberately a plain unit test (no Spring context): the argon2PasswordEncoder bean is
 * created once from these runtime properties at context startup, so the configurable
 * behaviour can only be exercised by manipulating the runtime properties directly and
 * forcing the cache to be re-read via {@link Security#resetEncoder()}.
 */
class SecurityArgon2ConfigTest {

	private Properties originalRuntimeProperties;

	@AfterEach
	void restoreRuntimeProperties() {
		Context.setRuntimeProperties(originalRuntimeProperties);
		Security.resetEncoder();
	}

	@Test
	void shouldUseDefaultsWhenNoRuntimePropertiesAreSet() {
		setRuntimeProperties(new Properties());
		String hash = Security.encodeStringArgon2("test");
		assertArgon2Params(hash, 65536, 3, 1);
	}

	@Test
	void shouldUseTheValuesFromTheRuntimeProperties() {
		Properties props = new Properties();
		props.setProperty("security.argon2.memory", "1024");
		props.setProperty("security.argon2.iterations", "2");
		props.setProperty("security.argon2.parallelism", "2");
		props.setProperty("security.argon2.saltLength", "16");
		props.setProperty("security.argon2.hashLength", "32");
		setRuntimeProperties(props);
		String hash = Security.encodeStringArgon2("test");
		assertArgon2Params(hash, 1024, 2, 2);
	}

	@Test
	void shouldClampValuesThatAreBelowTheAllowedMinimum() {
		Properties props = new Properties();
		props.setProperty("security.argon2.memory", "1");
		props.setProperty("security.argon2.iterations", "0");
		props.setProperty("security.argon2.parallelism", "0");
		props.setProperty("security.argon2.saltLength", "4");
		props.setProperty("security.argon2.hashLength", "2");
		setRuntimeProperties(props);
		String hash = Security.encodeStringArgon2("test");
		// memory is clamped to 8 * parallelism (1)
		assertArgon2Params(hash, 8, 1, 1);
	}

	@Test
	void shouldClampValuesThatExceedTheAllowedMaximum() {
		Properties props = new Properties();
		props.setProperty("security.argon2.iterations", "100");
		props.setProperty("security.argon2.parallelism", "16");
		props.setProperty("security.argon2.saltLength", "64");
		setRuntimeProperties(props);
		String hash = Security.encodeStringArgon2("test");
		assertArgon2Params(hash, 65536, 10, 8);
	}

	@Test
	void shouldUseDefaultsForNonNumericValues() {
		Properties props = new Properties();
		props.setProperty("security.argon2.memory", "large");
		props.setProperty("security.argon2.iterations", "many");
		props.setProperty("security.argon2.parallelism", "lots");
		props.setProperty("security.argon2.saltLength", "salty");
		props.setProperty("security.argon2.hashLength", "long");
		setRuntimeProperties(props);
		String hash = Security.encodeStringArgon2("test");
		assertArgon2Params(hash, 65536, 3, 1);
	}

	@Test
	void shouldClampMemoryToTheMaximumWhenExceeded() throws Exception {
		Properties props = new Properties();
		props.setProperty("security.argon2.memory", "2000000");
		props.setProperty("security.argon2.iterations", "1");
		setRuntimeProperties(props);
		// Load the cached configuration without encoding: an encode with 1 GiB of
		// Argon2 memory cost would exceed the surefire heap (-Xmx1g) and the CI runner.
		loadCachedConfig();
		assertEquals(1048576, cachedMemory());
		assertEquals(1, cachedIterations());
	}

	private void loadCachedConfig() throws Exception {
		Method load = Security.class.getDeclaredMethod("loadArgon2ConfigIfNecessary");
		load.setAccessible(true);
		load.invoke(null);
	}

	private int cachedMemory() throws Exception {
		Field field = Security.class.getDeclaredField("cachedMemory");
		field.setAccessible(true);
		return (Integer) field.get(null);
	}

	private int cachedIterations() throws Exception {
		Field field = Security.class.getDeclaredField("cachedIterations");
		field.setAccessible(true);
		return (Integer) field.get(null);
	}

	private void setRuntimeProperties(Properties props) {
		if (originalRuntimeProperties == null) {
			originalRuntimeProperties = Context.getRuntimeProperties();
		}
		Context.setRuntimeProperties(props);
		Security.resetEncoder();
	}

	private void assertArgon2Params(String hash, int memory, int iterations, int parallelism) {
		assertTrue(hash.startsWith("$argon2id$"), "Expected Argon2id PHC format, got: " + hash.substring(0, 20));
		String[] parts = hash.split("\\$");
		assertTrue(parts.length >= 5, "Unexpected PHC string layout: " + hash.substring(0, 20));
		String params = parts[3];
		assertTrue(params.contains("m=" + memory), "Expected m=" + memory + " in params, got: " + params);
		assertTrue(params.contains("t=" + iterations), "Expected t=" + iterations + " in params, got: " + params);
		assertTrue(params.contains("p=" + parallelism), "Expected p=" + parallelism + " in params, got: " + params);
	}
}
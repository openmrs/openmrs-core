/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Tests the {@code openmrsPasswordEncoder} bean as defined in applicationContext-service.xml,
 * under different {@code security.*} runtime properties.
 * <p>
 * Only the encoder beans are instantiated, so each test can use its own properties without
 * starting a second application context. The shared test harness does not support more than one
 * context per JVM.
 */
class OpenmrsPasswordEncoderBeanTest {

	private static final String PASSWORD = "password";

	@Test
	void shouldWriteLegacyHashesByDefault() {
		PasswordEncoder encoder = encoderBean(new Properties());

		String hash = encoder.encode(PASSWORD);
		assertFalse(hash.startsWith("{"), "expected an unprefixed legacy hash, got: " + hash);
		assertTrue(encoder.matches(PASSWORD, hash));
	}

	/**
	 * Hashes written while a site was opted in must keep authenticating if it opts back out.
	 */
	@Test
	void shouldVerifyArgon2HashesByDefault() {
		String argon2Hash = encoderBean(argon2OptIn()).encode(PASSWORD);
		PasswordEncoder encoder = encoderBean(new Properties());

		assertTrue(encoder.matches(PASSWORD, argon2Hash));
		assertFalse(encoder.matches("wrongPassword", argon2Hash));
	}

	@Test
	void shouldWriteArgon2HashesWhenOptedIn() {
		PasswordEncoder encoder = encoderBean(argon2OptIn());

		String hash = encoder.encode(PASSWORD);
		assertTrue(hash.startsWith("{argon2}$argon2id$"), "expected an argon2-prefixed PHC hash, got: " + hash);
		assertTrue(encoder.matches(PASSWORD, hash));
		assertFalse(encoder.matches("wrongPassword", hash));
		assertFalse(encoder.upgradeEncoding(hash));
	}

	@Test
	void shouldVerifyLegacyHashesWhenOptedIn() {
		PasswordEncoder encoder = encoderBean(argon2OptIn());
		String legacyHash = new LegacyOpenmrsPasswordEncoder().encode(PASSWORD);

		assertTrue(encoder.matches(PASSWORD, legacyHash));
		assertFalse(encoder.matches("wrongPassword", legacyHash));
		assertTrue(encoder.upgradeEncoding(legacyHash));
	}

	@Test
	void shouldHashWithTheConfiguredWorkFactors() {
		Properties properties = argon2OptIn();
		properties.setProperty("security.argon2.memory", "32768");
		properties.setProperty("security.argon2.iterations", "3");
		properties.setProperty("security.argon2.parallelism", "2");

		assertEquals("m=32768,t=3,p=2", workFactorsOf(encoderBean(properties).encode(PASSWORD)));
	}

	/**
	 * {@link PasswordEncoders#supportedEncoders()} hard-codes the work factors the bean otherwise
	 * takes from its property defaults, so the two have to be kept in step.
	 */
	@Test
	void shouldDefaultToTheSameWorkFactorsAsTheNoContextEncoder() {
		PasswordEncoder noContext = PasswordEncoders.supportedEncoders().get(PasswordEncoders.ARGON2_ID);

		assertEquals(workFactorsOf(noContext.encode(PASSWORD)), workFactorsOf(encoderBean(argon2OptIn()).encode(PASSWORD)));
	}

	@Test
	void shouldFailToCreateTheBeanForAnInvalidWorkFactor() {
		Properties properties = new Properties();
		properties.setProperty("security.argon2.memory", "0");

		assertThrows(BeanCreationException.class, () -> encoderBean(properties));
	}

	private static Properties argon2OptIn() {
		Properties properties = new Properties();
		properties.setProperty("security.passwordEncoder", PasswordEncoders.ARGON2_ID);
		return properties;
	}

	/**
	 * Builds {@code openmrsPasswordEncoder} and its dependencies from applicationContext-service.xml.
	 * A bare bean factory only parses the other definitions, so nothing else in the file is started.
	 */
	private static PasswordEncoder encoderBean(Properties properties) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		new XmlBeanDefinitionReader(beanFactory).loadBeanDefinitions(new ClassPathResource("applicationContext-service.xml"));
		beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver());

		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		configurer.setProperties(properties);
		configurer.postProcessBeanFactory(beanFactory);

		return beanFactory.getBean("openmrsPasswordEncoder", PasswordEncoder.class);
	}

	/**
	 * @return the parameter segment of a PHC string, e.g. {@code m=19456,t=2,p=1}
	 */
	private static String workFactorsOf(String phcHash) {
		int start = phcHash.indexOf("v=19$") + "v=19$".length();
		return phcHash.substring(start, phcHash.indexOf('$', start));
	}
}

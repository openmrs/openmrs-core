/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.notification.mail.velocity;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openmrs.notification.Message;
import org.openmrs.notification.MessagePreparator;
import org.openmrs.notification.Template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests for {@link VelocityMessagePreparator}, in particular that the underlying Velocity engine is
 * hardened against server-side template injection.
 */
public class VelocityMessagePreparatorTest {

	/**
	 * The engine must be configured with the SecureUberspector so that a template cannot use reflection
	 * to obtain {@link Runtime} (the classic Velocity SSTI -&gt; RCE primitive). With the default
	 * introspector {@code $foo.class.forName('java.lang.Runtime').getRuntime()} resolves to a live
	 * Runtime instance; the secure introspector blocks the reflective method calls so the reference
	 * cannot resolve.
	 *
	 * @see VelocityMessagePreparator#prepare(Template)
	 */
	@Test
	public void prepare_shouldNotAllowReflectiveAccessToRuntimeInTemplate() throws Exception {
		MessagePreparator preparator = new VelocityMessagePreparator();

		Map<String, Object> data = new HashMap<>();
		data.put("foo", "bar");

		Template template = new Template();
		template.setData(data);
		template.setTemplate("#set($r=$foo.class.forName('java.lang.Runtime').getRuntime())[$r]");

		Message message = preparator.prepare(template);

		assertFalse(message.getContent().contains("java.lang.Runtime"),
		    "SecureUberspector must block reflective access to java.lang.Runtime, but rendered: " + message.getContent());
	}

	/**
	 * Hardening the engine must not break legitimate variable substitution.
	 *
	 * @see VelocityMessagePreparator#prepare(Template)
	 */
	@Test
	public void prepare_shouldStillSubstituteOrdinaryVariables() throws Exception {
		MessagePreparator preparator = new VelocityMessagePreparator();

		Map<String, Object> data = new HashMap<>();
		data.put("name", "World");

		Template template = new Template();
		template.setData(data);
		template.setTemplate("Hello $name!");

		Message message = preparator.prepare(template);

		assertEquals("Hello World!", message.getContent());
	}
}

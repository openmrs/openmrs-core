/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.ContextMockHelper;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.module.ModuleException;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockServletContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class WebDaemonTest {

	@Test
	public void startOpenmrs_shouldThrowExceptionGivenNull() {
		LoggerFactory.getLogger(WebDaemonTest.class); //see TRUNK-6307
		assertThrows(ModuleException.class, () -> WebDaemon.startOpenmrs(null));
	}

	@Test
	public void startOpenmrs_shouldStartOpenmrsWithTheThreadContextOfItsCaller() {
		// the daemon thread opens a session before it starts OpenMRS
		ContextMockHelper contextMockHelper = new ContextMockHelper();
		contextMockHelper.setContextDAO(mock(ContextDAO.class));
		Map<String, String> seen = new ConcurrentHashMap<>();
		MockServletContext servletContext = new MockServletContext() {

			@Override
			public String getRealPath(String path) {
				// asked for first while OpenMRS starts, on the thread that starts it
				seen.put("thread", Thread.currentThread().getName());
				seen.put("context", String.valueOf(ThreadContext.get("webDaemonTest")));
				throw new IllegalStateException("stopped as OpenMRS began to start");
			}
		};
		try {
			ThreadContext.put("webDaemonTest", "caller");
			try {
				assertThrows(ModuleException.class, () -> WebDaemon.startOpenmrs(servletContext));
			} finally {
				ThreadContext.remove("webDaemonTest");
			}

			assertNotNull(seen.get("thread"));
			assertNotEquals(Thread.currentThread().getName(), seen.get("thread"));
			assertEquals("caller", seen.get("context"));

			// and leaves it behind once it is done, as a caller without it then starts OpenMRS
			assertThrows(ModuleException.class, () -> WebDaemon.startOpenmrs(servletContext));
			assertEquals("null", seen.get("context"));
		} finally {
			contextMockHelper.revertMocks();
		}
	}
}

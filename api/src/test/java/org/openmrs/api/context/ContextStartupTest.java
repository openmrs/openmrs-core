/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.context;

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.module.ModuleUtil;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Context#startup(Properties)}.
 */
public class ContextStartupTest {

	private ContextDAO previousDao;

	private AdministrationService previousAdministrationService;

	@BeforeEach
	public void snapshotContextState() {
		try {
			previousDao = Context.getContextDAO();
		} catch (Exception e) {
			previousDao = null;
		}

		previousAdministrationService = null;
		if (ServiceContext.isInstantiated()) {
			try {
				previousAdministrationService = ServiceContext.getInstance().getService(AdministrationService.class);
			} catch (Exception e) {
				// no administration service was registered before this test
			}
		}
	}

	@AfterEach
	public void restoreContextState() {
		try {
			if (Context.isSessionOpen()) {
				Context.closeSession();
			}
		} finally {
			Context.setDAO(previousDao);
			if (previousAdministrationService != null) {
				ServiceContext.getInstance().setService(AdministrationService.class, previousAdministrationService);
			} else if (ServiceContext.isInstantiated()) {
				// drop the singleton that this test populated so the mock administration service
				// does not leak into any other test running in the same JVM
				ServiceContext.destroyInstance();
			}
		}
	}

	/**
	 * The search index must be (re)built before any shared schema upgrade runs (TRUNK-5731), and it
	 * must run inside an open session: the real implementation adds a proxy privilege, which resolves
	 * through {@link Context#getUserContext()} and fails when no session has been opened.
	 */
	@Test
	public void startup_shouldBuildSearchIndexInsideAnOpenSessionBeforeCoreSetupOnVersionChange() throws Exception {
		ContextDAO dao = mock(ContextDAO.class);
		AdministrationService administrationService = mock(AdministrationService.class);
		when(administrationService.isCoreSetupOnVersionChangeNeeded()).thenReturn(true);

		doAnswer(invocation -> {
			assertNotNull(Context.getUserContext(), "setupSearchIndex() must run inside an open session");
			return null;
		}).when(dao).setupSearchIndex();

		ServiceContext.getInstance().setService(AdministrationService.class, administrationService);
		Context.setDAO(dao);

		try (MockedStatic<ModuleUtil> moduleUtil = mockStatic(ModuleUtil.class)) {
			Context.startup(new Properties());
		}

		InOrder inOrder = inOrder(dao, administrationService);
		inOrder.verify(dao).setupSearchIndex();
		inOrder.verify(administrationService).isCoreSetupOnVersionChangeNeeded();
		inOrder.verify(administrationService).runCoreSetupOnVersionChange();
	}
}

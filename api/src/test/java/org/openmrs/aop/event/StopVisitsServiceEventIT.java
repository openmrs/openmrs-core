/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.aop.event;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StopVisitsServiceEventIT extends BaseContextSensitiveTest {

	@Autowired
	private ServiceEventTestListener serviceEventTestListener;

	private VisitService visitService;

	@BeforeEach
	public void setUp() {
		visitService = Context.getVisitService();
		serviceEventTestListener.clearVisitSaveEvents();
	}

	@Test
	public void stopVisits_shouldPublishSaveServiceEventForEachClosedVisit() {
		executeDataSet("org/openmrs/api/include/VisitServiceTest-includeVisitsAndTypeToAutoClose.xml");
		String[] visitTypeNames = StringUtils.split(
		    Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GP_VISIT_TYPES_TO_AUTO_CLOSE), ",");

		String openVisitsQuery = "SELECT visit_id FROM visit WHERE voided = false AND date_stopped IS NULL AND visit_type_id IN (SELECT visit_type_id FROM visit_type WHERE NAME IN ('"
		        + StringUtils.join(visitTypeNames, "','") + "'))";
		int activeVisitCount = Context.getAdministrationService().executeSQL(openVisitsQuery, true).size();
		assertTrue(activeVisitCount > 0);

		serviceEventTestListener.clearVisitSaveEvents();
		visitService.stopVisits(null);

		assertEquals(activeVisitCount, serviceEventTestListener.getVisitSaveEvents().size());
		for (SaveServiceEvent<Visit> event : serviceEventTestListener.getVisitSaveEvents()) {
			assertNotNull(event.getEntity().getStopDatetime());
		}
	}
}

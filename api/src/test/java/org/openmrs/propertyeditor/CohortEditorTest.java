/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.propertyeditor;

import org.junit.jupiter.api.BeforeEach;
import org.openmrs.Cohort;
import org.openmrs.api.CohortService;
import org.springframework.beans.factory.annotation.Autowired;

public class CohortEditorTest extends BasePropertyEditorTest<Cohort, CohortEditor> {
	
	protected static final String COHORT_XML = "org/openmrs/api/include/CohortServiceTest-cohort.xml";
	
	private static final Integer EXISTING_ID = 1;
	
	@Autowired
	private CohortService cohortService;
	
	@BeforeEach
	public void prepareData() {
		executeDataSet(COHORT_XML);
	}
	
	@Override
	protected CohortEditor getNewEditor() {
		return new CohortEditor();
	}
	
	@Override
	protected Cohort getExistingObject() {
		return cohortService.getCohort(EXISTING_ID);
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.controller.concept;

import org.hibernate.ObjectNotFoundException;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;

/**
 * Tests against the {@link ConceptDrugFormController}
 */
public class ConceptDrugFormControllerTest extends BaseWebContextSensitiveTest {
	
	/**
	 * @verifies should purge the concept drug
	 * @see org.openmrs.web.controller.concept.ConceptDrugFormController#onSubmit(javax.servlet.http.HttpServletRequest,
	 * 		javax.servlet.http.HttpServletResponse, Object, org.springframework.validation.BindingResult)
	 */
	@Test
	public void onSubmit_shouldPurgeConceptDrug() throws Exception {
		executeDataSet("org/openmrs/api/include/ConceptServiceTest-drugSearch.xml");
		ConceptService service = Context.getConceptService();
		ConceptDrugFormController controller = (ConceptDrugFormController) applicationContext
		        .getBean("conceptDrugForm");
		
		Integer drugId = new Integer(444);
		Drug drug = service.getDrug(drugId);
		org.junit.Assert.assertEquals(drugId, drug.getDrugId());
		
		MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
		MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
		BindException errors = new BindException(drug, "drug");
		mockHttpServletRequest.setParameter("purgeDrug", String.valueOf(drugId));
		
		controller.onSubmit(mockHttpServletRequest, mockHttpServletResponse, drug, errors);
		Context.flushSession();
		org.junit.Assert.assertNull(service.getDrug(drugId));
	}
}
/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_9;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

import static org.hamcrest.Matchers.is;

/**
 * Tests functionality of {@link ConceptController}.
 */
public class ConceptController1_9Test extends MainResourceControllerTest {
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "concept";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_9.CONCEPT_UUID;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getAllCount()
	 */
	@Override
	public long getAllCount() {
		return 24;
	}
	
	@Test
	public void shouldFindConceptsBySourceUuid() throws Exception {
		SimpleObject response = deserialize(handle(newGetRequest(getURI(), new Parameter("term", "SSTRM-WGT234"))));
		List<Object> results = Util.getResultsList(response);
		
		Assert.assertEquals(1, results.size());
		Object next = results.iterator().next();
		Assert.assertThat((String) PropertyUtils.getProperty(next, "uuid"), is("c607c80f-1ea9-4da3-bb88-6276ce8868dd"));
	}
	
	@Test
	public void shouldFindNumericConceptsByQueryString() throws Exception {
		executeDataSet("numericConcept.xml");
		SimpleObject response = deserialize(handle(newGetRequest(getURI(), new Parameter("q", "HEIGHT"), new Parameter("v",
		        "full"))));
		List<Object> results = Util.getResultsList(response);
		
		Assert.assertEquals(1, results.size());
		Object next = results.iterator().next();
		Assert.assertThat((String) PropertyUtils.getProperty(next, "uuid"), is("568b58c8-e878-11e0-950d-00248140a5e3"));
	}
	
	@Test
	public void shouldFindConceptByReferenceTerm() throws Exception {
		executeDataSet("customConceptDataset1_9.xml");
		MockHttpServletRequest req = request(RequestMethod.GET, getURI() + "/" + "CIEL:WGT234");
		SimpleObject result = deserialize(handle(req));
		Assert.assertThat((String) PropertyUtils.getProperty(result, "uuid"), is("c607c80f-1ea9-4da3-bb88-6276ce8868dd"));
	}
	
}

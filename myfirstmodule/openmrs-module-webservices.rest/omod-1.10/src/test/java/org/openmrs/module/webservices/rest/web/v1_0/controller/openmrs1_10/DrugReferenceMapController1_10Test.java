/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_10;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Drug;
import org.openmrs.DrugReferenceMap;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_10;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;

public class DrugReferenceMapController1_10Test extends MainResourceControllerTest {
	
	private ConceptService conceptService;
	
	@Before
	public void before() throws Exception {
		conceptService = Context.getConceptService();
		executeDataSet(RestTestConstants1_10.DRUG_REFERENCE_MAP_TEST_DATASET);
	}
	
	@Override
	public String getURI() {
		return "/drugreferencemap";
	}
	
	@Override
	public String getUuid() {
		return RestTestConstants1_10.DRUG_REFERENCE_MAP_UUID;
	}
	
	@Override
	public long getAllCount() {
		return 0;
	}
	
	@Test
	public void shouldCreateNewDruReferenceMap() throws Exception {
		final String JSON = "{\n" + "  \"conceptReferenceTerm\":\"" + RestTestConstants1_10.CONCEPT_REF_TERM_UUID + "\"\n"
		        + ",\n" + "  \"conceptMapType\": \"" + RestTestConstants1_10.CONCEPT_MAP_TYPE_UUID + "\"\n" + ",\n"
		        + "  \"drug\": \"" + RestTestConstants1_10.DRUG_REFERENCE_DRUG_UUID + "\"\n" + "}";
		
		Drug drug = conceptService.getDrugByUuid(RestTestConstants1_10.DRUG_REFERENCE_DRUG_UUID);
		Set<DrugReferenceMap> maps = drug.getDrugReferenceMaps();
		Assert.assertEquals(1, maps.size());
		
		MockHttpServletRequest req = newPostRequest(getURI(), JSON);
		MockHttpServletResponse resp = handle(req);
		Drug updatedDrug = conceptService.getDrugByUuid(RestTestConstants1_10.DRUG_REFERENCE_DRUG_UUID);
		Set<DrugReferenceMap> updatedMaps = updatedDrug.getDrugReferenceMaps();
		Assert.assertEquals(2, updatedMaps.size());
		
		SimpleObject result = deserialize(resp);
		Assert.assertEquals(RestTestConstants1_10.CONCEPT_REF_TERM_UUID,
		    Util.getByPath(result, "conceptReferenceTerm/uuid"));
		Assert.assertEquals(RestTestConstants1_10.CONCEPT_MAP_TYPE_UUID, Util.getByPath(result, "conceptMapType/uuid"));
		Assert.assertEquals(RestTestConstants1_10.DRUG_REFERENCE_DRUG_UUID, Util.getByPath(result, "drug/uuid"));
		Assert.assertEquals("Panadol - concept_map_type2", Util.getByPath(result, "display"));
	}
	
	@Test
	public void shouldGetAdrugReferenceMapByUuid() throws Exception {
		MockHttpServletRequest req = request(RequestMethod.GET,
		    getURI() + "/" + RestTestConstants1_10.DRUG_REFERENCE_MAP_UUID);
		SimpleObject result = deserialize(handle(req));
		
		Assert.assertEquals(RestTestConstants1_10.DRUG_REFERENCE_MAP_UUID, Util.getByPath(result, "uuid"));
		Assert.assertEquals(RestTestConstants1_10.CONCEPT_MAP_TYPE_UUID2, Util.getByPath(result, "conceptMapType/uuid"));
		Assert.assertEquals(RestTestConstants1_10.DRUG_REFERENCE_DRUG_UUID, Util.getByPath(result, "drug/uuid"));
		Assert.assertEquals("Panadol - concept_map_type", Util.getByPath(result, "display"));
	}
	
	@Override
	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.OrderFrequency;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_10;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class OrderFrequencyResource1_10Test extends BaseDelegatingResourceTest<OrderFrequencyResource1_10, OrderFrequency> {
	
	@Autowired
	ConceptService conceptService;
	
	@Override
	public OrderFrequency newObject() {
		return Context.getOrderService().getOrderFrequencyByUuid(getUuidProperty());
	}
	
	@Override
	public String getDisplayProperty() {
		return "1/day x 7 days/week";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_10.ORDER_FREQUENCY_UUID;
	}
	
	@Test
	public void testFullConceptRepresentation() throws Exception {
		SimpleObject rep = getResource().asRepresentation(getObject(), new NamedRepresentation("fullconcept"));
		SimpleObject concept = (SimpleObject) rep.get("concept");
		List names = (List) concept.get("names");
		SimpleObject name = (SimpleObject) names.get(0);
		assertThat(name.get("locale"), notNullValue());
	}
	
	@Test
	public void testGetByUniqueIdWorksWithConceptMappings() throws Exception {
		ConceptSource snomed = conceptService.getConceptSource(2);
		ConceptReferenceTerm term = new ConceptReferenceTerm(snomed, "307486002", null);
		conceptService.saveConceptReferenceTerm(term);
		
		Concept concept = conceptService.getConcept(113);
		concept.addConceptMapping(new ConceptMap(term, conceptService.getConceptMapType(2)));
		conceptService.saveConcept(concept);
		
		OrderFrequency orderFrequency = getResource().getByUniqueId("SNOMED CT:307486002");
		assertThat(orderFrequency.getConcept(), is(concept));
	}
}

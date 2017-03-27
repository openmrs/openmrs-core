/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.attribute;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.test.BaseContextSensitiveTest;

/**
 * Integration tests for using {@link BaseAttribute}, {@link BaseAttributeType}, and {@link AttributeHandler}
 * in concert.
 */
public class AttributeIntegrationTest extends BaseContextSensitiveTest {
	
	VisitService service;
	
	@Before
	public void before() {
		service = Context.getVisitService();
	}
	
	@Test
	public void shouldTestAddingAnAttributeToSomethingAndSavingIt() throws InvalidCustomValueException, ParseException {
		Visit visit = service.getVisit(1);
		VisitAttributeType auditDate = service.getVisitAttributeType(1);
		
		VisitAttribute legalDate = new VisitAttribute();
		legalDate.setAttributeType(auditDate);
		// try using a subclass of java.util.Date, to make sure the handler can take subclasses.
		legalDate.setValue(new java.sql.Date(new SimpleDateFormat("yyyy-MM-dd").parse("2011-04-15").getTime()));
		visit.addAttribute(legalDate);
		
		service.saveVisit(visit);
		
		// saving the visit should have caused the date to be validated and saved
		Assert.assertNotNull(legalDate.getValueReference());
		Assert.assertEquals("2011-04-15", legalDate.getValueReference());
		
		VisitAttribute badDate = new VisitAttribute();
		badDate.setAttributeType(auditDate);
		// no value
		visit.addAttribute(badDate);
		
		try {
			service.saveVisit(visit);
			Assert.fail("Should have failed because of bad date attribute");
		}
		catch (APIException ex) {
			// expected this
		}
	}
	
}

/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.validator;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.api.APIException;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;
import org.springframework.validation.Errors;

public class VisitValidatorTest extends BaseContextSensitiveTest {
	
	protected static final String DATA_XML = "org/openmrs/validator/include/VisitValidatorTest.xml";
	
	private VisitService service;
	
	@Before
	public void before() throws Exception {
		executeDataSet(DATA_XML);
		service = Context.getVisitService();
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 * @verifies accept a visit that has the right number of attribute occurrences
	 */
	@Test
	public void validate_shouldAcceptAVisitThatHasTheRightNumberOfAttributeOccurrences() throws Exception {
		Visit visit = makeVisit();
		visit.addAttribute(makeAttribute("one"));
		visit.addAttribute(makeAttribute("two"));
		ValidateUtil.validate(visit);
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 * @verifies reject a visit if it has fewer than min occurs of an attribute
	 */
	@Test(expected = APIException.class)
	public void validate_shouldRejectAVisitIfItHasFewerThanMinOccursOfAnAttribute() throws Exception {
		Visit visit = makeVisit();
		visit.addAttribute(makeAttribute("one"));
		ValidateUtil.validate(visit);
	}
	
	/**
	 * @see VisitValidator#validate(Object,Errors)
	 * @verifies reject a visit if it has more than max occurs of an attribute
	 */
	@Test(expected = APIException.class)
	public void validate_shouldRejectAVisitIfItHasMoreThanMaxOccursOfAnAttribute() throws Exception {
		Visit visit = makeVisit();
		visit.addAttribute(makeAttribute("one"));
		visit.addAttribute(makeAttribute("two"));
		visit.addAttribute(makeAttribute("three"));
		visit.addAttribute(makeAttribute("four"));
		ValidateUtil.validate(visit);
	}
	
	private Visit makeVisit() {
		Visit visit = new Visit();
		visit.setPatient(Context.getPatientService().getPatient(2));
		visit.setStartDatetime(new Date());
		visit.setVisitType(service.getVisitType(1));
		return visit;
	}
	
	private VisitAttribute makeAttribute(String serializedValue) {
		VisitAttribute attr = new VisitAttribute();
		attr.setAttributeType(service.getVisitAttributeType(1));
		attr.setSerializedValue(serializedValue);
		return attr;
	}
	
}

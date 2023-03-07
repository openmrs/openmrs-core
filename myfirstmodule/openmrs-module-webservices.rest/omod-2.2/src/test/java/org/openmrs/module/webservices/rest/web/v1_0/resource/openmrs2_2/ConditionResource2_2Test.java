/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_2;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Condition;
import org.openmrs.ConditionClinicalStatus;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Patient;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.v1_0.RestTestConstants2_2;

import java.util.Date;
import java.util.Locale;

public class ConditionResource2_2Test extends BaseDelegatingResourceTest<ConditionResource2_2, Condition> {
	
	@Before
	public void before() throws Exception {
		executeDataSet(RestTestConstants2_2.CONDITION_TEST_DATA_XML);
	}
	
	@Override
	public Condition newObject() {
		return Context.getConditionService().getConditionByUuid(getUuidProperty());
	}
	
	@Override
	public String getDisplayProperty() {
		return "Concept 1";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants2_2.CODED_CONDITION_UUID;
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropPresent("condition");
		assertPropEquals("clinicalStatus", getObject().getClinicalStatus());
		assertPropEquals("verificationStatus", getObject().getVerificationStatus());
		assertPropEquals("previousVersion", getObject().getPreviousVersion());
		assertPropEquals("onsetDate", getObject().getOnsetDate());
		assertPropEquals("endDate", getObject().getEndDate());
		assertPropEquals("voided", getObject().getVoided());
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropPresent("condition");
		assertPropEquals("clinicalStatus", getObject().getClinicalStatus());
		assertPropEquals("verificationStatus", getObject().getVerificationStatus());
		assertPropEquals("previousVersion", getObject().getPreviousVersion());
		assertPropEquals("onsetDate", getObject().getOnsetDate());
		assertPropEquals("endDate", getObject().getEndDate());
		assertPropEquals("additionalDetail", getObject().getAdditionalDetail());
		assertPropEquals("voided", getObject().getVoided());
	}
	
	@Test
	public void testDisplayProperty() {
		Locale definedNameLocale = new Locale("en", "US");
		Concept concept = new Concept();
		ConceptName fullySpecifiedName = new ConceptName("some name", definedNameLocale);
		fullySpecifiedName.setConceptNameId(1);
		fullySpecifiedName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		fullySpecifiedName.setLocalePreferred(false);
		concept.addName(fullySpecifiedName);
		
		CodedOrFreeText codedOrFreeText = new CodedOrFreeText(concept, fullySpecifiedName, "");
		ConditionClinicalStatus clinicalStatus = ConditionClinicalStatus.ACTIVE;
		ConditionVerificationStatus verificationStatus = ConditionVerificationStatus.CONFIRMED;
		Patient patient = new Patient(2);
		Date onsetDate = new Date();
		String additionalDetail = "additionalDetail";
		int conditionId = 20;
		
		Condition condition = new Condition();
		condition.setConditionId(conditionId);
		condition.setCondition(codedOrFreeText);
		condition.setClinicalStatus(clinicalStatus);
		condition.setVerificationStatus(verificationStatus);
		condition.setAdditionalDetail(additionalDetail);
		condition.setOnsetDate(onsetDate);
		condition.setPatient(patient);
		
		ConditionResource2_2 resource = new ConditionResource2_2();
		String result = resource.getDisplayString(condition);
		
		Assert.assertEquals("some name", result);
	}
}

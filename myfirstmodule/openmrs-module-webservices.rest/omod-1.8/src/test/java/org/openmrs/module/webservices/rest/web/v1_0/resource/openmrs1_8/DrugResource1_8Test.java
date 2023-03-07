/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_8;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.springframework.util.Assert;

public class DrugResource1_8Test extends BaseDelegatingResourceTest<DrugResource1_8, Drug> {
	
	@Override
	public Drug newObject() {
		return Context.getConceptService().getDrugByUuid(getUuidProperty());
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("retired", getObject().isRetired());
		assertPropEquals("doseStrength", getObject().getDoseStrength());
		assertPropEquals("maximumDailyDose", getObject().getMaximumDailyDose());
		assertPropEquals("minimumDailyDose", getObject().getMinimumDailyDose());
		assertPropEquals("units", getObject().getUnits());
		assertPropEquals("combination", getObject().getCombination());
		assertPropPresent("concept");
		assertPropPresent("route");
		assertPropPresent("dosageForm");
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("name", getObject().getName());
		assertPropEquals("description", getObject().getDescription());
		assertPropEquals("retired", getObject().isRetired());
		assertPropEquals("doseStrength", getObject().getDoseStrength());
		assertPropEquals("maximumDailyDose", getObject().getMaximumDailyDose());
		assertPropEquals("minimumDailyDose", getObject().getMinimumDailyDose());
		assertPropEquals("units", getObject().getUnits());
		assertPropEquals("combination", getObject().getCombination());
		assertPropPresent("concept");
		assertPropPresent("route");
		assertPropPresent("dosageForm");
		assertPropPresent("auditInfo");
	}
	
	@Override
	public String getDisplayProperty() {
		return "Aspirin";
	}
	
	@Override
	public String getUuidProperty() {
		return RestTestConstants1_8.DRUG_UUID;
	}
	
	/**
	 * Tests
	 * {@link BaseDelegatingResource#setConvertedProperties(Object, java.util.Map, DelegatingResourceDescription, boolean)}
	 */
	@Test
	public void setConvertedProperties_shouldAllowSettingANullValue() {
		DrugResource1_8 resource = new DrugResource1_8();
		Drug drug = new Drug();
		drug.setRoute(new Concept());
		Map<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("route", null);
		resource.setConvertedProperties(drug, propertyMap, resource.getUpdatableProperties(), false);
		Assert.isNull(drug.getRoute());
	}
	
}

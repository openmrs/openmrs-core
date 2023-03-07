/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11;

import org.openmrs.DrugIngredient;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;

public class DrugIngredientResource1_11Test extends BaseDelegatingResourceTest<DrugIngredientResource1_11, DrugIngredient> {
	
	@Override
	public DrugIngredient newObject() {
		DrugIngredient ingredient = new DrugIngredient();
		ingredient.setIngredient(Context.getConceptService().getConcept(3));
		ingredient.setUuid(getUuidProperty());
		ingredient.setStrength(2d);
		ingredient.setUnits(Context.getConceptService().getConcept(8));
		return ingredient;
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("uuid", getObject().getUuid());
		assertPropEquals("strength", getObject().getStrength());
		assertPropPresent("ingredient");
		assertPropPresent("units");
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("uuid", getObject().getUuid());
		assertPropEquals("strength", getObject().getStrength());
		assertPropPresent("ingredient");
		assertPropPresent("units");
	}
	
	@Override
	public String getUuidProperty() {
		return "e322c90c-7cd9-4e25-bdff-f21d387759b6";
	}
	
	@Override
	public String getDisplayProperty() {
		return "COUGH SYRUP";
	}
}

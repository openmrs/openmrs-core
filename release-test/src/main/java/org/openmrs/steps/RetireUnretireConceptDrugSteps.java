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
package org.openmrs.steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;

import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.*;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

public class RetireUnretireConceptDrugSteps extends Steps {

	public RetireUnretireConceptDrugSteps(WebDriver driver) {
		super(driver);
	}

	@Given("I login to the openmrs application")
	public void logIn() {
		assertPresenceOf(link().with(text(equalTo("Log out"))));
	}

	@Given("I navigate to the the administration page")
	public void navigateToAdminUrl() {
		clickOn(link().with(text(equalTo("Administration"))));
	}



	@When("I edit a concept drug")
	public void navigateToEditConceptDrugUrl() {
        waitAndClickOn(link().with(text(equalTo("d4T-30"))));

	}

	@When("I provide a retire reason such as $retireReason")
	public void giveRetireReason(String retireReason) {
		type(retireReason, into(textbox().with(attribute("name", equalTo("retireReason")))));
	}

	@When("I retire the concept drug")
	public void retireConceptDrug() {
		clickOn(button().with(attribute("name", equalTo("retireDrug"))));
	}

	@Then("the concept drug should get retired")
	public void verifyConceptDrug() {
		assertPresenceOf(div().with(text(equalTo("Drug retired successfully"))));
	}
}

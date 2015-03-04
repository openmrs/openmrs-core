/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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

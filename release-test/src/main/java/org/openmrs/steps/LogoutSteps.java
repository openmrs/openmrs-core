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
import static org.openqa.selenium.lift.Finders.button;
import static org.openqa.selenium.lift.Finders.link;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

public class LogoutSteps extends Steps {

	public LogoutSteps(WebDriver driver) {
		super(driver);
	}

	@Given("I am already logged into openmrs")
	public void alreadyLoggedIn() {
		assertPresenceOf(link().with(text(equalTo("Log out"))));
	}

	@When("I click on link Log out")
	public void logOut() {
		clickOn(link().with(text(equalTo("Log out"))));
	}

	@Then("I must navigate to login page")
	public void verifyPage() {
		waitFor(button().with(attribute("value", equalTo("Log In"))));
	}
}

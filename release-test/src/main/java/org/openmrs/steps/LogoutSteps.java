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

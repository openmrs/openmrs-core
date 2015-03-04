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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.div;
import static org.openqa.selenium.lift.Finders.link;
import static org.openqa.selenium.lift.Finders.title;
import static org.openqa.selenium.lift.Matchers.text;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;

public class ViewServerLogSteps extends Steps {
	public ViewServerLogSteps(WebDriver driver) {
		super(driver);
	}

	@Given("I am on Admin page")
	public void iAmOnAdminPage() {
		assertPresenceOf(title().with(
				text(equalTo("OpenMRS - " + "Administration"))));
	}

	@When("I click on View Server Log")
	public void clickViewServerLog() {
		clickOn(link().with(text(equalTo("View Server Log"))));
	}

	@Then("take me to Server Log")
	public void verifyServerLog() {
		assertPresenceOf(link().with(text(equalTo("Set Implementation Id"))));
		assertPresenceOf(div().with(text(containsString("Server Log"))));
	}
}

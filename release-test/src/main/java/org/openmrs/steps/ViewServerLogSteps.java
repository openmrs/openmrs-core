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

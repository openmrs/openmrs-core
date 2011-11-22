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

import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.button;
import static org.openqa.selenium.lift.Finders.div;
import static org.openqa.selenium.lift.Finders.link;
import static org.openqa.selenium.lift.Finders.radioButton;
import static org.openqa.selenium.lift.Finders.textbox;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;

public class UnretireLocationSteps extends Steps {

	public UnretireLocationSteps(WebDriver driver) {
		super(driver);
	}

	@Given("The location was retired")
	public void verifyRetiredLocation() {
		assertPresenceOf(div().with(text(equalTo("Location retired successfully"))));
	}

	@When("I want to unretire the retired location")
	public void displayRetiredLocation() {
		clickOn(link().with(text(equalTo("Toggle Retired"))));
	}

	@When("I choose to edit the retired location")
	public void navigateToEditLocationUrl() {
		String locationXpath = "//table[@id = 'locationTable']/tbody/tr[last()]/td[2]/a"; //html/body/div/div[3]/div[2]/table/tbody/tr[3]/td/a
		waitFor(finderByXpath(locationXpath));
		clickOn(finderByXpath(locationXpath));
	}

	@When("I unretire the location")
	public void unretireLocation() {
		clickOn(button().with(attribute("name", equalTo("unretireLocation"))));
	}

	@Then("the location should get unretired")
	public void verifyLocation() {
		assertPresenceOf(div().with(text(equalTo("Location unretired successfully"))));
	}
}

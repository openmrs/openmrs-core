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

public class UnretireConceptDrugSteps extends Steps {

	public UnretireConceptDrugSteps(WebDriver driver) {
		super(driver);
	}

	@Given("The new concept drug was retired")
	public void verifyRetiredDrug() {
		assertPresenceOf(div().with(text(equalTo("Drug retired successfully"))));
	}

	@When("I want to unretire the retired drug")
	public void displayRetiredDrug() {
		clickOn(link().with(text(equalTo("Toggle Retired"))));
	}

	@When("I choose to edit the retired drug")
	public void selectRetiredDrug() {
		String drugXpath = "//table[@id = 'drugTable']/tbody/tr[2]/td[1]/a"; //html/body/div/div[3]/div[2]/table/tbody/tr[2]/td/a
		waitFor(finderByXpath(drugXpath));
		clickOn(finderByXpath(drugXpath));
	}

	@When("I unretire the drug")
	public void unretireDrug() {
		clickOn(button().with(attribute("name", equalTo("unretireDrug"))));
	}

	@Then("the concept drug should get unretired")
	public void verifyConceptDrug() {
		assertPresenceOf(div().with(text(equalTo("Drug unretired successfully"))));
	}
}

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

public class CreateEncounterTypeSteps extends Steps {

	public CreateEncounterTypeSteps(WebDriver driver) {
		super(driver);
	}

	@Given("I login to the openmrs application")
	public void logIn() {
		assertPresenceOf(link().with(text(equalTo("Log out"))));
	}

	@Given("I navigate to the the administration page")
	public void navigateToAdminUrl() {
		clickOn(link("Administration"));
	}

	@When("I choose to click $linktext")
	public void navigateToManageEncounterTypesUrl(String linktext) {
		clickOn(link(linktext));
	}

	@When("I choose to add a new encounter type")
	public void navigateToAddEncounterTypeUrl() {
		clickOn(link("Add Encounter Type"));
	}

	@When("I mention name $name and description $description")
	public void addEncounterTypeDetails(String name, String description) {
		//editing $name into name textbox
		type(name, into(textbox().with(attribute("name", equalTo("name")))));
		
		//editing $description into name textbox
		type(description, into(finderByXpath("//table/tbody/tr[2]/td[2]/textarea"))); //html/body/div/div[3]/form/fieldset/table/tbody/tr[2]/td[2]/textarea
	}

	@When("I save the encounter type")
	public void saveEncounterType() {
		clickOn(button().with(attribute("value", equalTo("Save Encounter Type"))));
	}

	@Then("the new encounter type should be saved")
	public void verifyEncounterType() {
		assertPresenceOf(div().with(text(equalTo("Encounter Type saved"))));
	}
}

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

public class EditPatientDemographicShortFormSteps extends Steps {

	public EditPatientDemographicShortFormSteps(WebDriver driver) {
		super(driver);
	}

	@Given("I edit the patient in the short form")
	public void editPatientInShortForm() throws InterruptedException {
		clickOn(link("Demographics"));
		clickOn(link("Edit this Patient (Short Form)"));

	}

	@When("I mention Address, Address2, City as $address, $address2, $city")
	public void enterInformation(String address, String address2, String city) {
		type(address,
				into(textbox().with(
						attribute("name", equalTo("personAddress.address1")))));
		type(address2,
				into(textbox().with(
						attribute("name", equalTo("personAddress.address2")))));
		type(city,
				into(textbox()
						.with(attribute("name",
								equalTo("personAddress.cityVillage")))));
	}

	@When("save the demographics")
	public void saveDemographics() {
		clickOn(button().with(attribute("id", equalTo("addButton"))));
	}

	@Then("the information should be saved")
	public void verifySavedInformation() {
		assertPresenceOf(div().with(text(equalTo("Patient saved"))));
	}
}

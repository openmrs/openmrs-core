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

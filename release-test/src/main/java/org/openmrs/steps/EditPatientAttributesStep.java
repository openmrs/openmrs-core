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
import static org.openqa.selenium.lift.Finders.textbox;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;

public class EditPatientAttributesStep extends Steps {

	public EditPatientAttributesStep(WebDriver driver) {
		super(driver);
	}

	@Given("I am on the manage patient page")
	public void openManagePatient() {
		clickOn(link().with(text(equalTo("Manage Patients"))));
	}

	@When("enter birthplace, citizenship, health district and race as $birthPlace, $citizenship, $district and $race respectively")
	public void editAttributes(String birthPlace, String citizenship,
			String district, String race) {
		type(birthPlace,
				into(textbox().with(attribute("name", equalTo("2")))));
		type(citizenship,
				into(textbox().with(attribute("name", equalTo("3")))));
		type(district,
				into(textbox().with(attribute("name", equalTo("6")))));
		type(race,
				into(textbox().with(attribute("name", equalTo("1")))));
	}

	@When("save")
	public void save() {
		clickOn(button().with(attribute("id", equalTo("saveButton"))));
	}

	@Then("the patient attributes should be updated")
	public void verifyAttributesUpdated() {
		assertPresenceOf(div().with(text(equalTo("Patient saved"))));
	}

}

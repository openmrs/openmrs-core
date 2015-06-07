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

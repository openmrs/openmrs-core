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

public class CreateAUserSteps extends Steps {

	public CreateAUserSteps(WebDriver driver) {
		super(driver);
	}

	@Given("I am on the manage users page")
	public void navigateToManageUsersPage() {
		clickOn(link().with(text(equalTo("Manage Users"))));
	}

	@When("I click on the Add User")
	public void addUser() {
		waitAndClickOn(link().with(text(equalTo("Add User"))));
	}

	@When("I create a new person")
	public void createNewPerson() {
		clickOn(button().with(attribute("id", equalTo("createNewPersonButton"))));
	}

	@When("I enter $given, $middle, $family, $gender, $username, $password, $confirmPassword")
	public void enterInfo(String given, String middle, String family, String gender, String username, String password, String confirmPassword) {
		type(random(given), into(textbox().with(attribute("name", equalTo("person.names[0].givenName")))));
		type(middle, into(textbox().with(attribute("name", equalTo("person.names[0].middleName")))));
		type(family, into(textbox().with(attribute("name", equalTo("person.names[0].familyName")))));
        clickOn(radioButton().with(
                attribute("name", equalTo("person.gender"))).with(attribute("value", equalTo(gender))));
		type(random(username), into(textbox().with(attribute("name", equalTo("username")))));
		type(password, into(passwordtextbox().with(attribute("name", equalTo("userFormPassword")))));
		type(confirmPassword, into(passwordtextbox().with(attribute("name", equalTo("confirm")))));
	}

	@When("I save the user")
	public void save() {
		clickOn(button().with(attribute("id", equalTo("saveButton"))));
	}

	@Then("the user should be saved/created")
	public void verifyUser() {
        waitFor(div().with(text(equalTo("User Saved"))));
	}
}

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

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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.*;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

public class EditUserPasswordSteps extends Steps {

	public EditUserPasswordSteps(WebDriver driver) {
		super(driver);
	}

	@Given("I login to the openmrs application with username $username and password $password")
	public void logIn(String username, String password) {
		type(username, into(textbox().with(attribute("id", equalTo("username")))));
		type(password, into(passwordtextbox().with(attribute("id", equalTo("password")))));
		clickOn(button());
	}

	@Given("I navigate to the the administration page")
	public void navigateToAdminUrl() {
		clickOn(link().with(text(equalTo("Administration"))));
	}

	@When("I click on the Manage Users")
	public void navigateToManageUsersUrl() {
		clickOn(link().with(text(equalTo("Manage Users"))));
	}

	@When("I search for user $name")
	public void searchUser(String name) {
		type(name, into(textbox().with(attribute("name", equalTo("name")))));
		clickOn(button().with(attribute("name", equalTo("action"))));
	}

	@When("I chose to edit the user")
	public void editUser() {
		//TODO currently the user to edit is hard coded to the first row of the users search result. Need to change this.
		WebElement openmrsSearchTable = driver.findElement(By.className("openmrsSearchTable"));
		List<WebElement> trList = openmrsSearchTable.findElements(By.tagName("tr"));
		if (trList.size() > 0) {
			trList.get(1).findElement(By.tagName("td")).findElement(By.tagName("a")).click();
		}
	}

	@When("I changed the $password, $confirmPassword")
	public void editPassword(String password, String confirmPassword) {
		type(password, into(passwordtextbox().with(attribute("name", equalTo("userFormPassword")))));
		type(confirmPassword, into(passwordtextbox().with(attribute("name", equalTo("confirm")))));
	}

	@When("I save the user")
	public void save() {
		clickOn(button().with(attribute("id", equalTo("saveButton"))));
	}

	@Then("the user's password should be changed")
	public void verifyUser() {
		assertPresenceOf(div().with(text(equalTo("User Saved"))));
	}
}

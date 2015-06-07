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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.button;
import static org.openqa.selenium.lift.Finders.cell;
import static org.openqa.selenium.lift.Finders.div;
import static org.openmrs.Finders.textarea;
import static org.openqa.selenium.lift.Finders.link;
import static org.openqa.selenium.lift.Finders.textbox;
import static org.openqa.selenium.lift.Matchers.text;
import static org.openqa.selenium.lift.match.AttributeMatcher.attribute;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;

public class CreateRoleSteps extends Steps {
	
	private String name;
	
	public CreateRoleSteps(WebDriver driver) {
		super(driver);
	}
	
	@Given("I choose to manage roles")
	public void manageRoles() {
		clickOn(link("Manage Roles"));
	}
	
	@When("I choose to add role")
	public void addRole() {
		clickOn(link("Add Role"));
	}
	
	@When("I mention the role name, description and privileges as $name, $description, $privilege respectively")
	public void enterDetails(String name, String description, String privilege) {
		this.name = name;
		type(name, into(textbox().with(attribute("name", equalTo("role")))));
		type(description, into(textarea().with(attribute("name", equalTo("description")))));
		clickOn(checkbox().with(attribute("id", equalTo("privileges.AddAllergies"))));
	}
	
	@When("save")
	public void save() {
		clickOn(button("Save Role"));
	}
	
	@Then("the role should be saved")
	public void verifySavedRole() {
		assertPresenceOf(div().with(text(containsString("Role saved"))));
		assertPresenceOf(cell().with(text(equalTo(name))));
	}
}

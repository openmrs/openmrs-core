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

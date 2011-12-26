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

public class DeleteRoleSteps extends Steps {

	public DeleteRoleSteps(WebDriver driver) {
		super(driver);
	}

	@Given("I am viewing the list of roles")
	public void listRoles() {
		assertPresenceOf(title().with(text(equalTo("OpenMRS - Role Management"))));
	}

	@When("I delete a role with the name $role")
	public void deleteRole(String role) {
		clickOn(first(checkbox().with(attribute("value", equalTo(role)))));
		clickOn(button("Delete Selected Roles"));
	}

	@Then("the role $role should be deleted")
	public void verifyDeleteRole(String role) {
		assertPresenceOf(div().with(text(equalTo(role + " deleted"))));
		assertAbsenceOf(cell().with(text(equalTo(role))));
	}
}

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

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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.*;
import static org.openqa.selenium.lift.Matchers.text;
import static org.openqa.selenium.lift.match.AttributeMatcher.attribute;

public class ChangeRolePrivilegesSteps extends Steps {
	
	public ChangeRolePrivilegesSteps(WebDriver driver) {
		super(driver);
	}
	
	@Given("I am viewing the list of roles")
	public void listRoles() {
		assertPresenceOf(title().with(text(equalTo("OpenMRS - Role Management"))));
	}
	
	@When("I edit a role with the name $role")
	public void editRole(String role) {
		clickOn(first(link(role)));
	}
	
	@When("change the privilege from Add Allergies to Add Cohorts")
	public void changePrivilege() {
		waitAndClickOn(checkbox().with(attribute("id", equalTo("privileges.AddAllergies"))));
		waitAndClickOn(checkbox().with(attribute("id", equalTo("privileges.AddCohorts"))));
	}
	
	@When("save the role")
	public void save() {
		clickOn(button("Save Role"));
	}
	
	@Then("the role $role should be saved and it should not have $oldPrivilege privilege but should have $newPrivilege privilege")
	public void verifyRolePrivilegeChange(String role, String oldPrivilege, String newPrivilege) {
		assertPresenceOf(div().with(text(containsString("Role saved"))));
		assertPresenceOf(cell().with(text(containsString(newPrivilege))));
		assertAbsenceOf(cell().with(text(containsString(oldPrivilege))));
	}
}

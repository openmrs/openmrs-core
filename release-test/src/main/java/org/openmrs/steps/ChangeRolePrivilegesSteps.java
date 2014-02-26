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

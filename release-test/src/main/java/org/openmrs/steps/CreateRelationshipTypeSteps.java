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

public class CreateRelationshipTypeSteps extends Steps {
	
	public CreateRelationshipTypeSteps(WebDriver driver) {
		super(driver);
	}
	
	@Then("take me to Relationship Type Management Page with $name as heading")
	public void verifyRelationshipPage(String name) {
		waitAndAssertFor(div().with(text(containsString(name))));
	}
	
	@Given("I am on the $name Page")
	public void onFindRelationshipTypeManagementPage(String name) {
		verifyRelationshipPage(name);
	}
	

	@Then("take me to Add Relationship Type page with $heading as heading and has a button with label $buttonText")
	public void verifyAddRelationshipTypePage(String heading, String buttonText) {
		waitAndAssertFor(div().with(text(containsString(heading))));
		waitAndAssertFor(button(buttonText));
	}

	@When("I type $value as relationship of A to B")
	public void enterRelationshipTypeOfAtoBName(String value) {
		type(random(value), into(textbox().with(attribute("name", equalTo("aIsToB")))));
	}
	
	@When("I type $value as relationship of B to A")
	public void enterRelationshipTypeOfBtoAName(String value) {
		type(random(value), into(textbox().with(attribute("name", equalTo("bIsToA")))));
	}
	
	@When("I type $value as description")
	public void enterRelationshipTypeDescription(String value) {
		type(value, into(finderByXpath("//div[@id='content']/form/fieldset/table/tbody/tr[3]/td[2]/textarea")));
	}
	
	@When("I click the $save button")
	public void clickOnSave(String save) {
		clickOn(button(save));
	}
	
	@Then("display message $successMessage")
	public void verifySuccessMessage(String successMessage) {
		assertPresenceOf(div().with(text(containsString(successMessage))));
	}
}

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

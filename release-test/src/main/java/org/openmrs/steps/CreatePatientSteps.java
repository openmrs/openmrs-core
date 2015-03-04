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

import java.util.Random;

import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.*;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

public class CreatePatientSteps extends Steps {

	public CreatePatientSteps(WebDriver driver) {
		super(driver);
	}

	@Given("I am on the Find/Create Patient Page")
	public void onFindCreatePatientPage() {
		waitAndAssertFor(title().with(text(equalTo("OpenMRS - Find Patient"))));

	}

	@When("I enter $name as Name")
	public void enterName(String name) {
		type(name, into(textbox().with(attribute("id", equalTo("personName")))));
	}

	@When("I enter random number as Age")
	public void enterAge() {
		type(random(""), into(textbox().with(attribute("id", equalTo("age")))));
	}

	@When("I select Male as Gender")
	public void selectGender() {
		clickOn(radioButton().with(attribute("id", equalTo("gender-M"))));

	}

	@Then("take me to the Create Patient Page")
	public void verifyIfIamOnCreatePatientPage() {
		assertPresenceOf(button("Save"));
	}

	@Given("I am on the Create Patient Page")
	public void givenIamOnCreatePatientPage() {
		assertPresenceOf(button("Save"));
	}

	@When("I enter the $familyName as the family name")
	public void enterFamilyName(String familyName) {
		type(familyName,
				into(textbox().with(
						attribute("name", equalTo("personName.familyName")))));
	}

	@When("I enter $code as Identifier Code")
	public void enterIdentifierCode(String code) {
		Random randomGenerator = new Random();
		int randomInt = randomGenerator.nextInt(100);
		type(code + Integer.toString(randomInt),
				finderByXpath("//form[@id=\'patientModel\']//table[@id=\'identifiers\']//tr[@id=\'existingIdentifiersRow[0]\']/td[1]/input"));
	}

	@When("I select Old Identification Number as Identifier Type with index $id")
	public void enterIdentifierType(int id) {
		selectAValueInDropDownByXpath(
				"//form[@id=\'patientModel\']//table[@id=\'identifiers\']//tr[@id=\'existingIdentifiersRow[0]\']/td[2]/select[@id=\'identifiers0.identifierType\']")
				.selectByIndex(id);
	}

	@When("I select Unknown Location as location with index $id")
	public void selectIdentifierLocation(int id) {
		selectAValueInDropDownByXpath(
				"//form[@id=\'patientModel\']//table[@id=\'identifiers\']//tr[@id=\'existingIdentifiersRow[0]\']/td[3]/div[@id=\'initialLocationBox0\']/select[@id=\'identifiers0.location\']")
				.selectByIndex(id);
	}
	@When("I select preferred option")
	public void selectPreferredOption(){
		clickOn(radioButton().with(
                attribute("id", equalTo("identifiers[0].preferred"))));
	}

	@When("I enter $address as address")
	public void enterAddress(String address) {
		type(address,
				into(textbox().with(
						attribute("name", equalTo("personAddress.address1")))));
	}

	@When("I enter $country as country")
	public void enterCountryName(String country) {
		type(country,
				into(textbox().with(
						attribute("name", equalTo("personAddress.country")))));
	}

	@Then("take me to Patient dashboard page with title Patient Dashboard")
	public void verifyIfPatientIsCreated() {
		assertPresenceOf(title().with(
				text(equalTo("OpenMRS - " + "Patient Dashboard"))));
	}

}

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
import org.openqa.selenium.lift.Finders;

import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.*;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

public class CreateAConceptSteps extends Steps {
	
	public CreateAConceptSteps(WebDriver driver) {
		super(driver);
	}
	
	@Given("I am on $title screen")
	public void onHomePage(String title) {
		assertPresenceOf(title().with(text(equalTo("OpenMRS - " + title))));
	}
	
	@Then("Take me to the $dictionary page")
	public void takeMeToDictionaryPage(String dictionary) {
		assertPresenceOf(title().with(text(equalTo("OpenMRS - " + dictionary))));
	}
	
	@When("I choose to add new concept")
	public void clickOnAddNewConcept() {
		clickOn(link().with(text(equalTo("Add new Concept"))));
	}
	
	@Then("Take me to the $creatingNewConcept form")
	public void takeMeToCreateNewConceptPage(String title) {
		assertPresenceOf(title().with(text(equalTo("OpenMRS - " + title))));
	}
	
	@When("I enter $test as a fully specified name")
	public void enterAFullySpecifiedName(String fullySpecifiedName) {
		type(random(fullySpecifiedName), into(textbox().with(attribute("id", equalTo("namesByLocale[en].name")))));
	}
	
	@When("I enter $short1 as the short name")
	public void enterShortName(String shortName) {
		type(shortName, into(textbox().with(attribute("name", equalTo("shortNamesByLocale[en].name")))));
	}
	
	@When("I click add Synonym button for en locale")
	public void clickAddSynonymButton() {
		clickOn(finderByXpath("//table[@id=\'conceptTable\']//td[@class=\'en\']/input[@value=\'Add Synonym\']"));
	}
	
	@When("Type $syn1 as the synonym name")
	public void enterNewSynonymName(String synonymName) {
		type(synonymName, into(textbox().with(attribute("name", equalTo("synonymsByLocale[en][0].name")))));
	}
	
	@When("I click add Search term button for en locale")
	public void clickAddSearchTermButton() {
		clickOn(finderByXpath("//table[@id=\'conceptTable\']//td[@class=\'en\']/input[@value=\'Add Search Term \']"));
	}
	
	@When("Type $term1 as the index term name")
	public void enterNewSearchTermName(String termName) {
		type(termName, into(textbox().with(attribute("name", equalTo("indexTermsByLocale[en][0].name")))));
	}
	
	@When("I select Question as the concept class")
	public void selectConceptClass() {
		selectAValueInDropDownByXpath("//select[@id=\'conceptClass\']").selectByValue("7");
	}
	
	@When("I check is set")
	public void checkIsSet() {
		clickOn(checkbox().with(attribute("id", equalTo("conceptSet"))));
	}
	
	@When("I select Boolean as the datatype")
	public void selectDatatype() {
		selectAValueInDropDownByXpath("//select[@id=\'datatype\']").selectByValue("10");
	}
	
	@When("I click $saveConcept button")
	public void clickSaveButton(String saveButtonLabel) {
		clickOn(button(saveButtonLabel));
	}
	
	@Then("The concept should get created with a success message")
	public void theConceptShouldGetCreated() {
		assertPresenceOf(Finders.div("openmrs_msg").with(text(equalTo("Concept saved successfully"))));
	}
}

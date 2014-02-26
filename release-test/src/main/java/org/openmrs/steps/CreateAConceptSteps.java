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

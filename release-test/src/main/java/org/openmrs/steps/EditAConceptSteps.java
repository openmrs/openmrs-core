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

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.lift.Finders;
import org.openqa.selenium.lift.find.HtmlTagFinder;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.textbox;
import static org.openqa.selenium.lift.Finders.title;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

public class EditAConceptSteps extends Steps {
	
	public EditAConceptSteps(WebDriver driver) {
		super(driver);
	}
	
	@When("I search for a concept by typing $aspirin and wait for the search hits")
	public void searchForAConceptAndWaitForTheHits(String phrase) {
        HtmlTagFinder conceptName = textbox().with(attribute("id", equalTo("inputNode")));
        waitFor(conceptName);
        type(phrase, into(conceptName));
		waitFor(Finders.table().with(attribute("id", equalTo("openmrsSearchTable"))));
	}
	
	@When("I select $aspirin from the hits")
	public void takeMeToCreateNewConceptPage(String selection) {
		clickOn(finderByXpath("//table[@id='openmrsSearchTable']/tbody/tr[@class='odd']/td/span[starts-with(text(),'" + selection + "')]"));
	}
	
	@Then("Take me to the viewing concept page")
	public void takeMeToViewingConceptPage() {
        waitAndAssertFor(title().with(text(containsString("OpenMRS - Viewing Concept"))));
	}
	

	@When("I change the fully specified name to $aspirin")
	public void editTheFullySpecifiedName(String newName) {
		waitFor(textbox().with(attribute("id", equalTo("namesByLocale[en].name"))));
        type(random(newName), into(textbox().with(attribute("id", equalTo("namesByLocale[en].name")))));
	}
	
	@When("I edit the synonym")
	public void editTheSynonymName() {
		type(random("syn"), into(textbox().with(attribute("name", equalTo("synonymsByLocale[en][0].name")))));
	}
	@When("I click on Add Search Term")
    public void clickOnAddSearchTerm(){
        getWebDriver().findElement(By.id("addSearch")).click();
    }
	@When("I edit the index term name")
	public void changeSearchTermName() {
		type(random("term"), into(textbox().with(attribute("name", equalTo("indexTermsByLocale[en][0].name")))));
	}
	
	@When("I edit the short name")
	public void changeShortName() {
		type(random("SHT"), into(textbox().with(attribute("name", equalTo("shortNamesByLocale[en].name")))));
	}
	
	@When("I change the concept class to Test")
	public void changeConceptClass() {
		selectAValueInDropDownByXpath("//select[@id=\'conceptClass\']").selectByValue("1");
	}
	
	@When("I check/uncheck is set")
	public void changeIsSet() {
		clickOn(checkbox().with(attribute("id", equalTo("conceptSet"))));
	}
	
	@When("I change the datatype to Text")
	public void changeDatatype() {
		selectAValueInDropDownByXpath("//select[@id=\'datatype\']").selectByValue("3");
	}
	
	@When("I click $saveConcept button")
	public void clickSaveButton(String saveButtonLabel) {
		clickOn(Finders.button(saveButtonLabel));
	}
	
	@Then("The concept should get saved with a success message")
	public void theConceptShouldGetCreated() {
		waitAndAssertFor(Finders.div("openmrs_msg").with(text(equalTo("Concept saved successfully"))));
	}
}

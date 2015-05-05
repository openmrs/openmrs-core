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
import org.openqa.selenium.lift.find.HtmlTagFinder;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.*;
import static org.openqa.selenium.lift.Matchers.text;
import static org.openqa.selenium.lift.match.AttributeMatcher.attribute;

public class CreateObservationSteps extends Steps {
    public CreateObservationSteps(WebDriver driver) {
        super(driver);
    }

    @Then("take me to Observation Management Page with Observation Management as heading")
    public void verifyManagementPage() {
        waitAndAssertFor(div().with(text(containsString("Observation Management"))));
    }


    @Given("I am on the Observation Management Page")
    public void onFindObservationManagementPage() {
    	verifyManagementPage();
    }


    @Then("take me to Add Observation page with $heading as heading and has a button with label $buttonText")
    public void verifyAddObservationPage(String heading, String buttonText) {
        waitAndAssertFor(div().with(text(containsString(heading))));
        waitAndAssertFor(button("Save Observation"));

    }

    @Given("I am on the Add Observation page")
    public void givenIamOnAddObservationPage() {
        assertPresenceOf(div().with(text(containsString("Observation"))));
    }


    @When("I type $name as person")
    public void enterPersonName(String name) {
        type(name, into(textbox().with(attribute("id", equalTo("person_id_selection")))));
        waitAndClickOn(finderByXpath("//li[@class='ui-menu-item'][1]"));
    }
    
    @When("I select Unknown Location as Location with index $index")
    public void selectLocation(int index) {
        selectAValueInDropDownByXpath("//table[@id=\'obsTable\']/tbody/tr[4]/td/select[@id=\'location\']").selectByIndex(index);
    }

    @When("I type $date as Observation Date")
    public void enterObservationDate(String date) {
        type(date, into(textbox().with(attribute("id", equalTo("obsDatetime")))));
    }

    @When("I type $conceptQuestion as Concept Question")
    public void enterConceptQuestion(String conceptQuestion) {
        type(conceptQuestion, into(textbox().with(attribute("id", equalTo("conceptId_selection")))));
        waitFor(finderByXpath("//ul[3]/li/a"));
        clickOn(finderByXpath("//ul[3]/li/a"));
    }

    @When("I type $conceptAnswer as Concept Answer")
    public void enterConceptAnswer(String conceptAnswer) {
        type(conceptAnswer, into(textbox().with(attribute("name", equalTo("valueNumeric")))));
    }

    @Then("display message Observation saved")
    public void verifySuccessMessage() {
        waitAndAssertFor(div().with(text(containsString("Observation saved"))));
    }

}

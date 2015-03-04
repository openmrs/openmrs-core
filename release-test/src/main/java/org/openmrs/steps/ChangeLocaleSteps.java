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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.button;
import static org.openqa.selenium.lift.Finders.div;
import static org.openqa.selenium.lift.Finders.link;
import static org.openqa.selenium.lift.Finders.textbox;
import static org.openqa.selenium.lift.Finders.title;
import static org.openqa.selenium.lift.Matchers.text;
import static org.openqa.selenium.lift.match.AttributeMatcher.attribute;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.lift.find.HtmlTagFinder;


public class ChangeLocaleSteps extends ViewLocaleAndThemeSteps {
    public ChangeLocaleSteps(WebDriver driver) {
        super(driver);
    }
    
    @When("I type $name as locale")
	public void enterRelationshipTypeOfAtoBName(String name) {
		type(name, into(textbox().with(attribute("name", equalTo("locale")))));
	}
    
    @When("I click on $submit button")
	public void clickOnSubmit(String submit) {
		clickOn(button());
	}
    
    @Then("display message $name")
	public void verifySuccessMessage(String name) {
		waitAndAssertFor(div().with(text(containsString(name))));
	}
}

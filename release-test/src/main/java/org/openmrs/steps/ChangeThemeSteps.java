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
import org.openqa.selenium.WebDriver;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.*;
import static org.openqa.selenium.lift.Matchers.text;
import static org.openqa.selenium.lift.match.AttributeMatcher.attribute;


public class ChangeThemeSteps extends ViewLocaleAndThemeSteps {
    public ChangeThemeSteps(WebDriver driver) {
        super(driver);
    }
	
	@When("I type $name as theme")
	public void enterRelationshipTypeOfBtoAName(String name) {
		type(name, into(textbox().with(attribute("name", equalTo("theme")))));
	}
    
    @When("I click on $submit button")
	public void clickOnSubmit(String submit) {
		clickOn(button());
	}
    
    @Then("display message $name")
	public void verifySuccessMessage(String name) {
		assertPresenceOf(div().with(text(containsString(name))));
	}

    @When("I click on the $manageLocaleAndTheme link")
    public void clickOnLink(String manageLocaleAndTheme) {
        clickOn(link().with(text(equalTo(manageLocaleAndTheme))));
    }
}

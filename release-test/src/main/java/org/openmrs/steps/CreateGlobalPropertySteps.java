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
import static org.openqa.selenium.lift.Finders.button;
import static org.openqa.selenium.lift.Finders.div;
import static org.openqa.selenium.lift.Matchers.text;


public class CreateGlobalPropertySteps extends ViewGlobalPropertySteps {
    public CreateGlobalPropertySteps(WebDriver driver) {
        super(driver);
    }

    @When("I press input button Add Property")
    public void whenIPressInputButtonAddProperty() {
    	clickOn(finderByXpath("//input[@value='Add Property']"));
    }
    
    @When("I type $name as name")
	public void enterGlobalPropertyName(String name) {
		type(random(name), into(finderByXpath("//tbody[@id='globalPropsList']/tr[count(//tbody[@id='globalPropsList']/tr) - 1]/td[1]/input")));
	}
    
    @When("I type $value as value")
	public void enterGlobalPropertyValue(String value) {
		type(random(value), into(finderByXpath("//tbody[@id='globalPropsList']/tr[count(//tbody[@id='globalPropsList']/tr) - 1]/td[2]/input")));
	}
    
    @When("I click on $save button")
	public void clickOnSave(String save) {
		clickOn(button(save));
	}
    
    @Then("display message $successMessage")
	public void verifySuccessMessage(String successMessage) {
		assertPresenceOf(div().with(text(containsString(successMessage))));
	}
}

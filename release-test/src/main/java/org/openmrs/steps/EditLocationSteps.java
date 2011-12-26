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
import static org.openmrs.find.TextAreaFinder.textarea;
import static org.openqa.selenium.lift.Finders.*;
import static org.openqa.selenium.lift.Matchers.text;
import static org.openqa.selenium.lift.match.AttributeMatcher.attribute;


public class EditLocationSteps extends Steps {

    public EditLocationSteps(WebDriver driver) {
        super(driver);
    }

    @Given("I login to the openmrs application")
	public void logIn() {
		assertPresenceOf(link().with(text(equalTo("Log out"))));
    }
    
    @Given("I navigate to the the administration page")
	public void navigateToAdminUrl() {
		clickOn(link().with(text(equalTo("Administration"))));
    }
    

    @When("I edit a location")
    public void navigateToEditALocationUrl() {
        clickOn(link().with(text(equalTo("Unknown Location"))));
    }

    @When("I mention name $name and description $description")
    public void editLocation(String name, String description) {
		type(name, into(textbox().with(attribute("name", equalTo("name")))));
		//type(description, into(finderByXpath("id('content')/x:form[1]/x:fieldset/x:table/x:tbody/x:tr[2]/x:td/x:textarea"))); //html/body/div/div[3]/form/fieldset/table/tbody/tr[2]/td[2]/textarea
        type(name, into(textarea().with(attribute("name", equalTo("description")))));
    }
    
    @When("I save the location")
	public void clickOnSave() {
		clickOn(button().with(attribute("value", equalTo("Save Location"))));
    }
    
    @Then("the new location name should get saved")
    public void verifySuccessMessage() {
	        assertPresenceOf(div().with(text(containsString("Location saved"))));
    }
}

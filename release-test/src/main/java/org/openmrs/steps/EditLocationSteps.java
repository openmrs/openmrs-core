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
    
    @When("I Choose to manage locations")
    public void clickOnManageLocations() {
		clickOn(link().with(text(equalTo("Manage Locations"))));
    }
    
    @When("I choose to edit a location")
    public void navigateToEditALocationUrl() {
		String locationXpath = "//table[@id = 'locationTable']/tbody/tr[2]/td[2]/a"; //html/body/div/div[3]/div[2]/table/tbody/tr[3]/td/a
		waitFor(finderByXpath(locationXpath));
		clickOn(finderByXpath(locationXpath));
    }

    @When("I choose to mention name $name and description $description")
    public void editLocation(String name, String description) {
		type(name, into(textbox().with(attribute("name", equalTo("name")))));
		type(description, into(finderByXpath("//table/tbody/tr[2]/td[2]/textarea"))); //html/body/div/div[3]/form/fieldset/table/tbody/tr[2]/td[2]/textarea
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

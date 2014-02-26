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
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.*;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

public class CreateEncounterSteps extends Steps {
	
	public CreateEncounterSteps(WebDriver driver) {
		super(driver);
	}
	
	@When("I enter $name, $provider, $location, $date, $providerRole")
	public void enterDetails(String name, String provider, String location, String date, String providerRole) throws InterruptedException {
		Thread.sleep(2000);
        type(name, into(textbox().with(attribute("id", equalTo("patientId_id_selection")))));
        String autoCompleteXPath = "//ul[@class='ui-autocomplete ui-menu ui-widget ui-widget-content ui-corner-all']";
        waitFor(finderByXpath(autoCompleteXPath));
        clickOn(finderByXpath(autoCompleteXPath));

        selectFrom(location, "location");
        type(date, into(textbox().with(attribute("name", equalTo("encounterDatetime")))));
        clickOn(textbox().with(attribute("name", equalTo("encounterDatetime"))));
        getWebDriver().findElement(By.id("addProviderButton")).click();
        selectFrom(providerRole, "roleIds[0]");
        type(provider, into(textbox().with(attribute("id", equalTo("providers[0]")))));
        WebElement providerElement = driver.findElement(By.id("providers[0]"));
        providerElement.sendKeys(Keys.TAB);
	}

	@When("I save the encounter")
	public void saveEncounter() {
		waitAndClickOn(button("Save Encounter"));
	}
	
	@Then("the encounter should be saved")
	public void verifySavedEncounter() {
        waitAndAssertFor(div().with(text(containsString("Encounter saved"))));
	}
	
}

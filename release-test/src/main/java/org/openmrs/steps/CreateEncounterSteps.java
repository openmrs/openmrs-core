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

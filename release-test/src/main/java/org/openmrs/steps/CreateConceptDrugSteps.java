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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.lift.Finders.button;
import static org.openqa.selenium.lift.Finders.div;
import static org.openqa.selenium.lift.Finders.link;
import static org.openqa.selenium.lift.Finders.radioButton;
import static org.openqa.selenium.lift.Finders.textbox;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CreateConceptDrugSteps extends Steps {

	public CreateConceptDrugSteps(WebDriver driver) {
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

	@When("I mention $name, $concept, $doseStrength, $units, $maximumDose and $minimumDose")
	public void addDrugDetails(String name, String concept, String doseStrength, String units, String maximumDose, String minimumDose) throws InterruptedException {
		type(name, into(textbox().with(attribute("name", equalTo("name")))));
		//editing the concept
		type(concept, into(textbox().with(attribute("id", equalTo("concept_selection")))));
        Thread.sleep(1000);
        WebElement conceptSelection = driver.findElement(By.id("concept_selection"));
        conceptSelection.sendKeys(Keys.TAB);
		//editing the combination
		clickOn(checkbox().with(attribute("name", equalTo("combination"))));

		//editing dose strength
		type(doseStrength, into(textbox().with(attribute("name", equalTo("doseStrength")))));

		//editing unit
		type(units, into(textbox().with(attribute("name", equalTo("units")))));

		//editing maximum dose
		type(maximumDose, into(textbox().with(attribute("name", equalTo("maximumDailyDose")))));

		//editing minimum dose
		type(minimumDose, into(textbox().with(attribute("name", equalTo("minimumDailyDose")))));
	}

	@When("I save the concept drug")
	public void saveConceptDrug() {
		clickOn(button().with(attribute("value", equalTo("Save Concept Drug"))));
	}

	@Then("the new drug should get saved")
	public void verifyConceptDrug() {
		waitAndAssertFor(div().with(text(equalTo("Concept Drug saved"))));
	}
}

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
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.*;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

public class EditConceptDrugSteps extends Steps {

	public EditConceptDrugSteps(WebDriver driver) {
		super(driver);
	}

	@When("I click on the Manage Concept Drugs")
	public void navigateToManageConceptDrugsUrl() {
		clickOn(link().with(text(equalTo("Manage Concept Drugs"))));
	}

	@When("I click on the edit option")
	public void navigateToEditConceptDrugUrl() {
        clickOn(finderByXpath("//table[@id='drugTable']/tbody/tr[2]/td[1]/a"));
		//clickOn(link().with(text(equalTo("Triomune-40"))));
	}

	@When("I change $name, $concept, $doseStrength, $units, $maximumDose and $minimumDose")
	public void editDrug(String name, String concept, String doseStrength, String units, String maximumDose, String minimumDose) throws InterruptedException {
		//editing $name into name textbox
		Thread.sleep(2000);
        type(name, into(textbox().with(attribute("name", equalTo("name")))));
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

	@Then("the changes to the drug should be saved")
	public void verifyConceptDrug() {
		assertPresenceOf(div().with(text(equalTo("Concept Drug saved"))));
	}
}

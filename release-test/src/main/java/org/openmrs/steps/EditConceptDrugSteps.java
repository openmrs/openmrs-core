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

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;

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
	public void editDrug(String name, String concept, String doseStrength, String units, String maximumDose, String minimumDose) {
		//editing $name into name textbox
		type(name, into(textbox().with(attribute("name", equalTo("name")))));
		
		//editing the concept
		String changeButtonXpath = "//table[@id = 'table']/tbody/tr[2]/td[1]/span[1]/span[1]/input[2]"; //html/body/div/div[3]/form/fieldset/table/tbody/tr[2]/td/span[1]/span[1]/input[2]
		waitFor(finderByXpath(changeButtonXpath));
		clickOn(finderByXpath(changeButtonXpath));
		type(concept, into(textbox().with(attribute("id", equalTo("conceptSearch")))));
		String conceptSearchXpath = "//table[@class = 'openmrsSearchTable']/tbody/tr[1]/td[2]/a"; //html/body/div/div[3]/form/fieldset/table/tbody/tr[2]/td/span/span[3]/span[2]/table/tbody/tr/td[2]/a/span[2]
		waitFor(finderByXpath(conceptSearchXpath));
		clickOn(finderByXpath(conceptSearchXpath));

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

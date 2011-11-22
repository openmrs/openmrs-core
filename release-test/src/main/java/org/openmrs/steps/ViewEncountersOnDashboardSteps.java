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
import static org.openqa.selenium.lift.Finders.div;
import static org.openqa.selenium.lift.Finders.first;
import static org.openqa.selenium.lift.Finders.link;
import static org.openqa.selenium.lift.Finders.title;
import static org.openqa.selenium.lift.Matchers.text;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;

public class ViewEncountersOnDashboardSteps extends Steps {

	public ViewEncountersOnDashboardSteps(WebDriver driver) {
		super(driver);
	}
	
	@Given("I am on the patient dashboard for $patientname")
	public void iAmOnFindCreatePatientPage(String patientName) {
		assertPresenceOf(title().with(text(containsString("Patient Dashboard"))));
	}
	
	@When ("I select the $name tab")
	public void clickOnEncountersTab(String name) {
		clickOn(first(link(name)));
	}
	
	@Then ("I should see the encounters associated to the patient")
	public void seeTheEncountersListed() {
		// make sure the tab loads
        waitFor(div("encounters"));
        
        // test to see if we're displaying the encounter list
        assertAbsenceOf(div("patientEncountersTable_info").with(text(equalTo("Showing 0 to 0 of 0 entries"))));
	}
	
}

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

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.*;
import static org.openqa.selenium.lift.Matchers.text;
import static org.openqa.selenium.lift.match.AttributeMatcher.attribute;

public class VerifyPatientDashboardSteps extends Steps {

	public VerifyPatientDashboardSteps(WebDriver driver) {
		super(driver);
	}
	
	@Given("I am on Find/Create Patient Page with $title as title")
	public void iAmOnFindCreatePatientPage(String title){
		assertPresenceOf(title().with(text(equalTo("OpenMRS - " +title))));
	}
	
	@When ("I search for a  patient $name")
	public void searchForPatient(String name){
		type(name, into(textbox().with(attribute("id", equalTo("inputNode")))));
        
	}
	
	@When ("view that patient's dashboard")
	@Alias("choose the patient")
	public void viewSelectedPatient(){
        String patientRecordXpath = "//table[@id=\'openmrsSearchTable\']/tbody/tr/td[3]";
		waitFor(finderByXpath(patientRecordXpath));
        clickOn(finderByXpath(patientRecordXpath));
			
	}
	
	@Then("the dashboard header should contain name, age, bmi, CD4, regimens, last encounter, Old identification number and OpenMRS identification number")
	public void verifyPatientDashBoard(){
    	assertPresenceOf(div().with(attribute("id",equalTo("patientHeaderPatientName"))).with(text(equalTo("Mr. Horatio L Hornblower Esq."))));
		assertPresenceOf(cell().with(attribute("id",equalTo("patientHeaderPatientAge"))).with(text(containsString("71 yrs"))));
		assertPresenceOf(table().with(attribute("id",equalTo("patientHeaderObs"))));
		assertPresenceOf(cell().with(attribute("class",equalTo("patientRecentObsConfigured"))));
		assertPresenceOf(cell().with(attribute("id", equalTo("patientHeaderObsRegimen"))));
		assertPresenceOf(div().with(attribute("id",equalTo("patientHeaderPreferredIdentifier"))).with(text(containsString("101-6"))));
		assertPresenceOf(cell().with(attribute("id", equalTo("patientHeaderOtherIdentifiers"))).with(text(containsString("Old Identification Number: 101"))));
		assertPresenceOf(title().with(text(equalTo("OpenMRS - Patient Dashboard"))));
	}
}

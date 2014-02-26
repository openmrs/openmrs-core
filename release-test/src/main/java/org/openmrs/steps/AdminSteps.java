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
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;

import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.title;
import static org.openqa.selenium.lift.Matchers.text;

public class AdminSteps extends Steps {

	public AdminSteps(WebDriver driver) {
		super(driver);
	}

	@Given("I am on $title screen")
	public void onHomePage(String title) {
		assertPresenceOf(title().with(text(equalTo("OpenMRS - " + title))));
	}

	@Then("take me to $title page")
	public void verifyAdminPage(String title) throws InterruptedException {
        Thread.sleep(5000);
        assertPresenceOf(title().with(text(equalTo("OpenMRS - " + title))));
	}

}

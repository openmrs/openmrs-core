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
import static org.openqa.selenium.lift.Finders.button;
import static org.openqa.selenium.lift.Finders.div;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

public class RetireUnretireLocationSteps extends Steps {

	public RetireUnretireLocationSteps(WebDriver driver) {
		super(driver);
	}

    @When("I unretire the location")
    public void unretireLocation() {
        clickOn(button().with(attribute("name", equalTo("unretireLocation"))));
    }

    @Then("the location should get unretired")
    public void verifyLocationUnretired() {
        assertPresenceOf(div().with(text(equalTo("Location unretired successfully"))));
    }
}

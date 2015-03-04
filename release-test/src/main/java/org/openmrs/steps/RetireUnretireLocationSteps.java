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

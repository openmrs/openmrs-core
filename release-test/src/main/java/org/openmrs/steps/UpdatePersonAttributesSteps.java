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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.lift.Finders;
import org.openqa.selenium.lift.find.Finder;

import static org.hamcrest.Matchers.*;
import static org.openqa.selenium.lift.Finders.button;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

public class UpdatePersonAttributesSteps extends Steps {

    private WebDriver driver;

    public UpdatePersonAttributesSteps(WebDriver driver) {
		super(driver);
        this.driver = driver;
    }

    @When("I change the attribute description to $description")
    public void changeTheAttributeDescriptionTo(String description){
        Finder<WebElement,WebDriver> descriptionXpath = finderByXpath("//textarea[@name='description']");
        waitFor(descriptionXpath);
        type(description, into(descriptionXpath));
    }

    @When("I save the attribute type")
    public void saveTheAttributeType(){
        clickOn(button().with(attribute("value",is("Save Person Attribute Type"))));
    }

    @Then("display message $message")
    public void displayMessageIs(String message){
        assertPresenceOf(Finders.div().with(attribute("id", equalTo("openmrs_msg"))).with(text(containsString(message))));

    }
}

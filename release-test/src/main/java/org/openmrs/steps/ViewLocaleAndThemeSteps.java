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
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;

import static org.hamcrest.Matchers.containsString;
import static org.openqa.selenium.lift.Finders.*;
import static org.openqa.selenium.lift.Matchers.text;


public class ViewLocaleAndThemeSteps extends Steps {
    public ViewLocaleAndThemeSteps(WebDriver driver) {
        super(driver);
    }

    @Given("I am on Admin page")
    public void iAmOnAdminPage() {
        assertPresenceOf(title("OpenMRS - Administration"));
    }
    
    @When("I click on the  $name link")
    public void clickManageGlobalPropertiesLink(String name) {
        clickOn(link(name));
    }
    
    @Then("take me to Locales And Themes Management Page with $name as heading")
    public void verifyManagementPage(String name) {
        waitAndAssertFor(div().with(text(containsString(name))));
    }
    
    @Given("I am on the $name Page")
    public void onFindGlobalPropertiesManagementPage(String name) {
    	verifyManagementPage(name);
    }
}

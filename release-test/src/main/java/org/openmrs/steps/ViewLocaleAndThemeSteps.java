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

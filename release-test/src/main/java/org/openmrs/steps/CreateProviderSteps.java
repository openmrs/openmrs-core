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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.div;
import static org.openqa.selenium.lift.Finders.textbox;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

public class CreateProviderSteps extends Steps {
    public static String providerIdentifier = String.valueOf(System.currentTimeMillis());
	public CreateProviderSteps(WebDriver driver) {
		super(driver);
	}
	
	@When("I enter identifier, $person")
    public void enterProviderDetails( String person){
        
        type(providerIdentifier, into(textbox().with(attribute("name", equalTo("identifier")))));
        type(person, into(textbox().with(attribute("name", equalTo("name")))));

    }

    @Then("the provider should be saved")
	public void verifySavedEncounter() {
        waitAndAssertFor(div().with(text(containsString("Provider saved"))));
	}

    @When("I enter $providerName as provider name")
    public void enterProviderName(String providerName)  {
        waitFor(textbox().with(attribute("id", equalTo("inputNode"))));
        type(providerName, into(textbox().with(attribute("id", equalTo("inputNode")))));
    }

    @When("I select identifier from provider search results")
    public void takeMeToProviderPage() throws InterruptedException {
        Thread.sleep(1000);
        WebElement openmrsSearchTable = driver.findElement(By.id("openmrsSearchTable"));
        List<WebElement> trList = openmrsSearchTable.findElements(By.tagName("tr"));
        for(WebElement tr : trList){
            List<WebElement> tdList = tr.findElements(By.tagName("td"));
            List<WebElement> selectedTD = select(tdList, having(on(WebElement.class).getText(), equalTo(providerIdentifier)));
            if(selectedTD.size() > 0){
                tr.click();

                break;
            }
        }
    }
    
    @When("I enter $retireReason as retired reason")
    public void retireProvider(String retireReason){
        type(retireReason, into(textbox().with(attribute("id", equalTo("retire")))));
    }

    @Then("the provider should be retired")
    public void verifyRetiredProvider() {
        waitAndAssertFor(div().with(text(containsString("Provider retired"))));
    }
}

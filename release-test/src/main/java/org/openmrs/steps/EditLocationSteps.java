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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.lift.Matchers;

import java.util.List;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.openmrs.find.TextAreaFinder.textarea;
import static org.openqa.selenium.lift.Finders.*;
import static org.openqa.selenium.lift.Matchers.text;
import static org.openqa.selenium.lift.match.AttributeMatcher.attribute;


public class EditLocationSteps extends Steps {

    public EditLocationSteps(WebDriver driver) {
        super(driver);
    }

    @Given("I login to the openmrs application")
	public void logIn() {
		assertPresenceOf(link().with(text(equalTo("Log out"))));
    }
    
    @Given("I navigate to the the administration page")
	public void navigateToAdminUrl() {
		clickOn(link().with(text(equalTo("Administration"))));
    }
    

    @When("I edit a location with name $locationName")
    public void navigateToEditALocationUrl(String locationName) {
        clickOn(link().with(text(equalTo(locationName))));
    }

    @When("I mention name $name and description $description")
    public void editLocation(String name, String description) {
		type(name, into(textbox().with(attribute("name", equalTo("name")))));
		//type(description, into(finderByXpath("id('content')/x:form[1]/x:fieldset/x:table/x:tbody/x:tr[2]/x:td/x:textarea"))); //html/body/div/div[3]/form/fieldset/table/tbody/tr[2]/td[2]/textarea
        type(name, into(textarea().with(attribute("name", equalTo("description")))));
    }
    
    @When("I save the location")
	public void clickOnSave() {
		clickOn(button().with(attribute("value", equalTo("Save Location"))));
    }
    
    @Then("the new location name should get saved")
    public void verifySuccessMessage() {
	        assertPresenceOf(div().with(text(containsString("Location saved"))));
    }
    
    @When("I enter $retireReason as retire reason")
    public void retireLocation(String retireReason){
        type(retireReason, into(textbox().with(attribute("name", equalTo("retireReason")))));
    }

    @Then("the location should be retired")
    public void locationRetiredSuccessfully(){
        assertPresenceOf(div().with(text(containsString("Location retired successfully"))));
    }

    @When("I want to unretire the retired location")
    public void displayRetiredLocation() {
        clickOn(link().with(text(equalTo("Toggle Retired"))));
    }

    @When("I chose to edit the retired location $retiredLocation")
    public void navigateToEditRetiredLocationUrl(String retiredLocation) {
//        HtmlTagFinder link = link().with(text(equalTo(retiredLocation)));
//        waitFor(link);
//        clickOn(link);
        clickOn(link().with(text(equalTo(retiredLocation))));
    }

    @When("I unretire the location")
    public void unretireLocation() {
        clickOn(button().with(Matchers.attribute("name", equalTo("unretireLocation"))));
    }

    @Then("the location should get unretired")
    public void verifyLocationUnretired() {
        assertPresenceOf(div().with(text(equalTo("Location unretired successfully"))));
    }

    @When("I check on $locationName")
    public void checkOnLocation(String locationName){
        WebElement locationTable = driver.findElement(By.id("locationTable"));
        List<WebElement> trList = locationTable.findElements(By.tagName("tr"));
        boolean isFoundTR = false;
        for(int i=0; i < trList.size(); i++){
            List<WebElement> tdList = trList.get(i).findElements(By.tagName("td"));
            List<WebElement> selectedTD = select(tdList, having(on(WebElement.class).getText(), equalTo(locationName)));
            if(selectedTD.size() > 0){
                isFoundTR = true;
            }
            if(isFoundTR){
                trList.get(i).findElement(By.name("locationId")).click();
                break;
            }
        }

    }


}

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

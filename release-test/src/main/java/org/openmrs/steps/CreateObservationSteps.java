package org.openmrs.steps;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.lift.find.HtmlTagFinder;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.*;
import static org.openqa.selenium.lift.Matchers.text;
import static org.openqa.selenium.lift.match.AttributeMatcher.attribute;

public class CreateObservationSteps extends Steps {
    public CreateObservationSteps(WebDriver driver) {
        super(driver);
    }

    @Given("I am on Admin page")
    public void iAmOnAdminPage() {
        assertPresenceOf(title().with(text(equalTo("OpenMRS - " + "Administration"))));
    }

    @When("I click on the Manage Observations link")
    public void clickManageObsLink() {
        clickOn(link().with(text(equalTo("Manage Observation"))));
    }

    @Then("take me to Observation Management Page with Observation Management as heading")
    public void verifyManagementPage() {
        assertPresenceOf(div().with(text(containsString("Observation Management"))));
    }


    @Given("I am on the Observation Management Page")
    public void onFindObservationManagementPage() {
    	verifyManagementPage();
    }

    @When("I click on $addObsLink  link")
    public void clickOnAddObservation(String addObsLink) {
        clickOn(link().with(text(equalTo("Add Observation"))));
    }

    @Then("take me to Add Observation page with $heading as heading and has a button with label $buttonText")
    public void verifyAddObservationPage(String heading, String buttonText) {
        assertPresenceOf(div().with(text(containsString(heading))));
        assertPresenceOf(button("Save Observation"));

    }

    @Given("I am on the Add Observation page")
    public void givenIamOnAddObservationPage() {
        assertPresenceOf(div().with(text(containsString("Observation"))));
    }


    @When("I type $name as person")
    public void enterPersonName(String name) {
        type(name, into(textbox().with(attribute("id", equalTo("person_id_selection")))));
        HtmlTagFinder link = link().with(attribute("class", equalTo("ui-corner-all")));
        waitFor(link);
        clickOn(link);

    }

    @When("I select Unknown Location as Location with index $index")
    public void selectLocation(int index) {
        selectAValueInDropDownByXpath("//table[@id=\'obsTable\']/tbody/tr[4]/td/select[@id=\'location\']").selectByIndex(index);
    }

    @When("I type $date as Observation Date")
    public void enterObservationDate(String date) {
        type(date, into(textbox().with(attribute("id", equalTo("obsDatetime")))));
    }

    @When("I type $conceptQuestion as Concept Question")
    public void enterConceptQuestion(String conceptQuestion) {
        type(conceptQuestion, into(textbox().with(attribute("id", equalTo("conceptId_selection")))));
        waitFor(finderByXpath("//ul[3]/li/a"));
        clickOn(finderByXpath("//ul[3]/li/a"));
    }

    @When("I type $conceptAnswer as Concept Answer")
    public void enterConceptAnswer(String conceptAnswer) {
        type(conceptAnswer, into(textbox().with(attribute("name", equalTo("valueNumeric")))));
    }

    @When("I click the Save Observation button")
    public void clickSaveObservationButton() {
        clickOn(button("Save Observation"));
    }

    @Then("display message Observation saved")
    public void verifySuccessMessage() {
        assertPresenceOf(div().with(text(containsString("Observation saved"))));
    }

}

package org.openmrs.steps;

import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.link;
import static org.openqa.selenium.lift.Finders.title;
import static org.openqa.selenium.lift.Matchers.text;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;

public class AdminSteps extends Steps {

	public AdminSteps(WebDriver driver) {
		super(driver);
	}

	@Given("I am on $title screen")
	public void onHomePage(String title) {
		assertPresenceOf(title().with(text(equalTo("OpenMRS - " + title))));
	}

	@When("I click on the $admin link")
	public void clickOnAdminLink(String admin) {
		clickOn(link().with(text(equalTo(admin))));
	}

	@Then("take me to $title page")
	public void verifyAdminPage(String title) {
		assertPresenceOf(title().with(text(equalTo("OpenMRS - " + title))));
	}

}

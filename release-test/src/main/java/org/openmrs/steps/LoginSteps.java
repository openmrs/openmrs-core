package org.openmrs.steps;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.button;
import static org.openqa.selenium.lift.Finders.div;
import static org.openqa.selenium.lift.Finders.textbox;
import static org.openqa.selenium.lift.Finders.title;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;

public class LoginSteps extends Steps {

	public LoginSteps(WebDriver driver) {
		super(driver);
	}

	@Given("I am on the login page of OpenMRS with url $url")
	public void onLoginPage(String url) {
		goTo(url);
		assertPresenceOf(button().with(attribute("value", equalTo("Log In"))));
	}

	@When("I enter $username as the username and $password as the password and click the 'Log In' button")
	public void logIn(String username, String password) {

		type(username, into(textbox()
				.with(attribute("id", equalTo("username")))));
		type(password,
				into(passwordtextbox().with(
						attribute("id", equalTo("password")))));
		clickOn(button());
	}

	@Then("take me to the $title screen and display welcome message for user $user")
	public void verifyPage(String title, String displayName) {
		assertPresenceOf(title().with(text(equalTo("OpenMRS - " + title))));
		assertPresenceOf(div().with(
				text(containsString("Hello, " + displayName + ". Welcome to"))));
	}

}

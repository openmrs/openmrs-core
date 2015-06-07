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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.lift.find.Finder;

public class LoginSteps extends Steps {

	public LoginSteps(WebDriver driver) {
		super(driver);
	}

	private boolean userAlreadyLoggedIn() {
		Finder<WebElement, WebDriver> f = finderByXpath("//span[@id='userLoggedInAs']").with(text(containsString("Currently logged in")));
		return f.findFrom(getWebDriver()).size() > 0;
	}

	@Given("I am on the login page of OpenMRS")
	public void onLoginPage() {
		String port = System.getProperty("jetty.port", "8080");
		String url = "http://localhost:" + port + "/openmrs/initialsetup";
		goTo(url);

		// the login button will only be there if the user hasn't logged in yet.
		// this check is just in case a scenario has two dependencies and both of them 
		// depend on the login_to_website story
		if (! userAlreadyLoggedIn()) {
			waitAndAssertFor(button().with(attribute("value", equalTo("Log In"))));
		}
	}

	//@When("I enter $username as the username and $password as the password and click the 'Log In' button")
	@When("I enter username and password as stored in system properties as $usernameProp and $passwordProp and click the 'Log In' button")
	public void logIn(String usernameProp, String passwordProp) {

		String username = System.getProperty(usernameProp, "admin");
		String password = System.getProperty(passwordProp, "Admin123");

		// (same as above resoning)
		// this check is just in case a scenario has two dependencies and both of them 
		// depend on the login_to_website story
		if (! userAlreadyLoggedIn()) {
			type(username, into(textbox()
					.with(attribute("id", equalTo("username")))));
			type(password,
					into(passwordtextbox().with(
							attribute("id", equalTo("password")))));
			clickOn(button());
		}
	}

	@Then("take me to the $title screen and display welcome message for user $user")
	public void verifyPage(String title, String displayName) {
		assertPresenceOf(title().with(text(equalTo("OpenMRS - " + title))));
		assertPresenceOf(div().with(
				text(containsString("Hello, " + displayName + ". Welcome to"))));
	}

}

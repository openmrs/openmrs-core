/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import static org.openqa.selenium.lift.match.NumericalMatchers.atLeast;

import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.lift.TestContext;
import org.openqa.selenium.lift.find.Finder;
import org.openqa.selenium.support.ui.Clock;
import org.openqa.selenium.support.ui.SystemClock;

public class CustomWebDriverContext implements TestContext {

	private WebDriver driver;
	private final Clock clock;

	public CustomWebDriverContext(WebDriver driver) {
		this(driver, new SystemClock());
	}

	CustomWebDriverContext(WebDriver driver, Clock clock) {
		this.driver = driver;
		this.clock = clock;
	}

	public void quit() {
		driver.quit();
	}

	public void goTo(String url) {
		driver.get(url);
	}

	public void assertPresenceOf(Finder<WebElement, WebDriver> finder) {
		assertPresenceOf(atLeast(1), finder);
	}

	public void assertAbsenceOf(Finder<WebElement, WebDriver> finder) {
		assertPresenceOf(atLeast(0), finder);
	}
	
	public void assertPresenceOf(Matcher<Integer> cardinalityConstraint,
			Finder<WebElement, WebDriver> finder) {
		Collection<WebElement> foundElements = finder.findFrom(driver);
		if (!cardinalityConstraint.matches(foundElements.size())) {
			Description description = new StringDescription();
			description.appendText("\nExpected: ")
					.appendDescriptionOf(cardinalityConstraint).appendText(" ")
					.appendDescriptionOf(finder).appendText("\n     got: ")
					.appendValue(foundElements.size()).appendText(" ")
					.appendDescriptionOf(finder).appendText("\n");

			failWith(description.toString());
		}
	}

	public void type(String input, Finder<WebElement, WebDriver> finder) {
		WebElement element = findOneElementTo("type into", finder);
		element.clear();
		element.sendKeys(input);
	}

	public void clickOn(Finder<WebElement, WebDriver> finder) {
		WebElement element = findOneElementTo("click on", finder);
		element.click();
	}

	public void clickOnFirst(Finder<WebElement, WebDriver> finder) {
		WebElement element = findFirstElementTo("click on", finder);
		element.click();
	}

	private WebElement findFirstElementTo(String action,
			Finder<WebElement, WebDriver> finder) {
		Collection<WebElement> foundElements = finder.findFrom(driver);
		if (foundElements.isEmpty()) {
			failWith("could not find element to " + action);
		}

		return foundElements.iterator().next();
	}

	private WebElement findOneElementTo(String action,
			Finder<WebElement, WebDriver> finder) {
		Collection<WebElement> foundElements = finder.findFrom(driver);
		if (foundElements.isEmpty()) {
			failWith("could not find element to " + action);
		} else if (foundElements.size() > 1) {
			failWith("did not know what to " + action + " - ambiguous");
		}

		return foundElements.iterator().next();
	}

	private void failWith(String message) throws AssertionError {
		throw new java.lang.AssertionError(message);
	}

	public void waitFor(Finder<WebElement, WebDriver> finder, long timeoutMillis) {
		long timeoutTime = clock.now() + timeoutMillis;
		while (clock.now() < timeoutTime) {
			Collection<WebElement> result = finder.findFrom(driver);
			for (WebElement webElement : result) {
				if ((webElement).isDisplayed()) {
					return; // found it
				}
			}
		}
		failWith("Element was not rendered within " + timeoutMillis + "ms");
	}
}

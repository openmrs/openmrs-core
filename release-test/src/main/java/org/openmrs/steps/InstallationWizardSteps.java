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

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openmrs.Steps;
import org.openqa.selenium.WebDriver;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.openqa.selenium.lift.Finders.*;
import static org.openqa.selenium.lift.Matchers.attribute;
import static org.openqa.selenium.lift.Matchers.text;

public class InstallationWizardSteps extends Steps {
    public InstallationWizardSteps(WebDriver driver) {
        super(driver);
    }

    @Given("I am on the $wizard")
    public void beginInstallation(String wizard) {
        String port = System.getProperty("jetty.port","8080");
        goTo("http://localhost:"+port+"/openmrs/initialsetup");
        assertPresenceOf(div().with(text(containsString(wizard))));
    }

    @When("I select the $option option")
    public void selectAdvancedOption(String option) {
        clickOn(radioButton().with(
                attribute("value", equalTo(option.toLowerCase()))));
    }

    @When("Continue")
    public void clickOnContinueButton() {
        clickOn(button().with(attribute("value", equalTo("continue"))));
    }

    @When("I select English as the language I prefer")
    public void selectLanguage(){
        selectAValueInDropDownByXpath("//select[@id=\'locale\']").selectByValue("en");
    }

    @When("click on Continue")
    @Alias("I Continue")
    public void clickOnContinueButtonByName(){
         clickOn(imageButton().with(attribute("name", equalTo("continue"))));
    }
    @Then("take me to $step of the installation wizard")
    public void verifyStep(String step) {
        waitFor(div().with(text(containsString(step))));
        assertPresenceOf(div().with(text(containsString(step))));
    }

    @Given("I am on $step of the installation wizard")
    public void onPage(String step) {
        verifyStep(step);
    }

    @When("I enter a database url and mention the database name, username, password, and port as stored in system properties as $databaseNameProp, $userProp, $passwordProp, and $portProp")
    public void enterConnectionUrl(String databaseNameProp, String userProp, String passwordProp, String portProp) {
    	
    	String database = System.getProperty(databaseNameProp, "openmrsReleaseTest");
    	String user = System.getProperty(userProp, "root");
    	String password = System.getProperty(passwordProp, "password");
    	String port = System.getProperty(portProp, "3336");

        type("jdbc:mysql://localhost:" + port + "/@DBNAME@?"
                + "autoReconnect=true&sessionVariables=storage_engine=InnoDB"
                + "&useUnicode=true&characterEncoding=UTF-8&server.initialize-user=true"
                + "&createDatabaseIfNotExist=true&server.basedir=target/database&server.datadir=target/database/data"
                + "&server.collation-server=utf8_general_ci&server.character-set-server=utf8",
                into(textbox().with(
                        attribute("name", equalTo("database_connection")))));
        
        clickOn(radioButton().with(
                attribute("name", equalTo("current_openmrs_database"))).with(attribute("value", equalTo("no"))));
        
        type(database,
                into(textbox().with(
                        attribute("name",
                                equalTo("openmrs_new_database_name")))));
        
        type(user,
                into(textbox().with(
                        attribute("name", equalTo("create_database_username")))));
        type(password,
                into(passwordtextbox().with(
                        attribute("name", equalTo("create_database_password")))));
    }


    @When("I mention username and password for the user with CREATE USER privileges as stored in system properties as $userProp and $passwordProp")
    public void enterUserName(String userProp, String passwordProp) {
    	
    	String user = System.getProperty(userProp, "root");
    	String password = System.getProperty(passwordProp, "password");
    	
        clickOn(radioButton().with(
                attribute("name", equalTo("create_tables"))).with(attribute("value", equalTo("yes"))));

        clickOn(radioButton().with(
                attribute("name", equalTo("add_demo_data"))).with(attribute("value", equalTo("yes"))));

        clickOn(radioButton().with(
                attribute("name", equalTo("current_database_user"))).with(attribute("value", equalTo("no"))));


        type(user,
                into(textbox().with(
                        attribute("name", equalTo("create_user_username")))));
        type(password,
                into(passwordtextbox().with(
                        attribute("name", equalTo("create_user_password")))));
    }

    @When("I Finish")
    public void clickOnFinishButton() {
        clickOn(button().with(attribute("value", equalTo("Finish"))));
    }

    @When("I type $password as the password confirm the same")
    public void enterOpenmrsPassword(String password) {
        type(password,
                into(passwordtextbox().with(
                        attribute("name", equalTo("new_admin_password")))));
        type(password,
                into(passwordtextbox()
                        .with(attribute("name",
                                equalTo("new_admin_password_confirm")))));
    }

    @Then("take me to login Page")
    public void verifyLoginPage() {
        waitFor(button().with(attribute("value", equalTo("Log In"))), 1800000);
        assertPresenceOf(title().with(
                text(containsString("OpenMRS - Home"))));
    }
}

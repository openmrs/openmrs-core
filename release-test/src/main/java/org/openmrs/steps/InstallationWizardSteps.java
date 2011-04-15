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
        goTo("http://localhost:8080/openmrs/initialsetup");
        assertPresenceOf(div().with(text(containsString(wizard))));
    }

    @When("I select the $option option")
    public void selectAdvancedOption(String option) {
        clickOn(radioButton().with(
                attribute("value", equalTo(option.toLowerCase()))));
    }

    @When("Continue")
    @Alias("I Continue")
    public void clickOnContinueButton() {
        clickOn(button().with(attribute("value", equalTo("Continue"))));
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

    @When("I enter a database url and mention $database as the database, $user as the user name and $password as the password")
    public void enterConnectionUrl(String database, String user, String password) {
        type("jdbc:mysql:mxj://localhost:3316/@DBNAME@?"
                + "autoReconnect=true&sessionVariables=storage_engine=InnoDB"
                + "&useUnicode=true&characterEncoding=UTF-8&server.initialize-user=true"
                + "&createDatabaseIfNotExist=true&server.basedir=target/database&server.datadir=target/database/data"
                + "&server.collation-server=utf8_general_ci&server.character-set-server=utf8",
                into(textbox().with(
                        attribute("name", equalTo("database_connection")))));
        type(database,
                into(textbox().with(
                        attribute("name",
                                equalTo("openmrs_current_database_name")))));
        clickOn(radioButton().with(
                attribute("name", equalTo("current_openmrs_database"))).with(attribute("value", equalTo("no"))));

        type(user,
                into(textbox().with(
                        attribute("name", equalTo("create_database_username")))));
        type(password,
                into(passwordtextbox().with(
                        attribute("name", equalTo("create_database_password")))));
    }

    @When("I mention $user as user name and $password as password for the user with CREATE USER privileges")
    public void enterUserName(String user, String password) {

        clickOn(radioButton().with(
                attribute("name", equalTo("create_tables"))).with(attribute("value", equalTo("yes"))));

        clickOn(radioButton().with(
                attribute("name", equalTo("add_demo_data"))).with(attribute("value", equalTo("yes"))));

        clickOn(radioButton().with(
                attribute("name", equalTo("current_database_user"))).with(attribute("value", equalTo("yes"))));


        type(user,
                into(textbox().with(
                        attribute("name", equalTo("current_database_username")))));
        type(password,
                into(passwordtextbox().with(
                        attribute("name", equalTo("current_database_password")))));
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

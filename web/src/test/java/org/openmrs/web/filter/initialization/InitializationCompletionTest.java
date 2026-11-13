/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.web.filter.initialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.UserContext;
import org.openmrs.api.context.UsernamePasswordAuthenticationScheme;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.test.jupiter.BaseContextSensitiveNonTransactionalTest;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.util.Security;
import org.openmrs.web.WebConstants;
import org.openmrs.web.filter.util.ErrorMessageConstants;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Tests the steps an install runs once the web application context is up, and how it ends when one
 * fails. They run against the standard test data, committed, with no authenticated user, as the
 * install thread runs them.
 */
@WebAppConfiguration
@ContextConfiguration(locations = { "classpath*:openmrs-servlet.xml",
        "classpath*:AltAuthSchemeTestingApplicationContext.xml" })
class InitializationCompletionTest extends BaseContextSensitiveNonTransactionalTest {

	/**
	 * User 1's password and salt as the core data seeds them, the hash of "test"
	 */
	private static final String SEEDED_PASSWORD_HASH = "4a1750c8607dfa237de36c6305715c223415189";

	private static final String SEEDED_SALT = "c788c6ad82a157b712392ca695dfcf2eed193d7f";

	private static final String CHOSEN_PASSWORD = "Chosen123";

	private static final String REPLACED_WORK_LOG_ENTRY = "Replaced the default admin password";

	private static final String INSTALLATION_SCRIPT_PROPERTY = "installation.script.property";

	@TempDir
	Path applicationDataDirectory;

	private String originalApplicationDataDirectory;

	private Properties originalRuntimeProperties;

	private boolean initializationRequiredBefore;

	private UserContext originalUserContext;

	private File runtimePropertiesFile;

	private InitializationFilter filter;

	/**
	 * The connection properties of the test database. Taken before the application data directory is
	 * moved, because the base class moves it to a temporary directory of its own whenever it hands out
	 * its runtime properties.
	 */
	private final Properties testDatabase = new Properties();

	@BeforeEach
	void setUpInstall() throws SQLException {
		testDatabase.putAll(getRuntimeProperties());
		// the install writes the runtime properties file into the application data directory
		assumeTrue(System.getenv("OPENMRS_RUNTIME_PROPERTIES_FILE") == null);
		assumeTrue(!new File(WebConstants.WEBAPP_NAME + "-runtime.properties").exists());
		originalApplicationDataDirectory = System.getProperty(OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY);
		OpenmrsUtil.setApplicationDataDirectory(applicationDataDirectory.toString());
		runtimePropertiesFile = applicationDataDirectory.resolve(WebConstants.WEBAPP_NAME + "-runtime.properties").toFile();

		originalRuntimeProperties = Context.getRuntimeProperties();
		initializationRequiredBefore = InitializationFilter.initializationRequired();
		originalUserContext = Context.getUserContext();
		filter = new InitializationFilter();
		filter.setInitializationComplete(false);

		executeUpdate("update users set password = ?, salt = ? where user_id = 1", SEEDED_PASSWORD_HASH, SEEDED_SALT);
	}

	@AfterEach
	void tearDownInstall() {
		if (filter == null) {
			// skipped before setting anything up
			return;
		}
		InitializationFilter.setInstallationStarted(false);
		filter.setInitializationComplete(!initializationRequiredBefore);
		Context.setUserContext(originalUserContext);
		Context.setRuntimeProperties(originalRuntimeProperties);
		OpenmrsUtil.setApplicationDataDirectory(originalApplicationDataDirectory);
		// these tests commit, so the next one has to start from the standard data again
		deleteAllData();
	}

	@Test
	void completeSetup_shouldReplaceTheSeededAdminPasswordWithTheChosenOne() throws Exception {
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		useAnonymousUserContext();

		assertTrue(job.completeSetup(runtimeProperties()));

		assertFalse(job.hasErrors());
		assertNotEquals(SEEDED_PASSWORD_HASH, storedAdminPassword());
		assertThat(job.wizardModel.workLog, hasItem(REPLACED_WORK_LOG_ENTRY));
		assertEquals(testDatabase.getProperty("connection.url"),
		    readRuntimeProperties(runtimePropertiesFile).getProperty("connection.url"));
		Context.authenticate(new UsernamePasswordCredentials("admin", CHOSEN_PASSWORD));
		useAnonymousUserContext();
		assertThrows(ContextAuthenticationException.class,
		    () -> Context.authenticate(new UsernamePasswordCredentials("admin", "test")));
	}

	@Test
	void completeSetup_shouldReplaceTheSeededAdminPasswordOfALockedOutAdmin() throws Exception {
		executeUpdate("insert into user_property (user_id, property, property_value) values (1, ?, ?)",
		    OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		useAnonymousUserContext();

		assertTrue(job.completeSetup(runtimeProperties()));

		assertNotEquals(SEEDED_PASSWORD_HASH, storedAdminPassword());
		assertTrue(runtimePropertiesFile.exists());
	}

	@Test
	void completeSetup_shouldReplaceTheSeededAdminPasswordWhileTheAdminPasswordIsLocked() throws Exception {
		Properties runtimeProperties = runtimeProperties();
		runtimeProperties.setProperty(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY, "true");
		// as the install sets the runtime properties before it starts the web application context
		Context.setRuntimeProperties(runtimeProperties);
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		useAnonymousUserContext();

		assertTrue(job.completeSetup(runtimeProperties));

		assertNotEquals(SEEDED_PASSWORD_HASH, storedAdminPassword());
		assertEquals("true", Context.getRuntimeProperties().getProperty(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY));
		assertEquals("true",
		    readRuntimeProperties(runtimePropertiesFile).getProperty(UserService.ADMIN_PASSWORD_LOCKED_PROPERTY));
	}

	@Test
	void completeSetup_shouldReplaceTheSeededAdminPasswordWhenTheInstallDidNotCreateTables() throws Exception {
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, false);
		useAnonymousUserContext();

		assertTrue(job.completeSetup(runtimeProperties()));

		assertNotEquals(SEEDED_PASSWORD_HASH, storedAdminPassword());
		assertTrue(runtimePropertiesFile.exists());
	}

	@Test
	void completeSetup_shouldFailAndNotWriteTheRuntimePropertiesWhenTheSeededAdminPasswordHasNoReplacement()
	        throws Exception {
		// the testing install has no admin password page
		InitializationFilter.InitializationCompletion job = job("", false);
		useAnonymousUserContext();

		assertFalse(job.completeSetup(runtimeProperties()));

		assertThat(job.getErrors(), hasKey(ErrorMessageConstants.ERROR_DB_ADM_PSDW_DEFAULT));
		assertEquals(SEEDED_PASSWORD_HASH, storedAdminPassword());
		assertFalse(runtimePropertiesFile.exists());
	}

	@Test
	void completeSetup_shouldLeaveAnAdminPasswordThatIsNotTheSeededOne() throws Exception {
		String changedPassword = Security.encodePassword("Changed123" + SEEDED_SALT);
		executeUpdate("update users set password = ? where user_id = 1", changedPassword);
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		useAnonymousUserContext();

		assertTrue(job.completeSetup(runtimeProperties()));

		assertEquals(changedPassword, storedAdminPassword());
		assertThat(job.wizardModel.workLog, not(hasItem(REPLACED_WORK_LOG_ENTRY)));
		// checking for the seeded password logs no one in, so no failed login is recorded against the admin
		assertNull(adminUserProperty(OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS));
		assertTrue(runtimePropertiesFile.exists());
	}

	@Test
	void completeSetup_shouldNotWriteTheRuntimePropertiesWhenTheImplementationIdFails() throws Exception {
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		job.wizardModel.implementationId = "TESTID";
		job.wizardModel.implementationIdName = "Test";
		job.wizardModel.implementationIdPassPhrase = "secret";
		// an id without a description is refused before the implementation id server is contacted
		job.wizardModel.implementationIdDescription = "";
		useAnonymousUserContext();

		assertFalse(job.completeSetup(runtimeProperties()));

		assertThat(job.getErrors(), hasKey(ErrorMessageConstants.ERROR_SET_INPL_ID));
		// replaced before the implementation id is set, so the seeded password is gone either way
		assertNotEquals(SEEDED_PASSWORD_HASH, storedAdminPassword());
		assertFalse(runtimePropertiesFile.exists());
	}

	@Test
	void completeSetup_shouldNotWriteTheRuntimePropertiesWhenThePasswordPolicyRefusesTheChosenPassword() throws Exception {
		// stricter than the default the wizard checked the chosen password against
		Context.getAdministrationService().setGlobalProperty(OpenmrsConstants.GP_PASSWORD_MINIMUM_LENGTH, "50");
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		useAnonymousUserContext();

		assertFalse(job.completeSetup(runtimeProperties()));

		assertThat(job.getErrors(), hasKey(ErrorMessageConstants.ERROR_COMPLETE_STARTUP));
		assertThat((String) job.getErrors().get(ErrorMessageConstants.ERROR_COMPLETE_STARTUP)[0],
		    containsString("at least 50 characters"));
		assertEquals(SEEDED_PASSWORD_HASH, storedAdminPassword());
		assertFalse(runtimePropertiesFile.exists());
	}

	@Test
	void completeSetup_shouldFailWhenTheChosenPasswordIsTheSeededOne() throws Exception {
		// a password policy lax enough to accept the seeded password, which the wizard's own checks refuse
		AdministrationService administrationService = Context.getAdministrationService();
		administrationService.setGlobalProperty(OpenmrsConstants.GP_PASSWORD_MINIMUM_LENGTH, "1");
		administrationService.setGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_UPPER_AND_LOWER_CASE, "false");
		administrationService.setGlobalProperty(OpenmrsConstants.GP_PASSWORD_REQUIRES_DIGIT, "false");
		InitializationFilter.InitializationCompletion job = job("test", true);
		useAnonymousUserContext();

		assertFalse(job.completeSetup(runtimeProperties()));

		assertThat(job.getErrors(), hasKey(ErrorMessageConstants.ERROR_COMPLETE_STARTUP));
		assertEquals("The admin user still has the seeded password.",
		    job.getErrors().get(ErrorMessageConstants.ERROR_COMPLETE_STARTUP)[0]);
		assertFalse(runtimePropertiesFile.exists());
	}

	@Test
	void completeSetup_shouldNotWriteTheRuntimePropertiesWhenUpdatingThePostgresSequencesFails() throws Exception {
		// what the PostgreSQL step throws on a driver that refuses it, which on H2 does nothing
		AdministrationService administrationService = mock(AdministrationService.class);
		doThrow(new APIException("sequences not updated")).when(administrationService).updatePostgresSequence();
		contextMockHelper.setAdministrationService(administrationService);
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		useAnonymousUserContext();

		assertFalse(job.completeSetup(runtimeProperties()));

		assertThat(job.getErrors(), hasKey(ErrorMessageConstants.ERROR_COMPLETE_STARTUP));
		// replaced before the sequences are updated, so the seeded password is gone either way
		assertNotEquals(SEEDED_PASSWORD_HASH, storedAdminPassword());
		assertFalse(runtimePropertiesFile.exists());
	}

	@Test
	void completeSetup_shouldDeleteTheInstallIncompleteFileOnceItHasWrittenTheRuntimeProperties() throws Exception {
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		assertTrue(job.markInstallIncomplete());
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
		useAnonymousUserContext();

		assertTrue(job.completeSetup(runtimeProperties()));

		assertTrue(runtimePropertiesFile.exists());
		assertFalse(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void completeSetup_shouldKeepTheInstallIncompleteFileWhenTheSeededAdminPasswordHasNoReplacement() throws Exception {
		writeExistingRuntimeProperties();
		InitializationFilter.InitializationCompletion job = job("", true);
		assertTrue(job.markInstallIncomplete());
		useAnonymousUserContext();

		assertFalse(job.completeSetup(runtimeProperties()));
		job.finishInstallation(false);

		assertEquals(SEEDED_PASSWORD_HASH, storedAdminPassword());
		assertEquals("yes", readRuntimeProperties(runtimePropertiesFile).getProperty("existing"));
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void completeSetup_shouldKeepTheInstallIncompleteFileUntilTheRuntimePropertiesAreWritten() throws Exception {
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		job.wizardModel.implementationId = "TESTID";
		job.wizardModel.implementationIdName = "Test";
		job.wizardModel.implementationIdPassPhrase = "secret";
		// an id without a description is refused before the implementation id server is contacted
		job.wizardModel.implementationIdDescription = "";
		assertTrue(job.markInstallIncomplete());
		useAnonymousUserContext();

		assertFalse(job.completeSetup(runtimeProperties()));

		assertNotEquals(SEEDED_PASSWORD_HASH, storedAdminPassword());
		assertFalse(runtimePropertiesFile.exists());
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void completeSetup_shouldKeepTheInstallIncompleteFileWhenItCannotWriteTheRuntimeProperties() throws Exception {
		writeExistingRuntimeProperties();
		// root can write a file whose write bit is cleared, so this runs only as another user
		assumeTrue(runtimePropertiesFile.setWritable(false) && !runtimePropertiesFile.canWrite());
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		assertTrue(job.markInstallIncomplete());
		useAnonymousUserContext();

		assertFalse(job.completeSetup(runtimeProperties()));

		// replaced in this database, while the runtime properties still name the database they named before
		assertNotEquals(SEEDED_PASSWORD_HASH, storedAdminPassword());
		assertTrue(InitializationFilter.getInstallIncompleteFile().exists());
	}

	@Test
	void completeSetup_shouldTakeABlankImplementationIdForNone() throws Exception {
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		job.wizardModel.implementationId = " ";
		useAnonymousUserContext();

		assertTrue(job.completeSetup(runtimeProperties()));

		assertFalse(job.hasErrors());
		assertTrue(runtimePropertiesFile.exists());
	}

	@Test
	void markInstallIncomplete_shouldStopTheInstallWhenItCannotWriteTheFile() {
		File directory = applicationDataDirectory.toFile();
		// root can write to a directory whose write bit is cleared, so this runs only as another user
		assumeTrue(directory.setWritable(false) && !directory.canWrite());
		try {
			InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);

			assertFalse(job.markInstallIncomplete());

			assertThat(job.getErrors(), hasKey(ErrorMessageConstants.ERROR_COMPLETE_STARTUP));
			assertFalse(InitializationFilter.getInstallIncompleteFile().exists());
		} finally {
			directory.setWritable(true);
		}
	}

	@Test
	void completeSetup_shouldNotRunOnceAnEarlierStepOfTheInstallReportedAnError() throws Exception {
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		job.reportError(ErrorMessageConstants.ERROR_DB_CREATE_TABLES_OR_ADD_DEMO_DATA, "chooselang.vm", "no tables");
		useAnonymousUserContext();

		assertFalse(job.completeSetup(runtimeProperties()));

		assertEquals(SEEDED_PASSWORD_HASH, storedAdminPassword());
		assertFalse(runtimePropertiesFile.exists());
	}

	@Test
	void completeSetup_shouldKeepTheRuntimePropertiesFileThatWasThereWhenWritingTheNewOneFails() throws Exception {
		writeExistingRuntimeProperties();
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		useAnonymousUserContext();

		assertFalse(job.completeSetup(failingPartWayThroughWriting(runtimeProperties())));
		job.finishInstallation(false);

		assertThat(job.getErrors(), hasKey(ErrorMessageConstants.ERROR_COMPLETE_STARTUP));
		assertEquals("yes", readRuntimeProperties(runtimePropertiesFile).getProperty("existing"));
		assertThat(filesTheInstallBeganToWrite(), empty());
	}

	@Test
	void completeSetup_shouldLeaveNoRuntimePropertiesFileWhenWritingTheFirstOneFails() throws Exception {
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		useAnonymousUserContext();

		assertFalse(job.completeSetup(failingPartWayThroughWriting(runtimeProperties())));

		// not even until the install ends
		assertFalse(runtimePropertiesFile.exists());
		assertThat(filesTheInstallBeganToWrite(), empty());
	}

	@Test
	void writeRuntimeProperties_shouldWriteTheFileALinkToTheRuntimePropertiesFileLeadsTo() throws Exception {
		Path linkedFile = Files.createDirectory(applicationDataDirectory.resolve("elsewhere")).resolve("runtime.properties");
		Files.write(linkedFile, "existing=yes\n".getBytes(StandardCharsets.UTF_8));
		try {
			Files.createSymbolicLink(runtimePropertiesFile.toPath(), linkedFile);
		} catch (IOException | UnsupportedOperationException e) {
			assumeTrue(false, "this file system or user cannot create symbolic links");
		}
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);

		job.writeRuntimeProperties(runtimeProperties());

		assertTrue(Files.isSymbolicLink(runtimePropertiesFile.toPath()));
		Properties written = readRuntimeProperties(linkedFile.toFile());
		assertEquals(testDatabase.getProperty("connection.url"), written.getProperty("connection.url"));
		assertNull(written.getProperty("existing"));
	}

	@Test
	void writeRuntimeProperties_shouldWriteTheFileALinkLeadsToThoughThatFileDoesNotExistYet() throws Exception {
		Files.createDirectory(applicationDataDirectory.resolve("elsewhere"));
		// relative, as the link's directory resolves it
		Path linkedFile = Path.of("elsewhere", "runtime.properties");
		try {
			Files.createSymbolicLink(runtimePropertiesFile.toPath(), linkedFile);
		} catch (IOException | UnsupportedOperationException e) {
			assumeTrue(false, "this file system or user cannot create symbolic links");
		}
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);

		job.writeRuntimeProperties(runtimeProperties());

		assertTrue(Files.isSymbolicLink(runtimePropertiesFile.toPath()));
		Properties written = readRuntimeProperties(applicationDataDirectory.resolve(linkedFile).toFile());
		assertEquals(testDatabase.getProperty("connection.url"), written.getProperty("connection.url"));
	}

	@Test
	void writeRuntimeProperties_shouldWriteAnExistingFileWhereItIsInADirectoryThatTakesNoNewFiles() throws Exception {
		writeExistingRuntimeProperties();
		File directory = applicationDataDirectory.toFile();
		// root can create files in a directory whose write bit is cleared, so this runs only as another user
		assumeTrue(directory.setWritable(false) && !directory.canWrite());
		try {
			InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);

			job.writeRuntimeProperties(runtimeProperties());

			Properties written = readRuntimeProperties(runtimePropertiesFile);
			assertEquals(testDatabase.getProperty("connection.url"), written.getProperty("connection.url"));
			assertNull(written.getProperty("existing"));
		} finally {
			directory.setWritable(true);
		}
	}

	@Test
	void writeRuntimeProperties_shouldLeaveNoNewFileBehindWhenItCannotTakeThePlaceOfTheRuntimePropertiesFile()
	        throws Exception {
		// a directory where the runtime properties file belongs, which no file can take the place of
		assertTrue(runtimePropertiesFile.mkdir());
		Files.write(runtimePropertiesFile.toPath().resolve("content"), new byte[] { 1 });
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);

		assertThrows(IOException.class, () -> job.writeRuntimeProperties(runtimeProperties()));

		assertTrue(Files.isDirectory(runtimePropertiesFile.toPath()));
		assertThat(filesTheInstallBeganToWrite(), empty());
	}

	@Test
	void writeRuntimeProperties_shouldPutANewFileInThePlaceOfAnExistingOneTheInstallsUserOwns() throws Exception {
		writeExistingRuntimeProperties();
		Object existingFile = Files.readAttributes(runtimePropertiesFile.toPath(), BasicFileAttributes.class).fileKey();
		// some file systems, such as Windows', do not tell which file a path names
		assumeTrue(existingFile != null);
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);

		job.writeRuntimeProperties(runtimeProperties());

		// so no start reads it written in part
		assertNotEquals(existingFile,
		    Files.readAttributes(runtimePropertiesFile.toPath(), BasicFileAttributes.class).fileKey());
		assertNull(readRuntimeProperties(runtimePropertiesFile).getProperty("existing"));
		assertThat(filesTheInstallBeganToWrite(), empty());
	}

	@Test
	void writeRuntimeProperties_shouldWriteAnExistingFileWithAnotherNameWhereItIs() throws Exception {
		// the number of names a file has is known only where files have Unix attributes
		assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("unix"));
		writeExistingRuntimeProperties();
		Path otherName = applicationDataDirectory.resolve("other-name.properties");
		Files.createLink(otherName, runtimePropertiesFile.toPath());
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);

		job.writeRuntimeProperties(runtimeProperties());

		// both names still name the one file, which holds the new properties
		Properties written = readRuntimeProperties(otherName.toFile());
		assertEquals(testDatabase.getProperty("connection.url"), written.getProperty("connection.url"));
		assertNull(written.getProperty("existing"));
		assertThat(filesTheInstallBeganToWrite(), empty());
	}

	@Test
	void writeRuntimeProperties_shouldWriteAFileOnlyItsOwnerCanRead() throws Exception {
		assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);

		job.writeRuntimeProperties(runtimeProperties());

		assertEquals(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
		    Files.getPosixFilePermissions(runtimePropertiesFile.toPath()));
		assertThat(filesTheInstallBeganToWrite(), empty());
	}

	@Test
	void finishInstallation_shouldKeepExistingRuntimePropertiesTheFailedInstallDidNotWrite() throws Exception {
		writeExistingRuntimeProperties();
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		job.reportError(ErrorMessageConstants.ERROR_DB_UPDATE_TO_LATEST, "chooselang.vm", "an update failed");

		job.finishInstallation(false);

		assertEquals("yes", readRuntimeProperties(runtimePropertiesFile).getProperty("existing"));
		assertTrue(InitializationFilter.initializationRequired());
	}

	@Test
	void finishInstallation_shouldKeepExistingRuntimePropertiesTheFailedInstallCouldNotOpen() throws Exception {
		writeExistingRuntimeProperties();
		// root can write a file whose write bit is cleared, so this runs only as another user
		assumeTrue(runtimePropertiesFile.setWritable(false) && !runtimePropertiesFile.canWrite());
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		useAnonymousUserContext();

		assertFalse(job.completeSetup(runtimeProperties()));
		job.finishInstallation(false);

		assertThat(job.getErrors(), hasKey(ErrorMessageConstants.ERROR_COMPLETE_STARTUP));
		// the setup got as far as opening the file
		assertThat((String) job.getErrors().get(ErrorMessageConstants.ERROR_COMPLETE_STARTUP)[0],
		    containsString(runtimePropertiesFile.getName()));
		assertEquals("yes", readRuntimeProperties(runtimePropertiesFile).getProperty("existing"));
		assertTrue(InitializationFilter.initializationRequired());
	}

	@Test
	void finishInstallation_shouldKeepTheRuntimePropertiesOfAnInstallThatOnlyFailedToStartOpenmrs() throws Exception {
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		useAnonymousUserContext();
		assertTrue(job.completeSetup(runtimeProperties()));
		job.reportError(ErrorMessageConstants.ERROR_MANDATORY_MOD_REQ, "chooselang.vm", "a mandatory module failed");

		job.finishInstallation(false);

		assertTrue(runtimePropertiesFile.exists());
		assertNotEquals(SEEDED_PASSWORD_HASH, storedAdminPassword());
		assertTrue(InitializationFilter.initializationRequired());
	}

	@Test
	void finishInstallation_shouldNotCompleteAnInstallThatDidNotFinishEvenWithoutAReportedError() {
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		InitializationFilter.setInstallationStarted(true);

		job.finishInstallation(false);

		// so the progress page leaves for the wizard rather than polling an install that has ended
		assertTrue(job.hasErrors());
		assertTrue(InitializationFilter.initializationRequired());
		assertFalse(InitializationFilter.isInstallationStarted());
	}

	@Test
	void finishInstallation_shouldCompleteAnInstallThatStartedOpenmrs() {
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		InitializationFilter.setInstallationStarted(true);

		job.finishInstallation(true);

		assertFalse(job.hasErrors());
		assertFalse(InitializationFilter.initializationRequired());
		assertFalse(InitializationFilter.isInstallationStarted());
	}

	@Test
	void finishInstallation_shouldPutBackTheRuntimePropertiesOpenmrsHadBeforeAFailedInstall() {
		Properties runtimePropertiesBeforeInstall = Context.getRuntimeProperties();
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		job.addRuntimeProperties(installationScriptProperty());
		assertNotEquals(runtimePropertiesBeforeInstall, Context.getRuntimeProperties());
		job.reportError(ErrorMessageConstants.ERROR_DB_UPDATE_TO_LATEST, "chooselang.vm", "an update failed");

		job.finishInstallation(false);

		assertEquals(runtimePropertiesBeforeInstall, Context.getRuntimeProperties());
	}

	@Test
	void finishInstallation_shouldPutBackTheRuntimePropertiesOpenmrsHadWhenOnlyStartingOpenmrsFailed() throws Exception {
		Properties runtimePropertiesBeforeInstall = Context.getRuntimeProperties();
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		// the test database's connection, which the setup steps use
		Properties installRuntimeProperties = runtimeProperties();
		installRuntimeProperties.putAll(installationScriptProperty());
		Properties runtimeProperties = job.addRuntimeProperties(installRuntimeProperties);
		assertNotEquals(runtimePropertiesBeforeInstall, Context.getRuntimeProperties());
		useAnonymousUserContext();
		assertTrue(job.completeSetup(runtimeProperties));
		job.reportError(ErrorMessageConstants.ERROR_MANDATORY_MOD_REQ, "chooselang.vm", "a mandatory module failed");

		job.finishInstallation(false);

		assertTrue(runtimePropertiesFile.exists());
		assertEquals(runtimePropertiesBeforeInstall, Context.getRuntimeProperties());
	}

	@Test
	void finishInstallation_shouldLeaveOpenmrsWithTheRuntimePropertiesOfAnInstallThatStartedIt() {
		InitializationFilter.InitializationCompletion job = job(CHOSEN_PASSWORD, true);
		job.addRuntimeProperties(installationScriptProperty());

		job.finishInstallation(true);

		assertEquals("yes", Context.getRuntimeProperties().getProperty(INSTALLATION_SCRIPT_PROPERTY));
	}

	/**
	 * Gives this thread a user context with no authenticated user and core's username and password
	 * authentication, which is what the install thread runs with. The web test context authenticates
	 * any password through its test scheme, and Context.logout() hands that scheme back.
	 */
	private void useAnonymousUserContext() {
		Context.setUserContext(new UserContext(new UsernamePasswordAuthenticationScheme()));
	}

	private InitializationFilter.InitializationCompletion job(String adminUserPassword, boolean createTables) {
		InitializationWizardModel model = new InitializationWizardModel();
		model.adminUserPassword = adminUserPassword;
		model.createTables = createTables;
		return filter.new InitializationCompletion(
		                                           model,
		                                           false);
	}

	/**
	 * The runtime properties an install of the test database writes
	 */
	private Properties runtimeProperties() {
		Properties runtimeProperties = new Properties();
		runtimeProperties.putAll(testDatabase);
		return runtimeProperties;
	}

	/**
	 * A runtime property that only the install has, as an installation script's property.* entries give
	 * it
	 */
	private Properties installationScriptProperty() {
		Properties properties = new Properties();
		properties.setProperty(INSTALLATION_SCRIPT_PROPERTY, "yes");
		return properties;
	}

	/**
	 * Writes a runtime properties file as one that was there before the install, told apart by its
	 * "existing" property
	 */
	private void writeExistingRuntimeProperties() throws IOException {
		Properties existing = runtimeProperties();
		existing.setProperty("existing", "yes");
		try (OutputStream out = new FileOutputStream(runtimePropertiesFile)) {
			existing.store(out, null);
		}
	}

	/**
	 * The given properties, whose writing fails once part of them is written, as when the disk fills up
	 */
	private Properties failingPartWayThroughWriting(Properties properties) {
		Properties failing = new Properties() {

			@Override
			public void store(Writer writer, String comments) throws IOException {
				writer.write("connection.url=");
				writer.flush();
				throw new IOException("No space left on device");
			}
		};
		failing.putAll(properties);
		return failing;
	}

	/**
	 * The files an install began to write into the application data directory in place of the runtime
	 * properties file
	 */
	private List<String> filesTheInstallBeganToWrite() {
		return Arrays.stream(applicationDataDirectory.toFile().list()).filter(name -> name.endsWith(".tmp"))
		        .collect(Collectors.toList());
	}

	private Properties readRuntimeProperties(File file) throws IOException {
		Properties properties = new Properties();
		try (InputStream in = new FileInputStream(file)) {
			properties.load(in);
		}
		return properties;
	}

	private String storedAdminPassword() throws SQLException {
		try (Connection connection = openConnection();
		        PreparedStatement statement = connection.prepareStatement("select password from users where user_id = 1");
		        ResultSet resultSet = statement.executeQuery()) {
			assertTrue(resultSet.next());
			return resultSet.getString(1);
		}
	}

	private String adminUserProperty(String property) throws SQLException {
		try (Connection connection = openConnection();
		        PreparedStatement statement = connection
		                .prepareStatement("select property_value from user_property where user_id = 1 and property = ?")) {
			statement.setString(1, property);
			try (ResultSet resultSet = statement.executeQuery()) {
				return resultSet.next() ? resultSet.getString(1) : null;
			}
		}
	}

	private void executeUpdate(String sql, String... parameters) throws SQLException {
		try (Connection connection = openConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
			for (int i = 0; i < parameters.length; i++) {
				statement.setString(i + 1, parameters[i]);
			}
			statement.executeUpdate();
		}
		Context.clearEntireCache();
	}

	/**
	 * A connection of its own, which commits, as the install's connection does
	 */
	private Connection openConnection() throws SQLException {
		return DriverManager.getConnection(testDatabase.getProperty("connection.url"),
		    testDatabase.getProperty("connection.username"), testDatabase.getProperty("connection.password"));
	}
}

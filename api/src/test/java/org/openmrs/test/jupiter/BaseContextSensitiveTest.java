/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.test.jupiter;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.DatabaseUnitRuntimeException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.stream.StreamingDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlProducer;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.H2Dialect;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.annotation.OpenmrsProfileExcludeFilter;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.ContextMockHelper;
import org.openmrs.api.context.Credentials;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.module.ModuleConstants;
import org.openmrs.test.Containers;
import org.openmrs.test.OpenmrsMetadataHandler;
import org.openmrs.test.SkipBaseSetup;
import org.openmrs.test.SkipBaseSetupAnnotationExecutionListener;
import org.openmrs.test.TestUtil;
import org.openmrs.util.DatabaseUtil;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.InputSource;

/**
 * This is the base for spring/context tests. Tests that NEED to use calls to the Context class and
 * use Services and/or the database should extend this class. NOTE: Tests that do not need access to
 * spring enabled services do not need this class and extending this will only slow those test cases
 * down. (because spring is started before test cases are run). Normal test cases do not need to
 * extend anything.
 * 
 * Use this class for Junit 5 tests.
 * 
 * @since 2.4.0
 */
@ContextConfiguration(locations = { "classpath:applicationContext-service.xml", "classpath*:openmrs-servlet.xml",
        "classpath*:moduleApplicationContext.xml", "classpath*:TestingApplicationContext.xml" })
@TestExecutionListeners(
	listeners = { SkipBaseSetupAnnotationExecutionListener.class,
		StartModuleExecutionListener.class },
        mergeMode = MERGE_WITH_DEFAULTS
)
@Transactional
@Rollback
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
public abstract class BaseContextSensitiveTest {
	
	private static final Logger log = LoggerFactory.getLogger(BaseContextSensitiveTest.class);
	
	/**
	 * Only the classpath/package path and filename of the initial dataset
	 */
	protected static final String INITIAL_XML_DATASET_PACKAGE_PATH = "org/openmrs/include/initialInMemoryTestDataSet.xml";
	
	protected static final String EXAMPLE_XML_DATASET_PACKAGE_PATH = "org/openmrs/include/standardTestDataset.xml";
	
	/**
	 * cached runtime properties
	 */
	protected static Properties runtimeProperties;
	
	/**
	 * Used for username/password dialog
	 */
	private static final Font font = new Font("Arial", Font.BOLD, 16);
	
	/**
	 * Our username field is outside of the getUsernameAndPassword() method so we can do our
	 * force-focus-on-the-username-field trick -- i.e., refer to the field within an anonymous
	 * TimerTask method.
	 */
	private static JTextField usernameField;
	
	/**
	 * This frame contains the password dialog box. In order to bring the frame to the front in the
	 * TimerTask method, we make it a private field
	 */
	private static Frame frame;
	
	/**
	 * Static variable to keep track of the number of times this class has been loaded (aka, number
	 * of tests already run)
	 */
	private static Integer loadCount = 0;
	
	/**
	 * Allows to determine if the DB is initialized with standard data
	 */
	private static boolean isBaseSetup;
	
	/**
	 * Stores a user authenticated for running tests which allows to discover a situation when some
	 * test authenticates as a different user and we need to revert to the original one
	 */
	private User authenticatedUser;
	
	@Autowired
	protected ApplicationContext applicationContext;
	/**
	 * Allows mocking services returned by Context. See {@link ContextMockHelper}
	 * 
	 * @since 1.11, 1.10, 1.9.9
	 */
	@InjectMocks
	protected ContextMockHelper contextMockHelper;
	
	private static volatile BaseContextSensitiveTest instance;
	
	/**
	 * Basic constructor for the super class to all openmrs api unit tests. This constructor sets up
	 * the classloader and the properties file so that by the type spring gets around to finally
	 * starting, the openmrs runtime properties are already in place A static load count is kept to
	 * count the number of times this class has been loaded.
	 * 
	 * @see #getLoadCount()
	 */
	public BaseContextSensitiveTest() {
		
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		
		if (!useInMemoryDatabase()) {
			Containers.ensureDatabaseRunning();
		}
		
		Properties props = getRuntimeProperties();
		
		log.debug("props: {}", props);
		
		Context.setRuntimeProperties(props);
		
		loadCount++;
		
		instance = this;
	}
	
	/**
	 * @since 1.11, 1.10, 1.9.9
	 */
	@AfterEach
	public void revertContextMocks() {
		contextMockHelper.revertMocks();
	}
	
	/**
	 * Modules should extend {@link BaseModuleContextSensitiveTest}, not this class. If they extend
	 * this class, then they won't work right when run in batches.
	 * 
	 * @throws Exception
	 */
	@BeforeEach
	public void checkNotModule() throws Exception {
		if (this.getClass().getPackage().toString().contains("org.openmrs.module.") && !(this instanceof BaseContextSensitiveTest)) {
			throw new RuntimeException("Module unit test classes should extend BaseModuleContextSensitiveTest, not just BaseContextSensitiveTest");
		}
	}
	
	/**
	 * Allows to ignore the test if the environment does not match the given parameters.
	 * 
	 * @param openmrsPlatformVersion
	 * @param modules
	 * @since 1.11.3, 1.10.2, 1.9.9
	 */
	public void assumeOpenmrsProfile(String openmrsPlatformVersion, String... modules) {
		OpenmrsProfileExcludeFilter filter = new OpenmrsProfileExcludeFilter();
		Map<String, Object> profile = new HashMap<>();
		profile.put("openmrsPlatformVersion", openmrsPlatformVersion);
		if (modules != null) {
			profile.put("modules", modules);
		} else {
			profile.put("modules", new String[0]);
		}
		String errorMessage = "Ignored. Expected profile: {openmrsPlatformVersion=" + openmrsPlatformVersion + ", modules=["
		        + StringUtils.join((String[]) profile.get("modules"), ", ") + "]}";
		assumeTrue(filter.matchOpenmrsProfileAttributes(profile), errorMessage);
	}
	
	/**
	 * Allows to ignore the test if the given modules are not running.
	 * 
	 * @param module in the format moduleId:version
	 * @param modules additional list of modules in the format moduleId:version
	 * @since 1.11.3, 1.10.2, 1.9.9
	 */
	public void assumeOpenmrsModules(String module, String... modules) {
		String[] allModules = ArrayUtils.addAll(modules, module);
		assumeOpenmrsProfile(null, allModules);
	}
	
	/**
	 * Allows to ignore the test if the environment does not match the given OpenMRS version.
	 * 
	 * @param openmrsPlatformVersion
	 * @since 1.11.3, 1.10.2, 1.9.9
	 */
	public void assumeOpenmrsPlatformVersion(String openmrsPlatformVersion) {
		assumeOpenmrsProfile(openmrsPlatformVersion);
	}
	
	/**
	 * Get the number of times this class has been loaded. This is a rough approx of how many tests
	 * have been run so far. This can be used to determine if the test is being run in a standalone
	 * context or if other tests have been run before.
	 * 
	 * @return number of times this class has been loaded
	 */
	public Integer getLoadCount() {
		return loadCount;
	}
	
	/**
	 * Used for runtime properties. The default is "openmrs" because most people will use that as
	 * the default. If your webapp and runtime properties are under a different name, override this
	 * method in your tests
	 * 
	 * @return String webapp name to assume when looking up the runtime properties
	 */
	public String getWebappName() {
		return "openmrs";
	}
	
	/**
	 * Mimics org.openmrs.web.Listener.getRuntimeProperties() Overrides the database connection
	 * properties if the user wants an in-memory database
	 * 
	 * @return Properties runtime
	 */
	public Properties getRuntimeProperties() {
		
		// cache the properties for subsequent calls
		if (runtimeProperties == null)
			runtimeProperties = TestUtil.getRuntimeProperties(getWebappName());
		
		// if we're using the in-memory hypersonic database, add those
		// connection properties here to override what is in the runtime
		// properties
		if (useInMemoryDatabase()) {
			runtimeProperties.setProperty(Environment.DIALECT, H2Dialect.class.getName());
			String url = "jdbc:h2:mem:openmrs;DB_CLOSE_DELAY=30;LOCK_TIMEOUT=10000;IGNORECASE=TRUE";
			runtimeProperties.setProperty(Environment.URL, url);
			runtimeProperties.setProperty(Environment.DRIVER, "org.h2.Driver");
			runtimeProperties.setProperty(Environment.USER, "sa");
			runtimeProperties.setProperty(Environment.PASS, "");
			
			// these properties need to be set in case the user has this exact
			// phrasing in their runtime file.
			runtimeProperties.setProperty("connection.username", "sa");
			runtimeProperties.setProperty("connection.password", "");
			runtimeProperties.setProperty("connection.url", url);
			
			// automatically create the tables defined in the hbm files
			runtimeProperties.setProperty(Environment.HBM2DDL_AUTO, "create-drop");
		}
		else {
			String url = System.getProperty("databaseUrl");
			String username = System.getProperty("databaseUsername");
			String password = System.getProperty("databasePassword");
			
			runtimeProperties.setProperty(Environment.URL, url);
			runtimeProperties.setProperty(Environment.DRIVER, System.getProperty("databaseDriver"));
			runtimeProperties.setProperty(Environment.USER, username);
			runtimeProperties.setProperty(Environment.PASS, password);
			runtimeProperties.setProperty(Environment.DIALECT, System.getProperty("databaseDialect"));
			
			// these properties need to be set in case the user has this exact
			// phrasing in their runtime file.
			runtimeProperties.setProperty("connection.username", username);
			runtimeProperties.setProperty("connection.password", password);
			runtimeProperties.setProperty("connection.url", url);
			
			//for the first time, automatically create the tables defined in the hbm files
			//after that, just update, if there are any changes. This is for performance reasons.
			runtimeProperties.setProperty(Environment.HBM2DDL_AUTO, "update");
		}
		
		// we don't want to try to load core modules in tests
		runtimeProperties.setProperty(ModuleConstants.IGNORE_CORE_MODULES_PROPERTY, "true");
		
		try {
			File tempappdir = File.createTempFile("appdir-for-unit-tests-", "");
			tempappdir.delete(); // so we can make it into a directory
			tempappdir.mkdir(); // turn it into a directory
			tempappdir.deleteOnExit(); // clean up when we're done with tests
			
			runtimeProperties.setProperty(OpenmrsConstants.APPLICATION_DATA_DIRECTORY_RUNTIME_PROPERTY, tempappdir
			        .getAbsolutePath());
			OpenmrsUtil.setApplicationDataDirectory(tempappdir.getAbsolutePath());
		}
		catch (IOException e) {
			log.error("Unable to create temp dir", e);
		}
		
		return runtimeProperties;
	}
	
	/**
	 * This method provides the credentials to authenticate the user that is authenticated through the base setup.
	 * This method can be overridden when setting up test application contexts that are *not* using the default authentication scheme.
	 * 
	 * @return The credentials to use for base setup authentication.
	 * @since 2.3.0
	 */
	protected Credentials getCredentials() {
		return new UsernamePasswordCredentials("admin", "test");
	}
	
	/**
	 * Authenticate to the Context. A popup box will appear asking the current user to enter
	 * credentials unless there is a junit.username and junit.password defined in the runtime
	 * properties
	 * 
	 * @throws Exception
	 */
	public void authenticate() {
		if (Context.isAuthenticated() && Context.getAuthenticatedUser().equals(authenticatedUser)) {
			return;
		}
		
		try {
			Context.authenticate(getCredentials());
			authenticatedUser = Context.getAuthenticatedUser();
			return;
		}
		catch (ContextAuthenticationException wrongCredentialsError) {
			if (useInMemoryDatabase()) {
				// if we get here the user is using some database other than the standard
				// in-memory database, prompt the user for input
				log.error("For some reason we couldn't auth as admin:test ?!", wrongCredentialsError);
			}
		}
		
		Integer attempts = 0;
		
		// TODO: how to make this a locale specific message for the user to see?
		String message = null;
		
		// only need to authenticate once per session
		while (!Context.isAuthenticated() && attempts < 3) {
			
			// look in the runtime properties for a defined username and
			// password first
			String junitusername = null;
			String junitpassword = null;
			
			try {
				Properties props = this.getRuntimeProperties();
				junitusername = props.getProperty("junit.username");
				junitpassword = props.getProperty("junit.password");
			}
			catch (Exception e) {
				// if anything happens just default to asking the user
			}
			
			String[] credentials = null;
			
			// ask the user for creds if no junit username/pass defined
			// in the runtime properties or if that username/pass failed already
			if (junitusername == null || junitpassword == null || attempts > 0) {
				credentials = askForUsernameAndPassword(message);
				// credentials are null if the user clicked "cancel" in popup
				if (credentials == null)
					return;
			} else
				credentials = new String[] { junitusername, junitpassword };
			
			// try to authenticate to the Context with either the runtime
			// defined credentials or the user supplied credentials from the
			// popup
			try {
				Context.authenticate(credentials[0], credentials[1]);
				authenticatedUser = Context.getAuthenticatedUser();
			}
			catch (ContextAuthenticationException e) {
				message = "Invalid username/password.  Try again.";
			}
			
			attempts++;
		}
	}
	
	/**
	 * Utility method for obtaining username and password through Swing interface for tests. Any
	 * tests extending the org.openmrs.BaseTest class may simply invoke this method by name.
	 * Username and password are returned in a two-member String array. If the user aborts, null is
	 * returned. <b> <em>Do not call for non-interactive tests, since this method will try to
	 * render an interactive dialog box for authentication!</em></b>
	 * 
	 * @param message string to display above username field
	 * @return Two-member String array containing username and password, respectively, or
	 *         <code>null</code> if user aborts dialog
	 */
	public static synchronized String[] askForUsernameAndPassword(String message) {
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {

		}
		
		if (message == null || "".equals(message))
			message = "Enter username/password to authenticate to OpenMRS...";
		
		JPanel panel = new JPanel(new GridBagLayout());
		JLabel usernameLabel = new JLabel("Username");
		usernameLabel.setFont(font);
		usernameField = new JTextField(20);
		usernameField.setFont(font);
		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setFont(font);
		JPasswordField passwordField = new JPasswordField(20);
		passwordField.setFont(font);
		panel.add(usernameLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST,
		        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 5, 0));
		panel.add(usernameField, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
		        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		panel.add(passwordLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.EAST,
		        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 5, 0));
		panel.add(passwordField, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.WEST,
		        GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		
		frame = new JFrame();
		Window window = new Window(frame);
		frame.setVisible(true);
		frame.setTitle("JUnit Test Credentials");
		
		// We use a TimerTask to force focus on username, but still use
		// JOptionPane for model dialog
		TimerTask later = new TimerTask() {
			
			@Override
			public void run() {
				if (frame != null) {
					// bring the dialog's window to the front
					frame.toFront();
					usernameField.grabFocus();
				}
			}
		};
		// try setting focus half a second from now
		new Timer().schedule(later, 500);
		
		// attention grabber for those people that aren't as observant
		TimerTask laterStill = new TimerTask() {
			
			@Override
			public void run() {
				if (frame != null) {
					frame.toFront(); // bring the dialog's window to the
					// front
					usernameField.grabFocus();
				}
			}
		};
		// if the user hasn't done anything in 10 seconds, tell them the window
		// is there
		new Timer().schedule(laterStill, 10000);
		
		// show the dialog box
		int response = JOptionPane.showConfirmDialog(window, panel, message, JOptionPane.OK_CANCEL_OPTION);
		
		// clear out the window so the timer doesn't screw up
		laterStill.cancel();
		frame.setVisible(false);
		window.setVisible(false);
		frame = null;
		
		// response of 2 is the cancel button, response of -1 is the little red
		// X in the top right
		return (response == 2 || response == -1 ? null : new String[] { usernameField.getText(),
		        String.valueOf(passwordField.getPassword()) });
	}
	
	/**
	 * Override this method to turn on/off the in-memory database. The default is to use the
	 * in-memory database. When this method returns false, the database defined by the runtime
	 * properties is used instead
	 * 
	 * @return true/false whether or not to use an in memory database
	 */
	public Boolean useInMemoryDatabase() {
		return !"false".equals(System.getProperty("useInMemoryDatabase"));
	}
	
	/**
	 * Get the database connection currently in use by the testing framework.
	 * <p>
	 * Note that if you commit a transaction, any changes done by a test will not be rolled back and
	 * you will need to clean up yourself by calling for example {@link #deleteAllData()}.
	 * 
	 * @return Connection jdbc connection to the database
	 */
	public Connection getConnection() {
		SessionFactory sessionFactory = (SessionFactory) applicationContext.getBean("sessionFactory");
		
		return sessionFactory.getCurrentSession().doReturningWork(connection -> connection);
	}
	
	/**
	 * This initializes the empty in-memory database with some rows in order to actually run some
	 * tests
	 *
	 * @throws SQLException
	 * @throws Exception
	 */
	public void initializeInMemoryDatabase() throws SQLException {
		//Don't allow the user to overwrite data
		if (!useInMemoryDatabase())
			throw new RuntimeException(
			        "You shouldn't be initializing a NON in-memory database. Consider unoverriding useInMemoryDatabase");

		//Because creator property in the superclass is mapped with optional set to false, the autoddl tool marks the 
		//column as not nullable but for person it is actually nullable, we need to first drop the constraint from 
		//person.creator column, historically this was to allow inserting the very first row. Ideally, this should not 
		//be necessary outside of tests because tables are created using liquibase and not autoddl
		dropNotNullConstraint("person", "creator");
		setAutoIncrementOnTablesWithNativeIfNotAssignedIdentityGenerator();
		executeDataSet(INITIAL_XML_DATASET_PACKAGE_PATH);
	}

	public void setAutoIncrementOnTablesWithNativeIfNotAssignedIdentityGenerator() throws SQLException {
		/*
		 * Hbm2ddl used in tests creates primary key columns, which are not auto incremented if
		 * NativeIfNotAssignedIdentityGenerator is used. We need to alter those columns in tests.
		 */
		List<String> tables = Collections.singletonList("concept");
		for (String table : tables) {
			getConnection().prepareStatement("ALTER TABLE " + table + " ALTER COLUMN " + table + "_id INT AUTO_INCREMENT")
					.execute();
		}
	}

	/**
	 * Drops the not null constraint from the the specified column in the specified table
	 *
	 * @param columnName the column from which to remove the constraint
	 * @param tableName the table that contains the column
	 * @throws SQLException
	 */
	protected void dropNotNullConstraint(String tableName, String columnName) throws SQLException {
		if (!useInMemoryDatabase()) {
			throw new RuntimeException("Altering column nullability is not supported for a non in-memory database");
		}
		final String sql = "ALTER TABLE " + tableName + " ALTER COLUMN " + columnName + " SET NULL";
		DatabaseUtil.executeSQL(getConnection(), sql, false);
	}

	/**
	 * Note that with the H2 DB this operation always commits an open transaction.
	 * 
	 * @param connection
	 * @throws SQLException
	 */
	protected void turnOnDBConstraints(Connection connection) throws SQLException {
		String constraintsOnSql;
		if (useInMemoryDatabase()) {
			constraintsOnSql = "SET REFERENTIAL_INTEGRITY TRUE";
		} else {
			if ("postgres".equals(System.getProperty("database"))) {
				constraintsOnSql = "SET session_replication_role = origin;";
			}
			else {
				constraintsOnSql = "SET FOREIGN_KEY_CHECKS=1;";
			}
		}
		PreparedStatement ps = connection.prepareStatement(constraintsOnSql);
		ps.execute();
		ps.close();
	}
	
	protected void turnOffDBConstraints(Connection connection) throws SQLException {
		String constraintsOffSql;
		if (useInMemoryDatabase()) {
			constraintsOffSql = "SET REFERENTIAL_INTEGRITY FALSE";
		} else {
			if ("postgres".equals(System.getProperty("database"))) {
				constraintsOffSql = "SET session_replication_role = replica;";
			}
			else {
				constraintsOffSql = "SET FOREIGN_KEY_CHECKS=0;";
			}
		}
		PreparedStatement ps = connection.prepareStatement(constraintsOffSql);
		ps.execute();
		ps.close();
	}
	
	/**
	 * Used by {@link #executeDataSet(String)} to cache the parsed xml files. This speeds up
	 * subsequent runs of the dataset
	 */
	private static Map<String, IDataSet> cachedDatasets = new HashMap<>();
	
	/**
	 * Runs the flat xml data file at the classpath location specified by
	 * <code>datasetFilename</code> This is a convenience method. It simply creates an
	 * {@link IDataSet} and calls {@link #executeDataSet(IDataSet)}
	 * 
	 * @param datasetFilename String path/filename on the classpath of the xml data set to clean
	 *            insert into the current database
	 * @see #getConnection()
	 * @see #executeDataSet(IDataSet)
	 */
	public void executeDataSet(String datasetFilename) {
		
		// try to get the given filename from the cache
		IDataSet xmlDataSetToRun = cachedDatasets.get(datasetFilename);
		
		// if we didn't find it in the cache, load it
		if (xmlDataSetToRun == null) {
			File file = new File(datasetFilename);
			
			InputStream fileInInputStreamFormat = null;
			Reader reader = null;
			try {
				try {
					// try to load the file if its a straight up path to the file or
					// if its a classpath path to the file
					if (file.exists()) {
						fileInInputStreamFormat = new FileInputStream(datasetFilename);
					} else {
						fileInInputStreamFormat = getClass().getClassLoader().getResourceAsStream(datasetFilename);
						if (fileInInputStreamFormat == null)
							throw new FileNotFoundException("Unable to find '" + datasetFilename + "' in the classpath");
					}
					
					reader = new InputStreamReader(fileInInputStreamFormat, StandardCharsets.UTF_8);
					ReplacementDataSet replacementDataSet = new ReplacementDataSet(
					        new FlatXmlDataSet(reader, false, true, false));
					replacementDataSet.addReplacementObject("[NULL]", null);
					xmlDataSetToRun = replacementDataSet;
					
					reader.close();
				}
				catch (DataSetException | IOException e) {
					throw new DatabaseUnitRuntimeException(e);
				}
			}
			finally {
				IOUtils.closeQuietly(fileInInputStreamFormat);
				IOUtils.closeQuietly(reader);
			}
			
			// cache the xmldataset for future runs of this file
			cachedDatasets.put(datasetFilename, xmlDataSetToRun);
		}
		
		executeDataSet(xmlDataSetToRun);
	}
	
	/**
	 * Runs the large flat xml dataset. It does not cache the file as opposed to
	 * {@link #executeDataSet(String)}.
	 * 
	 * @param datasetFilename
	 * @throws Exception
	 * @since 1.10
	 */
	public void executeLargeDataSet(String datasetFilename) throws Exception {
		InputStream inputStream = null;
		try {
			final File file = new File(datasetFilename);
			if (file.exists()) {
				inputStream = new FileInputStream(datasetFilename);
			} else {
				inputStream = getClass().getClassLoader().getResourceAsStream(datasetFilename);
				if (inputStream == null)
					throw new FileNotFoundException("Unable to find '" + datasetFilename + "' in the classpath");
			}
			
			final FlatXmlProducer flatXmlProducer = new FlatXmlProducer(new InputSource(inputStream));
			final StreamingDataSet streamingDataSet = new StreamingDataSet(flatXmlProducer);
			
			final ReplacementDataSet replacementDataSet = new ReplacementDataSet(streamingDataSet);
			replacementDataSet.addReplacementObject("[NULL]", null);
			
			executeDataSet(replacementDataSet);
			
			inputStream.close();
		}
		finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
	
	/**
	 * Runs the xml data file at the classpath location specified by <code>datasetFilename</code>
	 * using XmlDataSet. It simply creates an {@link IDataSet} and calls
	 * {@link #executeDataSet(IDataSet)}. <br>
	 * <br>
	 * This method is different than {@link #executeDataSet(String)} in that this one does not
	 * expect a flat file xml but instead a true XmlDataSet. <br>
	 * <br>
	 * In addition, there is no replacing of [NULL] values in strings.
	 * 
	 * @param datasetFilename String path/filename on the classpath of the xml data set to clean
	 *            insert into the current database
	 * @see #getConnection()
	 * @see #executeDataSet(IDataSet)
	 */
	public void executeXmlDataSet(String datasetFilename) throws Exception {
		
		// try to get the given filename from the cache
		IDataSet xmlDataSetToRun = cachedDatasets.get(datasetFilename);
		
		// if we didn't find it in the cache, load it
		if (xmlDataSetToRun == null) {
			File file = new File(datasetFilename);
			
			InputStream fileInInputStreamFormat = null;
			
			try {
				// try to load the file if its a straight up path to the file or
				// if its a classpath path to the file
				if (file.exists())
					fileInInputStreamFormat = new FileInputStream(datasetFilename);
				else {
					fileInInputStreamFormat = getClass().getClassLoader().getResourceAsStream(datasetFilename);
					if (fileInInputStreamFormat == null)
						throw new FileNotFoundException("Unable to find '" + datasetFilename + "' in the classpath");
				}
				
				XmlDataSet xmlDataSet = null;
				xmlDataSet = new XmlDataSet(fileInInputStreamFormat);
				xmlDataSetToRun = xmlDataSet;
				
				fileInInputStreamFormat.close();
			}
			finally {
				IOUtils.closeQuietly(fileInInputStreamFormat);
			}
			
			// cache the xmldataset for future runs of this file
			cachedDatasets.put(datasetFilename, xmlDataSetToRun);
		}
		
		executeDataSet(xmlDataSetToRun);
	}
	
	/**
	 * Run the given dataset specified by the <code>dataset</code> argument
	 * 
	 * @param dataset IDataSet to run on the current database used by Spring
	 * @see #getConnection()
	 */
	public void executeDataSet(IDataSet dataset) {
		try {
			Connection connection = getConnection();
			
			IDatabaseConnection dbUnitConn = setupDatabaseConnection(connection);
			
			//Do the actual update/insert:
			//insert new rows, update existing rows, and leave others alone
			DatabaseOperation.REFRESH.execute(dbUnitConn, dataset);
			
			if (isPostgreSQL()) {
				Context.getAdministrationService().updatePostgresSequence();
			}
		}
		catch (DatabaseUnitException | SQLException e) {
			throw new DatabaseUnitRuntimeException(e);
		}
	}
	
	protected boolean isPostgreSQL() {
		return "postgres".equals(System.getProperty("database"));
	}
	
	protected IDatabaseConnection setupDatabaseConnection(Connection connection) throws DatabaseUnitException {
		IDatabaseConnection dbUnitConn = new DatabaseConnection(connection);
		DatabaseConfig config = dbUnitConn.getConfig();
		
		if (useInMemoryDatabase()) {
			//Setup the db connection to use H2 config.
			config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
		}
		else {
			config.setProperty(DatabaseConfig.PROPERTY_METADATA_HANDLER, new OpenmrsMetadataHandler());
		}
		
		return dbUnitConn;
	}
	
	/**
	 * This is a convenience method to clear out all rows in all tables in the current connection.
	 * <p>
	 * This operation always results in a commit.
	 * 
	 * @throws Exception
	 */
	public synchronized void deleteAllData() {
		try {
			Context.clearSession();
			
			Connection connection = getConnection();
			
			turnOffDBConstraints(connection);
			
			IDatabaseConnection dbUnitConn = setupDatabaseConnection(connection);
			
			String databaseName = System.getProperty("databaseName");
			
			// find all the tables for this connection
			ResultSet resultSet = connection.getMetaData().getTables(databaseName, getSchemaPattern(), "%", new String[] {"TABLE"});
			DefaultDataSet dataset = new DefaultDataSet();
			while (resultSet.next()) {
				String tableName = resultSet.getString(3);
				dataset.addTable(new DefaultTable(tableName));
			}
			
			// do the actual deleting/truncating
			DatabaseOperation.DELETE_ALL.execute(dbUnitConn, dataset);
			
			turnOnDBConstraints(connection);
			
			connection.commit();
			
			updateSearchIndex();
			
			isBaseSetup = false;
		}
		catch (SQLException | DatabaseUnitException e) {
			throw new DatabaseUnitRuntimeException(e);
		}
	}
	
	private String getSchemaPattern() {
		if (useInMemoryDatabase()) {
			return "PUBLIC";
		}
		else {
			return "public";
		}
	}
	
	/**
	 * Method to clear the hibernate cache
	 */
	@BeforeEach
	public void clearHibernateCache() {
		SessionFactory sf = (SessionFactory) applicationContext.getBean("sessionFactory");
		sf.getCache().evictCollectionRegions();
		sf.getCache().evictEntityRegions();
	}
	
	/**
	 * This method is run before all test methods that extend this {@link BaseContextSensitiveTest}
	 * unless you annotate your method with the "@SkipBaseSetup" annotation After running this
	 * method an in-memory database will be available that has the content of the rows from
	 * {@link #INITIAL_XML_DATASET_PACKAGE_PATH} and {@link #EXAMPLE_XML_DATASET_PACKAGE_PATH} xml
	 * files. This method will also ask to be authenticated against the current Context and
	 * database. The {@link #initializeInMemoryDatabase()} method has a user of admin:test.
	 * <p>
	 * If you annotate a test with "@SkipBaseSetup", this method will call {@link #deleteAllData()},
	 * but only if you use the in memory DB.
	 * 
	 * @throws SQLException
	 * @see SkipBaseSetup
	 * @see SkipBaseSetupAnnotationExecutionListener
	 * @see #initializeInMemoryDatabase()
	 * @see #authenticate()
	 */
	@BeforeEach
	public void baseSetupWithStandardDataAndAuthentication() throws SQLException {
		// Open a session if needed
		if (!Context.isSessionOpen()) {
			Context.openSession();
		}
		
		// The skipBaseSetup flag is controlled by the @SkipBaseSetup annotation. 		if (useInMemoryDatabase()) {
		if (!skipBaseSetup) {
			if (!isBaseSetup) {
				
				deleteAllData();
				
				if (useInMemoryDatabase()) {
					initializeInMemoryDatabase();
				}
				else {
					executeDataSet(INITIAL_XML_DATASET_PACKAGE_PATH);
				}
				
				executeDataSet(EXAMPLE_XML_DATASET_PACKAGE_PATH);
				
				//Commit so that it is not rolled back after a test.
				getConnection().commit();

				updateSearchIndex();
				
				isBaseSetup = true;
			}
			
			authenticate();
		} else {
			if (isBaseSetup) {
				deleteAllData();
			}
		}
		
		Context.clearSession();
	}
	
	public Class<?>[] getIndexedTypes() {
		return new Class<?>[] { ConceptName.class, Drug.class, PersonName.class, PersonAttribute.class,
				PatientIdentifier.class};
	}
	
	/**
	 * It needs to be call if you want to do a concept search after you modify a concept in a test.
	 * It is because index is automatically updated only after transaction is committed, which
	 * happens only at the end of a test in our transactional tests.
	 */
	public void updateSearchIndex() {
		for (Class<?> indexType : getIndexedTypes()) {
			Context.updateSearchIndexForType(indexType);
		}
	}
	
	@AfterEach
	public void clearSessionAfterEachTest() {
		// clear the session to make sure nothing is cached, etc
		Context.clearSession();
		
		// needed because the authenticatedUser is the only object that sticks
		// around after tests and the clearSession call
		if (Context.isSessionOpen())
			Context.logout();
	}
	
	/**
	 * Called after each test class. This is called once per test class that extends
	 * {@link BaseContextSensitiveTest}. Needed so that "unit of work" that is the test class is
	 * surrounded by a pair of open/close session calls.
	 * 
	 * @throws Exception
	 */
	@AfterAll
	public static synchronized void closeSessionAfterEachClass() throws Exception {
		//Some tests add data via executeDataset()
		//We need to delete it in order not to interfere with others
		if (instance != null) {
			try {
				instance.deleteAllData();
			}
			catch (Exception ex) {
				//No need to worry about this
			}
			instance = null;
		}
		
		// clean up the session so we don't leak memory
		if (Context.isSessionOpen()) {
			Context.closeSession();
		}
	}
	
	/**
	 * Instance variable used by the {@link #baseSetupWithStandardDataAndAuthentication()} method to
	 * know whether the current "@Test" method has asked to be _not_ do the initialize/standard
	 * data/authenticate
	 * 
	 * @see SkipBaseSetup
	 * @see SkipBaseSetupAnnotationExecutionListener
	 * @see #baseSetupWithStandardDataAndAuthentication()
	 */
	private boolean skipBaseSetup = false;
	
	/**
	 * Don't run the {@link #setupDatabaseWithStandardData()} method. This means that the associated
	 * "@Test" must call one of these:
	 * 
	 * <pre>
	 *  * initializeInMemoryDatabase() ;
	 *  * executeDataSet(EXAMPLE_DATA_SET);
	 *  * Authenticate
	 * </pre>
	 * 
	 * on its own if any of those results are needed. This method is called before all "@Test"
	 * methods that have been annotated with the "@SkipBaseSetup" annotation.
	 * 
	 * @throws Exception
	 * @see SkipBaseSetup
	 * @see SkipBaseSetupAnnotationExecutionListener
	 * @see #baseSetupWithStandardDataAndAuthentication()
	 */
	public void skipBaseSetup() throws Exception {
		skipBaseSetup = true;
	}
	
}

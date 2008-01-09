/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.test;

import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.HSQLDialect;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.test.AbstractTransactionalSpringContextTests;

/**
 * This is the base for spring/context tests. Tests that NEED to use calls to
 * the Context class and use Services and/or the database should extend this
 * class.
 * 
 * NOTE: Tests that do not need access to spring enabled services do not need
 * this class and extending this will only slow those test cases down. (because
 * spring is started before test cases are run). Normal test cases should extend
 * {@link junit.framework.TestCase}
 * 
 */
public abstract class BaseContextSensitiveTest extends
        AbstractTransactionalSpringContextTests {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * Only the classpath/package path and filename of the initial dataset
	 */
	protected static final String INITIAL_XML_DATASET_PACKAGE_PATH = "org/openmrs/test/include/initialInMemoryTestDataSet.xml";
	
	/**
	 * cached runtime properties
	 */
	protected static Properties runtimeProperties;

	/**
	 * Used for username/password dialog
	 */
	private final Font font = new Font("Arial", Font.BOLD, 16);

	/**
	 * Our username field is outside of the getUsernameAndPassword() method so
	 * we can do our force-focus-on-the-username-field trick -- i.e., refer to
	 * the field within an anonymous TimerTask method.
	 */
	private JTextField usernameField;

	/*
	 * This frame contains the password dialog box. In order to bring the frame
	 * to the front in the TimerTask method, we make it a private field
	 */
	private Frame frame;

	/**
	 * Private variable defining whether or not the columns have been initialized
	 * in the hsql database yet (adding password and salt columns)
	 */
	protected static boolean columnsAdded = false;

	/**
	 * @see org.springframework.test.AbstractSingleSpringContextTests#getConfigLocations()
	 */
	protected String[] getConfigLocations() {
		Thread.currentThread()
		      .setContextClassLoader(OpenmrsClassLoader.getInstance());

		return new String[] { "classpath:applicationContext-service.xml" };
	}

	/**
	 * This method is called before Spring is setup, so its used to set the
	 * runtime properties on Context.
	 * 
	 * @see org.springframework.test.AbstractSpringContextTests#contextKeyString(java.lang.Object)
	 */
	protected String contextKeyString(Object contextKey) {
		Properties props = getRuntimeProperties();

		if (log.isDebugEnabled())
			log.debug("props: " + props);

		Context.setRuntimeProperties(props);

		// continue as normal
		return super.contextKeyString(contextKey);
	}

	/**
	 * Used for runtime properties. The default is "openmrs" because most people
	 * will use that as the default. If your webapp and runtime properties are
	 * under a different name, override this method in your tests
	 * 
	 * @return String webapp name to assume when looking up the runtime
	 *         properties
	 */
	public String getWebappName() {
		return "openmrs";
	}

	/**
	 * If a user wants to have their test commit to the database, then they
	 * probably do not want to be using an in-memory database. Set
	 * <code>useInMemoryDatabase</code> to false when this method is called
	 * 
	 * @see org.springframework.test.AbstractTransactionalSpringContextTests#setComplete() @
	 */
	@Override
	protected void setComplete() throws UnsupportedOperationException {
		if (useInMemoryDatabase() == true)
			throw new UnsupportedOperationException("Completing a transaction for an in-memory database does not make sense. You should override useInMemoryDatabase() to return false");
		
		super.setComplete();
	}

	/**
	 * Mimics org.openmrs.web.Listener.getRuntimeProperties()
	 * 
	 * Overrides the database connection properties if the user wants an in-memory database
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
		if (useInMemoryDatabase() == true) {
			runtimeProperties.setProperty(Environment.DIALECT, HSQLDialect.class.getName());
			runtimeProperties.setProperty(Environment.URL, "jdbc:hsqldb:mem:openmrs");
			runtimeProperties.setProperty(Environment.DRIVER, "org.hsqldb.jdbcDriver");
			runtimeProperties.setProperty(Environment.USER, "sa");
			runtimeProperties.setProperty(Environment.PASS, "");
			
			// these two properties need to be set in case the user has this exact
			// phrasing in their runtime file.  If 
			runtimeProperties.setProperty("connection.username", "sa");
			runtimeProperties.setProperty("connection.password", "");
			
			// automatically create the tables defined in the hbm files
			runtimeProperties.setProperty(Environment.HBM2DDL_AUTO, "create-drop");
		}

		return runtimeProperties;
	}

	/**
	 * Authenticate to the Context. A popup box will appear asking the current
	 * user to enter credentials unless there is a junit.username and
	 * junit.userpwd defined in the runtime properties
	 * 
	 * @throws Exception
	 */
	public void authenticate() throws Exception {
		Integer attempts = 0;

		// TODO: how to make this a locale specific message for the user to see?
		String message = null;

		// only need to authenticate once per session
		while (Context.isAuthenticated() == false && attempts < 3) {

			// look in the runtime properties for a defined username and
			// password first
			String junitusername = null;
			String junitpassword = null;

			try {
				Properties props = this.getRuntimeProperties();
				junitusername = props.getProperty("junit.username");
				junitpassword = props.getProperty("junit.password");
			} catch (Exception e) {
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
			} catch (ContextAuthenticationException e) {
				message = "Invalid username/password.  Try again.";
			}

			attempts++;
		}
	}

	/**
	 * Utility method for obtaining username and password through Swing
	 * interface for tests. Any tests extending the org.openmrs.BaseTest class
	 * may simply invoke this method by name. Username and password are returned
	 * in a two-member String array. If the user aborts, null is returned. <b>
	 * <em>Do not call for non-interactive tests, since this method will try to 
	 * render an interactive dialog box for authentication!</em></b>
	 * 
	 * @param message string to display above username field
	 * 
	 * @return Two-member String array containing username and password,
	 *         respectively, or <code>null</code> if user aborts dialog
	 */
	public synchronized String[] askForUsernameAndPassword(String message) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {

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
		panel.add(usernameLabel,
		          new GridBagConstraints(0, 0, 1, 1, 0, 0,
		                                 GridBagConstraints.EAST,
		                                 GridBagConstraints.HORIZONTAL,
		                                 new Insets(0, 0, 0, 0),
		                                 5, 0));
		panel.add(usernameField,
		          new GridBagConstraints(1, 0, 1, 1, 0, 0,
		                                 GridBagConstraints.WEST,
		                                 GridBagConstraints.HORIZONTAL,
		                                 new Insets(0, 0, 0, 0),
		                                 0, 0));
		panel.add(passwordLabel,
		          new GridBagConstraints(0, 1, 1, 1, 0, 0,
		                                 GridBagConstraints.EAST,
		                                 GridBagConstraints.HORIZONTAL,
		                                 new Insets(0, 0, 0, 0),
		                                 5, 0));
		panel.add(passwordField,
		          new GridBagConstraints(1, 1, 1, 1, 0, 0,
		                                 GridBagConstraints.WEST,
		                                 GridBagConstraints.HORIZONTAL,
		                                 new Insets(0, 0, 0, 0),
		                                 0, 0));

		frame = new JFrame();
		Window window = new Window(frame);
		frame.setVisible(true);
		frame.setTitle("JUnit Test Credentials");

		// We use a TimerTask to force focus on username, but still use
		// JOptionPane for model dialog
		TimerTask later = new TimerTask() {
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
		int response = JOptionPane.showConfirmDialog(window,
		                                             panel,
		                                             message,
		                                             JOptionPane.OK_CANCEL_OPTION);

		// clear out the window so the timer doesn't screw up
		laterStill.cancel();
		frame.setVisible(false);
		window.setVisible(false);
		frame = null;

		// response of 2 is the cancel button, response of -1 is the little red
		// X in the top right
		return (response == 2 || response == -1 ? null : new String[] {
		        usernameField.getText(),
		        String.valueOf(passwordField.getPassword()) });
	}

	/**
	 * Override this method to turn on/off the in-memory database. The default is to
	 * use the in-memory database. When this method returns false, the database
	 * defined by the runtime properties is used instead
	 * 
	 * @return true/false whether or not to use an in memory database
	 */
	public Boolean useInMemoryDatabase() {
		return true;
	}
	
	/**
	 * Get the database connection currently in use by the testing
	 * framework
	 * 
	 * @return Connection jdbc connection to the database
	 */
	@SuppressWarnings("deprecation")
    public Connection getConnection() {
		SessionFactory sessionFactory = (SessionFactory)applicationContext.getBean("sessionFactory");
		
		return sessionFactory.getCurrentSession().connection();
	}

	/**
	 * This initializes the empty in-memory hsql database with some rows in
	 * order to actually run some tests
	 */
	public void initializeInMemoryDatabase() throws Exception {
		// don't allow the user to overwrite their data
		if (useInMemoryDatabase() == false) 
			throw new Exception("You shouldn't be initializing a NON in-memory database. Consider unoverriding useInMemoryDatabase");
		
		// we only want to add columns once. Hsql won't roll back "alter table" 
		// commands
		if (columnsAdded == false) {
			Connection connection = getConnection();
			
			// add the password and salt columns to the users table
			// because they are not in the hibernate mapping files
			String sql = "alter table users add column password varchar(255)";
			PreparedStatement ps = connection.prepareStatement(sql);
			ps.execute();
			ps.close();
			sql = "alter table users add column salt varchar(255)";
			ps = connection.prepareStatement(sql);
			ps.execute();
			ps.close();
		   
	        columnsAdded  = true;
		}
		
		executeDataSet(INITIAL_XML_DATASET_PACKAGE_PATH);
	}
	
	/**
	 * Runs the flat xml data file at the classpath location specified by
	 * <code>datasetFilename</code>
	 * 
	 * This is a convenience method.  It simply creates an {@link IDataSet} and
	 * calls {@link #executeDataSet(IDataSet)}
	 * 
	 * @param datasetFilename String path/filename on the classpath of the xml 
	 * 			data set to clean insert into the current database
	 * 
	 * @see #getConnection()
	 * @see #executeDataSet(IDataSet)
	 */
	public void executeDataSet(String datasetFilename) throws Exception {
		
		File file = new File(datasetFilename);
		
		if (file.exists()) {
			executeDataSet(new FlatXmlDataSet(new FileInputStream(datasetFilename)));
		}
		else {
			InputStream stream = getClass().getClassLoader().getResourceAsStream(datasetFilename);
		
			if (stream == null)
				throw new FileNotFoundException("Unable to find '" + datasetFilename + "' in the classpath");
			
			try {
				executeDataSet(new FlatXmlDataSet(stream));
			}
			finally {
				stream.close();
			}
		}
	}
	
	/**
	 * Run the given dataset specified by the <code>dataset</code> argument
	 * 
	 * @param dataset IDataSet to run on the current database used by spring
	 * 
	 * @see #getConnection()
	 */
	public void executeDataSet(IDataSet dataset) throws Exception {
		Connection connection = getConnection();
		
		// convert the current session's connection to a dbunit connection
		IDatabaseConnection dbUnitConn = new DatabaseConnection(connection);
		
		// turn off the database constraints
		if (useInMemoryDatabase()) {
			// use the hsql datatypefactory so that boolean properties work correctly
			DatabaseConfig config = dbUnitConn.getConfig();
			config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
			                   new HsqldbDataTypeFactory());
			
			// for the hsql database 
			String sql = "SET REFERENTIAL_INTEGRITY FALSE";
			PreparedStatement ps = connection.prepareStatement(sql);
	        ps.execute();
	        ps.close();
		}
		else {
			// for the mysql database
			String sql = "SET FOREIGN_KEY_CHECKS=0;";
			PreparedStatement ps = connection.prepareStatement(sql);
	        ps.execute();
	        ps.close();
		}
		
		// do the actual update/insert:
        // insert new rows, update existing rows, and leave others alone
		DatabaseOperation.REFRESH.execute(dbUnitConn, dataset);
		
		//turn foreign key checks back on
		if (useInMemoryDatabase()) {
			// for the hsql database
			String sql = "SET REFERENTIAL_INTEGRITY TRUE";
			PreparedStatement ps = connection.prepareStatement(sql);
	        ps.execute();
	        ps.close();
		}
		else {
			// for the mysql db
			String sql = "SET FOREIGN_KEY_CHECKS=1;";
			PreparedStatement ps = connection.prepareStatement(sql);
	        ps.execute();
	        ps.close();
		}
	}
	
	/**
	 * @see org.springframework.test.AbstractSingleSpringContextTests#setDirty()
	 */
	protected void setDirty() {
		// reset the columns flag because the user has declared they want to rebuild the context
		// before the next test
		columnsAdded = false;
		
		super.setDirty();
	}
}

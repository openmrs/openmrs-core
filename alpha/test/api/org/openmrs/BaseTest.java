package org.openmrs;

import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.util.OpenmrsClassLoader;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.test.AbstractTransactionalSpringContextTests;

public abstract class BaseTest extends AbstractTransactionalSpringContextTests  {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	protected String[] getConfigLocations() {
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());
		
	    return new String[] {
	    		"classpath:applicationContext-service.xml",
	    		"classpath*:moduleApplicationContext.xml"
	    };
	}

	/**
	 * This method is called before Spring is setup, so its used to set the runtime
	 * properties on Context.
	 */
	protected String contextKeyString(Object contextKey) {
		Properties props = getRuntimeProperties();
		log.debug("props: " + props);
		Context.setRuntimeProperties(props);
		
		// continue as normal
		return super.contextKeyString(contextKey);
	}

	public void startup() {
		Context.startup(getRuntimeProperties());
	}
	
	public void shutdown() {
		Context.shutdown();
	}

	/* Used for username/password dialog */
	private static final Font font = new Font("Arial", Font.BOLD, 16);

	/*
	 * Our username field is outside of the getUsernameAndPassword() method so
	 * we can do our force-focus-on-the-username-field trick -- i.e., refer to
	 * the field within an anonymous TimerTask method.
	 */
	private JTextField usernameField;
	
	/*
	 * This frame contains the password dialog box.  In order to bring the frame
	 * to the front in the TimerTask method, we make it a private field  
	 */
	private Frame frame;
	
	/**
	 * Utility method for obtaining username and password through Swing
	 * interface for tests. Any tests extending the org.openmrs.BaseTest class
	 * may simply invoke this method by name. Username and password are returned
	 * in a two-member String array. If the user aborts, null is returned. <b><em>Do no call for
	 * non-interactive tests, since this method will try to render an
	 * interactive dialog box for authentication!</em></b>
	 * 
	 * @param message string to display above username field
	 * 
	 * @return Two-member String array containing username and password,
	 *         respectively, or <code>null</code> if user aborts dialog
	 */
	public synchronized String[] getUsernameAndPassword(String message) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		
		}
		
		if (message == null || "".equals(message))
			message = "Enter credentials...";
		
		JPanel panel = new JPanel(new GridBagLayout());
		JLabel usernameLabel = new JLabel("Username");
		usernameLabel.setFont(font);
		usernameField = new JTextField(20);
		usernameField.setFont(font);
		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setFont(font);
		JPasswordField passwordField = new JPasswordField(20);
		passwordField.setFont(font);
		panel.add(usernameLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0,
				GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 5, 0));
		panel.add(usernameField, new GridBagConstraints(1, 0, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));
		panel.add(passwordLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0,
				GridBagConstraints.EAST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 5, 0));
		panel.add(passwordField, new GridBagConstraints(1, 1, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));
		
		frame = new JFrame();
		Window window = new Window(frame);
		frame.setVisible(true);
		frame.setTitle("JUnit Test Credentials");
		
		// We use a TimerTask to force focus on username, but still use
		// JOptionPane for model dialog
		TimerTask later = new TimerTask() {
			public void run() {
				if (frame != null) {
					frame.toFront(); // bring the dialog's window to the front
					usernameField.grabFocus();
				}
			}
		};
		new Timer().schedule(later, 500); // try setting focus half a second from now
		
		// attention grabber for those people that aren't as observant
		TimerTask laterStill = new TimerTask() {
			public void run() {
				if (frame != null) {
					frame.toFront(); // bring the dialog's window to the front
					usernameField.grabFocus();
				}
			}
		};
		new Timer().schedule(laterStill, 10000); // if the user hasn't done anything in 10 seconds, tell them the window is there
		
		// show the dialog box
		int response = JOptionPane.showConfirmDialog(window, panel,
				message, JOptionPane.OK_CANCEL_OPTION);
		
		// clear out the window so the timer doesn't screw up
		laterStill.cancel();
		frame.setVisible(false);
		window.setVisible(false);
		frame = null;
		
		// response of 2 is the cancel button, response of -1 is the little red X in the top right
		return (response == 2 || response == -1 ? null : new String[] { usernameField.getText(),
				String.valueOf(passwordField.getPassword()) });
	}
	
	/**
	 * Asks the user for credentials and authenticates to the context
	 * 
	 * @throws Exception
	 */
	public void authenticate() throws Exception {
		Integer attempts = 0;
		String message = null;
		while (Context.isAuthenticated() == false && attempts < 3) {
			//System.out.println("Asking for valid credentials...");
			String[] credentials = getUsernameAndPassword(message);
			
			// credentials are null if the user clicked "cancel"
			if (credentials == null)
				return;
			
			try {
				Context.authenticate(credentials[0], credentials[1]);
			}
			catch (ContextAuthenticationException e) {
				message = "Invalid username/password.  Try again.";
			}
			
			attempts++;
		}
	}
	
	/**
	 * used for runtime properties
	 * 
	 * @return
	 */
	public String getWebappName() {
		return "openmrs";
	}
	
	/**
	 * Mimics org.openmrs.web.Listinger.getRuntimeProperties()
	 * 
	 * @return Properties
	 */
	public Properties getRuntimeProperties() {
		Properties props = new Properties();
		
		try {
			FileInputStream propertyStream = null;

			// Look for environment variable {WEBAPP.NAME}_RUNTIME_PROPERTIES_FILE
			String webapp = getWebappName().toUpperCase();
			log.debug("webapp: " + webapp);
			String env = webapp + "_RUNTIME_PROPERTIES_FILE";
			
			String filepath = System.getenv(env);

			if (filepath != null) {
				try {
					propertyStream = new FileInputStream(filepath);
				}
				catch (IOException e) { }
			}

			// env is the name of the file to look for in the directories
			String filename = webapp + "-runtime.properties";
			
			if (propertyStream == null) {
				filepath = OpenmrsUtil.getApplicationDataDirectory() + filename;
				try {
					propertyStream = new FileInputStream(filepath);
				}
				catch (IOException e) { }	
			}
			
			// look in current directory last
			if (propertyStream == null) {
				filepath = filename;
				try {
					propertyStream = new FileInputStream(filepath);
				}
				catch (IOException e) { }
			}
			
			if (propertyStream == null)
				throw new IOException("Could not open '" + filename + "' in user or local directory.");
			
			props.load(propertyStream);
			propertyStream.close();

		} catch (IOException e) {
		}
		
		return props;
	}
	
	/**
	 * Called before every 'test' method in the test class
	 * 
	 * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpBeforeTransaction()
	 */
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		Context.openSession();
	}
	
	/**
	 * Called after every 'test' method in the test class
	 * 
	 * @see org.springframework.test.AbstractTransactionalSpringContextTests#onTearDownAfterTransaction()
	 */
	@Override
	protected void onTearDownAfterTransaction() throws Exception {
		Context.closeSession();
	}
	
}

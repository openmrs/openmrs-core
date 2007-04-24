package org.openmrs.installer;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * This class has been taken from
 * http://forum.java.sun.com/thread.jspa?threadID=767307&messageID=4374260
 * 
 * @author Dirk de Jager
 * 
 */
public class MyAuthenticator extends Authenticator {

	String username;

	String password;

	// This method is called when a password-protected URL is accessed
	protected PasswordAuthentication getPasswordAuthentication() {

		// Return the information
		return new PasswordAuthentication(username, password.toCharArray());
	}

	public MyAuthenticator(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}

}

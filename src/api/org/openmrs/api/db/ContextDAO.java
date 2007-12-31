package org.openmrs.api.db;

import java.util.Properties;

import org.openmrs.User;
import org.openmrs.api.context.ContextAuthenticationException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Defines the functions that the Context needs to access the database
 * @version 1.1
 */
public interface ContextDAO {

	/**
	 * Authenticate user with the given username and password.
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws ContextAuthenticationException
	 */
	@Transactional(readOnly=true)
	public User authenticate(String username, String password)
		throws ContextAuthenticationException;

	/**
	 * Open session.
	 *
	 */
	public void openSession();

	
	/**
	 * Close session.
	 */
	public void closeSession();

	/**
	 * Clear session.
	 */
	@Transactional
	public void clearSession();
	
	/**
	 * Starts the OpenMRS System
	 * Should be called prior to any kind of activity
	 * @param Properties
	 */
	@Transactional
	public void startup(Properties props);
	
	/**
	 * Stops the OpenMRS System
	 * Should be called after all activity has ended and application is closing
	 */
	@Transactional
	public void shutdown();
	
	/**
	 * Compares core data against the current database and 
	 * inserts data into the database where necessary
	 */
	@Transactional
	public void checkCoreDataset();
	
	public void closeDatabaseConnection();
	
}

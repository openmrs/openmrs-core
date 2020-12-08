/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;

import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.util.OpenmrsConstants;

/**
 * Defines the functions that the Context needs to access the database
 */
public interface ContextDAO {
	
	/**
	 * Authenticate user with the given username and password.
	 * 
	 * @param username user's username or systemId
	 * @param password user's password
	 * @return a valid user if authentication succeeds
	 * @throws ContextAuthenticationException
	 * <strong>Should</strong> authenticate given username and password
	 * <strong>Should</strong> authenticate given systemId and password
	 * <strong>Should</strong> authenticate given systemId without hyphen and password
	 * <strong>Should</strong> not authenticate given username and incorrect password
	 * <strong>Should</strong> not authenticate given systemId and incorrect password
	 * <strong>Should</strong> not authenticate given incorrect username
	 * <strong>Should</strong> not authenticate given incorrect systemId
	 * <strong>Should</strong> not authenticate given null login
	 * <strong>Should</strong> not authenticate given empty login
	 * <strong>Should</strong> not authenticate given null password when password in database is null
	 * <strong>Should</strong> not authenticate given non null password when password in database is null
	 * <strong>Should</strong> not authenticate when password in database is empty
	 * <strong>Should</strong> give identical error messages between username and password mismatch
	 * <strong>Should</strong> lockout user after eight failed attempts
	 * <strong>Should</strong> authenticateWithCorrectHashedPassword
	 * <strong>Should</strong> authenticateWithIncorrectHashedPassword
	 * <strong>Should</strong> set uuid on user property when authentication fails with valid user
	 * <strong>Should</strong> pass regression test for 1580
	 * <strong>Should</strong> throw a ContextAuthenticationException if username is an empty string
	 * <strong>Should</strong> throw a ContextAuthenticationException if username is white space
	 */
	public User authenticate(String username, String password) throws ContextAuthenticationException;
	
	/**
	 * Gets a user given the uuid. Privilege checks are not done here because this is used by the
	 * {@link Context#getAuthenticatedUser()} method.
	 * 
	 * @param uuid uuid of user to fetch
	 * @return the User from the database
	 * @throws ContextAuthenticationException
	 */
	public User getUserByUuid(String uuid) throws ContextAuthenticationException;
	
	/**
	 * Gets a user given the username. Privilege checks are not done here because this is used by the
	 * {@link Context#getAuthenticatedUser()} or {@link Context#authenticate(org.openmrs.api.context.Credentials)} methods.
	 * 
	 * @param username The username of the user to fetch
	 * @return The matched user, null otherwise.
	 * 
	 * @since 2.3.0
	 */
	public User getUserByUsername(String username);
	
	/**
	 * Creates a new user.
	 * When the users are managed by a third-party authentication provider, it will happen that a successfully authenticated user still needs to be created in OpenMRS.
	 * This method is made available to authentication schemes to create new users on the fly.
	 * 
	 * @param user A new user to be created.
	 * @param password The password for the new user.
	 * @param roleNames A list of role names to add to the user.
	 * @return The newly created user
	 * @throws Exception 
	 * 
	 * @since 2.3.0
	 */
	public User createUser(User user, String password, List<String> roleNames) throws Exception;
	
	/**
	 * Open session.
	 */
	public void openSession();
	
	/**
	 * Close session.
	 */
	public void closeSession();
	
	/**
	 * @see org.openmrs.api.context.Context#clearSession()
	 */
	public void clearSession();
	
	/**
	 * @see org.openmrs.api.context.Context#flushSession()
	 */
	public void flushSession();
	
	/**
	 * Used to clear a cached object out of a session in the middle of a unit of work. Future
	 * updates to this object will not be saved. Future gets of this object will not fetch this
	 * cached copy
	 * 
	 * @param obj The object to evict/remove from the session
	 * @see org.openmrs.api.context.Context#evictFromSession(Object)
	 */
	public void evictFromSession(Object obj);

	/**
	 * Used to re-read the state of the given instance from the underlying database.
	 * @since 2.0
	 *
	 * @param obj The object to refresh from the database in the session
	 * @see org.openmrs.api.context.Context#refreshEntity(Object)
	 */
	public void refreshEntity(Object obj);

	/**
	 * Starts the OpenMRS System
	 * <p>
	 * Should be called prior to any kind of activity
	 * 
	 * @param props Properties
	 */
	public void startup(Properties props);
	
	/**
	 * Stops the OpenMRS System Should be called after all activity has ended and application is
	 * closing
	 */
	public void shutdown();
	
	/**
	 * Merge in the default properties defined for this database connection
	 * 
	 * @param runtimeProperties The current user specific runtime properties
	 */
	public void mergeDefaultRuntimeProperties(Properties runtimeProperties);
	
	/**
	 * Updates the search index if necessary.
	 * <p>
	 * The update is triggered if {@link OpenmrsConstants#GP_SEARCH_INDEX_VERSION} is blank
	 * or the value does not match {@link OpenmrsConstants#SEARCH_INDEX_VERSION}.
	 */
	public void setupSearchIndex();
	
	/**
	 * @see Context#updateSearchIndex()
	 */
	public void updateSearchIndex();

	/**
	 * @see Context#updateSearchIndexAsync()
	 */
	public Future<?> updateSearchIndexAsync();
	
	/**
	 * @see Context#updateSearchIndexForObject(Object)
	 */
	public void updateSearchIndexForObject(Object object);
	
	/**
	 * @see Context#updateSearchIndexForType(Class)
	 */
	public void updateSearchIndexForType(Class<?> type);
}

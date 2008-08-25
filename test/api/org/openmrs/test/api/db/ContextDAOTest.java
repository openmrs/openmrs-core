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
package org.openmrs.test.api.db;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.User;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.api.db.hibernate.HibernateContextDAO;
import org.openmrs.test.testutil.BaseContextSensitiveTest;

/**
 * This class tests the {@link ContextDAO} linked to from the Context. 
 * Currently that file is the {@link HibernateContextDAO}
 * 
 * So far we have thoroughly analyzed:
 *     public User authenticate(String, String) on 21/Aug/2008
 */
public class ContextDAOTest extends BaseContextSensitiveTest {

	private ContextDAO dao = null;
	
	@Before
	public void runExtraSetup() throws Exception {
		executeDataSet("org/openmrs/test/api/db/include/contextDAOTest.xml");
		
		if (dao == null) {
			// fetch the dao from the spring application context
			// this bean name matches the name in /metadata/spring/applicationContext-service.xml
			dao = (ContextDAO) applicationContext.getBean("contextDAO");
		}
	}
	
	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 *	 test = should authenticate given username and password
	 */
	@Test
	public void authenticate_shouldAuthenticateGivenUsernameAndPassword() throws Exception {
		User u = dao.authenticate("admin", "test");
		Assert.assertEquals("Should be the admin user", "admin", u.getUsername());
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should authenticate given systemId and password
	 */
	@Test
	public void authenticate_shouldAuthenticateGivenSystemIdAndPassword() throws Exception {
		User u = dao.authenticate("1-8", "test");
		Assert.assertEquals("Should be the 1-8 user", "1-8", u.getSystemId());
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should authenticate given systemId without hyphen and password
	 * 
	 * Fixed bug #982
	 */
	@Test
	public void authenticate_shouldAuthenticateGivenSystemIdWithoutHyphenAndPassword() throws Exception {
		User u = dao.authenticate("18", "test");
		Assert.assertEquals("Should be the 1-8 user", "1-8", u.getSystemId());
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate given username and incorrect password
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenUsernameAndIncorrectPassword()
	        throws Exception {
		dao.authenticate("admin", "wrong");
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate given systemId and incorrect password
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenSystemIdAndIncorrectPassword() throws Exception {
		dao.authenticate("1-8", "wrong");
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate given incorrect username
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenIncorrectUsername() throws Exception {
		dao.authenticate("administrator", "test");
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate given incorrect systemId
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenIncorrectSystemId() throws Exception {
		dao.authenticate("1-9", "test");
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 *	test = should not authenticate given null login
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenNullLogin() throws Exception {
		dao.authenticate(null, "test");
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate given empty login
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenEmptyLogin() throws Exception {
		dao.authenticate("", "test");
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate when password in database is null
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateWhenPasswordInDatabaseIsNull() throws Exception {
		dao.authenticate("admin", null);
	}
	
	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate given non null password when password in database is null
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenNonNullPasswordWhenPasswordInDatabaseIsNull() throws Exception {
		dao.authenticate("nullpassword", "password");
	}
	
	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate given null password when password in database is null
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateGivenNullPasswordWhenPasswordInDatabaseIsNull() throws Exception {
		dao.authenticate("nullpassword", null);
	}

	/**
	 * @verifies {@link ContextDAO#authenticate(String, String)}
	 * 	test = should not authenticate when password in database is empty
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void authenticate_shouldNotAuthenticateWhenPasswordInDatabaseIsEmpty() throws Exception {
		dao.authenticate("emptypassword", "");
	}

}

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
	public void runBeforeEachTest() throws Exception {
		initializeInMemoryDatabase();
		authenticate();
		executeDataSet("org/openmrs/test/include/exampleTestDataset.xml");
		executeDataSet("org/openmrs/test/api/db/include/contextDAOTest.xml");
		
		if (dao == null) {
			// fetch the dao from the spring application context
			// this bean name matches the name in /metadata/spring/applicationContext-service.xml
			dao = (ContextDAO) applicationContext.getBean("contextDAO");
		}
	}
	
	/**
	 * tests public User authenticate(String, String)
	 * 
	 * Authenticating with a correct username and password should succeed
	 */
	@Test
	public void shouldAuthenticateIfLoginIsUsername() throws Exception {
		User u = dao.authenticate("admin", "test");
		Assert.assertEquals("Should be the admin user", "admin", u.getUsername());
	}

	/**
	 * tests public User authenticate(String, String)
	 * 
	 * Authenticating with a correct systemId and password should succeed
	 */
	@Test
	public void shouldAuthenticateIfLoginIsSystemId() throws Exception {
		User u = dao.authenticate("1-8", "test");
		Assert.assertEquals("Should be the 1-8 user", "1-8", u.getSystemId());
	}

	/**
	 * tests public User authenticate(String, String)
	 * 
	 * Authenticating with a correct systemId without the hyphen and password should succeed
	 * 
	 * Fixed bug #982
	 */
	@Test
	public void shouldAuthenticateIfLoginIsSystemIdWithoutHyphen() throws Exception {
		User u = dao.authenticate("18", "test");
		Assert.assertEquals("Should be the 1-8 user", "1-8", u.getSystemId());
	}

	/**
	 * tests public User authenticate(String, String)
	 * 
	 * Authenticating with a correct username and an incorrect password should fail
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void shouldNotAuthenticateIfLoginIsUsernameAndPasswordIsIncorrect()
	        throws Exception {
		dao.authenticate("admin", "wrong");
	}

	/**
	 * tests public User authenticate(String, String)
	 * 
	 * Authenticating with a correct systemId and an incorrect password should fail
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void shouldNotAuthenticateIfLoginIsSystemIdAndPasswordIsIncorrect() throws Exception {
		dao.authenticate("1-8", "wrong");
	}

	/**
	 * tests public User authenticate(String, String)
	 * 
	 * Authenticating with an incorrect username should fail
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void shouldNotAuthenticateIfLoginIsIncorrectUsername() throws Exception {
		dao.authenticate("administrator", "test");
	}

	/**
	 * tests public User authenticate(String, String)
	 * 
	 * Authenticating with an incorrect systemId should fail
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void shouldNotAuthenticateIfLoginIsIncorrectSystemId() throws Exception {
		dao.authenticate("1-9", "test");
	}

	/**
	 * tests public User authenticate(String, String)
	 *
	 * Authenticating with null login should fail
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void shouldNotAuthenticateIfLoginIsNull() throws Exception {
		dao.authenticate(null, "test");
	}

	/**
	 * tests public User authenticate(String, String)
	 * 
	 * Authenticating with empty login should fail
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void shouldNotAuthenticateIfLoginIsEmpty() throws Exception {
		dao.authenticate("", "test");
	}

	/**
	 * tests public User authenticate(String, String)
	 * 
	 * Authenticating with null password should fail
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void shouldNotAuthenticateIfPasswordIsNull() throws Exception {
		dao.authenticate("admin", null);
	}
	
	/**
	 * tests public User authenticate(String, String)
	 * 
	 * Authenticating with null password in database, but given a non-null password, should fail
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void shouldNotAuthenticateWhenNullPasswordInDatabaseIfPasswordIsNotNull() throws Exception {
		dao.authenticate("nullpassword", "password");
	}
	
	/**
	 * tests public User authenticate(String, String)
	 * 
	 * Authenticating with null password in database, and given the correct password, should fail
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void shouldNotAuthenticateWhenNullPasswordInDatabaseIfPasswordIsNull() throws Exception {
		dao.authenticate("nullpassword", null);
	}

	/**
	 * tests public User authenticate(String, String)
	 * 
	 * Authenticating with empty password in database, and given the correct password, should fail
	 */
	@Test(expected=ContextAuthenticationException.class)
	public void shouldNotAuthenticateWhenEmptyPasswordInDatabase() throws Exception {
		dao.authenticate("emptypassword", "");
	}

}

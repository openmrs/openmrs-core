/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import java.util.Arrays;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.User;
import org.openmrs.api.cache.RolePrivilegeCacheTestUtil;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UsernamePasswordCredentials;
import org.openmrs.api.db.DAOException;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the security state methods of {@link UserService}.
 */
public class UserServiceSecurityStateTest extends BaseContextSensitiveTest {

	/**
	 * Provides {@code test_user}, which has no roles of its own and so holds no privilege beyond the
	 * implicit {@code Authenticated} role.
	 */
	private static final String TEST_USER_DATASET = "org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml";

	private static final String TEST_USERNAME = "test_user";

	private static final String TEST_USER_PASSWORD = "test";

	private static final Integer ADMIN_USER_ID = 1;

	private static final String PROPERTY = "test.secret";

	private static final String DENIAL = "Security state may be accessed only from a daemon thread or under the "
	        + PrivilegeConstants.MANAGE_USER_SECURITY_STATE + " proxy privilege";

	@Autowired
	private UserService service;

	@Autowired
	private SessionFactory sessionFactory;

	@BeforeEach
	public void loadTestUser() {
		executeDataSet(TEST_USER_DATASET);
		RolePrivilegeCacheTestUtil.clear();
		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_USER_SECURITY_STATE);
	}

	@AfterEach
	public void restoreAdmin() {
		Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_USER_SECURITY_STATE);
		RolePrivilegeCacheTestUtil.clear();
		Context.logout();
		authenticate();
	}

	/**
	 * @see UserService#getSecurityState(User, String)
	 */
	@Test
	public void getSecurityState_shouldFailWithoutTheManageUserSecurityStateProxyPrivilege() {
		User self = authenticateAsTestUser();

		AccessDeniedException denial = assertThrows(AccessDeniedException.class,
		    () -> service.getSecurityState(self, PROPERTY));

		assertEquals(DENIAL, denial.getMessage());
	}

	/**
	 * @see UserService#getSecurityState(User, String)
	 */
	@Test
	public void getSecurityState_shouldFailForASuperuserWithoutTheProxyPrivilege() {
		User admin = admin();
		Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_USER_SECURITY_STATE);

		AccessDeniedException denial = assertThrows(AccessDeniedException.class,
		    () -> service.getSecurityState(admin, PROPERTY));

		assertEquals(DENIAL, denial.getMessage());
	}

	/**
	 * @see UserService#getSecurityState(User, String)
	 */
	@Test
	public void getSecurityState_shouldAcceptTheProxyPrivilegeRegardlessOfCase() {
		User admin = admin();
		service.saveSecurityState(admin, PROPERTY, "3");
		Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_USER_SECURITY_STATE);
		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_USER_SECURITY_STATE.toUpperCase());

		assertEquals("3", service.getSecurityState(admin, PROPERTY));
	}

	/**
	 * @see UserService#saveSecurityState(User, String, String)
	 */
	@Test
	public void setSecurityState_shouldFailWithoutTheManageUserSecurityStateProxyPrivilege() {
		User self = authenticateAsTestUser();

		AccessDeniedException denial = assertThrows(AccessDeniedException.class,
		    () -> service.saveSecurityState(self, PROPERTY, "value"));

		assertEquals(DENIAL, denial.getMessage());
	}

	/**
	 * @see UserService#saveSecurityStateIfValueMatches(User, String, String, String)
	 */
	@Test
	public void compareAndSetSecurityState_shouldFailWithoutTheManageUserSecurityStateProxyPrivilege() {
		User self = authenticateAsTestUser();

		AccessDeniedException denial = assertThrows(AccessDeniedException.class,
		    () -> service.saveSecurityStateIfValueMatches(self, PROPERTY, null, "value"));

		assertEquals(DENIAL, denial.getMessage());
	}

	/**
	 * @see UserService#deleteSecurityStateIfValueMatches(User, String, String)
	 */
	@Test
	public void deleteSecurityStateIfValueMatches_shouldFailWithoutTheManageUserSecurityStateProxyPrivilege() {
		User self = authenticateAsTestUser();

		AccessDeniedException denial = assertThrows(AccessDeniedException.class,
		    () -> service.deleteSecurityStateIfValueMatches(self, PROPERTY, "value"));

		assertEquals(DENIAL, denial.getMessage());
	}

	/**
	 * @see UserService#deleteSecurityState(User, String)
	 */
	@Test
	public void deleteSecurityState_shouldFailWithoutTheManageUserSecurityStateProxyPrivilege() {
		User self = authenticateAsTestUser();

		AccessDeniedException denial = assertThrows(AccessDeniedException.class,
		    () -> service.deleteSecurityState(self, PROPERTY));

		assertEquals(DENIAL, denial.getMessage());
	}

	/**
	 * @see UserService#incrementSecurityState(User, String)
	 */
	@Test
	public void incrementSecurityState_shouldFailWithoutTheManageUserSecurityStateProxyPrivilege() {
		User self = authenticateAsTestUser();

		AccessDeniedException denial = assertThrows(AccessDeniedException.class,
		    () -> service.incrementSecurityState(self, PROPERTY));

		assertEquals(DENIAL, denial.getMessage());
	}

	/**
	 * @see UserService#getSecurityState(User, String)
	 */
	@Test
	public void getSecurityState_shouldReadTheValueFromTheDatabase() {
		service.saveSecurityState(admin(), PROPERTY, "value");
		Context.clearSession();

		assertEquals("value", service.getSecurityState(admin(), PROPERTY));
	}

	/**
	 * @see UserService#getSecurityState(User, String)
	 */
	@Test
	public void getSecurityState_shouldReturnNullWhenTheUserHasNoSuchEntry() {
		assertNull(service.getSecurityState(admin(), PROPERTY));
	}

	/**
	 * @see UserService#getSecurityState(User, String)
	 */
	@Test
	public void getSecurityState_shouldFailForANullUser() {
		assertThrows(APIException.class, () -> service.getSecurityState(null, PROPERTY));
	}

	/**
	 * @see UserService#getSecurityState(User, String)
	 */
	@Test
	public void getSecurityState_shouldFailForAnUnsavedUser() {
		assertThrows(APIException.class, () -> service.getSecurityState(new User(), PROPERTY));
	}

	/**
	 * @see UserService#getSecurityState(User, String)
	 */
	@Test
	public void getSecurityState_shouldReadTheCallersOwnEntryUnderAProxyPrivilege() {
		User testUser = Context.getUserService().getUserByUsername(TEST_USERNAME);
		service.saveSecurityState(testUser, PROPERTY, "value");
		User self = authenticateAsTestUser();

		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_USER_SECURITY_STATE);

		assertEquals("value", service.getSecurityState(self, PROPERTY));
	}

	/**
	 * @see UserService#saveSecurityState(User, String, String)
	 */
	@Test
	public void setSecurityState_shouldCreateTheEntryWhenItDoesNotExist() {
		service.saveSecurityState(admin(), PROPERTY, "value");

		assertEquals("value", service.getSecurityState(admin(), PROPERTY));
	}

	/**
	 * @see UserService#saveSecurityState(User, String, String)
	 */
	@Test
	public void setSecurityState_shouldReplaceTheValueOfAnExistingEntry() {
		service.saveSecurityState(admin(), PROPERTY, "value");

		service.saveSecurityState(admin(), PROPERTY, "other");

		assertEquals("other", service.getSecurityState(admin(), PROPERTY));
	}

	/**
	 * @see UserService#saveSecurityState(User, String, String)
	 */
	@Test
	public void setSecurityState_shouldRecordTheCallerAsTheCreatorOfANewEntry() {
		User admin = admin();
		User caller = authenticateAsTestUser();
		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_USER_SECURITY_STATE);

		service.saveSecurityState(admin, PROPERTY, "value");

		assertEquals(Arrays.asList(caller.getUserId(), null), auditOf(PROPERTY));
	}

	/**
	 * @see UserService#saveSecurityState(User, String, String)
	 */
	@Test
	public void setSecurityState_shouldRecordTheCallerAsTheUserWhoChangedAnExistingEntry() {
		User admin = admin();
		service.saveSecurityState(admin, PROPERTY, "value");
		User caller = authenticateAsTestUser();
		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_USER_SECURITY_STATE);

		service.saveSecurityState(admin, PROPERTY, "other");

		assertEquals(List.of(ADMIN_USER_ID, caller.getUserId()), auditOf(PROPERTY));
	}

	/**
	 * @see UserService#saveSecurityState(User, String, String)
	 */
	@Test
	public void setSecurityState_shouldNotStoreTheEntryAsAUserProperty() {
		service.saveSecurityState(admin(), PROPERTY, "value");
		Context.flushSession();
		Context.clearSession();

		assertFalse(Context.getUserService().getUser(ADMIN_USER_ID).getUserProperties().containsKey(PROPERTY));
	}

	/**
	 * @see UserService#saveSecurityState(User, String, String)
	 */
	@Test
	public void setSecurityState_shouldFailForABlankValue() {
		assertThrows(ValidationException.class, () -> service.saveSecurityState(admin(), PROPERTY, " "));
	}

	/**
	 * @see UserService#saveSecurityState(User, String, String)
	 */
	@Test
	public void setSecurityState_shouldStoreAValueLongerThan4000Characters() {
		String value = "x".repeat(5000);

		service.saveSecurityState(admin(), PROPERTY, value);

		assertEquals(value, service.getSecurityState(admin(), PROPERTY));
	}

	/**
	 * @see UserService#saveSecurityState(User, String, String)
	 */
	@Test
	public void setSecurityState_shouldFailForABlankProperty() {
		assertThrows(ValidationException.class, () -> service.saveSecurityState(admin(), " ", "value"));
	}

	/**
	 * @see UserService#saveSecurityState(User, String, String)
	 */
	@Test
	public void setSecurityState_shouldFailForAnUnsavedUser() {
		assertThrows(ValidationException.class, () -> service.saveSecurityState(new User(), PROPERTY, "value"));
	}

	/**
	 * @see UserService#saveSecurityStateIfValueMatches(User, String, String, String)
	 */
	@Test
	public void compareAndSetSecurityState_shouldCreateTheEntryWhenExpectedValueIsNullAndTheEntryDoesNotExist() {
		assertTrue(service.saveSecurityStateIfValueMatches(admin(), PROPERTY, null, "value"));
	}

	/**
	 * @see UserService#saveSecurityStateIfValueMatches(User, String, String, String)
	 */
	@Test
	public void compareAndSetSecurityState_shouldNotCreateTheEntryWhenExpectedValueIsNullAndTheEntryExists() {
		service.saveSecurityState(admin(), PROPERTY, "value");

		assertFalse(service.saveSecurityStateIfValueMatches(admin(), PROPERTY, null, "other"));
	}

	/**
	 * @see UserService#saveSecurityStateIfValueMatches(User, String, String, String)
	 */
	@Test
	public void compareAndSetSecurityState_shouldReplaceTheValueWhenTheCurrentValueMatches() {
		service.saveSecurityState(admin(), PROPERTY, "value");

		service.saveSecurityStateIfValueMatches(admin(), PROPERTY, "value", "other");

		assertEquals("other", service.getSecurityState(admin(), PROPERTY));
	}

	/**
	 * @see UserService#saveSecurityStateIfValueMatches(User, String, String, String)
	 */
	@Test
	public void compareAndSetSecurityState_shouldNotReplaceTheValueWhenTheCurrentValueDoesNotMatch() {
		service.saveSecurityState(admin(), PROPERTY, "value");

		assertFalse(service.saveSecurityStateIfValueMatches(admin(), PROPERTY, "stale", "other"));
	}

	/**
	 * @see UserService#deleteSecurityStateIfValueMatches(User, String, String)
	 */
	@Test
	public void deleteSecurityStateIfValueMatches_shouldRemoveTheEntryWhenTheCurrentValueMatches() {
		service.saveSecurityState(admin(), PROPERTY, "value");

		service.deleteSecurityStateIfValueMatches(admin(), PROPERTY, "value");

		assertNull(service.getSecurityState(admin(), PROPERTY));
	}

	/**
	 * @see UserService#deleteSecurityStateIfValueMatches(User, String, String)
	 */
	@Test
	public void deleteSecurityStateIfValueMatches_shouldNotRemoveTheEntryWhenTheCurrentValueDoesNotMatch() {
		service.saveSecurityState(admin(), PROPERTY, "value");

		assertFalse(service.deleteSecurityStateIfValueMatches(admin(), PROPERTY, "stale"));
	}

	/**
	 * @see UserService#deleteSecurityStateIfValueMatches(User, String, String)
	 */
	@Test
	public void deleteSecurityStateIfValueMatches_shouldFailForABlankExpectedValue() {
		assertThrows(ValidationException.class, () -> service.deleteSecurityStateIfValueMatches(admin(), PROPERTY, ""));
	}

	/**
	 * @see UserService#deleteSecurityState(User, String)
	 */
	@Test
	public void deleteSecurityState_shouldRemoveTheEntry() {
		service.saveSecurityState(admin(), PROPERTY, "value");

		service.deleteSecurityState(admin(), PROPERTY);

		assertNull(service.getSecurityState(admin(), PROPERTY));
	}

	/**
	 * @see UserService#deleteSecurityState(User, String)
	 */
	@Test
	public void deleteSecurityState_shouldReturnFalseWhenTheUserHasNoSuchEntry() {
		assertFalse(service.deleteSecurityState(admin(), PROPERTY));
	}

	/**
	 * @see UserService#deleteSecurityState(User, String)
	 */
	@Test
	public void deleteSecurityState_shouldFailForANullUser() {
		assertThrows(APIException.class, () -> service.deleteSecurityState(null, PROPERTY));
	}

	/**
	 * @see UserService#deleteSecurityState(User, String)
	 */
	@Test
	public void deleteSecurityState_shouldFailForAnUnsavedUser() {
		assertThrows(APIException.class, () -> service.deleteSecurityState(new User(), PROPERTY));
	}

	/**
	 * @see UserService#incrementSecurityState(User, String)
	 */
	@Test
	public void incrementSecurityState_shouldCreateTheEntryWithTheValueOne() {
		assertEquals(1, service.incrementSecurityState(admin(), PROPERTY));
	}

	/**
	 * @see UserService#incrementSecurityState(User, String)
	 */
	@Test
	public void incrementSecurityState_shouldAddOneToTheCurrentValue() {
		service.saveSecurityState(admin(), PROPERTY, "41");

		assertEquals(42, service.incrementSecurityState(admin(), PROPERTY));
	}

	/**
	 * @see UserService#incrementSecurityState(User, String)
	 */
	@Test
	public void incrementSecurityState_shouldFailWhenTheCurrentValueIsNotANumber() {
		service.saveSecurityState(admin(), PROPERTY, "not a number");

		assertThrows(DAOException.class, () -> service.incrementSecurityState(admin(), PROPERTY));
	}

	/**
	 * @see UserService#incrementSecurityState(User, String)
	 */
	@Test
	public void incrementSecurityState_shouldFailWhenTheCurrentValueIsTheLargestLong() {
		service.saveSecurityState(admin(), PROPERTY, String.valueOf(Long.MAX_VALUE));

		assertThrows(DAOException.class, () -> service.incrementSecurityState(admin(), PROPERTY));
	}

	private User admin() {
		return Context.getUserService().getUser(ADMIN_USER_ID);
	}

	private List<Integer> auditOf(String property) {
		Object[] row = sessionFactory.getCurrentSession()
		        .createNativeQuery(
		            "select creator, changed_by from user_security_state where user_id = ?1 and property = ?2",
		            Object[].class)
		        .setParameter(1, ADMIN_USER_ID).setParameter(2, property).getSingleResult();
		return Arrays.asList((Integer) row[0], (Integer) row[1]);
	}

	private User authenticateAsTestUser() {
		Context.logout();
		Context.authenticate(new UsernamePasswordCredentials(TEST_USERNAME, TEST_USER_PASSWORD));
		return Context.getAuthenticatedUser();
	}
}

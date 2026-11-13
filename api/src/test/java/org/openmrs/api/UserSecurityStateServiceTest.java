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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link UserSecurityStateService}.
 */
public class UserSecurityStateServiceTest extends BaseContextSensitiveTest {

	/**
	 * Provides {@code test_user}, which has no roles of its own and so holds no privilege beyond the
	 * implicit {@code Authenticated} role.
	 */
	private static final String TEST_USER_DATASET = "org/openmrs/api/include/AdministrationServiceTest-globalproperties.xml";

	private static final String TEST_USERNAME = "test_user";

	private static final String TEST_USER_PASSWORD = "test";

	private static final Integer ADMIN_USER_ID = 1;

	private static final String PROPERTY = "test.secret";

	private static final String DENIAL = "Privileges required: " + PrivilegeConstants.MANAGE_USER_SECURITY_STATE;

	@Autowired
	private UserSecurityStateService service;

	@Autowired
	private SessionFactory sessionFactory;

	@BeforeEach
	public void loadTestUser() {
		executeDataSet(TEST_USER_DATASET);
		RolePrivilegeCacheTestUtil.clear();
	}

	@AfterEach
	public void restoreAdmin() {
		Context.removeProxyPrivilege(PrivilegeConstants.MANAGE_USER_SECURITY_STATE);
		RolePrivilegeCacheTestUtil.clear();
		Context.logout();
		authenticate();
	}

	/**
	 * @see UserSecurityStateService#getValue(User, String)
	 */
	@Test
	public void getValue_shouldFailWithoutTheManageUserSecurityStatePrivilege() {
		User self = authenticateAsTestUser();

		APIAuthenticationException denial = assertThrows(APIAuthenticationException.class,
		    () -> service.getValue(self, PROPERTY));

		assertEquals(DENIAL, denial.getMessage());
	}

	/**
	 * @see UserSecurityStateService#setValue(User, String, String)
	 */
	@Test
	public void setValue_shouldFailWithoutTheManageUserSecurityStatePrivilege() {
		User self = authenticateAsTestUser();

		APIAuthenticationException denial = assertThrows(APIAuthenticationException.class,
		    () -> service.setValue(self, PROPERTY, "value"));

		assertEquals(DENIAL, denial.getMessage());
	}

	/**
	 * @see UserSecurityStateService#compareAndSet(User, String, String, String)
	 */
	@Test
	public void compareAndSet_shouldFailWithoutTheManageUserSecurityStatePrivilege() {
		User self = authenticateAsTestUser();

		APIAuthenticationException denial = assertThrows(APIAuthenticationException.class,
		    () -> service.compareAndSet(self, PROPERTY, null, "value"));

		assertEquals(DENIAL, denial.getMessage());
	}

	/**
	 * @see UserSecurityStateService#deleteIfValueMatches(User, String, String)
	 */
	@Test
	public void deleteIfValueMatches_shouldFailWithoutTheManageUserSecurityStatePrivilege() {
		User self = authenticateAsTestUser();

		APIAuthenticationException denial = assertThrows(APIAuthenticationException.class,
		    () -> service.deleteIfValueMatches(self, PROPERTY, "value"));

		assertEquals(DENIAL, denial.getMessage());
	}

	/**
	 * @see UserSecurityStateService#delete(User, String)
	 */
	@Test
	public void delete_shouldFailWithoutTheManageUserSecurityStatePrivilege() {
		User self = authenticateAsTestUser();

		APIAuthenticationException denial = assertThrows(APIAuthenticationException.class,
		    () -> service.delete(self, PROPERTY));

		assertEquals(DENIAL, denial.getMessage());
	}

	/**
	 * @see UserSecurityStateService#increment(User, String)
	 */
	@Test
	public void increment_shouldFailWithoutTheManageUserSecurityStatePrivilege() {
		User self = authenticateAsTestUser();

		APIAuthenticationException denial = assertThrows(APIAuthenticationException.class,
		    () -> service.increment(self, PROPERTY));

		assertEquals(DENIAL, denial.getMessage());
	}

	/**
	 * @see UserSecurityStateService#getValue(User, String)
	 */
	@Test
	public void getValue_shouldReadTheValueFromTheDatabase() {
		service.setValue(admin(), PROPERTY, "value");
		Context.clearSession();

		assertEquals("value", service.getValue(admin(), PROPERTY));
	}

	/**
	 * @see UserSecurityStateService#getValue(User, String)
	 */
	@Test
	public void getValue_shouldReturnNullWhenTheUserHasNoSuchEntry() {
		assertNull(service.getValue(admin(), PROPERTY));
	}

	/**
	 * @see UserSecurityStateService#getValue(User, String)
	 */
	@Test
	public void getValue_shouldReadTheCallersOwnEntryUnderAProxyPrivilege() {
		User testUser = Context.getUserService().getUserByUsername(TEST_USERNAME);
		service.setValue(testUser, PROPERTY, "value");
		User self = authenticateAsTestUser();

		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_USER_SECURITY_STATE);

		assertEquals("value", service.getValue(self, PROPERTY));
	}

	/**
	 * @see UserSecurityStateService#setValue(User, String, String)
	 */
	@Test
	public void setValue_shouldCreateTheEntryWhenItDoesNotExist() {
		service.setValue(admin(), PROPERTY, "value");

		assertEquals("value", service.getValue(admin(), PROPERTY));
	}

	/**
	 * @see UserSecurityStateService#setValue(User, String, String)
	 */
	@Test
	public void setValue_shouldReplaceTheValueOfAnExistingEntry() {
		service.setValue(admin(), PROPERTY, "value");

		service.setValue(admin(), PROPERTY, "other");

		assertEquals("other", service.getValue(admin(), PROPERTY));
	}

	/**
	 * @see UserSecurityStateService#setValue(User, String, String)
	 */
	@Test
	public void setValue_shouldRecordTheCallerAsTheCreatorOfANewEntry() {
		User admin = admin();
		User caller = authenticateAsTestUser();
		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_USER_SECURITY_STATE);

		service.setValue(admin, PROPERTY, "value");

		assertEquals(Arrays.asList(caller.getUserId(), null), auditOf(PROPERTY));
	}

	/**
	 * @see UserSecurityStateService#setValue(User, String, String)
	 */
	@Test
	public void setValue_shouldRecordTheCallerAsTheUserWhoChangedAnExistingEntry() {
		User admin = admin();
		service.setValue(admin, PROPERTY, "value");
		User caller = authenticateAsTestUser();
		Context.addProxyPrivilege(PrivilegeConstants.MANAGE_USER_SECURITY_STATE);

		service.setValue(admin, PROPERTY, "other");

		assertEquals(List.of(ADMIN_USER_ID, caller.getUserId()), auditOf(PROPERTY));
	}

	/**
	 * @see UserSecurityStateService#setValue(User, String, String)
	 */
	@Test
	public void setValue_shouldNotStoreTheEntryAsAUserProperty() {
		service.setValue(admin(), PROPERTY, "value");
		Context.flushSession();
		Context.clearSession();

		assertFalse(Context.getUserService().getUser(ADMIN_USER_ID).getUserProperties().containsKey(PROPERTY));
	}

	/**
	 * @see UserSecurityStateService#setValue(User, String, String)
	 */
	@Test
	public void setValue_shouldFailForABlankValue() {
		assertThrows(ValidationException.class, () -> service.setValue(admin(), PROPERTY, " "));
	}

	/**
	 * @see UserSecurityStateService#setValue(User, String, String)
	 */
	@Test
	public void setValue_shouldStoreAValueLongerThan4000Characters() {
		String value = "x".repeat(5000);

		service.setValue(admin(), PROPERTY, value);

		assertEquals(value, service.getValue(admin(), PROPERTY));
	}

	/**
	 * @see UserSecurityStateService#setValue(User, String, String)
	 */
	@Test
	public void setValue_shouldFailForABlankProperty() {
		assertThrows(ValidationException.class, () -> service.setValue(admin(), " ", "value"));
	}

	/**
	 * @see UserSecurityStateService#setValue(User, String, String)
	 */
	@Test
	public void setValue_shouldFailForAnUnsavedUser() {
		assertThrows(ValidationException.class, () -> service.setValue(new User(), PROPERTY, "value"));
	}

	/**
	 * @see UserSecurityStateService#compareAndSet(User, String, String, String)
	 */
	@Test
	public void compareAndSet_shouldCreateTheEntryWhenExpectedValueIsNullAndTheEntryDoesNotExist() {
		assertTrue(service.compareAndSet(admin(), PROPERTY, null, "value"));
	}

	/**
	 * @see UserSecurityStateService#compareAndSet(User, String, String, String)
	 */
	@Test
	public void compareAndSet_shouldNotCreateTheEntryWhenExpectedValueIsNullAndTheEntryExists() {
		service.setValue(admin(), PROPERTY, "value");

		assertFalse(service.compareAndSet(admin(), PROPERTY, null, "other"));
	}

	/**
	 * @see UserSecurityStateService#compareAndSet(User, String, String, String)
	 */
	@Test
	public void compareAndSet_shouldReplaceTheValueWhenTheCurrentValueMatches() {
		service.setValue(admin(), PROPERTY, "value");

		service.compareAndSet(admin(), PROPERTY, "value", "other");

		assertEquals("other", service.getValue(admin(), PROPERTY));
	}

	/**
	 * @see UserSecurityStateService#compareAndSet(User, String, String, String)
	 */
	@Test
	public void compareAndSet_shouldNotReplaceTheValueWhenTheCurrentValueDoesNotMatch() {
		service.setValue(admin(), PROPERTY, "value");

		assertFalse(service.compareAndSet(admin(), PROPERTY, "stale", "other"));
	}

	/**
	 * @see UserSecurityStateService#deleteIfValueMatches(User, String, String)
	 */
	@Test
	public void deleteIfValueMatches_shouldRemoveTheEntryWhenTheCurrentValueMatches() {
		service.setValue(admin(), PROPERTY, "value");

		service.deleteIfValueMatches(admin(), PROPERTY, "value");

		assertNull(service.getValue(admin(), PROPERTY));
	}

	/**
	 * @see UserSecurityStateService#deleteIfValueMatches(User, String, String)
	 */
	@Test
	public void deleteIfValueMatches_shouldNotRemoveTheEntryWhenTheCurrentValueDoesNotMatch() {
		service.setValue(admin(), PROPERTY, "value");

		assertFalse(service.deleteIfValueMatches(admin(), PROPERTY, "stale"));
	}

	/**
	 * @see UserSecurityStateService#deleteIfValueMatches(User, String, String)
	 */
	@Test
	public void deleteIfValueMatches_shouldFailForABlankExpectedValue() {
		assertThrows(ValidationException.class, () -> service.deleteIfValueMatches(admin(), PROPERTY, ""));
	}

	/**
	 * @see UserSecurityStateService#delete(User, String)
	 */
	@Test
	public void delete_shouldRemoveTheEntry() {
		service.setValue(admin(), PROPERTY, "value");

		service.delete(admin(), PROPERTY);

		assertNull(service.getValue(admin(), PROPERTY));
	}

	/**
	 * @see UserSecurityStateService#delete(User, String)
	 */
	@Test
	public void delete_shouldReturnFalseWhenTheUserHasNoSuchEntry() {
		assertFalse(service.delete(admin(), PROPERTY));
	}

	/**
	 * @see UserSecurityStateService#increment(User, String)
	 */
	@Test
	public void increment_shouldCreateTheEntryWithTheValueOne() {
		assertEquals(1, service.increment(admin(), PROPERTY));
	}

	/**
	 * @see UserSecurityStateService#increment(User, String)
	 */
	@Test
	public void increment_shouldAddOneToTheCurrentValue() {
		service.setValue(admin(), PROPERTY, "41");

		assertEquals(42, service.increment(admin(), PROPERTY));
	}

	/**
	 * @see UserSecurityStateService#increment(User, String)
	 */
	@Test
	public void increment_shouldFailWhenTheCurrentValueIsNotANumber() {
		service.setValue(admin(), PROPERTY, "not a number");

		assertThrows(DAOException.class, () -> service.increment(admin(), PROPERTY));
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

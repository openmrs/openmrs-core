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

import java.sql.PreparedStatement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveNonTransactionalTest;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.util.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceConcurrencyIT extends BaseContextSensitiveNonTransactionalTest {

	@Autowired
	private UserService userService;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@AfterEach
	public void deleteCommittedData() {
		deleteAllData();
	}

	@Test
	public void changePasswordUsingActivationKey_shouldRejectAKeyAnUncommittedResetHasUsed() throws Exception {
		String key = "h4ph0fpNzQCIPSw8plJI";
		try (PreparedStatement ps = getConnection()
		        .prepareStatement("update users set activation_key = ? where user_id = 502")) {
			ps.setString(1, Security.encodeString(key) + ":" + (System.currentTimeMillis() + 600000));
			ps.executeUpdate();
		}
		getConnection().commit();

		CompletableFuture<Throwable> second = new TransactionTemplate(transactionManager).execute(status -> {
			userService.changePasswordUsingActivationKey(key, "Passw0rdOne");
			CompletableFuture<Throwable> overlapping = CompletableFuture.supplyAsync(() -> {
				Context.openSession();
				Context.addProxyPrivilege(PrivilegeConstants.GET_GLOBAL_PROPERTIES);
				try {
					userService.changePasswordUsingActivationKey(key, "Passw0rdTwo");
					return null;
				} catch (Throwable t) {
					return t;
				} finally {
					Context.closeSession();
				}
			});
			assertThrows(TimeoutException.class, () -> overlapping.get(1, TimeUnit.SECONDS));
			return overlapping;
		});
		assertInstanceOf(InvalidActivationKeyException.class, second.get(30, TimeUnit.SECONDS));
	}
}

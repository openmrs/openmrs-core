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
package org.openmrs.test.synchronization.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.util.Date;

import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.test.annotation.NotTransactional;

/**
 *
 */
public class SyncUserTest extends SyncBaseTest {

	@Override
    public String getInitialDataset() {
	    return "org/openmrs/test/synchronization/engine/include/SyncCreateTest.xml";
    }
	
	@Test
	@NotTransactional
	public void testCreateUser() throws Exception {
		runSyncTest(new SyncTestHelper() {
			UserService us = Context.getUserService();
			public void runOnChild() {
				User u = new User();
				u.setUsername("djazayeri");
				u.addName(new PersonName("Darius", "Graham", "Jazayeri"));
				u.setGender("M");
				u.addRole(us.getRole("System Developer"));
				u.addRole(us.getRole("Provider"));
				us.saveUser(u, "test");
			}
			public void runOnParent() {
				User u = us.getUserByUsername("djazayeri");
				assertNotNull("User not created", u);
				assertEquals("Failed to create person name", u.getPersonName().getGivenName(), "Darius");
				assertEquals("Failed to assign roles", u.getRoles().size(), 2);
			}
		});
	}

	@Test
	@NotTransactional
	public void testChangePwd() throws Exception {
		runSyncTest(new SyncTestHelper() {
			UserService us = Context.getUserService();
			String newPWD = "NewPassword";
			String userGuid = null; 
			public void runOnChild() {
				userGuid = us.getUser(1).getGuid();
				us.saveUser(us.getUser(1), newPWD);
				assertEquals(userGuid,us.getUser(1).getGuid());
			}
			public void runOnParent() {
				assertEquals(userGuid,us.getUser(1).getGuid());
				Context.authenticate(us.getUser(1).getUsername(), newPWD);
			}
		});
	}

	@Test
	@NotTransactional
	public void testEditUser() throws Exception {
		runSyncTest(new SyncTestHelper() {
			UserService us = Context.getUserService();
			Date d = ymd.parse("1978-04-11");
			int numRolesBefore;
			public void runOnChild() {
				User u = us.getUser(1);
				u.setBirthdate(d);
				u.addName(new PersonName("Darius", "Graham", "Jazayeri"));
				numRolesBefore = u.getRoles().size();
				u.addRole(us.getRole("Provider"));
				us.saveUser(u, null);
			}
			public void runOnParent() {
				User u = us.getUser(1);
				assertEquals("Failed to create person name", u.getNames().size(), 2);
				assertEquals("Failed to assign roles", u.getRoles().size(), numRolesBefore + 1);
				assertEquals("Failed to set birthdate", OpenmrsUtil.compare(u.getBirthdate(), d), 0);
			}
		});
	}
	
	@Test
	@NotTransactional
	public void testCreateRoleAndPrivilege() throws Exception {
		runSyncTest(new SyncTestHelper() {
			public void runOnChild() {
				Privilege priv = new Privilege("Kitchen Use");
				priv.setDescription("Can step into the kitchen");
				Context.getUserService().savePrivilege(priv);
				Role role = new Role("Chef");
				role.setDescription("One who cooks");
				role.addPrivilege(priv);
				Context.getUserService().saveRole(role);
			}
			public void runOnParent() {
				Privilege priv = Context.getUserService().getPrivilege("Kitchen Use");
				assertEquals("Privilege failed", "Can step into the kitchen", priv.getDescription());
				Role role = Context.getUserService().getRole("Chef");
				assertEquals("Role failed", "One who cooks", role.getDescription());
			}
		});
	}
	
	@Test
	@NotTransactional
	public void testAddPrivilegeToRole() throws Exception {
		runSyncTest(new SyncTestHelper() {
			int numAtStart = 0;
			public void runOnChild() {
				Privilege priv = Context.getUserService().getPrivilege("Manage Locations");
				Role role = Context.getUserService().getRole("Provider");
				numAtStart = role.getPrivileges().size();
				role.addPrivilege(priv);
				Context.getUserService().saveRole(role);
			}
			public void runOnParent() {
				Role role = Context.getUserService().getRole("Provider");
				assertEquals("Failed to create role",
				             numAtStart + 1,
				             role.getPrivileges().size());
				assertTrue("Does not have newly granted privilege", role.hasPrivilege("Manage Locations"));
			}
		});
	}

}

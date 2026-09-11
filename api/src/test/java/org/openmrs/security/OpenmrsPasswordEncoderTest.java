/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.security;

import java.sql.Timestamp;
import java.util.Date;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.Security;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves that {@link Security#hashMatches(String, String)}/{@link Security#encodePassword(String)}
 * correctly delegate to the {@code openmrsPasswordEncoder} bean registered by
 * {@link OpenmrsSecurityConfig#openmrsPasswordEncoder()}, and that a successful login with a legacy
 * (unprefixed) hash transparently upgrades the stored hash, per the change in
 * {@link org.openmrs.api.db.hibernate.HibernateContextDAO#authenticate(String, String)}.
 */
public class OpenmrsPasswordEncoderTest extends BaseContextSensitiveTest {

	@Autowired
	private SessionFactory sessionFactory;

	@Test
	public void encodePassword_shouldProduceAnIdPrefixedHashThatHashMatchesVerifies() {
		String hash = Security.encodePassword("test" + "somesalt");
		assertTrue(Security.isUpgradedHash(hash));
		assertTrue(Security.hashMatches(hash, "test" + "somesalt"));
		assertFalse(Security.hashMatches(hash, "wrong" + "somesalt"));
	}

	@Test
	public void authenticate_shouldUpgradeLegacyHashOnSuccessfulLogin() {
		// BaseContextSensitiveTest's own setup already authenticated as admin:test once (see
		// authenticate()/getCredentials() there), which - correctly - already exercised the
		// upgrade-on-login path once. Reset back to the original fixture (legacy-format) hash from
		// initialInMemoryTestDataSet.xml so this test can observe the transition explicitly.
		setStoredPassword(1, "4a1750c8607d0fa237de36c6305715c223415189", "c788c6ad82a157b712392ca695dfcf2eed193d7f");
		assertFalse(Security.isUpgradedHash(getStoredPassword(1)));

		Context.logout();
		Context.authenticate("admin", "test");

		String passwordAfter = getStoredPassword(1);
		assertTrue(Security.isUpgradedHash(passwordAfter));

		// a second login must keep working against the now-upgraded hash
		Context.logout();
		Context.authenticate("admin", "test");
		assertEquals(passwordAfter, getStoredPassword(1));
	}

	@Test
	public void authenticate_shouldSetChangedByAndDateChangedWhenUpgradingLegacyHash() {
		// baseline both audit columns to values a successful upgrade must overwrite: someone other
		// than admin (user 0, the daemon user, also present in initialInMemoryTestDataSet.xml), and a
		// date well before the login this test triggers
		setStoredPassword(1, "4a1750c8607d0fa237de36c6305715c223415189", "c788c6ad82a157b712392ca695dfcf2eed193d7f");
		setStoredAudit(1, 0, Timestamp.valueOf("2000-01-01 00:00:00"));

		Date before = new Date();
		Context.logout();
		Context.authenticate("admin", "test");

		// the rewrite has no other actor to attribute itself to but the user who just logged in, same
		// as any other password change would record
		assertEquals(1, getStoredChangedBy(1));
		assertFalse(getStoredDateChanged(1).before(before));
	}

	private String getStoredPassword(Integer userId) {
		Query<String> query = sessionFactory.getCurrentSession()
		        .createNativeQuery("select password from users where user_id = ?1", String.class);
		query.setParameter(1, userId);
		return query.uniqueResult();
	}

	private void setStoredPassword(Integer userId, String password, String salt) {
		sessionFactory.getCurrentSession().createNativeQuery("update users set password = ?1, salt = ?2 where user_id = ?3")
		        .setParameter(1, password).setParameter(2, salt).setParameter(3, userId).executeUpdate();
	}

	private Integer getStoredChangedBy(Integer userId) {
		Query<Integer> query = sessionFactory.getCurrentSession()
		        .createNativeQuery("select changed_by from users where user_id = ?1", Integer.class);
		query.setParameter(1, userId);
		return query.uniqueResult();
	}

	private Timestamp getStoredDateChanged(Integer userId) {
		Query<Timestamp> query = sessionFactory.getCurrentSession()
		        .createNativeQuery("select date_changed from users where user_id = ?1", Timestamp.class);
		query.setParameter(1, userId);
		return query.uniqueResult();
	}

	private void setStoredAudit(Integer userId, Integer changedBy, Timestamp dateChanged) {
		sessionFactory.getCurrentSession()
		        .createNativeQuery("update users set changed_by = ?1, date_changed = ?2 where user_id = ?3")
		        .setParameter(1, changedBy).setParameter(2, dateChanged).setParameter(3, userId).executeUpdate();
	}
}

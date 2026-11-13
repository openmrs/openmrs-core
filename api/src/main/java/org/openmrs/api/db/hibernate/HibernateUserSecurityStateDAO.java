/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.sql.Timestamp;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.openmrs.User;
import org.openmrs.UserSecurityState;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.UserSecurityStateDAO;
import org.openmrs.util.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Hibernate implementation of {@link UserSecurityStateDAO}. Entries are read and written with
 * native SQL, so they never pass through the session or second-level cache. Every conditional write
 * first locks the user's row in the users table, then compares the current value in Java (so the
 * comparison is exact on every database, whatever the column's collation), which serializes the
 * writes for one user (including the login attempt counting in
 * {@link HibernateContextDAO#authenticate(String, String)}) without blocking other users.
 *
 * @since 3.0.0, 2.9.0
 */
@Repository("userSecurityStateDAO")
public class HibernateUserSecurityStateDAO implements UserSecurityStateDAO {

	private final SessionFactory sessionFactory;

	@Autowired
	public HibernateUserSecurityStateDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @see UserSecurityStateDAO#getValue(Integer, String)
	 */
	@Override
	public String getValue(Integer userId, String property) {
		return select("select property_value from user_security_state where user_id = ?1 and property = ?2", userId,
		    property);
	}

	/**
	 * @see UserSecurityStateDAO#setValue(Integer, String, String)
	 */
	@Override
	public void setValue(Integer userId, String property, String value) {
		lockUser(userId);
		if (getValueForUpdate(userId, property) == null) {
			insert(userId, property, value);
		} else {
			update(userId, property, value);
		}
	}

	/**
	 * @see UserSecurityStateDAO#compareAndSet(Integer, String, String, String)
	 */
	@Override
	public boolean compareAndSet(Integer userId, String property, String expectedValue, String newValue) {
		lockUser(userId);
		String current = getValueForUpdate(userId, property);
		if (expectedValue == null) {
			if (current != null) {
				return false;
			}
			insert(userId, property, newValue);
			return true;
		}
		if (!expectedValue.equals(current)) {
			return false;
		}
		update(userId, property, newValue);
		return true;
	}

	/**
	 * @see UserSecurityStateDAO#deleteIfValueMatches(Integer, String, String)
	 */
	@Override
	public boolean deleteIfValueMatches(Integer userId, String property, String expectedValue) {
		lockUser(userId);
		if (!expectedValue.equals(getValueForUpdate(userId, property))) {
			return false;
		}
		return delete(userId, property);
	}

	/**
	 * @see UserSecurityStateDAO#delete(Integer, String)
	 */
	@Override
	public boolean delete(Integer userId, String property) {
		return mutation("delete from user_security_state where user_id = ?1 and property = ?2").setParameter(1, userId)
		        .setParameter(2, property).executeUpdate() > 0;
	}

	/**
	 * @see UserSecurityStateDAO#increment(Integer, String)
	 */
	@Override
	public long increment(Integer userId, String property) {
		lockUser(userId);
		String current = getValueForUpdate(userId, property);
		if (current == null) {
			insert(userId, property, "1");
			return 1;
		}
		long next;
		try {
			next = Long.parseLong(current) + 1;
		} catch (NumberFormatException e) {
			throw new DAOException("The user security state entry " + property + " of user " + userId
			        + " does not hold a number and cannot be incremented", e);
		}
		update(userId, property, String.valueOf(next));
		return next;
	}

	/**
	 * @see UserSecurityStateDAO#deleteAll(Integer)
	 */
	@Override
	public void deleteAll(Integer userId) {
		mutation("delete from user_security_state where user_id = ?1").setParameter(1, userId).executeUpdate();
	}

	private Session session() {
		return sessionFactory.getCurrentSession();
	}

	private NativeQuery<?> mutation(String sql) {
		return session().createNativeQuery(sql).addSynchronizedEntityClass(UserSecurityState.class);
	}

	private String select(String sql, Integer userId, String property) {
		return session().createNativeQuery(sql, String.class).addSynchronizedEntityClass(UserSecurityState.class)
		        .setParameter(1, userId).setParameter(2, property).uniqueResult();
	}

	private void lockUser(Integer userId) {
		session().flush();
		Integer lockedUserId = session()
		        .createNativeQuery("select user_id from users where user_id = ?1 for update", Integer.class)
		        .addSynchronizedEntityClass(User.class).setParameter(1, userId).uniqueResult();
		if (lockedUserId == null) {
			throw new DAOException("No user with id " + userId);
		}
	}

	private String getValueForUpdate(Integer userId, String property) {
		return select("select property_value from user_security_state where user_id = ?1 and property = ?2 for update",
		    userId, property);
	}

	private void insert(Integer userId, String property, String value) {
		mutation("insert into user_security_state (uuid, user_id, property, property_value, creator, date_created) "
		        + "values (?1, ?2, ?3, ?4, ?5, ?6)").setParameter(1, UuidUtil.newUuidString()).setParameter(2, userId)
		        .setParameter(3, property).setParameter(4, value).setParameter(5, actorUserId()).setParameter(6, now())
		        .executeUpdate();
	}

	private void update(Integer userId, String property, String value) {
		mutation("update user_security_state set property_value = ?1, changed_by = ?2, date_changed = ?3 "
		        + "where user_id = ?4 and property = ?5").setParameter(1, value).setParameter(2, actorUserId())
		        .setParameter(3, now()).setParameter(4, userId).setParameter(5, property).executeUpdate();
	}

	private Integer actorUserId() {
		User actor = Context.getAuthenticatedUser();
		return actor == null ? null : actor.getUserId();
	}

	private Timestamp now() {
		return new Timestamp(System.currentTimeMillis());
	}
}

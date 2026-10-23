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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.openmrs.api.db.StoredRole;

/**
 * Reads roles and role assignments as stored in the database, ignoring the session's copies, which
 * may hold unsaved changes. Privilege checks run in the middle of arbitrary unsaved changes, so
 * reading never flushes them. A change saved in the current transaction is seen once it has been
 * flushed.
 */
final class StoredRoleReader {

	private StoredRoleReader() {
	}

	/**
	 * @param session the session to read through
	 * @param roleName the role's name, matched as the database compares names
	 * @return the role, named as stored, or null if there is no such role
	 */
	static StoredRole read(Session session, String roleName) {
		FlushMode flushMode = session.getHibernateFlushMode();
		session.setHibernateFlushMode(FlushMode.MANUAL);
		try {
			String name = session.createQuery("select r.role from Role r where r.role = :name", String.class)
			        .setParameter("name", roleName).uniqueResult();
			if (name == null) {
				return null;
			}
			List<String> privileges = session
			        .createQuery("select p.privilege from Role r join r.privileges p where r.role = :name", String.class)
			        .setParameter("name", name).getResultList();
			List<String> parentRoles = session
			        .createQuery("select i.role from Role r join r.inheritedRoles i where r.role = :name", String.class)
			        .setParameter("name", name).getResultList();
			return new StoredRole(name, new HashSet<>(privileges), new HashSet<>(parentRoles));
		} finally {
			session.setHibernateFlushMode(flushMode);
		}
	}

	/**
	 * @param session the session to read through
	 * @param userId the user's id
	 * @return the names of the roles stored as assigned to the user
	 */
	static Set<String> readUserRoleNames(Session session, Integer userId) {
		FlushMode flushMode = session.getHibernateFlushMode();
		session.setHibernateFlushMode(FlushMode.MANUAL);
		try {
			return new HashSet<>(
			        session.createQuery("select r.role from User u join u.roles r where u.userId = :userId", String.class)
			                .setParameter("userId", userId).getResultList());
		} finally {
			session.setHibernateFlushMode(flushMode);
		}
	}
}

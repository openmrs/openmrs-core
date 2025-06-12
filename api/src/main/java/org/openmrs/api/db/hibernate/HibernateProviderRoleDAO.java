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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.ProviderRole;
import org.openmrs.api.db.ProviderRoleDAO;

import java.util.List;

/**
 * It is a default implementation of  {@link org.openmrs.api.db.ProviderRoleDAO}.
 */
public class HibernateProviderRoleDAO implements ProviderRoleDAO {

	protected final Log log = LogFactory.getLog(this.getClass());

	private SessionFactory sessionFactory;

	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @return the sessionFactory
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	@Override
	public List<ProviderRole> getAllProviderRoles(boolean includeRetired) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ProviderRole.class);
		if (!includeRetired) {
			criteria.add(Restrictions.eq("retired", false));
		}
		return (List<ProviderRole>) criteria.list();
	}

	@Override
	public ProviderRole getProviderRole(Integer id) {
		return sessionFactory.getCurrentSession().get(ProviderRole.class, id);
	}

	@Override
	public ProviderRole getProviderRoleByUuid(String uuid) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ProviderRole.class);
		criteria.add(Restrictions.eq("uuid", uuid));
		return (ProviderRole) criteria.uniqueResult();
	}

	@Override
	public ProviderRole  saveProviderRole(ProviderRole role) {
		sessionFactory.getCurrentSession().saveOrUpdate(role);
		return role;
	}

	@Override
	public void deleteProviderRole(ProviderRole role) {
		sessionFactory.getCurrentSession().delete(role);
	}
}

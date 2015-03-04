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

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Person;
import org.openmrs.activelist.ActiveListItem;
import org.openmrs.activelist.ActiveListType;
import org.openmrs.api.db.ActiveListDAO;
import org.openmrs.api.db.DAOException;

/**
 * Hibernate specific database methods for the ActiveListService
 * 
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.db.ActiveListDAO
 * @see org.openmrs.api.ActiveListService
 */
public class HibernateActiveListDAO implements ActiveListDAO {
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.ActiveListDAO#getActiveListItems(org.openmrs.Person,
	 *      org.openmrs.activelist.ActiveListType)
	 */
	public List<ActiveListItem> getActiveListItems(Person p, ActiveListType type) throws DAOException {
		return getActiveListItems(ActiveListItem.class, p, type);
	}
	
	/**
	 * @see org.openmrs.api.db.ActiveListDAO#getActiveListItems(java.lang.Class, org.openmrs.Person,
	 *      org.openmrs.activelist.ActiveListType)
	 */
	@SuppressWarnings("unchecked")
	public <T extends ActiveListItem> List<T> getActiveListItems(Class<T> clazz, Person p, ActiveListType type)
	        throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(clazz);
		criteria.add(Restrictions.eq("activeListType", type));
		criteria.add(Restrictions.eq("person", p));
		criteria.add(Restrictions.eq("voided", Boolean.FALSE));
		criteria.addOrder(Order.desc("startDate"));
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ActiveListDAO#getActiveListItem(java.lang.Class, java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public <T extends ActiveListItem> T getActiveListItem(Class<T> clazz, Integer activeListItemId) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(clazz);
		criteria.add(Restrictions.eq("activeListId", activeListItemId));
		return (T) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ActiveListDAO#getActiveListItemByUuid(java.lang.String)
	 */
	public ActiveListItem getActiveListItemByUuid(String uuid) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(ActiveListItem.class);
		criteria.add(Restrictions.eq("uuid", uuid));
		return (ActiveListItem) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.ActiveListDAO#saveActiveListItem(org.openmrs.activelist.ActiveListItem)
	 */
	public ActiveListItem saveActiveListItem(ActiveListItem item) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(item);
		return item;
	}
	
	/**
	 * @see org.openmrs.api.db.ActiveListDAO#deleteActiveListItem(org.openmrs.activelist.ActiveListItem)
	 */
	public void deleteActiveListItem(ActiveListItem item) throws DAOException {
		sessionFactory.getCurrentSession().delete(item);
	}
}

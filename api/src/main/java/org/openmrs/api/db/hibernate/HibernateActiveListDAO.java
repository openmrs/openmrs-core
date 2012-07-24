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
package org.openmrs.api.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Order;
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
	
	private static final Log log = LogFactory.getLog(HibernateActiveListDAO.class);
	
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
}

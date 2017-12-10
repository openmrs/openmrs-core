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
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.OrderSet;
import org.openmrs.OrderSetMember;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.OrderSetDAO;

/**
 * This class should not be used directly. This is just a common implementation of the OrderSetDAO that
 * is used by the OrderSetService. This class is injected by spring into the desired OrderSetService
 * class. This injection is determined by the xml mappings and elements in the spring application
 * context: /metadata/api/spring/applicationContext.xml.<br/>
 * <br/>
 * The OrderSetService should be used for all Order related database manipulation.
 *
 * @see org.openmrs.api.OrderSetService
 * @see org.openmrs.api.db.OrderSetDAO
 * @since 1.12
 */
public class HibernateOrderSetDAO implements OrderSetDAO {
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateOrderSetDAO() {
	}
	
	/**
	 * Set session factory
	 *
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderSetDAO#save(OrderSet)
	 */
	@Override
	public OrderSet save(OrderSet orderSet) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(orderSet);
		return orderSet;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderSetDAO#getOrderSets(boolean)
	 */
	@Override
	public List<OrderSet> getOrderSets(boolean includeRetired) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(OrderSet.class, "orderSet");
		
		if (!includeRetired) {
			crit.add(Restrictions.eq("retired", Boolean.FALSE));
		}
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderSetDAO#getOrderSetById(Integer)
	 */
	@Override
	public OrderSet getOrderSetById(Integer orderSetId) throws DAOException {
		return (OrderSet) sessionFactory.getCurrentSession().get(OrderSet.class, orderSetId);
	}
	
	/**
	 * @see org.openmrs.api.db.OrderSetDAO#getOrderSetByUniqueUuid(String)
	 */
	@Override
	public OrderSet getOrderSetByUniqueUuid(String orderSetUuid) throws DAOException {
		return (OrderSet) sessionFactory.getCurrentSession().createQuery("from OrderSet o where o.uuid = :uuid").setString(
		    "uuid", orderSetUuid).uniqueResult();
	}
	

	/**
	 * @see org.openmrs.api.db.OrderSetDAO#getOrderSetMemberByUuid(String)
	 */
	@Override
	public OrderSetMember getOrderSetMemberByUuid(String uuid) throws DAOException {
		return (OrderSetMember) sessionFactory.getCurrentSession().createQuery("from OrderSetMember osm where osm.uuid = :uuid").setString(
				"uuid", uuid).uniqueResult();
	}

}

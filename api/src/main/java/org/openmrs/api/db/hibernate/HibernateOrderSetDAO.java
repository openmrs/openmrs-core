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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.OrderSet;
import org.openmrs.OrderSetAttribute;
import org.openmrs.OrderSetAttributeType;
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
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderSet> cq = cb.createQuery(OrderSet.class);
		Root<OrderSet> root = cq.from(OrderSet.class);

		if (!includeRetired) {
			cq.where(cb.isFalse(root.get("retired")));
		}

		return session.createQuery(cq).getResultList();
	}


	/**
	 * @see org.openmrs.api.db.OrderSetDAO#getOrderSetById(Integer)
	 */
	@Override
	public OrderSet getOrderSetById(Integer orderSetId) throws DAOException {
		return sessionFactory.getCurrentSession().get(OrderSet.class, orderSetId);
	}
	
	/**
	 * @see org.openmrs.api.db.OrderSetDAO#getOrderSetByUniqueUuid(String)
	 */
	@Override
	public OrderSet getOrderSetByUniqueUuid(String orderSetUuid) throws DAOException {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, OrderSet.class, orderSetUuid);
	}
	
	/**
	 * @see org.openmrs.api.db.OrderSetDAO#getOrderSetMemberByUuid(String)
	 */
	@Override
	public OrderSetMember getOrderSetMemberByUuid(String uuid) throws DAOException {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, OrderSetMember.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.OrderSetDAO#getAllOrderSetAttributeTypes()
	 */
	@Override
	public List<OrderSetAttributeType> getAllOrderSetAttributeTypes() {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderSetAttributeType> cq = cb.createQuery(OrderSetAttributeType.class);
		cq.from(OrderSetAttributeType.class);
		
		return session.createQuery(cq).getResultList();

	}

	/**
	 * @see org.openmrs.api.db.OrderSetDAO#getOrderSetAttributeType(java.lang.Integer)
	 */
	@Override
	public OrderSetAttributeType getOrderSetAttributeType(Integer id) {
		return sessionFactory.getCurrentSession().get(OrderSetAttributeType.class, id);
	}

	/**
	 * @see org.openmrs.api.db.OrderSetDAO#getOrderSetAttributeTypeByUuid(java.lang.String)
	 */
	@Override
	public OrderSetAttributeType getOrderSetAttributeTypeByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, OrderSetAttributeType.class, uuid);
	}

	/**
	 * @see org.openmrs.api.db.OrderSetDAO#saveOrderSetAttributeType(org.openmrs.OrderSetAttributeType)
	 */
	@Override
	public OrderSetAttributeType saveOrderSetAttributeType(OrderSetAttributeType orderSetAttributeType) {
		sessionFactory.getCurrentSession().saveOrUpdate(orderSetAttributeType);
		return orderSetAttributeType;
	}

	/**
	 * @see org.openmrs.api.db.OrderSetDAO#deleteOrderSetAttributeType(org.openmrs.OrderSetAttributeType)
	 */
	@Override
	public void deleteOrderSetAttributeType(OrderSetAttributeType orderSetAttributeType) {
		sessionFactory.getCurrentSession().delete(orderSetAttributeType);
	}

	/**
	 * @see org.openmrs.api.db.OrderSetDAO#getOrderSetAttributeByUuid(java.lang.String)
	 */
	@Override
	public OrderSetAttribute getOrderSetAttributeByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, OrderSetAttribute.class, uuid);
	}

	/**
	 * @see org.openmrs.api.db.OrderSetDAO#getOrderSetAttributeTypeByName(java.lang.String)
	 */
	@Override
	public OrderSetAttributeType getOrderSetAttributeTypeByName(String name) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderSetAttributeType> cq = cb.createQuery(OrderSetAttributeType.class);
		Root<OrderSetAttributeType> root = cq.from(OrderSetAttributeType.class);

		cq.where(cb.equal(root.get("name"), name));

		return session.createQuery(cq).uniqueResult();
	}
}

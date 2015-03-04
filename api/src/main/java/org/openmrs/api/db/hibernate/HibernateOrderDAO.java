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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.OrderService.ORDER_STATUS;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.OrderDAO;

/**
 * This class should not be used directly. This is just a common implementation of the OrderDAO that
 * is used by the OrderService. This class is injected by spring into the desired OrderService
 * class. This injection is determined by the xml mappings and elements in the spring application
 * context: /metadata/api/spring/applicationContext.xml.<br/>
 * <br/>
 * The OrderService should be used for all Order related database manipulation.
 * 
 * @see org.openmrs.api.OrderService
 * @see org.openmrs.api.db.OrderDAO
 */
public class HibernateOrderDAO implements OrderDAO {
	
	protected static final Log log = LogFactory.getLog(HibernateOrderDAO.class);
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateOrderDAO() {
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
	 * @see org.openmrs.api.db.OrderDAO#saveOrderType(org.openmrs.OrderType)
	 * @see org.openmrs.api.OrderService#saveOrderType(org.openmrs.OrderType)
	 */
	public OrderType saveOrderType(OrderType orderType) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(orderType);
		
		return orderType;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderType(java.lang.Integer)
	 * @see org.openmrs.api.OrderService#getOrderType(java.lang.Integer)
	 */
	public OrderType getOrderType(Integer orderTypeId) throws DAOException {
		if (log.isDebugEnabled())
			log.debug("getting orderType with id: " + orderTypeId);
		
		return (OrderType) sessionFactory.getCurrentSession().get(OrderType.class, orderTypeId);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrderTypes()
	 */
	@SuppressWarnings("unchecked")
	public List<OrderType> getAllOrderTypes(boolean includeRetired) throws DAOException {
		log.debug("getting all order types");
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(OrderType.class);
		
		if (includeRetired == false) {
			// TODO implement OrderType.retired
			crit.add(Expression.eq("retired", false));
		}
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#deleteOrderType(org.openmrs.OrderType)
	 * @see org.openmrs.api.OrderService#purgeOrderType(org.openmrs.OrderType)
	 */
	public void deleteOrderType(OrderType orderType) throws DAOException {
		sessionFactory.getCurrentSession().delete(orderType);
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#saveOrder(org.openmrs.Order)
	 * @see org.openmrs.api.OrderService#saveOrder(org.openmrs.Order)
	 */
	public Order saveOrder(Order order) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(order);
		
		return order;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#deleteOrder(org.openmrs.Order)
	 * @see org.openmrs.api.OrderService#purgeOrder(org.openmrs.Order)
	 */
	public void deleteOrder(Order order) throws DAOException {
		sessionFactory.getCurrentSession().delete(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrder(java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public <Ord extends Order> Ord getOrder(Integer orderId, Class<Ord> orderClassType) throws DAOException {
		if (log.isDebugEnabled())
			log.debug("getting order #" + orderId + " with class: " + orderClassType);
		
		return (Ord) sessionFactory.getCurrentSession().get(orderClassType, orderId);
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrders(java.lang.Class, java.util.List, java.util.List,
	 *      org.openmrs.api.OrderService.ORDER_STATUS, java.util.List, java.util.List,
	 *      java.util.List)
	 * @see org.openmrs.api.OrderService#getOrders(java.lang.Class, java.util.List, java.util.List,
	 *      org.openmrs.api.OrderService.ORDER_STATUS, java.util.List, java.util.List,
	 *      java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public <Ord extends Order> List<Ord> getOrders(Class<Ord> orderClassType, List<Patient> patients,
	        List<Concept> concepts, ORDER_STATUS status, List<User> orderers, List<Encounter> encounters,
	        List<OrderType> orderTypes) {
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(orderClassType);
		
		if (patients.size() > 0)
			crit.add(Expression.in("patient", patients));
		
		if (concepts.size() > 0)
			crit.add(Expression.in("concept", concepts));
		
		// only the "ANY" status cares about voided Orders.  All others 
		// do not want voided orders included in the list
		if (status != ORDER_STATUS.ANY)
			crit.add(Expression.eq("voided", false));
		// we are not checking the other status's here because they are 
		// algorithm dependent  
		
		if (orderers.size() > 0)
			crit.add(Expression.in("orderer", orderers));
		
		if (encounters.size() > 0)
			crit.add(Expression.in("encounter", encounters));
		
		if (orderTypes.size() > 0)
			crit.add(Expression.in("orderType", orderTypes));
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderByUuid(java.lang.String)
	 */
	public Order getOrderByUuid(String uuid) {
		return (Order) sessionFactory.getCurrentSession().createQuery("from Order o where o.uuid = :uuid").setString("uuid",
		    uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderTypeByUuid(java.lang.String)
	 */
	public OrderType getOrderTypeByUuid(String uuid) {
		return (OrderType) sessionFactory.getCurrentSession().createQuery("from OrderType ot where ot.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * Delete Obs that references (deleted) Order
	 */
	public void deleteObsThatReference(Order order) {
		if (order != null) {
			sessionFactory.getCurrentSession().createQuery("delete Obs where order = :order").setParameter("order", order)
			        .executeUpdate();
		}
	}
	
}

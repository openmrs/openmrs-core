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

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderGroup;
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
	 *      java.util.List, java.util.Date)
	 * @see org.openmrs.api.OrderService#getOrders(java.lang.Class, java.util.List, java.util.List,
	 *      org.openmrs.api.OrderService.ORDER_STATUS, java.util.List, java.util.List,
	 *      java.util.List, java.util.Date)
	 */
	@SuppressWarnings("unchecked")
	public <Ord extends Order> List<Ord> getOrders(Class<Ord> orderClassType, List<Patient> patients,
	        List<Concept> concepts, ORDER_STATUS status, List<User> orderers, List<Encounter> encounters,
	        List<OrderType> orderTypes, Date asOfDate) {
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(orderClassType);
		
		if (patients.size() > 0)
			crit.add(Expression.in("patient", patients));
		
		if (concepts.size() > 0)
			crit.add(Expression.in("concept", concepts));
		
		// only the "ANY" status cares about voided Orders.  All others 
		// do not want voided orders included in the list
		// so exclude them here first
		if (status != ORDER_STATUS.ANY)
			crit.add(Expression.eq("voided", false));
		
		if (status == ORDER_STATUS.ACTIVE && asOfDate != null) {
			crit.add(Expression.le("startDate", asOfDate)); // startDate cannot be null?
			
			crit.add(Expression.or(Expression.isNull("discontinuedDate"), Expression.ge("discontinueDate", asOfDate)));
			
			crit.add(Expression.or(Expression.isNull("autoExpireDate"), Expression.ge("autoExpireDate", asOfDate)));
			
			crit.add(Expression.or(Expression.isNull("dateActivated"), Expression.le("dateActivated", asOfDate)));
			
		}
		
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
	 * @see org.openmrs.api.db.OrderDAO#getOrderByOrderNumber(java.lang.String)
	 */
	public Order getOrderByOrderNumber(String orderNumber) {
		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(Order.class, "order");
		searchCriteria.add(Expression.eq("order.orderNumber", orderNumber));
		return (Order) searchCriteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDA#getMaximumOrderId()
	 */
	public Integer getMaximumOrderId() {
		Query query = sessionFactory.getCurrentSession().createQuery("select max(orderId) from Order");
		Object maxOrderId = query.uniqueResult();
		if (maxOrderId == null)
			return 0;
		
		return (Integer) maxOrderId;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#saveOrderGroup(org.openmrs.OrderGroup)
	 */
	@Override
	public OrderGroup saveOrderGroup(OrderGroup orderGroup) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(orderGroup);
		
		return orderGroup;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderGroup(java.lang.Integer)
	 */
	@Override
	public OrderGroup getOrderGroup(Integer orderGroupId) throws DAOException {
		return (OrderGroup) sessionFactory.getCurrentSession().get(OrderGroup.class, orderGroupId);
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderGroupByUuid(java.lang.String)
	 */
	@Override
	public OrderGroup getOrderGroupByUuid(String uuid) throws DAOException {
		return (OrderGroup) sessionFactory.getCurrentSession().createQuery("from OrderGroup og where og.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderGroupsByPatient(org.openmrs.Patient)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<OrderGroup> getOrderGroupsByPatient(Patient patient) throws DAOException {
		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(OrderGroup.class, "orderGroup");
		searchCriteria.add(Expression.eq("orderGroup.patient", patient));
		return (List<OrderGroup>) searchCriteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#isActivatedInDatabase(org.openmrs.Order)
	 */
	@Override
	public boolean isActivatedInDatabase(Order order) {
		if (order.getOrderId() == null)
			return false;
		Query query = sessionFactory.getCurrentSession().createSQLQuery(
		    "select count(*) from orders where order_id = :orderId and date_activated is not null");
		query.setInteger("orderId", order.getOrderId());
		return query.uniqueResult().equals(1);
	}
	
}

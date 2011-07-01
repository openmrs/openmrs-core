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
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Order.OrderAction;
import org.openmrs.OrderGroup;
import org.openmrs.OrderSet;
import org.openmrs.Patient;
import org.openmrs.PublishedOrderSet;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.util.OpenmrsConstants;

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
	 *      java.util.List, java.util.List, java.util.List, java.util.Date)
	 * @see org.openmrs.api.OrderService#getOrders(java.lang.Class, java.util.List, java.util.List,
	 *      java.util.List, java.util.List, java.util.List, java.util.Date, java.util.List,
	 *      java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public <Ord extends Order> List<Ord> getOrders(Class<Ord> orderClassType, List<Patient> patients,
	        List<Concept> concepts, List<User> orderers, List<Encounter> encounters, Date asOfDate,
	        List<OrderAction> actionsToInclude, List<OrderAction> actionsToExclude) {
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(orderClassType);
		
		if (patients != null && patients.size() > 0)
			crit.add(Expression.in("patient", patients));
		
		if (concepts != null && concepts.size() > 0)
			crit.add(Expression.in("concept", concepts));
		
		// if an asOfDate is passed in, then we need to restrict to just active type of orders
		if (asOfDate != null) {
			crit.add(Expression.le("startDate", asOfDate)); // startDate cannot be null?
			
			crit.add(Expression.or(Expression.isNull("discontinuedDate"), Expression.ge("discontinueDate", asOfDate)));
			
			crit.add(Expression.or(Expression.isNull("autoExpireDate"), Expression.ge("autoExpireDate", asOfDate)));
			
			crit.add(Expression.or(Expression.isNull("dateActivated"), Expression.le("dateActivated", asOfDate)));
			
		}
		
		// we are not checking the other status's here because they are 
		// algorithm dependent
		
		if (orderers != null && orderers.size() > 0)
			crit.add(Expression.in("orderer", orderers));
		
		if (encounters != null && encounters.size() > 0)
			crit.add(Expression.in("encounter", encounters));
		
		if (actionsToInclude != null && actionsToInclude.size() > 0)
			crit.add(Expression.in("action", actionsToInclude));
		
		if (actionsToExclude != null && actionsToExclude.size() > 0)
			crit.add(Expression.not(Expression.in("action", actionsToExclude)));
		
		crit.add(Expression.eq("voided", false));
		
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
	 * @see org.openmrs.api.db.OrderDAO#getOrderByOrderNumber(java.lang.String)
	 */
	public Order getOrderByOrderNumber(String orderNumber) {
		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(Order.class, "order");
		searchCriteria.add(Expression.eq("order.orderNumber", orderNumber));
		return (Order) searchCriteria.uniqueResult();
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
		return (OrderGroup) sessionFactory.getCurrentSession().createQuery("from OrderGroup where uuid = :uuid").setString(
		    "uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderGroupsByPatient(org.openmrs.Patient)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<OrderGroup> getOrderGroupsByPatient(Patient patient) throws DAOException {
		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(OrderGroup.class);
		searchCriteria.add(Expression.eq("patient", patient));
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
		return ((Number) query.uniqueResult()).intValue() == 1;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderNumberInDatabase(org.openmrs.Order)
	 */
	@Override
	public String getOrderNumberInDatabase(Order order) {
		if (order.getOrderId() == null)
			return null;
		Query query = sessionFactory.getCurrentSession().createSQLQuery(
		    "select order_number from orders where order_id = :orderId");
		query.setInteger("orderId", order.getOrderId());
		return (String) query.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderSet(java.lang.Integer)
	 */
	@Override
	public OrderSet getOrderSet(Integer orderSetId) {
		return (OrderSet) sessionFactory.getCurrentSession().get(OrderSet.class, orderSetId);
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderSetByUuid(java.lang.String)
	 */
	@Override
	public OrderSet getOrderSetByUuid(String uuid) {
		return (OrderSet) sessionFactory.getCurrentSession().createQuery("from OrderSet where uuid = :uuid").setString(
		    "uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#saveOrderSet(org.openmrs.OrderSet)
	 */
	@Override
	public OrderSet saveOrderSet(OrderSet orderSet) {
		sessionFactory.getCurrentSession().save(orderSet);
		return orderSet;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#publishOrderSet(org.openmrs.Concept, org.openmrs.OrderSet)
	 */
	@Override
	public PublishedOrderSet publishOrderSet(Concept asConcept, OrderSet content) {
		Session session = sessionFactory.getCurrentSession();
		// see if there's already an order set published for this concept
		PublishedOrderSet already = getPublishedOrderSet(asConcept);
		if (already != null) {
			if (already.getOrderSet().equals(content)) {
				return already;
			} else {
				session.delete(already);
			}
		} else {
			// see if this order set is already published as another concept
			already = (PublishedOrderSet) session.createCriteria(PublishedOrderSet.class).add(
			    Restrictions.eq("orderSet", content)).uniqueResult();
			if (already != null) {
				// we know since we're in the else block this isn't what we want
				session.delete(already);
			}
		}
		
		PublishedOrderSet published = new PublishedOrderSet();
		published.setConcept(asConcept);
		published.setOrderSet(content);
		session.save(published);
		return published;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getPublishedOrderSet(org.openmrs.Concept)
	 */
	@Override
	public PublishedOrderSet getPublishedOrderSet(Concept concept) {
		return (PublishedOrderSet) sessionFactory.getCurrentSession().createCriteria(PublishedOrderSet.class).add(
		    Restrictions.eq("concept", concept)).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getPublishedOrderSets(java.lang.String)
	 */
	@Override
	public List<PublishedOrderSet> getPublishedOrderSets(String query) {
		// TODO fix this hacky implementation once we decide what we want to do
		return sessionFactory.getCurrentSession().createCriteria(PublishedOrderSet.class).createCriteria("orderSet").add(
		    Restrictions.ilike("name", query, MatchMode.START)).list();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getHighestOrderId()
	 */
	@Override
	public Integer getHighestOrderId() {
		Query query = sessionFactory.getCurrentSession().createSQLQuery("SELECT max(order_id) FROM orders");
		return (Integer) query.uniqueResult();
	}
	
}

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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Order.OrderAction;
import org.openmrs.Patient;
import org.openmrs.User;
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
	        List<OrderAction> actionsToInclude, List<OrderAction> actionsToExclude, boolean includeVoided) {
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(orderClassType);
		
		if (patients != null && patients.size() > 0)
			crit.add(Restrictions.in("patient", patients));
		
		if (concepts != null && concepts.size() > 0)
			crit.add(Restrictions.in("concept", concepts));
		
		// if an asOfDate is passed in, then we need to restrict to just active type of orders
		if (asOfDate != null) {
			crit.add(Restrictions.le("startDate", asOfDate)); // startDate cannot be null?
			
			crit.add(Restrictions.or(Restrictions.isNull("discontinuedDate"), Restrictions.ge("discontinueDate", asOfDate)));
			
			crit.add(Restrictions.or(Restrictions.isNull("autoExpireDate"), Restrictions.ge("autoExpireDate", asOfDate)));
			
		}
		
		// we are not checking the other status's here because they are 
		// algorithm dependent
		
		if (orderers != null && orderers.size() > 0)
			crit.add(Restrictions.in("orderer", orderers));
		
		if (encounters != null && encounters.size() > 0)
			crit.add(Restrictions.in("encounter", encounters));
		
		if (actionsToInclude != null && actionsToInclude.size() > 0)
			crit.add(Restrictions.in("action", actionsToInclude));
		
		if (actionsToExclude != null && actionsToExclude.size() > 0)
			crit.add(Restrictions.not(Restrictions.in("action", actionsToExclude)));
		
		if (!includeVoided)
			crit.add(Restrictions.eq("voided", false));
		
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
		searchCriteria.add(Restrictions.eq("order.orderNumber", orderNumber));
		return (Order) searchCriteria.uniqueResult();
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
	 * @see org.openmrs.api.db.OrderDAO#getHighestOrderId()
	 */
	@Override
	public Integer getHighestOrderId() {
		Query query = sessionFactory.getCurrentSession().createSQLQuery("SELECT max(order_id) FROM orders");
		return (Integer) query.uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	public List<DrugOrder> getDrugOrdersByPatientAndIngredient(Patient patient, Concept ingredient) {
		Criteria searchDrugOrderCriteria = sessionFactory.getCurrentSession().createCriteria(DrugOrder.class, "order");
		
		searchDrugOrderCriteria.add(Restrictions.eq("order.patient", patient));
		
		searchDrugOrderCriteria.createAlias("drug", "drug");
		Criterion lhs = Restrictions.eq("drug.concept", ingredient);
		
		searchDrugOrderCriteria.createAlias("drug.ingredients", "ingredients");
		Criterion rhs = Restrictions.eq("ingredients.ingredient", ingredient);
		
		searchDrugOrderCriteria.add(Restrictions.or(lhs, rhs));
		
		return (List<DrugOrder>) searchDrugOrderCriteria.list();
	}
	
	/**
	 *  Delete Obs that references (deleted) Order
	 */
	public void deleteObsThatReference(Order order) {
		if (order != null) {
			sessionFactory.getCurrentSession().createQuery("delete Obs where order = :order").setParameter("order", order)
			        .executeUpdate();
			
		}
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.db.OrderDAO#getOrderHistoryByOrderNumber(java.lang.String)
	 */
	@Override
	public List<Order> getOrderHistoryByOrderNumber(String orderNumber) {
		
		Order initial = getOrderByOrderNumber(orderNumber);
		
		// Find previous orders 
		String workNumber = initial.getPreviousOrderNumber();
		LinkedList<Order> previousOrders = new LinkedList<Order>();
		while (workNumber != null) {
			Order previous = getOrderByOrderNumber(workNumber);
			previousOrders.addFirst(previous);
			workNumber = previous.getPreviousOrderNumber();
		}
		
		// Find next orders
		Order nextOrder = null;
		workNumber = orderNumber;
		List<Order> nextOrders = new ArrayList<Order>();
		do {
			// Assumes an order has zero or one next order
			Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(Order.class, "order");
			searchCriteria.add(Restrictions.eq("order.previousOrderNumber", workNumber));
			nextOrder = (Order) searchCriteria.uniqueResult();
			if (nextOrder != null) {
				nextOrders.add(nextOrder);
				workNumber = nextOrder.getOrderNumber();
			}
		} while (nextOrder != null);
		
		List<Order> result = previousOrders;
		result.add(initial);
		result.addAll(nextOrders);
		
		return result;
		
	}
	
}

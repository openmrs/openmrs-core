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
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.LockOptions;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Order;
import org.openmrs.Order.Action;
import org.openmrs.OrderFrequency;
import org.openmrs.Patient;
import org.openmrs.TestOrder;
import org.openmrs.User;
import org.openmrs.api.APIException;
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
	 *      java.util.List, java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public <Ord extends Order> List<Ord> getOrders(Class<Ord> orderClassType, List<Patient> patients,
	        List<Concept> concepts, List<User> orderers, List<Encounter> encounters) {
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(orderClassType);
		
		if (patients.size() > 0)
			crit.add(Restrictions.in("patient", patients));
		
		if (concepts.size() > 0)
			crit.add(Restrictions.in("concept", concepts));
		
		// we are not checking the other status's here because they are
		// algorithm dependent  
		
		if (orderers.size() > 0)
			crit.add(Restrictions.in("orderer", orderers));
		
		if (encounters.size() > 0)
			crit.add(Restrictions.in("encounter", encounters));
		
		crit.addOrder(org.hibernate.criterion.Order.desc("startDate"));
		
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
	 * Delete Obs that references (deleted) Order
	 */
	public void deleteObsThatReference(Order order) {
		if (order != null) {
			sessionFactory.getCurrentSession().createQuery("delete Obs where order = :order").setParameter("order", order)
			        .executeUpdate();
		}
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
	 * @see org.openmrs.api.db.OrderDAO#getNextOrderNumberSeedSequenceValue()
	 */
	@Override
	public Long getNextOrderNumberSeedSequenceValue() {
		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(GlobalProperty.class);
		searchCriteria.add(Restrictions.eq("property", OpenmrsConstants.GLOBAL_PROPERTY_NEXT_ORDER_NUMBER_SEED));
		GlobalProperty globalProperty = (GlobalProperty) sessionFactory.getCurrentSession().get(GlobalProperty.class,
		    OpenmrsConstants.GLOBAL_PROPERTY_NEXT_ORDER_NUMBER_SEED, LockOptions.UPGRADE);
		
		if (globalProperty == null) {
			throw new APIException("Missing global property named: "
			        + OpenmrsConstants.GLOBAL_PROPERTY_NEXT_ORDER_NUMBER_SEED);
		}
		
		String gpTextValue = globalProperty.getPropertyValue();
		if (StringUtils.isBlank(gpTextValue)) {
			throw new APIException("Invalid value for global property named: "
			        + OpenmrsConstants.GLOBAL_PROPERTY_NEXT_ORDER_NUMBER_SEED);
		}
		
		Long gpNumericValue = null;
		try {
			gpNumericValue = Long.parseLong(gpTextValue);
		}
		catch (NumberFormatException ex) {
			throw new APIException("Invalid value for global property named: "
			        + OpenmrsConstants.GLOBAL_PROPERTY_NEXT_ORDER_NUMBER_SEED);
		}
		
		globalProperty.setPropertyValue(String.valueOf(gpNumericValue + 1));
		
		sessionFactory.getCurrentSession().save(globalProperty);
		
		return gpNumericValue;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getActiveOrders(org.openmrs.Patient, Class,
	 *      org.openmrs.CareSetting, java.util.Date)
	 */
	@SuppressWarnings("unchecked")
	public <Ord extends Order> List<Ord> getActiveOrders(Patient patient, Class<Ord> orderClass, CareSetting careSetting,
	        Date asOfDate) {
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(orderClass);
		
		crit.add(Restrictions.eq("patient", patient));
		if (careSetting != null) {
			crit.add(Restrictions.eq("careSetting", careSetting));
		}
		
		//See javadocs on OrderService#getActiveOrders for the description of an active Order
		crit.add(Restrictions.ne("action", Action.DISCONTINUE));
		crit.add(Restrictions.eq("voided", false));
		
		Criterion startDateEqualToOrBeforeAsOfDate = Restrictions.le("startDate", asOfDate);
		crit.add(startDateEqualToOrBeforeAsOfDate);
		
		Disjunction dateStoppedAndAutoExpDateDisjunction = Restrictions.disjunction();
		Criterion stopAndAutoExpDateAreBothNull = Restrictions.and(Restrictions.isNull("dateStopped"), Restrictions
		        .isNull("autoExpireDate"));
		dateStoppedAndAutoExpDateDisjunction.add(stopAndAutoExpDateAreBothNull);
		
		Criterion autoExpireDateEqualToOrAfterAsOfDate = Restrictions.and(Restrictions.isNull("dateStopped"), Restrictions
		        .ge("autoExpireDate", asOfDate));
		dateStoppedAndAutoExpDateDisjunction.add(autoExpireDateEqualToOrAfterAsOfDate);
		
		dateStoppedAndAutoExpDateDisjunction.add(Restrictions.ge("dateStopped", asOfDate));
		
		crit.add(dateStoppedAndAutoExpDateDisjunction);
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getCareSetting(Integer)
	 */
	@Override
	public CareSetting getCareSetting(Integer careSettingId) {
		return (CareSetting) sessionFactory.getCurrentSession().get(CareSetting.class, careSettingId);
	}
	
	/**
	 * @see OrderDAO#getCareSettingByUuid(String)
	 */
	@Override
	public CareSetting getCareSettingByUuid(String uuid) {
		return (CareSetting) sessionFactory.getCurrentSession().createQuery("from CareSetting cs where cs.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see OrderDAO#getCareSettingByName(String)
	 */
	@Override
	public CareSetting getCareSettingByName(String name) {
		return (CareSetting) sessionFactory.getCurrentSession().createCriteria(CareSetting.class).add(
		    Restrictions.ilike("name", name)).uniqueResult();
	}
	
	/**
	 * @see OrderDAO#getCareSettings(boolean)
	 */
	@Override
	public List<CareSetting> getCareSettings(boolean includeRetired) {
		Criteria c = sessionFactory.getCurrentSession().createCriteria(CareSetting.class);
		if (!includeRetired) {
			c.add(Restrictions.eq("retired", false));
		}
		return c.list();
	}
	
	/**
	 * @See OrderDAO#getOrderFrequency
	 */
	@Override
	public OrderFrequency getOrderFrequency(Integer orderFrequencyId) {
		return (OrderFrequency) sessionFactory.getCurrentSession().get(OrderFrequency.class, orderFrequencyId);
	}
	
	/**
	 * @See OrderDAO#getOrderFrequencyByUuid
	 */
	@Override
	public OrderFrequency getOrderFrequencyByUuid(String uuid) {
		return (OrderFrequency) sessionFactory.getCurrentSession().createQuery("from OrderFrequency o where o.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @See OrderDAO#getOrderFrequencies(boolean)
	 */
	@Override
	public List<OrderFrequency> getOrderFrequencies(boolean includeRetired) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(OrderFrequency.class);
		if (!includeRetired) {
			criteria.add(Restrictions.eq("retired", false));
		}
		return criteria.list();
	}
	
	/**
	 * @See OrderDAO#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	@Override
	public List<OrderFrequency> getOrderFrequencies(String searchPhrase, Locale locale, boolean exactLocale,
	        boolean includeRetired) {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(OrderFrequency.class, "orderFreq");
		criteria.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
		
		//match on the concept names of the concepts
		criteria.createAlias("orderFreq.concept", "concept");
		criteria.createAlias("concept.names", "conceptName");
		criteria.add(Restrictions.ilike("conceptName.name", searchPhrase, MatchMode.ANYWHERE));
		if (locale != null) {
			List<Locale> locales = new ArrayList<Locale>(2);
			locales.add(locale);
			//look in the broader locale too if exactLocale is false e.g en for en_GB
			if (!exactLocale && StringUtils.isNotBlank(locale.getCountry())) {
				locales.add(new Locale(locale.getLanguage()));
			}
			criteria.add(Restrictions.in("conceptName.locale", locales));
		}
		
		if (!includeRetired) {
			criteria.add(Restrictions.eq("orderFreq.retired", false));
		}
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#saveOrderFrequency(org.openmrs.OrderFrequency)
	 */
	@Override
	public OrderFrequency saveOrderFrequency(OrderFrequency orderFrequency) {
		sessionFactory.getCurrentSession().saveOrUpdate(orderFrequency);
		return orderFrequency;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#purgeOrderFrequency(org.openmrs.OrderFrequency)
	 */
	@Override
	public void purgeOrderFrequency(OrderFrequency orderFrequency) {
		sessionFactory.getCurrentSession().delete(orderFrequency);
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#isOrderFrequencyInUse(org.openmrs.OrderFrequency)
	 */
	@Override
	public boolean isOrderFrequencyInUse(OrderFrequency orderFrequency) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(DrugOrder.class);
		criteria.add(Restrictions.eq("frequency", orderFrequency));
		criteria.setMaxResults(1);
		if (criteria.list().size() > 0) {
			return true;
		}
		
		criteria = sessionFactory.getCurrentSession().createCriteria(TestOrder.class);
		criteria.add(Restrictions.eq("frequency", orderFrequency));
		criteria.setMaxResults(1);
		if (criteria.list().size() > 0) {
			return true;
		}
		
		return false;
	}
}

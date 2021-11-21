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

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.CareSetting;
import org.openmrs.Encounter;
import org.openmrs.GlobalProperty;
import org.openmrs.Order;
import org.openmrs.OrderAttribute;
import org.openmrs.OrderAttributeType;
import org.openmrs.OrderFrequency;
import org.openmrs.OrderGroup;
import org.openmrs.OrderGroupAttribute;
import org.openmrs.OrderGroupAttributeType;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.OrderDAO;
import org.openmrs.parameter.OrderSearchCriteria;
import org.openmrs.User;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;


/**
 * This class should not be used directly. This is just a common implementation of the OrderDAO that
 * is used by the OrderService. This class is injected by spring into the desired OrderService
 * class. This injection is determined by the xml mappings and elements in the spring application
 * context: /metadata/api/spring/applicationContext.xml.<br>
 * <br>
 * The OrderService should be used for all Order related database manipulation.
 * 
 * @see org.openmrs.api.OrderService
 * @see org.openmrs.api.db.OrderDAO
 */
public class HibernateOrderDAO implements OrderDAO {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateOrderDAO.class);
	
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
	 * @see org.openmrs.api.OrderService#saveOrder(org.openmrs.Order, org.openmrs.api.OrderContext)
	 */
	@Override
	public Order saveOrder(Order order) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(order);
		
		return order;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#deleteOrder(org.openmrs.Order)
	 * @see org.openmrs.api.OrderService#purgeOrder(org.openmrs.Order)
	 */
	@Override
	public void deleteOrder(Order order) throws DAOException {
		sessionFactory.getCurrentSession().delete(order);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getOrder(java.lang.Integer)
	 */
	@Override
	public Order getOrder(Integer orderId) throws DAOException {
		log.debug("getting order #{}", orderId);
		
		return (Order) sessionFactory.getCurrentSession().get(Order.class, orderId);
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrders(org.openmrs.OrderType, java.util.List,
	 *      java.util.List, java.util.List, java.util.List)
	 */
	@Override
	public List<Order> getOrders(OrderType orderType, List<Patient> patients, List<Concept> concepts, List<User> orderers,
	        List<Encounter> encounters) {
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Order.class);
		
		if (orderType != null) {
			crit.add(Restrictions.eq("orderType", orderType));
		}
		
		if (!patients.isEmpty()) {
			crit.add(Restrictions.in("patient", patients));
		}
		
		if (!concepts.isEmpty()) {
			crit.add(Restrictions.in("concept", concepts));
		}
		
		// we are not checking the other status's here because they are
		// algorithm dependent  
		
		if (!orderers.isEmpty()) {
			crit.add(Restrictions.in("orderer", orderers));
		}
		
		if (!encounters.isEmpty()) {
			crit.add(Restrictions.in("encounter", encounters));
		}
		
		crit.addOrder(org.hibernate.criterion.Order.desc("dateActivated"));
		
		return crit.list();
	}

	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrders(OrderSearchCriteria)
	 */
	@Override
	public List<Order> getOrders(OrderSearchCriteria searchCriteria) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Order.class);
		
		if (searchCriteria.getPatient() != null && searchCriteria.getPatient().getPatientId() != null) {
			crit.add(Restrictions.eq("patient", searchCriteria.getPatient()));
		}
		if (searchCriteria.getCareSetting() != null && searchCriteria.getCareSetting().getId() != null) {
			crit.add(Restrictions.eq("careSetting", searchCriteria.getCareSetting()));
		}
		if (searchCriteria.getConcepts() != null && !searchCriteria.getConcepts().isEmpty()) {
			crit.add(Restrictions.in("concept", searchCriteria.getConcepts()));
		}
		if (searchCriteria.getOrderTypes() != null && !searchCriteria.getOrderTypes().isEmpty()) {
			crit.add(Restrictions.in("orderType", searchCriteria.getOrderTypes()));
		}
		if (searchCriteria.getOrderNumber() != null) {
			crit.add(Restrictions.eq("orderNumber", searchCriteria.getOrderNumber()).ignoreCase());
		}
		if (searchCriteria.getAccessionNumber() != null) {
			crit.add(Restrictions.eq("accessionNumber", searchCriteria.getAccessionNumber()).ignoreCase());
		}
		if (searchCriteria.getActivatedOnOrBeforeDate() != null) {
			// set the date's time to the last millisecond of the date
			Calendar cal = Calendar.getInstance();
			cal.setTime(searchCriteria.getActivatedOnOrBeforeDate());
			crit.add(Restrictions.le("dateActivated", OpenmrsUtil.getLastMomentOfDay(cal.getTime())));
		}
		if (searchCriteria.getActivatedOnOrAfterDate() != null) {
			// set the date's time to 00:00:00.000
			Calendar cal = Calendar.getInstance();
			cal.setTime(searchCriteria.getActivatedOnOrAfterDate());
			crit.add(Restrictions.ge("dateActivated", OpenmrsUtil.firstSecondOfDay(cal.getTime())));
		}
		if (searchCriteria.isStopped()) {
			// an order is considered Canceled regardless of the time when the dateStopped was set
			crit.add(Restrictions.isNotNull("dateStopped"));
		}
		if (searchCriteria.getAutoExpireOnOrBeforeDate() != null) {
			// set the date's time to the last millisecond of the date
			Calendar cal = Calendar.getInstance();
			cal.setTime(searchCriteria.getAutoExpireOnOrBeforeDate());
			crit.add(Restrictions.le("autoExpireDate", OpenmrsUtil.getLastMomentOfDay(cal.getTime())));
		}
        if (searchCriteria.getAction() != null) {
            crit.add(Restrictions.eq("action", searchCriteria.getAction()));
        }
        if (searchCriteria.getExcludeDiscontinueOrders()) {
            crit.add(Restrictions.or(
                    Restrictions.ne("action", Order.Action.DISCONTINUE),
                    Restrictions.isNull("action")));
        }
        SimpleExpression fulfillerStatusExpr = null;
        if (searchCriteria.getFulfillerStatus() != null) {
            fulfillerStatusExpr = Restrictions.eq("fulfillerStatus", searchCriteria.getFulfillerStatus());
		}
        Criterion fulfillerStatusCriteria = null;
        if (searchCriteria.getIncludeNullFulfillerStatus() != null ) {
            if (searchCriteria.getIncludeNullFulfillerStatus().booleanValue()) {
                fulfillerStatusCriteria = Restrictions.isNull("fulfillerStatus");
            } else {
                fulfillerStatusCriteria = Restrictions.isNotNull("fulfillerStatus");
            }
        }

        if (fulfillerStatusExpr != null && fulfillerStatusCriteria != null) {
            crit.add(Restrictions.or(fulfillerStatusExpr, fulfillerStatusCriteria));
        } else if (fulfillerStatusExpr != null) {
            crit.add(fulfillerStatusExpr);
        } else if ( fulfillerStatusCriteria != null ){
            crit.add(fulfillerStatusCriteria);
        }

		if (searchCriteria.getExcludeCanceledAndExpired()) {
			Calendar cal = Calendar.getInstance();
			// exclude expired orders (include only orders with autoExpireDate = null or autoExpireDate in the future)
			crit.add(Restrictions.or(
					Restrictions.isNull("autoExpireDate"),
					Restrictions.gt("autoExpireDate", cal.getTime())));
			// exclude Canceled Orders
			crit.add(Restrictions.or(
					Restrictions.isNull("dateStopped"),
					Restrictions.gt("dateStopped", cal.getTime())));
		}
		if (searchCriteria.getCanceledOrExpiredOnOrBeforeDate() != null) {
			// set the date's time to the last millisecond of the date
			Calendar cal = Calendar.getInstance();
			cal.setTime(searchCriteria.getCanceledOrExpiredOnOrBeforeDate());
			crit.add(Restrictions.or(
					Restrictions.and(
							Restrictions.isNotNull("dateStopped"),
							Restrictions.le("dateStopped", OpenmrsUtil.getLastMomentOfDay(cal.getTime()))),
					Restrictions.and(
							Restrictions.isNotNull("autoExpireDate"),
							Restrictions.le("autoExpireDate", OpenmrsUtil.getLastMomentOfDay(cal.getTime())))));
		}
		if (!searchCriteria.getIncludeVoided()) {
			crit.add(Restrictions.eq("voided", false));
		}

		crit.addOrder(org.hibernate.criterion.Order.desc("dateActivated"));
		
		return crit.list();
	}
	
	/**
	 * @see OrderDAO#getOrders(org.openmrs.Patient, org.openmrs.CareSetting, java.util.List,
	 *      boolean, boolean)
	 */
	@Override
	public List<Order> getOrders(Patient patient, CareSetting careSetting, List<OrderType> orderTypes,
	        boolean includeVoided, boolean includeDiscontinuationOrders) {
		return createOrderCriteria(patient, careSetting, orderTypes, includeVoided, includeDiscontinuationOrders).list();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderByUuid(java.lang.String)
	 */
	@Override
	public Order getOrderByUuid(String uuid) {
		return (Order) sessionFactory.getCurrentSession().createQuery("from Order o where o.uuid = :uuid").setString("uuid",
		    uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getRevisionOrder(org.openmrs.Order)
	 */
	@Override
	public Order getDiscontinuationOrder(Order order) {

		return (Order) sessionFactory.getCurrentSession().createCriteria(Order.class).add(
		    Restrictions.eq("previousOrder", order)).add(Restrictions.eq("action", Order.Action.DISCONTINUE)).add(
		    Restrictions.eq("voided", false)).uniqueResult();
	}
	
	@Override
	public Order getRevisionOrder(Order order) throws APIException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Order.class);
		criteria.add(Restrictions.eq("previousOrder", order)).add(Restrictions.eq("action", Order.Action.REVISE)).add(
		    Restrictions.eq("voided", false));
		return (Order) criteria.uniqueResult();
	}
	
	@Override
	public List<Object[]> getOrderFromDatabase(Order order, boolean isOrderADrugOrder) throws APIException {
		String sql = "SELECT patient_id, care_setting, concept_id FROM orders WHERE order_id = :orderId";
		
		if (isOrderADrugOrder) {
			sql = " SELECT o.patient_id, o.care_setting, o.concept_id, d.drug_inventory_id "
			        + " FROM orders o, drug_order d WHERE o.order_id = d.order_id AND o.order_id = :orderId";
		}
		Query query = sessionFactory.getCurrentSession().createSQLQuery(sql);
		query.setParameter("orderId", order.getOrderId());
		
		//prevent hibernate from flushing before fetching the list
		query.setHibernateFlushMode(FlushMode.MANUAL);
		
		return query.list();
	}
	
	/**
	 * @see OrderDAO#saveOrderGroup(OrderGroup)
	 */
	@Override
	public OrderGroup saveOrderGroup(OrderGroup orderGroup) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(orderGroup);
		return orderGroup;
	}
	
	/**
	 * @see OrderDAO#getOrderGroupByUuid(String)
	 * @see org.openmrs.api.OrderService#getOrderGroupByUuid(String)
	 */
	@Override
	public OrderGroup getOrderGroupByUuid(String uuid) throws DAOException {
		return (OrderGroup) sessionFactory.getCurrentSession().createQuery("from OrderGroup o where o.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see OrderDAO#getOrderGroupById(Integer)
	 * @see org.openmrs.api.OrderService#getOrderGroup(Integer)
	 */
	@Override
	public OrderGroup getOrderGroupById(Integer orderGroupId) throws DAOException {
		return (OrderGroup) sessionFactory.getCurrentSession().get(OrderGroup.class, orderGroupId);
	}
	
	/**
	 * Delete Obs that references (deleted) Order
	 */
	@Override
	public void deleteObsThatReference(Order order) {
		if (order != null) {
			sessionFactory.getCurrentSession().createQuery("delete Obs where order = :order").setParameter("order", order)
			        .executeUpdate();
		}
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderByOrderNumber(java.lang.String)
	 */
	@Override
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
		GlobalProperty globalProperty = (GlobalProperty) sessionFactory.getCurrentSession().get(GlobalProperty.class,
		    OpenmrsConstants.GP_NEXT_ORDER_NUMBER_SEED, LockOptions.UPGRADE);
		
		if (globalProperty == null) {
			throw new APIException("GlobalProperty.missing", new Object[] { OpenmrsConstants.GP_NEXT_ORDER_NUMBER_SEED });
		}
		
		String gpTextValue = globalProperty.getPropertyValue();
		if (StringUtils.isBlank(gpTextValue)) {
			throw new APIException("GlobalProperty.invalid.value",
			        new Object[] { OpenmrsConstants.GP_NEXT_ORDER_NUMBER_SEED });
		}
		
		Long gpNumericValue;
		try {
			gpNumericValue = Long.parseLong(gpTextValue);
		}
		catch (NumberFormatException ex) {
			throw new APIException("GlobalProperty.invalid.value",
			        new Object[] { OpenmrsConstants.GP_NEXT_ORDER_NUMBER_SEED });
		}
		
		globalProperty.setPropertyValue(String.valueOf(gpNumericValue + 1));
		
		sessionFactory.getCurrentSession().save(globalProperty);
		
		return gpNumericValue;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getActiveOrders(org.openmrs.Patient, java.util.List,
	 *      org.openmrs.CareSetting, java.util.Date)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Order> getActiveOrders(Patient patient, List<OrderType> orderTypes, CareSetting careSetting, Date asOfDate) {
		Criteria crit = createOrderCriteria(patient, careSetting, orderTypes, false, false);
		crit.add(Restrictions.le("dateActivated", asOfDate));
		
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
	 * Creates and returns a Criteria Object filtering on the specified parameters
	 * 
	 * @param patient
	 * @param careSetting
	 * @param orderTypes
	 * @param includeVoided
	 * @param includeDiscontinuationOrders
	 * @return
	 */
	private Criteria createOrderCriteria(Patient patient, CareSetting careSetting, List<OrderType> orderTypes,
	        boolean includeVoided, boolean includeDiscontinuationOrders) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Order.class);
		if (patient != null) {
			criteria.add(Restrictions.eq("patient", patient));
		}
		if (careSetting != null) {
			criteria.add(Restrictions.eq("careSetting", careSetting));
		}
		if (orderTypes != null && !orderTypes.isEmpty()) {
			criteria.add(Restrictions.in("orderType", orderTypes));
		}
		if (!includeVoided) {
			criteria.add(Restrictions.eq("voided", false));
		}
		if (!includeDiscontinuationOrders) {
			criteria.add(Restrictions.ne("action", Order.Action.DISCONTINUE));
		}
		
		return criteria;
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
	 * @see OrderDAO#getOrderTypeByName
	 */
	@Override
	public OrderType getOrderTypeByName(String orderTypeName) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(OrderType.class);
		criteria.add(Restrictions.eq("name", orderTypeName));
		return (OrderType) criteria.uniqueResult();
	}
	
	/**
	 * @see OrderDAO#getOrderFrequency
	 */
	@Override
	public OrderFrequency getOrderFrequency(Integer orderFrequencyId) {
		return (OrderFrequency) sessionFactory.getCurrentSession().get(OrderFrequency.class, orderFrequencyId);
	}
	
	/**
	 * @see OrderDAO#getOrderFrequencyByUuid
	 */
	@Override
	public OrderFrequency getOrderFrequencyByUuid(String uuid) {
		return (OrderFrequency) sessionFactory.getCurrentSession().createQuery("from OrderFrequency o where o.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see OrderDAO#getOrderFrequencies(boolean)
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
	 * @see OrderDAO#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
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
			List<Locale> locales = new ArrayList<>(2);
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
		
		Set<EntityType<?>> entities = sessionFactory.getMetamodel().getEntities();
		
		for (EntityType<?> entityTpe : entities) {
			Class<?> entityClass = entityTpe.getJavaType();
			if (Order.class.equals(entityClass)) {
				//ignore the org.openmrs.Order class itself
				continue;
			}

			if (!Order.class.isAssignableFrom(entityClass)) {
				//not a sub class of Order
				continue;
			}

			for (Attribute<?,?> attribute : entityTpe.getDeclaredAttributes()) {
				if (attribute.getJavaType().equals(OrderFrequency.class)) {
					Criteria criteria = sessionFactory.getCurrentSession().createCriteria(entityClass);
					criteria.add(Restrictions.eq(attribute.getName(), orderFrequency));
					criteria.setMaxResults(1);
					if (!criteria.list().isEmpty()) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderFrequencyByConcept(org.openmrs.Concept)
	 */
	@Override
	public OrderFrequency getOrderFrequencyByConcept(Concept concept) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(OrderFrequency.class);
		criteria.add(Restrictions.eq("concept", concept));
		return (OrderFrequency) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderType(Integer)
	 */
	@Override
	public OrderType getOrderType(Integer orderTypeId) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(OrderType.class);
		criteria.add(Restrictions.eq("orderTypeId", orderTypeId));
		return (OrderType) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderTypeByUuid(String)
	 */
	@Override
	public OrderType getOrderTypeByUuid(String uuid) {
		return (OrderType) sessionFactory.getCurrentSession().createQuery("from OrderType o where o.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderTypes(boolean)
	 */
	@Override
	public List<OrderType> getOrderTypes(boolean includeRetired) {
		Criteria c = sessionFactory.getCurrentSession().createCriteria(OrderType.class);
		if (!includeRetired) {
			c.add(Restrictions.eq("retired", false));
		}
		return c.list();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderTypeByConceptClass(org.openmrs.ConceptClass)
	 */
	@Override
	public OrderType getOrderTypeByConceptClass(ConceptClass conceptClass) {
		return (OrderType) sessionFactory.getCurrentSession().createQuery(
		    "from OrderType where :conceptClass in elements(conceptClasses)").setParameter("conceptClass", conceptClass)
		        .uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.OrderService#saveOrderType(org.openmrs.OrderType)
	 */
	@Override
	public OrderType saveOrderType(OrderType orderType) {
		sessionFactory.getCurrentSession().saveOrUpdate(orderType);
		return orderType;
	}
	
	/**
	 * @see org.openmrs.api.OrderService#purgeOrderType(org.openmrs.OrderType)
	 */
	@Override
	public void purgeOrderType(OrderType orderType) {
		sessionFactory.getCurrentSession().delete(orderType);
	}
	
	/**
	 * @see org.openmrs.api.OrderService#getSubtypes(org.openmrs.OrderType, boolean)
	 */
	@Override
	public List<OrderType> getOrderSubtypes(OrderType orderType, boolean includeRetired) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(OrderType.class);
		criteria.add(Restrictions.eq("parent", orderType));
		if (!includeRetired) {
			criteria.add(Restrictions.eq("retired", false));
		}
		return criteria.list();
	}
	
	@Override
	public boolean isOrderTypeInUse(OrderType orderType) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Order.class);
		criteria.add(Restrictions.eq("orderType", orderType));
		return !criteria.list().isEmpty();
	}
	/**
	 * @see OrderDAO#getOrderGroupsByPatient(Patient)
	 */
	@Override
	public List<OrderGroup> getOrderGroupsByPatient(Patient patient) throws DAOException {
		if (patient == null) {
			throw new APIException("Patient cannot be null");
		}
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(OrderGroup.class);
		criteria.add(Restrictions.eq("patient", patient));
		return criteria.list();
	}

	/**
	 * @see OrderDAO#getOrderGroupsByEncounter(Encounter)
	 */
	@Override
	public List<OrderGroup> getOrderGroupsByEncounter(Encounter encounter) throws DAOException {
		if (encounter == null) {
			throw new APIException("Encounter cannot be null");
		}
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(OrderGroup.class);
		criteria.add(Restrictions.eq("encounter", encounter));
		return criteria.list();
	}

	/**
	 * @see org.openmrs.api.db.OrderDAO#getAllOrderGroupAttributeTypes()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<OrderGroupAttributeType> getAllOrderGroupAttributeTypes() throws DAOException{
		return sessionFactory.getCurrentSession().createCriteria(OrderGroupAttributeType.class).list();
	}

	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderGroupAttributeType(java.lang.Integer)
	 */
	@Override
	public OrderGroupAttributeType getOrderGroupAttributeType(Integer orderGroupAttributeTypeId) throws DAOException{
		return sessionFactory.getCurrentSession().get(OrderGroupAttributeType.class, orderGroupAttributeTypeId);
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderGroupAttributeTypeByUuid(java.lang.String)
	 */
	@Override
	public OrderGroupAttributeType getOrderGroupAttributeTypeByUuid(String uuid) throws DAOException{
		return (OrderGroupAttributeType) sessionFactory.getCurrentSession().createCriteria(OrderGroupAttributeType.class).add(
			Restrictions.eq("uuid", uuid)).uniqueResult();
	}

	/**
	 * @see org.openmrs.api.db.OrderDAO#saveOrderGroupAttributeType(org.openmrs.OrderGroupAttributeType)
	 */
	@Override
	public OrderGroupAttributeType saveOrderGroupAttributeType(OrderGroupAttributeType orderGroupAttributeType)throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(orderGroupAttributeType);
		return orderGroupAttributeType;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#deleteOrderGroupAttributeType(org.openmrs.OrderGroupAttributeType)
	 */
	@Override
	public void deleteOrderGroupAttributeType(OrderGroupAttributeType orderGroupAttributeType) throws DAOException{
		sessionFactory.getCurrentSession().delete(orderGroupAttributeType);
	}

	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderGroupAttributeByUuid(String)
	 */
	@Override
	public OrderGroupAttribute getOrderGroupAttributeByUuid(String uuid)  throws DAOException{
		return (OrderGroupAttribute) sessionFactory.getCurrentSession().createQuery("from OrderGroupAttribute d where d.uuid = :uuid")
			.setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderGroupAttributeTypeByName(String)
	 */
	@Override
	public OrderGroupAttributeType getOrderGroupAttributeTypeByName(String name) throws DAOException{
		return (OrderGroupAttributeType) sessionFactory.getCurrentSession().createCriteria(OrderGroupAttributeType.class).add(
			Restrictions.eq("name", name)).uniqueResult();
	}

	/**
	 * @param uuid The uuid associated with the order attribute to retrieve.
	 * @see org.openmrs.api.db.OrderDAO#getOrderAttributeByUuid(String)
	 */
	@Override
	public OrderAttribute getOrderAttributeByUuid(String uuid) throws DAOException {
		return (OrderAttribute) sessionFactory.getCurrentSession()
				.createQuery("from OrderAttribute a where a.uuid = :uuid")
				.setString("uuid", uuid).uniqueResult();
	}

	/**
	 * @see org.openmrs.api.db.OrderDAO#getAllOrderAttributeTypes()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<OrderAttributeType> getAllOrderAttributeTypes() throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(OrderAttributeType.class).list();
	}

	/**
	 * @param orderAttributeTypeId The orderAttributeTypeId for the order attribute type to retrieve.
	 * @see org.openmrs.api.db.OrderDAO#getOrderAttributeTypeById(Integer)
	 */
	@Override
	public OrderAttributeType getOrderAttributeTypeById(Integer orderAttributeTypeId) throws DAOException {
		return sessionFactory.getCurrentSession().get(OrderAttributeType.class, orderAttributeTypeId);
	}

	/**
	 * @param uuid The uuid associated with the order attribute type to retrieve
	 * @see org.openmrs.api.db.OrderDAO#getOrderAttributeTypeByUuid(String)
	 */
	@Override
	public OrderAttributeType getOrderAttributeTypeByUuid(String uuid) throws DAOException {
		return (OrderAttributeType) sessionFactory.getCurrentSession().createCriteria(OrderAttributeType.class)
				.add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}

	/**
	 * @param orderAttributeType The orderAttributeType to save
	 * @see org.openmrs.api.db.OrderDAO#saveOrderAttributeType(OrderAttributeType)
	 */
	@Override
	public OrderAttributeType saveOrderAttributeType(OrderAttributeType orderAttributeType) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(orderAttributeType);
		return orderAttributeType;
	}

	/**
	 * @param orderAttributeType The orderAttributeType to retire
	 * @see org.openmrs.api.db.OrderDAO#purgeOrderAttributeType(OrderAttributeType)
	 */
	@Override
	public void deleteOrderAttributeType(OrderAttributeType orderAttributeType) throws DAOException {
		sessionFactory.getCurrentSession().delete(orderAttributeType);
	}

	/**
	 * @param name The name of the order attribute type to retrieve
	 * @see org.openmrs.api.db.OrderDAO#getOrderAttributeTypeByName(String)
	 */
	@Override
	public OrderAttributeType getOrderAttributeTypeByName(String name) throws DAOException {
		return (OrderAttributeType) sessionFactory.getCurrentSession().createCriteria(OrderAttributeType.class)
				.add(Restrictions.eq("name", name)).uniqueResult();
	}
}

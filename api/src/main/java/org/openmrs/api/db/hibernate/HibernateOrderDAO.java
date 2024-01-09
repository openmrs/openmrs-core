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
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Concept;
import org.openmrs.CareSetting;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptName;
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

import javax.persistence.FlushModeType;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
		
		return sessionFactory.getCurrentSession().get(Order.class, orderId);
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrders(org.openmrs.OrderType, java.util.List,
	 *      java.util.List, java.util.List, java.util.List)
	 */
	@Override
	public List<Order> getOrders(OrderType orderType, List<Patient> patients, List<Concept> concepts, List<User> orderers, List<Encounter> encounters) {

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> root = cq.from(Order.class);

		List<Predicate> predicates = new ArrayList<>();

		if (orderType != null) {
			predicates.add(cb.equal(root.get("orderType"), orderType));
		}

		if (!patients.isEmpty()) {
			predicates.add(root.get("patient").in(patients));
		}

		if (!concepts.isEmpty()) {
			predicates.add(root.get("concept").in(concepts));
		}

		// we are not checking the other status's here because they are
		// algorithm dependent  

		if (!orderers.isEmpty()) {
			predicates.add(root.get("orderer").in(orderers));
		}

		if (!encounters.isEmpty()) {
			predicates.add(root.get("encounter").in(encounters));
		}

		cq.where(predicates.toArray(new Predicate[]{}));
		cq.orderBy(cb.desc(root.get("dateActivated")));

		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrders(OrderSearchCriteria)
	 */
	@Override
	public List<Order> getOrders(OrderSearchCriteria searchCriteria) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> root = cq.from(Order.class);

		List<Predicate> predicates = new ArrayList<>();

		if (searchCriteria.getPatient() != null && searchCriteria.getPatient().getPatientId() != null) {
			predicates.add(cb.equal(root.get("patient"), searchCriteria.getPatient()));
		}
		if (searchCriteria.getCareSetting() != null && searchCriteria.getCareSetting().getId() != null) {
			predicates.add(cb.equal(root.get("careSetting"), searchCriteria.getCareSetting()));
		}
		if (searchCriteria.getConcepts() != null && !searchCriteria.getConcepts().isEmpty()) {
			predicates.add(root.get("concept").in(searchCriteria.getConcepts()));
		}
		if (searchCriteria.getOrderTypes() != null && !searchCriteria.getOrderTypes().isEmpty()) {
			predicates.add(root.get("orderType").in(searchCriteria.getOrderTypes()));
		}
		if (searchCriteria.getOrderNumber() != null) {
			predicates.add(cb.equal(cb.lower(root.get("orderNumber")), searchCriteria.getOrderNumber().toLowerCase()));
		}
		if (searchCriteria.getAccessionNumber() != null) {
			predicates.add(cb.equal(cb.lower(root.get("accessionNumber")), searchCriteria.getAccessionNumber().toLowerCase()));
		}
		if (searchCriteria.getActivatedOnOrBeforeDate() != null) {
			// set the date's time to the last millisecond of the date
			Calendar cal = Calendar.getInstance();
			cal.setTime(searchCriteria.getActivatedOnOrBeforeDate());
			predicates.add(cb.lessThanOrEqualTo(root.get("dateActivated"), OpenmrsUtil.getLastMomentOfDay(cal.getTime())));
		}
		if (searchCriteria.getActivatedOnOrAfterDate() != null) {
			// set the date's time to 00:00:00.000
			Calendar cal = Calendar.getInstance();
			cal.setTime(searchCriteria.getActivatedOnOrAfterDate());
			predicates.add(cb.greaterThanOrEqualTo(root.get("dateActivated"), OpenmrsUtil.firstSecondOfDay(cal.getTime())));
		}
		if (searchCriteria.isStopped()) {
			// an order is considered Canceled regardless of the time when the dateStopped was set
			predicates.add(cb.isNotNull(root.get("dateStopped")));
		}
		if (searchCriteria.getAutoExpireOnOrBeforeDate() != null) {
			// set the date's time to the last millisecond of the date
			Calendar cal = Calendar.getInstance();
			cal.setTime(searchCriteria.getAutoExpireOnOrBeforeDate());
			predicates.add(cb.lessThanOrEqualTo(root.get("autoExpireDate"), OpenmrsUtil.getLastMomentOfDay(cal.getTime())));
		}
		if (searchCriteria.getAction() != null) {
			predicates.add(cb.equal(root.get("action"), searchCriteria.getAction()));
		}
		if (searchCriteria.getExcludeDiscontinueOrders()) {
			predicates.add(cb.or(
				cb.notEqual(root.get("action"), Order.Action.DISCONTINUE),
				cb.isNull(root.get("action"))));
		}

		Predicate fulfillerStatusExpr = null;
		if (searchCriteria.getFulfillerStatus() != null) {
			fulfillerStatusExpr = cb.equal(root.get("fulfillerStatus"), searchCriteria.getFulfillerStatus());
		}
		
		Predicate fulfillerStatusCriteria = null;
		if (searchCriteria.getIncludeNullFulfillerStatus() != null ) {
			if (searchCriteria.getIncludeNullFulfillerStatus()) {
				fulfillerStatusCriteria = cb.isNull(root.get("fulfillerStatus"));
			} else {
				fulfillerStatusCriteria = cb.isNotNull(root.get("fulfillerStatus"));
			}
		}

		if (fulfillerStatusExpr != null && fulfillerStatusCriteria != null) {
			predicates.add(cb.or(fulfillerStatusExpr, fulfillerStatusCriteria));
		} else if (fulfillerStatusExpr != null) {
			predicates.add(fulfillerStatusExpr);
		} else if ( fulfillerStatusCriteria != null ){
			predicates.add(fulfillerStatusCriteria);
		}
		
		if (searchCriteria.getExcludeCanceledAndExpired()) {
			Calendar cal = Calendar.getInstance();
			// exclude expired orders (include only orders with autoExpireDate = null or autoExpireDate in the future)
			predicates.add(cb.or(
				cb.isNull(root.get("autoExpireDate")),
				cb.greaterThan(root.get("autoExpireDate"), cal.getTime())));
			// exclude Canceled Orders
			predicates.add(cb.or(
				cb.isNull(root.get("dateStopped")),
				cb.greaterThan(root.get("dateStopped"), cal.getTime())));
		}
		if (searchCriteria.getCanceledOrExpiredOnOrBeforeDate() != null) {
			// set the date's time to the last millisecond of the date
			Calendar cal = Calendar.getInstance();
			cal.setTime(searchCriteria.getCanceledOrExpiredOnOrBeforeDate());
			predicates.add(cb.or(
				cb.and(cb.isNotNull(root.get("dateStopped")), cb.lessThanOrEqualTo(root.get("dateStopped"), OpenmrsUtil.getLastMomentOfDay(cal.getTime()))),
				cb.and(cb.isNotNull(root.get("autoExpireDate")), cb.lessThanOrEqualTo(root.get("autoExpireDate"), OpenmrsUtil.getLastMomentOfDay(cal.getTime())))));
		}
		if (!searchCriteria.getIncludeVoided()) {
			predicates.add(cb.isFalse(root.get("voided")));
		}

		cq.where(predicates.toArray(new Predicate[]{}));
		cq.orderBy(cb.desc(root.get("dateActivated")));

		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see OrderDAO#getOrders(org.openmrs.Patient, org.openmrs.CareSetting, java.util.List,
	 *      boolean, boolean)
	 */
	@Override
	public List<Order> getOrders(Patient patient, CareSetting careSetting, List<OrderType> orderTypes, boolean includeVoided,
	        boolean includeDiscontinuationOrders) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> root = cq.from(Order.class);

		List<Predicate> predicates = createOrderCriteria(cb, root, patient, careSetting, orderTypes, includeVoided, includeDiscontinuationOrders);

		cq.where(predicates.toArray(new Predicate[]{}));

		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderByUuid(java.lang.String)
	 */
	@Override
	public Order getOrderByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, Order.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getRevisionOrder(org.openmrs.Order)
	 */
	@Override
	public Order getDiscontinuationOrder(Order order) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> root = cq.from(Order.class);

		cq.where(
			cb.equal(root.get("previousOrder"), order),
			cb.equal(root.get("action"), Order.Action.DISCONTINUE),
			cb.isFalse(root.get("voided"))
		);

		return session.createQuery(cq).uniqueResult();
	}
	
	@Override
	public Order getRevisionOrder(Order order) throws APIException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> root = cq.from(Order.class);

		cq.where(
			cb.equal(root.get("previousOrder"), order),
			cb.equal(root.get("action"), Order.Action.REVISE),
			cb.isFalse(root.get("voided"))
		);

		return session.createQuery(cq).uniqueResult();
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
		
		//prevent jpa from flushing before fetching the list
		query.setFlushMode(FlushModeType.COMMIT);
		
		return query.getResultList();
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
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, OrderGroup.class, uuid);
	}
	
	/**
	 * @see OrderDAO#getOrderGroupById(Integer)
	 * @see org.openmrs.api.OrderService#getOrderGroup(Integer)
	 */
	@Override
	public OrderGroup getOrderGroupById(Integer orderGroupId) throws DAOException {
		return sessionFactory.getCurrentSession().get(OrderGroup.class, orderGroupId);
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
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> root = cq.from(Order.class);

		cq.where(cb.equal(root.get("orderNumber"), orderNumber));

		return session.createQuery(cq).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getNextOrderNumberSeedSequenceValue()
	 */
	@Override
	public Long getNextOrderNumberSeedSequenceValue() {
		GlobalProperty globalProperty = sessionFactory.getCurrentSession().get(GlobalProperty.class,
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
	public List<Order> getActiveOrders(Patient patient, List<OrderType> orderTypes, CareSetting careSetting, Date asOfDate) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> root = cq.from(Order.class);

		List<Predicate> predicates = createOrderCriteria(cb, root, patient, careSetting, orderTypes, false, false);

		predicates.add(cb.lessThanOrEqualTo(root.get("dateActivated"), asOfDate));

		Predicate dateStoppedAndAutoExpDateCondition = cb.or(
			cb.and(cb.isNull(root.get("dateStopped")), cb.isNull(root.get("autoExpireDate"))),
			cb.and(cb.isNull(root.get("dateStopped")), cb.greaterThanOrEqualTo(root.get("autoExpireDate"), asOfDate)),
			cb.greaterThanOrEqualTo(root.get("dateStopped"), asOfDate)
		);

		predicates.add(dateStoppedAndAutoExpDateCondition);

		cq.where(predicates.toArray(new Predicate[]{}));

		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * Creates and returns a list of predicates filtering on the specified parameters
	 * 
	 * @param cb
	 * @param root
	 * @param patient
	 * @param careSetting
	 * @param orderTypes
	 * @param includeVoided
	 * @param includeDiscontinuationOrders
	 * @return
	 */
	private List<Predicate> createOrderCriteria(CriteriaBuilder cb, Root<Order> root, Patient patient,
	        CareSetting careSetting, List<OrderType> orderTypes, boolean includeVoided,
	        boolean includeDiscontinuationOrders) {
		List<Predicate> predicates = new ArrayList<>();

		if (patient != null) {
			predicates.add(cb.equal(root.get("patient"), patient));
		}
		if (careSetting != null) {
			predicates.add(cb.equal(root.get("careSetting"), careSetting));
		}
		if (orderTypes != null && !orderTypes.isEmpty()) {
			predicates.add(root.get("orderType").in(orderTypes));
		}
		if (!includeVoided) {
			predicates.add(cb.isFalse(root.get("voided")));
		}
		if (!includeDiscontinuationOrders) {
			predicates.add(cb.notEqual(root.get("action"), Order.Action.DISCONTINUE));
		}

		return predicates;
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getCareSetting(Integer)
	 */
	@Override
	public CareSetting getCareSetting(Integer careSettingId) {
		return sessionFactory.getCurrentSession().get(CareSetting.class, careSettingId);
	}
	
	/**
	 * @see OrderDAO#getCareSettingByUuid(String)
	 */
	@Override
	public CareSetting getCareSettingByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, CareSetting.class, uuid);
	}
	
	/**
	 * @see OrderDAO#getCareSettingByName(String)
	 */
	@Override
	public CareSetting getCareSettingByName(String name) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<CareSetting> cq = cb.createQuery(CareSetting.class);
		Root<CareSetting> root = cq.from(CareSetting.class);

		cq.where(cb.like(cb.lower(root.get("name")), name.toLowerCase()));

		return session.createQuery(cq).uniqueResult();
	}
	
	/**
	 * @see OrderDAO#getCareSettings(boolean)
	 */
	@Override
	public List<CareSetting> getCareSettings(boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<CareSetting> cq = cb.createQuery(CareSetting.class);
		Root<CareSetting> root = cq.from(CareSetting.class);

		if (!includeRetired) {
			cq.where(cb.isFalse(root.get("retired")));
		}

		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see OrderDAO#getOrderTypeByName
	 */
	@Override
	public OrderType getOrderTypeByName(String orderTypeName) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderType> cq = cb.createQuery(OrderType.class);
		Root<OrderType> root = cq.from(OrderType.class);

		cq.where(cb.equal(root.get("name"), orderTypeName));

		return session.createQuery(cq).uniqueResult();
	}
	
	/**
	 * @see OrderDAO#getOrderFrequency
	 */
	@Override
	public OrderFrequency getOrderFrequency(Integer orderFrequencyId) {
		return sessionFactory.getCurrentSession().get(OrderFrequency.class, orderFrequencyId);
	}
	
	/**
	 * @see OrderDAO#getOrderFrequencyByUuid
	 */
	@Override
	public OrderFrequency getOrderFrequencyByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, OrderFrequency.class, uuid);
	}
	
	/**
	 * @see OrderDAO#getOrderFrequencies(boolean)
	 */
	@Override
	public List<OrderFrequency> getOrderFrequencies(boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderFrequency> cq = cb.createQuery(OrderFrequency.class);
		Root<OrderFrequency> root = cq.from(OrderFrequency.class);

		if (!includeRetired) {
			cq.where(cb.isFalse(root.get("retired")));
		}

		return session.createQuery(cq).getResultList();
	}
	
	/**
	 * @see OrderDAO#getOrderFrequencies(String, java.util.Locale, boolean, boolean)
	 */
	@Override
	public List<OrderFrequency> getOrderFrequencies(String searchPhrase, Locale locale, boolean exactLocale,
	        boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderFrequency> cq = cb.createQuery(OrderFrequency.class);
		Root<OrderFrequency> root = cq.from(OrderFrequency.class);

		Join<OrderFrequency, Concept> conceptJoin = root.join("concept");
		Join<Concept, ConceptName> conceptNameJoin = conceptJoin.join("names");

		List<Predicate> predicates = new ArrayList<>();
		
		Predicate searchPhrasePredicate = cb.like(cb.lower(conceptNameJoin.get("name")), MatchMode.ANYWHERE.toLowerCasePattern(searchPhrase));
		predicates.add(searchPhrasePredicate);

		if (locale != null) {
			List<Locale> locales = new ArrayList<>(2);
			locales.add(locale);
			//look in the broader locale too if exactLocale is false e.g en for en_GB
			if (!exactLocale && StringUtils.isNotBlank(locale.getCountry())) {
				locales.add(new Locale(locale.getLanguage()));
			}
			predicates.add(conceptNameJoin.get("locale").in(locales));
		}
		if (!includeRetired) {
			predicates.add(cb.isFalse(root.get("retired")));
		}
		cq.where(predicates.toArray(new Predicate[]{})).distinct(true);

		return session.createQuery(cq).list();
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
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
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
					CriteriaQuery<?> cq = cb.createQuery(entityClass);
					Root<?> root = cq.from(entityClass);
					cq.where(cb.equal(root.get(attribute.getName()), orderFrequency));
					cq.distinct(true);

					Query query = session.createQuery(cq);
					query.setMaxResults(1);
					if (!query.getResultList().isEmpty()) {
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
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderFrequency> cq = cb.createQuery(OrderFrequency.class);
		Root<OrderFrequency> root = cq.from(OrderFrequency.class);

		cq.where(cb.equal(root.get("concept"), concept));

		return session.createQuery(cq).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderType(Integer)
	 */
	@Override
	public OrderType getOrderType(Integer orderTypeId) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderType> cq = cb.createQuery(OrderType.class);
		Root<OrderType> root = cq.from(OrderType.class);

		cq.where(cb.equal(root.get("orderTypeId"), orderTypeId));

		return session.createQuery(cq).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderTypeByUuid(String)
	 */
	@Override
	public OrderType getOrderTypeByUuid(String uuid) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, OrderType.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderTypes(boolean)
	 */
	@Override
	public List<OrderType> getOrderTypes(boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderType> cq = cb.createQuery(OrderType.class);
		Root<OrderType> root = cq.from(OrderType.class);

		if (!includeRetired) {
			cq.where(cb.isFalse(root.get("retired")));
		}

		return session.createQuery(cq).getResultList();
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
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderType> cq = cb.createQuery(OrderType.class);
		Root<OrderType> root = cq.from(OrderType.class);

		List<Predicate> predicates = new ArrayList<>();
		if (!includeRetired) {
			predicates.add(cb.isFalse(root.get("retired")));
		}
		predicates.add(cb.equal(root.get("parent"), orderType));

		cq.where(predicates.toArray(new Predicate[]{}));
		
		return session.createQuery(cq).getResultList();
	}
	
	@Override
	public boolean isOrderTypeInUse(OrderType orderType) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Order> cq = cb.createQuery(Order.class);
		Root<Order> root = cq.from(Order.class);

		cq.where(cb.equal(root.get("orderType"), orderType));

		return !session.createQuery(cq).getResultList().isEmpty();
	}
	
	/**
	 * @see OrderDAO#getOrderGroupsByPatient(Patient)
	 */
	@Override
	public List<OrderGroup> getOrderGroupsByPatient(Patient patient) {
		if (patient == null) {
			throw new APIException("Patient cannot be null");
		}

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderGroup> cq = cb.createQuery(OrderGroup.class);
		Root<OrderGroup> root = cq.from(OrderGroup.class);
		
		cq.where(cb.equal(root.get("patient"), patient));

		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see OrderDAO#getOrderGroupsByEncounter(Encounter)
	 */
	@Override
	public List<OrderGroup> getOrderGroupsByEncounter(Encounter encounter) {
		if (encounter == null) {
			throw new APIException("Encounter cannot be null");
		}

		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderGroup> cq = cb.createQuery(OrderGroup.class);
		Root<OrderGroup> root = cq.from(OrderGroup.class);

		cq.where(cb.equal(root.get("encounter"), encounter));

		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.api.db.OrderDAO#getAllOrderGroupAttributeTypes()
	 */
	@Override
	public List<OrderGroupAttributeType> getAllOrderGroupAttributeTypes() {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderGroupAttributeType> cq = cb.createQuery(OrderGroupAttributeType.class);
		cq.from(OrderGroupAttributeType.class);

		return session.createQuery(cq).getResultList();
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
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, OrderGroupAttributeType.class, uuid);
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
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, OrderGroupAttribute.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.OrderDAO#getOrderGroupAttributeTypeByName(String)
	 */
	@Override
	public OrderGroupAttributeType getOrderGroupAttributeTypeByName(String name) throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderGroupAttributeType> cq = cb.createQuery(OrderGroupAttributeType.class);
		Root<OrderGroupAttributeType> root = cq.from(OrderGroupAttributeType.class);
		
		cq.where(cb.equal(root.get("name"), name));
		
		return session.createQuery(cq).uniqueResult();
	}


	/**
	 * @param uuid The uuid associated with the order attribute to retrieve.
	 * @see org.openmrs.api.db.OrderDAO#getOrderAttributeByUuid(String)
	 */
	@Override
	public OrderAttribute getOrderAttributeByUuid(String uuid) throws DAOException {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, OrderAttribute.class, uuid);
	}

	/**
	 * @see org.openmrs.api.db.OrderDAO#getAllOrderAttributeTypes()
	 */
	@Override
	public List<OrderAttributeType> getAllOrderAttributeTypes() throws DAOException {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderAttributeType> cq = cb.createQuery(OrderAttributeType.class);
		cq.from(OrderAttributeType.class);
		
		return session.createQuery(cq).getResultList();
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
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, OrderAttributeType.class, uuid);
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
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<OrderAttributeType> cq = cb.createQuery(OrderAttributeType.class);
		Root<OrderAttributeType> root = cq.from(OrderAttributeType.class);
		
		cq.where(cb.equal(root.get("name"), name));
		
		return session.createQuery(cq).uniqueResult();
	}
}

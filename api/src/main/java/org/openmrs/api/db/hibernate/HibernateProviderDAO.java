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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ProviderDAO;
import org.openmrs.util.OpenmrsConstants;

/**
 * Hibernate specific Provider related functions. This class should not be used directly. All calls
 * should go through the {@link org.openmrs.api.ProviderService} methods.
 *
 * @since 1.9
 */
public class HibernateProviderDAO implements ProviderDAO {
	
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getAllProviders(boolean)
	 */
	@Override
	public List<Provider> getAllProviders(boolean includeRetired) {
		return getAll(includeRetired, Provider.class);
	}
	
	private Session getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#saveProvider(org.openmrs.Provider)
	 */
	@Override
	public Provider saveProvider(Provider provider) {
		getSession().saveOrUpdate(provider);
		return provider;
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#deleteProvider(org.openmrs.Provider)
	 */
	@Override
	public void deleteProvider(Provider provider) {
		getSession().delete(provider);
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getProvider(java.lang.Integer)
	 */
	@Override
	public Provider getProvider(Integer id) {
		return getSession().get(Provider.class, id);
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getProviderByUuid(java.lang.String)
	 */
	@Override
	public Provider getProviderByUuid(String uuid) {
		return getByUuid(uuid, Provider.class);
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getProvidersByPerson(org.openmrs.Person, boolean)
	 */
	@Override
	public Collection<Provider> getProvidersByPerson(Person person, boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Provider> cq = cb.createQuery(Provider.class);
		Root<Provider> root = cq.from(Provider.class);

		List<Predicate> predicates = new ArrayList<>();
		List<Order> orders = new ArrayList<>();

		if (!includeRetired) {
			predicates.add(cb.isFalse(root.get("retired")));
		} else {
			//push retired Provider to the end of the returned list
			orders.add(cb.asc(root.get("retired")));
		}
		predicates.add(cb.equal(root.get("person"), person));
		
		orders.add(cb.asc(root.get("providerId")));
		
		cq.where(predicates.toArray(new Predicate[]{})).orderBy(orders);
		return session.createQuery(cq).getResultList();
	}

	/**
	 * @see org.openmrs.api.db.ProviderDAO#getProviderAttribute(Integer)
	 */
	@Override
	public ProviderAttribute getProviderAttribute(Integer providerAttributeID) {
		return getSession().get(ProviderAttribute.class, providerAttributeID);
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getProviderAttributeByUuid(String)
	 */
	
	@Override
	public ProviderAttribute getProviderAttributeByUuid(String uuid) {
		return getByUuid(uuid, ProviderAttribute.class);
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getProviders(String, Map, Integer, Integer, boolean)
	 */
	@Override
	public List<Provider> getProviders(String name, Map<ProviderAttributeType, String> serializedAttributeValues,
	        Integer start, Integer length, boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Provider> cq = cb.createQuery(Provider.class);
		Root<Provider> root = cq.from(Provider.class);

		List<Predicate> predicates = prepareProviderCriteria(cb, root, name, includeRetired);
		cq.where(predicates.toArray(new Predicate[]{})).distinct(true);
		
		if (includeRetired) {
			//push retired Provider to the end of the returned list
			cq.orderBy(cb.asc(root.get("retired")));
		}
		
		TypedQuery<Provider> typedQuery = session.createQuery(cq);
		if (start != null) {
			typedQuery.setFirstResult(start);
		}
		if (length != null) {
			typedQuery.setMaxResults(length);
		}
		
		List<Provider> providers = typedQuery.getResultList();
		if (serializedAttributeValues != null) {
			CollectionUtils.filter(providers, new AttributeMatcherPredicate<Provider, ProviderAttributeType>(
			        serializedAttributeValues));
		}
		return providers;
	}
	
	private MatchMode getMatchMode() {
		String matchMode = Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_PROVIDER_SEARCH_MATCH_MODE);
		
		if (MatchMode.START.toString().equalsIgnoreCase(matchMode)) {
			return MatchMode.START;
		}
		if (MatchMode.ANYWHERE.toString().equalsIgnoreCase(matchMode)) {
			return MatchMode.ANYWHERE;
		}
		if (MatchMode.END.toString().equalsIgnoreCase(matchMode)) {
			return MatchMode.END;
		}
		return MatchMode.EXACT;
	}

	/**
	 * Prepares a list of JPA predicates for searching Provider entities based on a specified name
	 * and retirement status.
	 *
	 * @param cb The CriteriaBuilder used for creating predicates.
	 * @param root The root entity (Provider) in the CriteriaQuery.
	 * @param name The provider's name or a part of it to be used in the search. If blank, it defaults to a wildcard search.
	 * @param includeRetired Boolean flag indicating whether to include retired providers in the search.
	 * @return List<Predicate> A list of predicates that can be added to a CriteriaQuery for filtering Provider entities.
	 */
	private List<Predicate> prepareProviderCriteria(CriteriaBuilder cb, Root<Provider> root, String name, boolean includeRetired) {
		if (StringUtils.isBlank(name)) {
			name = "%";
		}

		List<Predicate> predicates = new ArrayList<>();
		if (!includeRetired) {
			predicates.add(cb.isFalse(root.get("retired")));
		}

		Predicate orCondition = cb.or(
			cb.like(cb.lower(root.get("identifier")), getMatchMode().toLowerCasePattern(name)),
			cb.like(cb.lower(root.get("name")), MatchMode.ANYWHERE.toLowerCasePattern(name))
		);

		Join<Provider, Person> personJoin = root.join("person", JoinType.LEFT);
		Join<Person, PersonName> personNameJoin = personJoin.join("names", JoinType.LEFT);

		List<Predicate> splitNamePredicates = new ArrayList<>();
		String[] splitNames = name.split(" ");
		
		for (String splitName : splitNames) {
			splitNamePredicates.add(getNameSearchExpression(splitName, cb, personNameJoin));
		}
		Predicate andCondition = cb.and(splitNamePredicates.toArray(new Predicate[]{}));

		predicates.add(cb.or(orCondition, andCondition));
		
		return predicates;
	}
	
	/**
	 * Creates or that matches the input name with Provider-Person-Names (not voided)
	 *
	 * @param name The name string to be matched against the PersonName fields.
	 * @param cb The CriteriaBuilder used for creating the CriteriaQuery predicates.
	 * @param personNameJoin The join to the PersonName entity, allowing access to its fields.
	 * @return Predicate The compound predicate representing the desired search conditions.
	 */
	private Predicate getNameSearchExpression(String name, CriteriaBuilder cb, Join<Person, PersonName> personNameJoin) {
		MatchMode mode = MatchMode.ANYWHERE;

		Predicate voidedPredicate = cb.isFalse(personNameJoin.get("voided"));

		Predicate givenNamePredicate = cb.like(cb.lower(personNameJoin.get("givenName")), mode.toLowerCasePattern(name));
		Predicate middleNamePredicate = cb.like(cb.lower(personNameJoin.get("middleName")), mode.toLowerCasePattern(name));
		Predicate familyNamePredicate = cb.like(cb.lower(personNameJoin.get("familyName")), mode.toLowerCasePattern(name));
		Predicate familyName2Predicate = cb.like(cb.lower(personNameJoin.get("familyName2")), mode.toLowerCasePattern(name));

		Predicate orPredicate = cb.or(givenNamePredicate, middleNamePredicate, familyNamePredicate, familyName2Predicate);

		return cb.and(voidedPredicate, orPredicate);
	}

	/**
	 * @see org.openmrs.api.db.ProviderDAO#getCountOfProviders(String, boolean)
	 */
	@Override
	public Long getCountOfProviders(String name, boolean includeRetired) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Provider> root = cq.from(Provider.class);

		List<Predicate> predicates = prepareProviderCriteria(cb, root, name, includeRetired);

		cq.select(cb.countDistinct(root)).where(predicates.toArray(new Predicate[]{}));

		return session.createQuery(cq).getSingleResult();
	}


	/* (non-Javadoc)
	 * @see org.openmrs.api.db.ProviderDAO#getAllProviderAttributeTypes(boolean)
	 */
	@Override
	public List<ProviderAttributeType> getAllProviderAttributeTypes(boolean includeRetired) {
		return getAll(includeRetired, ProviderAttributeType.class);
	}

	private <T> List<T> getAll(boolean includeRetired, Class<T> clazz) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> root = cq.from(clazz);

		List<Order> orderList = new ArrayList<>();
		if (!includeRetired) {
			cq.where(cb.isFalse(root.get("retired")));
		} else {
			//push retired Provider to the end of the returned list
			orderList.add(cb.asc(root.get("retired")));
		}
		orderList.add(cb.asc(root.get("name")));
		cq.orderBy(orderList);

		return session.createQuery(cq).getResultList();
	}
	
	private <T> T getByUuid(String uuid, Class<T> clazz) {
		return HibernateUtil.getUniqueEntityByUUID(sessionFactory, clazz, uuid);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.db.ProviderDAO#getProviderAttributeType(java.lang.Integer)
	 */
	@Override
	public ProviderAttributeType getProviderAttributeType(Integer providerAttributeTypeId) {
		return getSession().get(ProviderAttributeType.class, providerAttributeTypeId);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.db.ProviderDAO#getProviderAttributeTypeByUuid(java.lang.String)
	 */
	@Override
	public ProviderAttributeType getProviderAttributeTypeByUuid(String uuid) {
		return getByUuid(uuid, ProviderAttributeType.class);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.db.ProviderDAO#getProviderAttributeTypeByName(java.lang.String)
	 */
	@Override
	public ProviderAttributeType getProviderAttributeTypeByName(String name) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<ProviderAttributeType> cq = cb.createQuery(ProviderAttributeType.class);
		Root<ProviderAttributeType> root = cq.from(ProviderAttributeType.class);

		cq.where(cb.isFalse(root.get("retired")),
				 cb.equal(root.get("name"), name));

		List<ProviderAttributeType> list = session.createQuery(cq).getResultList();

		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.db.ProviderDAO#saveProviderAttributeType(org.openmrs.ProviderAttributeType)
	 */
	@Override
	public ProviderAttributeType saveProviderAttributeType(ProviderAttributeType providerAttributeType) {
		getSession().saveOrUpdate(providerAttributeType);
		return providerAttributeType;
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.db.ProviderDAO#deleteProviderAttributeType(org.openmrs.ProviderAttributeType)
	 */
	@Override
	public void deleteProviderAttributeType(ProviderAttributeType providerAttributeType) {
		getSession().delete(providerAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getProviderByIdentifier(java.lang.String)
	 */
	@Override
	public boolean isProviderIdentifierUnique(Provider provider) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<Provider> root = cq.from(Provider.class);

		List<Predicate> predicates = new ArrayList<>();
		predicates.add(cb.equal(root.get("identifier"), provider.getIdentifier()));
		if (provider.getProviderId() != null) {
			predicates.add(cb.notEqual(root.get("providerId"), provider.getProviderId()));
		}

		cq.select(cb.countDistinct(root.get("providerId")))
			.where(predicates.toArray(new Predicate[]{}));

		return session.createQuery(cq).uniqueResult() == 0L;
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getProviderByIdentifier(java.lang.String)
	 */
	@Override
	public Provider getProviderByIdentifier(String identifier) {
		Session session = sessionFactory.getCurrentSession();
		CriteriaBuilder cb = session.getCriteriaBuilder();
		CriteriaQuery<Provider> cq = cb.createQuery(Provider.class);
		Root<Provider> root = cq.from(Provider.class);
		
		cq.where(cb.equal(cb.lower(root.get("identifier")), MatchMode.EXACT.toLowerCasePattern(identifier)));

		return session.createQuery(cq).uniqueResult();
	}
}

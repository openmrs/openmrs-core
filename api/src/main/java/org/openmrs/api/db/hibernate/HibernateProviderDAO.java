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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.IlikeExpression;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.ProviderDAO;

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
	 * @see org.openmrs.api.db.ProviderDAO#getAllProviders()
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
		return (Provider) getSession().load(Provider.class, id);
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getProviderByUuid(java.lang.String)
	 */
	@Override
	public Provider getProviderByUuid(String uuid) {
		return getByUuid(uuid, Provider.class);
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getProvidersByPerson(org.openmrs.Person)
	 */
	@Override
	public Collection<Provider> getProvidersByPerson(Person person) {
		Criteria criteria = getSession().createCriteria(Provider.class);
		criteria.add(Restrictions.eq("person", person));
		criteria.addOrder(Order.asc("providerId"));
		@SuppressWarnings("unchecked")
		List<Provider> list = criteria.list();
		return list;
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getProviderAttribute(Integer)
	 */
	@Override
	public ProviderAttribute getProviderAttribute(Integer providerAttributeID) {
		return (ProviderAttribute) getSession().load(ProviderAttribute.class, providerAttributeID);
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getProviderAttributeByUuid(String)
	 */
	
	@Override
	public ProviderAttribute getProviderAttributeByUuid(String uuid) {
		return getByUuid(uuid, ProviderAttribute.class);
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getProviders(java.lang.String, java.util.Map,
	 *      java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<Provider> getProviders(String name, Map<ProviderAttributeType, String> serializedAttributeValues,
	        Integer start, Integer length) {
		Criteria criteria = prepareProviderCriteria(name);
		if (start != null)
			criteria.setFirstResult(start);
		if (length != null)
			criteria.setMaxResults(length);
		List<Provider> providers = criteria.list();
		if (serializedAttributeValues != null) {
			CollectionUtils.filter(providers, new AttributeMatcherPredicate<Provider, ProviderAttributeType>(
			        serializedAttributeValues));
		}
		return providers;
	}
	
	/**
	 * Creates a Provider Criteria based on name
	 * 
	 * @param name represents provider name
	 * @return Criteria represents the hibernate criteria to search
	 */
	private Criteria prepareProviderCriteria(String name) {
		if (StringUtils.isBlank(name)) {
			name = "%";
		}
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Provider.class).createAlias("person", "p",
		    Criteria.LEFT_JOIN);
		
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		if (name != null) {
			criteria.createAlias("p.names", "personName", Criteria.LEFT_JOIN);
			criteria.add(getNameSearchExpression(name));
		}
		return criteria;
	}
	
	/**
	 * Creates Logical expression that matches the input name with Provider -Name or
	 * Provider-Person-Names(that are not voided)
	 * 
	 * @param name
	 * @return LogicalExpression
	 */
	private LogicalExpression getNameSearchExpression(String name) {
		
		MatchMode mode = MatchMode.ANYWHERE;
		IlikeExpression providerNameExpression = (IlikeExpression) Expression.ilike("name", name, mode);
		IlikeExpression givenName = (IlikeExpression) Expression.ilike("personName.givenName", name, mode);
		IlikeExpression middleName = (IlikeExpression) Expression.ilike("personName.middleName", name, mode);
		IlikeExpression familyName = (IlikeExpression) Expression.ilike("personName.familyName", name, mode);
		IlikeExpression familyName2 = (IlikeExpression) Expression.ilike("personName.familyName2", name, mode);
		LogicalExpression personNameExpression = Expression.and(Expression.eq("personName.voided", false), Expression.or(
		    familyName2, Expression.or(familyName, Expression.or(middleName, givenName))));
		return Expression.or(providerNameExpression, personNameExpression);
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getCountOfProviders(java.lang.String)
	 */
	@Override
	public Long getCountOfProviders(String name) {
		Criteria criteria = prepareProviderCriteria(name);
		criteria.setProjection(Projections.countDistinct("providerId"));
		return (Long) criteria.uniqueResult();
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.db.ProviderDAO#getAllProviderAttributeTypes(boolean)
	 */
	@Override
	public List<ProviderAttributeType> getAllProviderAttributeTypes(boolean includeRetired) {
		return getAll(includeRetired, ProviderAttributeType.class);
	}
	
	private <T> List<T> getAll(boolean includeRetired, Class<T> clazz) {
		Criteria criteria = getSession().createCriteria(clazz);
		if (!includeRetired) {
			criteria.add(Expression.eq("retired", false));
		} else {
			//push retired Provider to the end of the returned list
			criteria.addOrder(Order.asc("retired"));
		}
		criteria.addOrder(Order.asc("name"));
		return criteria.list();
	}
	
	private <T> T getByUuid(String uuid, Class<T> clazz) {
		Criteria criteria = getSession().createCriteria(clazz);
		criteria.add(Restrictions.eq("uuid", uuid));
		return (T) criteria.uniqueResult();
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.db.ProviderDAO#getProviderAttributeType(java.lang.Integer)
	 */
	@Override
	public ProviderAttributeType getProviderAttributeType(Integer providerAttributeTypeId) {
		return (ProviderAttributeType) getSession().load(ProviderAttributeType.class, providerAttributeTypeId);
	}
	
	/* (non-Javadoc)
	 * @see org.openmrs.api.db.ProviderDAO#getProviderAttributeTypeByUuid(java.lang.String)
	 */
	@Override
	public ProviderAttributeType getProviderAttributeTypeByUuid(String uuid) {
		return getByUuid(uuid, ProviderAttributeType.class);
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
	public boolean isProviderIdentifierUnique(Provider provider) throws DAOException {
		
		Criteria criteria = getSession().createCriteria(Provider.class);
		criteria.add(Restrictions.eq("identifier", provider.getIdentifier()));
		if (provider.getProviderId() != null)
			criteria.add(Restrictions.not(Restrictions.eq("providerId", provider.getProviderId())));
		criteria.setProjection(Projections.countDistinct("providerId"));
		
		return (Long) criteria.uniqueResult() == 0L;
	}
	
}

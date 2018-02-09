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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
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
		return (Provider) getSession().get(Provider.class, id);
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
		Criteria criteria = getSession().createCriteria(Provider.class);
		if (!includeRetired) {
			criteria.add(Restrictions.eq("retired", false));
		} else {
			//push retired Provider to the end of the returned list
			criteria.addOrder(Order.asc("retired"));
		}
		criteria.add(Restrictions.eq("person", person));
		
		criteria.addOrder(Order.asc("providerId"));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getProviderAttribute(Integer)
	 */
	@Override
	public ProviderAttribute getProviderAttribute(Integer providerAttributeID) {
		return (ProviderAttribute) getSession().get(ProviderAttribute.class, providerAttributeID);
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
		Criteria criteria = prepareProviderCriteria(name, includeRetired);
		if (start != null) {
			criteria.setFirstResult(start);
		}
		if (length != null) {
			criteria.setMaxResults(length);
		}
		
		if (includeRetired) {
			//push retired Provider to the end of the returned list
			criteria.addOrder(Order.asc("retired"));
		}
		
		List<Provider> providers = criteria.list();
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
	 * Creates a Provider Criteria based on name
	 *
	 * @param name represents provider name
	 * @param includeRetired
	 * @return Criteria represents the hibernate criteria to search
	 */
	private Criteria prepareProviderCriteria(String name, boolean includeRetired) {
		if (StringUtils.isBlank(name)) {
			name = "%";
		}
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Provider.class).createAlias("person", "p",
		    JoinType.LEFT_OUTER_JOIN);
		
		if (!includeRetired) {
			criteria.add(Restrictions.eq("retired", false));
		}
		
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		criteria.createAlias("p.names", "personName", JoinType.LEFT_OUTER_JOIN);
		
		Disjunction or = Restrictions.disjunction();
		or.add(Restrictions.ilike("identifier", name, getMatchMode()));
		or.add(Restrictions.ilike("name", name, MatchMode.ANYWHERE));
		
		Conjunction and = Restrictions.conjunction();
		or.add(and);
		
		String[] splitNames = name.split(" ");
		for (String splitName : splitNames) {
			and.add(getNameSearchExpression(splitName));
		}
		
		criteria.add(or);
		
		return criteria;
	}
	
	/**
	 * Creates or that matches the input name with Provider-Person-Names (not voided)
	 *
	 * @param name
	 * @return Junction
	 */
	private Junction getNameSearchExpression(String name) {
		MatchMode mode = MatchMode.ANYWHERE;
		
		Conjunction and = Restrictions.conjunction();
		and.add(Restrictions.eq("personName.voided", false));
		
		Disjunction or = Restrictions.disjunction();
		and.add(or);
		
		or.add(Restrictions.ilike("personName.givenName", name, mode));
		or.add(Restrictions.ilike("personName.middleName", name, mode));
		or.add(Restrictions.ilike("personName.familyName", name, mode));
		or.add(Restrictions.ilike("personName.familyName2", name, mode));
		
		return and;
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getCountOfProviders(String, boolean)
	 */
	@Override
	public Long getCountOfProviders(String name, boolean includeRetired) {
	  Criteria criteria = prepareProviderCriteria(name, includeRetired);
	  return (long) criteria.list().size();
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
			criteria.add(Restrictions.eq("retired", false));
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
		return (ProviderAttributeType) getSession().get(ProviderAttributeType.class, providerAttributeTypeId);
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
		if (provider.getProviderId() != null) {
			criteria.add(Restrictions.not(Restrictions.eq("providerId", provider.getProviderId())));
		}
		criteria.setProjection(Projections.countDistinct("providerId"));
		
		return (Long) criteria.uniqueResult() == 0L;
	}
	
	/**
	 * @see org.openmrs.api.db.ProviderDAO#getProviderByIdentifier(java.lang.String)
	 */
	@Override
	public Provider getProviderByIdentifier(String identifier) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Provider.class);
		criteria.add(Restrictions.ilike("identifier", identifier, MatchMode.EXACT));
		return (Provider) criteria.uniqueResult();
	}
}

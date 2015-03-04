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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.VisitDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Hibernate specific visit related functions This class should not be used directly. All calls
 * should go through the {@link org.openmrs.api.VisitService} methods.
 *
 * @since 1.9
 */
public class HibernateVisitDAO implements VisitDAO {
	
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	private Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getAllVisitTypes()
	 */
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<VisitType> getAllVisitTypes() throws APIException {
		return getCurrentSession().createCriteria(VisitType.class).list();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getAllVisitTypes(boolean)
	 */
	@Override
	public List<VisitType> getAllVisitTypes(boolean includeRetired) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(VisitType.class);
		return includeRetired ? criteria.list() : criteria.add(Restrictions.eq("retired", includeRetired)).list();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitType(java.lang.Integer)
	 */
	@Transactional(readOnly = true)
	public VisitType getVisitType(Integer visitTypeId) {
		return (VisitType) sessionFactory.getCurrentSession().get(VisitType.class, visitTypeId);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitTypeByUuid(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public VisitType getVisitTypeByUuid(String uuid) {
		return (VisitType) sessionFactory.getCurrentSession().createQuery("from VisitType vt where vt.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitTypes(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	public List<VisitType> getVisitTypes(String fuzzySearchPhrase) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(VisitType.class);
		criteria.add(Restrictions.ilike("name", fuzzySearchPhrase, MatchMode.ANYWHERE));
		criteria.addOrder(Order.asc("name"));
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#saveVisitType(org.openmrs.VisitType)
	 */
	@Transactional
	public VisitType saveVisitType(VisitType visitType) {
		sessionFactory.getCurrentSession().saveOrUpdate(visitType);
		return visitType;
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#purgeVisitType(org.openmrs.VisitType)
	 */
	@Transactional
	public void purgeVisitType(VisitType visitType) {
		sessionFactory.getCurrentSession().delete(visitType);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisit(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public Visit getVisit(Integer visitId) throws DAOException {
		return (Visit) getCurrentSession().get(Visit.class, visitId);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public Visit getVisitByUuid(String uuid) throws DAOException {
		return (Visit) getCurrentSession().createQuery("from Visit v where v.uuid = :uuid").setString("uuid", uuid)
		        .uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#saveVisit(org.openmrs.Visit)
	 */
	@Override
	@Transactional
	public Visit saveVisit(Visit visit) throws DAOException {
		getCurrentSession().saveOrUpdate(visit);
		return visit;
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#deleteVisit(org.openmrs.Visit)
	 */
	@Override
	@Transactional
	public void deleteVisit(Visit visit) throws DAOException {
		getCurrentSession().delete(visit);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisits(java.util.Collection, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, java.util.Date, java.util.Date,
	 *      java.util.Date, java.util.Date, java.util.Map, boolean, boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<Visit> getVisits(Collection<VisitType> visitTypes, Collection<Patient> patients,
	        Collection<Location> locations, Collection<Concept> indications, Date minStartDatetime, Date maxStartDatetime,
	        Date minEndDatetime, Date maxEndDatetime, final Map<VisitAttributeType, String> serializedAttributeValues,
	        boolean includeInactive, boolean includeVoided) throws DAOException {
		
		Criteria criteria = getCurrentSession().createCriteria(Visit.class);
		
		if (visitTypes != null) {
			criteria.add(Restrictions.in("visitType", visitTypes));
		}
		if (patients != null) {
			criteria.add(Restrictions.in("patient", patients));
		}
		if (locations != null) {
			criteria.add(Restrictions.in("location", locations));
		}
		if (indications != null) {
			criteria.add(Restrictions.in("indication", indications));
		}
		
		if (minStartDatetime != null) {
			criteria.add(Restrictions.ge("startDatetime", minStartDatetime));
		}
		if (maxStartDatetime != null) {
			criteria.add(Restrictions.le("startDatetime", maxStartDatetime));
		}
		
		// active visits have null end date, so it doesn't make sense to search against it if include inactive is set to false
		if (!includeInactive) {
			// the user only asked for currently active visits, so stop time needs to be null or after right now
			criteria.add(Restrictions.or(Restrictions.isNull("stopDatetime"), Restrictions.gt("stopDatetime", new Date())));
		} else {
			if (minEndDatetime != null) {
				criteria.add(Restrictions.or(Restrictions.isNull("stopDatetime"), Restrictions.ge("stopDatetime",
				    minEndDatetime)));
			}
			if (maxEndDatetime != null) {
				criteria.add(Restrictions.le("stopDatetime", maxEndDatetime));
			}
		}
		
		if (!includeVoided) {
			criteria.add(Restrictions.eq("voided", false));
		}
		
		criteria.addOrder(Order.desc("startDatetime"));
		criteria.addOrder(Order.desc("visitId"));
		
		List<Visit> visits = criteria.list();
		
		if (serializedAttributeValues != null) {
			CollectionUtils.filter(visits, new AttributeMatcherPredicate<Visit, VisitAttributeType>(
			        serializedAttributeValues));
		}
		
		return visits;
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getAllVisitAttributeTypes()
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<VisitAttributeType> getAllVisitAttributeTypes() {
		return getCurrentSession().createCriteria(VisitAttributeType.class).list();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitAttributeType(java.lang.Integer)
	 */
	@Override
	@Transactional(readOnly = true)
	public VisitAttributeType getVisitAttributeType(Integer id) {
		return (VisitAttributeType) getCurrentSession().get(VisitAttributeType.class, id);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitAttributeTypeByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public VisitAttributeType getVisitAttributeTypeByUuid(String uuid) {
		return (VisitAttributeType) getCurrentSession().createCriteria(VisitAttributeType.class).add(
		    Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#saveVisitAttributeType(org.openmrs.VisitAttributeType)
	 */
	@Override
	@Transactional
	public VisitAttributeType saveVisitAttributeType(VisitAttributeType visitAttributeType) {
		getCurrentSession().saveOrUpdate(visitAttributeType);
		return visitAttributeType;
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#deleteVisitAttributeType(org.openmrs.VisitAttributeType)
	 */
	@Override
	@Transactional
	public void deleteVisitAttributeType(VisitAttributeType visitAttributeType) {
		getCurrentSession().delete(visitAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitAttributeByUuid(java.lang.String)
	 */
	@Override
	@Transactional(readOnly = true)
	public VisitAttribute getVisitAttributeByUuid(String uuid) {
		return (VisitAttribute) getCurrentSession().createCriteria(VisitAttribute.class).add(Restrictions.eq("uuid", uuid))
		        .uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getNextVisit(Visit, Collection, Date)
	 */
	@Override
	public Visit getNextVisit(Visit previousVisit, Collection<VisitType> visitTypes, Date maximumStartDate) {
		Criteria criteria = getCurrentSession().createCriteria(Visit.class);
		criteria.add(Restrictions.eq("voided", false)).add(
		    Restrictions.gt("visitId", (previousVisit != null) ? previousVisit.getVisitId() : 0)).addOrder(
		    Order.asc("visitId")).add(Restrictions.isNull("stopDatetime")).setMaxResults(1);
		if (maximumStartDate != null) {
			criteria.add(Restrictions.le("startDatetime", maximumStartDate));
		}
		
		if (CollectionUtils.isNotEmpty(visitTypes)) {
			criteria.add(Restrictions.in("visitType", visitTypes));
		}
		
		return (Visit) criteria.uniqueResult();
	}
}

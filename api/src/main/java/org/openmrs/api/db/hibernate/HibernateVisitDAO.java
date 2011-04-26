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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
	public List<VisitType> getAllVisitTypes() throws APIException {
		return getCurrentSession().createCriteria(VisitType.class).list();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitType(java.lang.Integer)
	 */
	public VisitType getVisitType(Integer visitTypeId) {
		return (VisitType) sessionFactory.getCurrentSession().get(VisitType.class, visitTypeId);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitTypeByUuid(java.lang.String)
	 */
	public VisitType getVisitTypeByUuid(String uuid) {
		return (VisitType) sessionFactory.getCurrentSession().createQuery("from VisitType vt where vt.uuid = :uuid")
		        .setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitTypes(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<VisitType> getVisitTypes(String fuzzySearchPhrase) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(VisitType.class);
		criteria.add(Restrictions.ilike("name", fuzzySearchPhrase, MatchMode.ANYWHERE));
		criteria.addOrder(Order.asc("name"));
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#saveVisitType(org.openmrs.VisitType)
	 */
	public VisitType saveVisitType(VisitType visitType) {
		sessionFactory.getCurrentSession().saveOrUpdate(visitType);
		return visitType;
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#purgeVisitType(org.openmrs.VisitType)
	 */
	public void purgeVisitType(VisitType visitType) {
		sessionFactory.getCurrentSession().delete(visitType);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisit(java.lang.Integer)
	 */
	@Override
	public Visit getVisit(Integer visitId) throws DAOException {
		return (Visit) getCurrentSession().get(Visit.class, visitId);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitByUuid(java.lang.String)
	 */
	@Override
	public Visit getVisitByUuid(String uuid) throws DAOException {
		return (Visit) getCurrentSession().createQuery("from Visit v where v.uuid = :uuid").setString("uuid", uuid)
		        .uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#saveVisit(org.openmrs.Visit)
	 */
	@Override
	public Visit saveVisit(Visit visit) throws DAOException {
		getCurrentSession().saveOrUpdate(visit);
		return visit;
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#deleteVisit(org.openmrs.Visit)
	 */
	@Override
	public void deleteVisit(Visit visit) throws DAOException {
		getCurrentSession().delete(visit);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisits(java.util.Collection, java.util.Collection,
	 *      java.util.Collection, java.util.Collection, java.util.Date, java.util.Date,
	 *      java.util.Date, java.util.Date, boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Visit> getVisits(Collection<VisitType> visitTypes, Collection<Patient> patients,
	        Collection<Location> locations, Collection<Concept> indications, Date minStartDatetime, Date maxStartDatetime,
	        Date minEndDatetime, Date maxEndDatetime, Map<VisitAttributeType, String> serializedAttributeValues,
	        boolean includeInactive, boolean includeVoided) throws DAOException {
		
		Criteria criteria = getCurrentSession().createCriteria(Visit.class);
		
		if (visitTypes != null)
			criteria.add(Restrictions.in("visitType", visitTypes));
		if (patients != null)
			criteria.add(Restrictions.in("patient", patients));
		if (locations != null)
			criteria.add(Restrictions.in("location", locations));
		if (indications != null)
			criteria.add(Restrictions.in("indication", indications));
		
		if (minStartDatetime != null)
			criteria.add(Restrictions.ge("startDatetime", minStartDatetime));
		if (maxStartDatetime != null)
			criteria.add(Restrictions.le("startDatetime", maxStartDatetime));
		
		//active visits have null end date, so it doesn't make sense to search against it if include inactive it set to false
		if (!includeInactive)
			criteria.add(Restrictions.isNull("stopDatetime"));
		else {
			if (minEndDatetime != null)
				criteria.add(Restrictions.ge("stopDatetime", minEndDatetime));
			if (maxEndDatetime != null)
				criteria.add(Restrictions.le("stopDatetime", maxEndDatetime));
		}
		
		if (!includeVoided)
			criteria.add(Restrictions.eq("voided", false));
		
		if (serializedAttributeValues == null)
			return criteria.list();
		
		List<Visit> ret = new ArrayList<Visit>();
		for (Visit visit : (List<Visit>) criteria.list()) {
			boolean allMatch = true;
			for (Map.Entry<VisitAttributeType, String> e : serializedAttributeValues.entrySet()) {
				boolean match = false;
				for (VisitAttribute attr : visit.getActiveAttributes(e.getKey())) {
					if (attr.getSerializedValue().equals(e.getValue())) {
						match = true;
						break;
					}
				}
				if (!match) {
					allMatch = false;
					break;
				}
			}
			if (allMatch)
				ret.add(visit);
		}
		return ret;
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getAllVisitAttributeTypes()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<VisitAttributeType> getAllVisitAttributeTypes() {
		return getCurrentSession().createCriteria(VisitAttributeType.class).list();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitAttributeType(java.lang.Integer)
	 */
	@Override
	public VisitAttributeType getVisitAttributeType(Integer id) {
		return (VisitAttributeType) getCurrentSession().get(VisitAttributeType.class, id);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitAttributeTypeByUuid(java.lang.String)
	 */
	@Override
	public VisitAttributeType getVisitAttributeTypeByUuid(String uuid) {
		return (VisitAttributeType) getCurrentSession().createCriteria(VisitAttributeType.class).add(
		    Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#saveVisitAttributeType(org.openmrs.VisitAttributeType)
	 */
	@Override
	public VisitAttributeType saveVisitAttributeType(VisitAttributeType visitAttributeType) {
		getCurrentSession().saveOrUpdate(visitAttributeType);
		return visitAttributeType;
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#deleteVisitAttributeType(org.openmrs.VisitAttributeType)
	 */
	@Override
	public void deleteVisitAttributeType(VisitAttributeType visitAttributeType) {
		getCurrentSession().delete(visitAttributeType);
	}
	
	/**
	 * @see org.openmrs.api.db.VisitDAO#getVisitAttributeByUuid(java.lang.String)
	 */
	@Override
	public VisitAttribute getVisitAttributeByUuid(String uuid) {
		return (VisitAttribute) getCurrentSession().createCriteria(VisitAttribute.class).add(Restrictions.eq("uuid", uuid))
		        .uniqueResult();
	}
	
}

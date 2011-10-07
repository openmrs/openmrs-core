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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.EncounterDAO;

/**
 * Hibernate specific dao for the {@link EncounterService} All calls should be made on the
 * Context.getEncounterService() object
 * 
 * @see EncounterDAO
 * @see EncounterService
 */
public class HibernateEncounterDAO implements EncounterDAO {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#saveEncounter(org.openmrs.Encounter)
	 */
	public Encounter saveEncounter(Encounter encounter) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(encounter);
		return encounter;
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#deleteEncounter(org.openmrs.Encounter)
	 */
	public void deleteEncounter(Encounter encounter) throws DAOException {
		sessionFactory.getCurrentSession().delete(encounter);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounter(java.lang.Integer)
	 */
	public Encounter getEncounter(Integer encounterId) throws DAOException {
		return (Encounter) sessionFactory.getCurrentSession().get(Encounter.class, encounterId);
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncountersByPatientId(java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<Encounter> getEncountersByPatientId(Integer patientId) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Encounter.class).createAlias("patient", "p")
		        .add(Expression.eq("p.patientId", patientId)).add(Expression.eq("voided", false))
		        .addOrder(Order.desc("encounterDatetime"));
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounters(org.openmrs.Patient, org.openmrs.Location,
	 *      java.util.Date, java.util.Date, java.util.Collection, java.util.Collection,
	 *      java.util.Collection, boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Encounter> getEncounters(Patient patient, Location location, Date fromDate, Date toDate,
	                                     Collection<Form> enteredViaForms, Collection<EncounterType> encounterTypes,
	                                     Collection<Provider> providers, Collection<VisitType> visitTypes,
	                                     Collection<Visit> visits, boolean includeVoided) {
		
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Encounter.class);
		
		if (patient != null && patient.getPatientId() != null) {
			crit.add(Expression.eq("patient", patient));
		}
		if (location != null && location.getLocationId() != null) {
			crit.add(Expression.eq("location", location));
		}
		if (fromDate != null) {
			crit.add(Expression.ge("encounterDatetime", fromDate));
		}
		if (toDate != null) {
			crit.add(Expression.le("encounterDatetime", toDate));
		}
		if (enteredViaForms != null && enteredViaForms.size() > 0) {
			crit.add(Expression.in("form", enteredViaForms));
		}
		if (encounterTypes != null && encounterTypes.size() > 0) {
			crit.add(Expression.in("encounterType", encounterTypes));
		}
		if (providers != null && providers.size() > 0) {
			crit.createAlias("encounterProviders", "ep");
			crit.add(Expression.in("ep.provider", providers));
		}
		if (visitTypes != null && visitTypes.size() > 0) {
			crit.createAlias("visit", "v");
			crit.add(Expression.in("v.visitType", visitTypes));
		}
		if (visits != null && visits.size() > 0) {
			crit.add(Expression.in("visit", visits));
		}
		if (!includeVoided) {
			crit.add(Expression.eq("voided", false));
		}
		crit.addOrder(Order.asc("encounterDatetime"));
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#saveEncounterType(org.openmrs.EncounterType)
	 */
	public EncounterType saveEncounterType(EncounterType encounterType) {
		sessionFactory.getCurrentSession().saveOrUpdate(encounterType);
		return encounterType;
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#deleteEncounterType(org.openmrs.EncounterType)
	 */
	public void deleteEncounterType(EncounterType encounterType) throws DAOException {
		sessionFactory.getCurrentSession().delete(encounterType);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterType(java.lang.Integer)
	 */
	public EncounterType getEncounterType(Integer encounterTypeId) throws DAOException {
		return (EncounterType) sessionFactory.getCurrentSession().get(EncounterType.class, encounterTypeId);
	}
	
	/**
	 * @see org.openmrs.api.EncounterService#getEncounterType(java.lang.String)
	 */
	public EncounterType getEncounterType(String name) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(EncounterType.class);
		crit.add(Expression.eq("retired", false));
		crit.add(Expression.eq("name", name));
		EncounterType encounterType = (EncounterType) crit.uniqueResult();
		
		return encounterType;
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getAllEncounterTypes(java.lang.Boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<EncounterType> getAllEncounterTypes(Boolean includeRetired) throws DAOException {
		
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(EncounterType.class);
		
		criteria.addOrder(Order.asc("name"));
		
		if (includeRetired == false)
			criteria.add(Expression.eq("retired", false));
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#findEncounterTypes(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<EncounterType> findEncounterTypes(String name) throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(EncounterType.class)
		        // 'ilike' case insensitive search
		        .add(Expression.ilike("name", name, MatchMode.START)).addOrder(Order.asc("name"))
		        .addOrder(Order.asc("retired")).list();
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getSavedEncounterDatetime(org.openmrs.Encounter)
	 */
	public Date getSavedEncounterDatetime(Encounter encounter) {
		SQLQuery sql = sessionFactory.getCurrentSession().createSQLQuery(
		    "select encounter_datetime from encounter where encounter_id = :encounterId");
		sql.setInteger("encounterId", encounter.getEncounterId());
		return (Date) sql.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounterByUuid(java.lang.String)
	 */
	public Encounter getEncounterByUuid(String uuid) {
		return getClassByUuid(Encounter.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounterTypeByUuid(java.lang.String)
	 */
	public EncounterType getEncounterTypeByUuid(String uuid) {
		return getClassByUuid(EncounterType.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounters(String, Integer, Integer, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Encounter> getEncounters(String query, Integer start, Integer length, boolean includeVoided) {
		if (StringUtils.isBlank(query)) {
			return Collections.emptyList();
		}
		
		Criteria criteria = createEncounterByQueryCriteria(query, includeVoided);
		
		if (start != null)
			criteria.setFirstResult(start);
		if (length != null && length > 0)
			criteria.setMaxResults(length);
		
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getSavedEncounterLocation(org.openmrs.Encounter)
	 */
	public Location getSavedEncounterLocation(Encounter encounter) {
		SQLQuery sql = sessionFactory.getCurrentSession().createSQLQuery(
		    "select location_id from encounter where encounter_id = :encounterId");
		sql.setInteger("encounterId", encounter.getEncounterId());
		return Context.getLocationService().getLocation((Integer) sql.uniqueResult());
	}
	
	/**
	 * @see EncounterDAO#getAllEncounters(org.openmrs.Cohort)
	 */
	@Override
	public Map<Integer, List<Encounter>> getAllEncounters(Cohort patients) {
		HashMap<Integer, List<Encounter>> encountersBypatient = new HashMap<Integer, List<Encounter>>();
		
		@SuppressWarnings("unchecked")
		List<Encounter> allEncounters = createEncounterCriteria(patients).list();
		
		// set up the return map
		for (Encounter encounter : allEncounters) {
			Integer patientId = encounter.getPatientId();
			List<Encounter> encounters = encountersBypatient.get(patientId);
			
			if (encounters == null) {
				encounters = new ArrayList<Encounter>();
			}
			
			encounters.add(encounter);
			if (!encountersBypatient.containsKey(patientId)) {
				encountersBypatient.put(patientId, encounters);
			}
		}
		return encountersBypatient;
	}
	
	/**
	 * Create the criteria for fetching all encounters based on cohort
	 * 
	 * @param patients
	 * @return a map of patient with their encounters
	 */
	private Criteria createEncounterCriteria(Cohort patients) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Encounter.class);
		criteria.setCacheMode(org.hibernate.CacheMode.IGNORE);
		
		// only include this where clause if patients were passed in
		if (patients != null)
			criteria.add(Restrictions.in("patient.personId", patients.getMemberIds()));
		
		criteria.add(Restrictions.eq("voided", false));
		
		criteria.addOrder(org.hibernate.criterion.Order.desc("patient.personId"));
		criteria.addOrder(org.hibernate.criterion.Order.desc("encounterDatetime"));
		return criteria;
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getCountOfEncounters(java.lang.String, boolean)
	 */
	@Override
	public Integer getCountOfEncounters(String query, boolean includeVoided) {
		Criteria criteria = createEncounterByQueryCriteria(query, includeVoided);
		
		criteria.setProjection(Projections.countDistinct("enc.encounterId"));
		return (Integer) criteria.uniqueResult();
	}
	
	/**
	 * Utility method that returns a criteria for searching for patient encounters that match the
	 * specified search phrase
	 * 
	 * @param query patient name or identifier
	 * @param includeVoided Specifies whether voided encounters should be included
	 * @return Criteria
	 */
	private Criteria createEncounterByQueryCriteria(String query, boolean includeVoided) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Encounter.class, "enc");
		if (!includeVoided)
			criteria.add(Restrictions.eq("voided", false));
		
		criteria = criteria.createCriteria("patient", "pat");
		String name = null;
		String identifier = null;
		if (query.matches(".*\\d+.*")) {
			identifier = query;
		} else {
			// there is no number in the string, search on name
			name = query;
		}
		criteria = new PatientSearchCriteria(sessionFactory, criteria).prepareCriteria(name, identifier,
		    new ArrayList<PatientIdentifierType>(), false, true);
		return criteria;
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncountersByVisit(Visit)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Encounter> getEncountersByVisit(Visit visit, boolean includeVoided) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Encounter.class)
		        .add(Expression.eq("visit", visit));
		if (!includeVoided) {
			crit.add(Expression.eq("voided", false));
		}
		crit.addOrder(Order.asc("encounterDatetime"));
		
		return crit.list();
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#saveEncounterRole(EncounterRole encounterRole)
	 */
	@Override
	public EncounterRole saveEncounterRole(EncounterRole encounterRole) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(encounterRole);
		return encounterRole;
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#deleteEncounterRole(org.openmrs.EncounterRole)
	 */
	@Override
	public void deleteEncounterRole(EncounterRole encounterRole) throws DAOException {
		sessionFactory.getCurrentSession().delete(encounterRole);
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounterRole(Integer)
	 */
	@Override
	public EncounterRole getEncounterRole(Integer encounterRoleId) throws DAOException {
		return (EncounterRole) sessionFactory.getCurrentSession().get(EncounterRole.class, encounterRoleId);
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncounterRoleByUuid(String)
	 */
	@Override
	public EncounterRole getEncounterRoleByUuid(String uuid) {
		return getClassByUuid(EncounterRole.class, uuid);
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getAllEncounterRoles(boolean)
	 */
	@Override
	public List<EncounterRole> getAllEncounterRoles(boolean includeRetired) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(EncounterRole.class);
		return includeRetired ? criteria.list() : criteria.add(Restrictions.eq("retired", includeRetired)).list();
	}
	
	/**
	 * Convenience method since this DAO fetches several different domain objects by uuid
	 * 
	 * @param uuid uuid to fetch
	 * @param table a simple classname (e.g. "Encounter")
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T> T getClassByUuid(Class<T> clazz, String uuid) {
		return (T) sessionFactory.getCurrentSession().createCriteria(clazz).add(Restrictions.eq("uuid", uuid))
		        .uniqueResult();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Encounter> getEncountersNotAssignedToAnyVisit(Patient patient) throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(Encounter.class).add(Restrictions.eq("patient", patient))
		        .add(Restrictions.isNull("visit")).add(Restrictions.eq("voided", false))
		        .addOrder(Order.desc("encounterDatetime")).setMaxResults(100).list();
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncountersByVisitsAndPatient(org.openmrs.Patient,
	 *      boolean, java.lang.String, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<Encounter> getEncountersByVisitsAndPatient(Patient patient, boolean includeVoided, String query,
	                                                       Integer start, Integer length) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Encounter.class);
		addEncountersByPatientCriteria(criteria, patient, includeVoided, query);
		
		@SuppressWarnings("unchecked")
		List<Encounter> encounters = criteria.list();
		
		criteria = sessionFactory.getCurrentSession().createCriteria(Visit.class);
		addEmptyVisitsByPatientCriteria(criteria, patient, includeVoided, query);
		
		@SuppressWarnings("unchecked")
		List<Visit> emptyVisits = criteria.list();
		
		if (!emptyVisits.isEmpty()) {
			for (Visit emptyVisit : emptyVisits) {
				Encounter mockEncounter = new Encounter();
				mockEncounter.setVisit(emptyVisit);
				encounters.add(mockEncounter);
			}
			
			Collections.sort(encounters, new Comparator<Encounter>() {
				
				@Override
				public int compare(Encounter o1, Encounter o2) {
					Date o1start = (o1.getVisit() != null) ? o1.getVisit().getStartDatetime() : o1.getEncounterDatetime();
					Date o2start = (o2.getVisit() != null) ? o2.getVisit().getStartDatetime() : o2.getEncounterDatetime();
					return o2start.compareTo(o1start);
				}
			});
		}
		
		if (start == null) {
			start = 0;
		}
		if (length == null) {
			length = encounters.size();
		}
		int end = start + length;
		if (end > encounters.size()) {
			end = encounters.size();
		}
		
		return encounters.subList(start, end);
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterDAO#getEncountersByVisitsAndPatientCount(org.openmrs.Patient,
	 *      boolean, java.lang.String)
	 */
	@Override
	public Integer getEncountersByVisitsAndPatientCount(Patient patient, boolean includeVoided, String query) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Visit.class);
		addEmptyVisitsByPatientCriteria(criteria, patient, includeVoided, query);
		
		criteria.setProjection(Projections.rowCount());
		Integer count = ((Number) criteria.uniqueResult()).intValue();
		
		criteria = sessionFactory.getCurrentSession().createCriteria(Encounter.class);
		addEncountersByPatientCriteria(criteria, patient, includeVoided, query);
		
		criteria.setProjection(Projections.rowCount());
		count = count + ((Number) criteria.uniqueResult()).intValue();
		
		return count;
	}
	
	private void addEmptyVisitsByPatientCriteria(Criteria criteria, Patient patient, boolean includeVoided, String query) {
		criteria.add(Restrictions.eq("patient", patient));
		criteria.add(Restrictions.isEmpty("encounters"));
		
		if (!includeVoided) {
			criteria.add(Restrictions.eq("voided", includeVoided));
		}
		
		if (query != null && !StringUtils.isBlank(query)) {
			criteria.createAlias("visitType", "visitType", Criteria.LEFT_JOIN);
			criteria.createAlias("location", "location", Criteria.LEFT_JOIN);
			
			Disjunction or = Restrictions.disjunction();
			criteria.add(or);
			or.add(Restrictions.ilike("visitType.name", query, MatchMode.ANYWHERE));
			or.add(Restrictions.ilike("location.name", query, MatchMode.ANYWHERE));
		}
		
		criteria.addOrder(Order.desc("startDatetime"));
	}
	
	private void addEncountersByPatientCriteria(Criteria criteria, Patient patient, boolean includeVoided, String query) {
		criteria.add(Restrictions.eq("patient", patient));
		criteria.createAlias("visit", "visit", Criteria.LEFT_JOIN);
		
		if (!includeVoided) {
			criteria.add(Restrictions.eq("voided", includeVoided));
		}
		
		if (query != null && !StringUtils.isBlank(query)) {
			criteria.createAlias("visit.visitType", "visitType", Criteria.LEFT_JOIN);
			criteria.createAlias("visit.location", "visitLocation", Criteria.LEFT_JOIN);
			criteria.createAlias("location", "location", Criteria.LEFT_JOIN);
			criteria.createAlias("encounterType", "encounterType", Criteria.LEFT_JOIN);
			
			Disjunction or = Restrictions.disjunction();
			criteria.add(or);
			or.add(Restrictions.ilike("visitType.name", query, MatchMode.ANYWHERE));
			or.add(Restrictions.ilike("visitLocation.name", query, MatchMode.ANYWHERE));
			or.add(Restrictions.ilike("location.name", query, MatchMode.ANYWHERE));
			or.add(Restrictions.ilike("encounterType.name", query, MatchMode.ANYWHERE));
		}
		
		criteria.addOrder(Order.desc("visit.startDatetime"));
		criteria.addOrder(Order.desc("encounterDatetime"));
	}
}

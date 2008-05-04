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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.ObsService;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.ObsDAO;
import org.openmrs.logic.Aggregation;
import org.openmrs.logic.Constraint;
import org.openmrs.logic.DateConstraint;
import org.openmrs.reporting.PatientSet;
import org.openmrs.util.OpenmrsUtil;

/**
 * 
 * @see org.openmrs.api.db.ObsDAO
 * @see org.openmrs.api.ObsService
 */
public class HibernateObsDAO implements ObsDAO {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;

	public HibernateObsDAO() {
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
	 * @see org.openmrs.api.db.ObsDAO#createObs(org.openmrs.Obs)
	 */
	public void createObs(Obs obs) throws DAOException {
		sessionFactory.getCurrentSession().persist(obs);
	}

	/**
	 * @see org.openmrs.api.db.ObsService#deleteObs(org.openmrs.Obs)
	 */
	public void deleteObs(Obs obs) throws DAOException {
		sessionFactory.getCurrentSession().delete(obs);
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getObs(java.lang.Integer)
	 */
	public Obs getObs(Integer obsId) throws DAOException {
		return (Obs) sessionFactory.getCurrentSession().get(Obs.class, obsId);
	}
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#findObservations(java.lang.Integer, boolean, java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> findObservations(Integer id, boolean includeVoided, Integer personType)
			throws DAOException {

		Criteria criteria = sessionFactory.getCurrentSession()
				.createCriteria(Obs.class)
				.createAlias("person", "p")
				.createAlias("encounter", "e")
				.add(
					Expression.or(
						Expression.eq("p.personId", id),
						Expression.like("e.encounterId", id)
					)
				);

		if (includeVoided == false) {
			criteria.add(Expression.eq("voided", new Boolean(false)));
		}
		
		getCriteriaPersonModifier(criteria, personType);

		return criteria.list();
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#getMimeType(java.lang.Integer)
	 */
	public MimeType getMimeType(Integer mimeTypeId) throws DAOException {
		MimeType mimeType = new MimeType();
		mimeType = (MimeType) sessionFactory.getCurrentSession().get(
				MimeType.class, mimeTypeId);

		return mimeType;
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#getMimeTypes()
	 */
	@SuppressWarnings("unchecked")
	public List<MimeType> getMimeTypes() throws DAOException {
		List<MimeType> mimeTypes = sessionFactory.getCurrentSession()
				.createCriteria(MimeType.class).list();

		return mimeTypes;
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#updateObs(org.openmrs.Obs)
	 */
	public void updateObs(Obs obs) throws DAOException {
		if (obs.getObsId() == null)
			createObs(obs);
		else {
			if (obs.hasGroupMembers()) {
				// hibernate has a problem updating child collections
				// if the parent object was already saved so we do it 
				// explicitly here
				for (Obs member : obs.getGroupMembers())
					if (member.getObsId() == null)
						updateObs(member);
			}
			
			Obs o = (Obs)sessionFactory.getCurrentSession().merge(obs);
		}
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservations(org.openmrs.Concept, org.openmrs.Location, java.lang.String, java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> getObservations(Concept c, Location location, String sort, Integer personType) {
		String q = "select obs " + getHqlPersonModifier(personType, "obs.location = :loc and obs.concept = :concept");
		if (sort != null && sort.length() > 0)
			q += " order by :sort";

		Query query = sessionFactory.getCurrentSession().createQuery(q);
		query.setParameter("loc", location);
		query.setParameter("concept", c);

		if (sort != null && sort.length() > 0)
			query.setParameter("sort", "obs." + sort);

		return query.list();
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservations(org.openmrs.Encounter)
	 */
	@SuppressWarnings("unchecked")
	public Set<Obs> getObservations(Encounter whichEncounter) {
		Query query = sessionFactory.getCurrentSession().createQuery(
				"from Obs obs where obs.encounter = :e");
		query.setParameter("e", whichEncounter);
		Set<Obs> ret = new HashSet<Obs>(query.list());

		return ret;
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservations(org.openmrs.Person, org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public Set<Obs> getObservations(Person who, Concept question, boolean includeVoided) {
		String s = "from Obs obs where obs.person = :p and obs.concept = :c";
		if (!includeVoided)
			s += " and obs.voided = false";
		Query query = sessionFactory.getCurrentSession().createQuery(s);
		query.setParameter("p", who);
		query.setParameter("c", question);
		Set<Obs> ret = new HashSet<Obs>(query.list());

		return ret;
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#getLastNObservations(java.lang.Integer, org.openmrs.Person, org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> getLastNObservations(Integer n, Person who, Concept question) {
		Query query = sessionFactory
				.getCurrentSession()
				.createQuery(
						"from Obs obs where obs.person = :p and obs.concept = :c order by obs.obsDatetime desc");
		query.setParameter("p", who);
		query.setParameter("c", question);
		query.setMaxResults(n);

		return query.list();
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservations(org.openmrs.Concept, java.lang.String, java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> getObservations(Concept question, String sort, Integer personType) {
		
		if (sort == null || sort.equals(""))
			sort = "obsId";
		
		Query query = sessionFactory.getCurrentSession().createQuery(
				"select obs " + getHqlPersonModifier(personType, "obs.concept = :c and obs.voided = false") + 
				" order by obs." + sort)
				.setParameter("c", question);

		return query.list();
	}
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservationsAnsweredByConcept(org.openmrs.Concept, java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> getObservationsAnsweredByConcept(Concept answer, Integer personType) {
		Query query = sessionFactory.getCurrentSession().createQuery(
				"select obs " + getHqlPersonModifier(personType, "obs.valueCoded = :c and obs.voided = false"))
				.setParameter("c", answer);

		return query.list();
	}
	
	
	/**
	 * @see org.openmrs.api.db.ObsDAO#getNumericAnswersForConcept(org.openmrs.Concept, java.lang.Boolean, java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getNumericAnswersForConcept(Concept answer, Boolean sortByValue, Integer personType) {
		
		String sort = "obs.obsDatetime desc";
		if (sortByValue)
			sort = "obs.valueNumeric asc";
		
		String sql = "";
		sql += "select obs.obsId, obs.obsDatetime, obs.valueNumeric ";
		sql += getHqlPersonModifier(personType, "obs.concept = :c and obs.valueNumeric is not null and obs.voided = false");
		sql += " order by " + sort;
		
		Query query = sessionFactory.getCurrentSession().createQuery(sql)
				.setParameter("c", answer);

		return query.list();
	}
	
	/**
	 * 
	 * @param personType
	 * @return
	 */
	private String getHqlPersonModifier(Integer personType, String whereClause) {
		String from = "from Obs obs";
		List<String> whereClauses = new Vector<String>();
		
		if (whereClause != null && whereClause.length() > 0) {
			whereClauses.add("(" + whereClause + ")");
		}
		
		if ((personType & ObsService.PATIENT) == ObsService.PATIENT) {
			from += ", Patient p";
			whereClauses.add("p.patientId = obs.person.personId");
		}
		
		if ((personType & ObsService.USER) == ObsService.USER) {
			from += ", User u";
			whereClauses.add("u.userId = obs.person.personId");
		}
		
		if ((personType & ObsService.PERSON) == ObsService.PERSON) {
			// all observations are already on person's.  Limit to non-patient and non-users here?
			//from += ", Person person";
			//whereClauses.add("person.personId = obs.personId");
		}
		
		if (whereClauses.size() > 0)
			from = from + " where " + OpenmrsUtil.join(whereClauses, " and ");
		
		return from + " ";
		
	}
	
	private void getCriteriaPersonModifier(Criteria criteria, Integer personType) {
		if ((personType & ObsService.PATIENT) == ObsService.PATIENT) {
			criteria.createAlias("Patient", "p");
			criteria.add(Restrictions.eqProperty("obs.person.personId", "p.patientId"));
		}
		
		if ((personType & ObsService.USER) == ObsService.USER) {
			criteria.createAlias("User", "u");
			criteria.add(Restrictions.eqProperty("obs.person.personId", "u.userId"));
		}
		
		if ((personType & ObsService.PERSON) == ObsService.PERSON) {
			// all observations are already on person's.  Limit to non-patient and non-users here?
			//criteria.createAlias("Person", "person");
			//criteria.add(Restrictions.eqProperty("obs.person.personId", "person.personId"));
		}
		
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservations(org.openmrs.Person)
	 */
	@SuppressWarnings("unchecked")
	public Set<Obs> getObservations(Person who, boolean includeVoided) {
		String s = "from Obs obs where obs.person = :p";
		if (!includeVoided)
			s += " and obs.voided = false";
		Query query = sessionFactory.getCurrentSession().createQuery(s);
		query.setParameter("p", who);
		Set<Obs> ret = new HashSet<Obs>(query.list());

		return ret;
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#getVoidedObservations()
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> getVoidedObservations() throws DAOException {
		Query query = sessionFactory
				.getCurrentSession()
				.createQuery(
						"from Obs obs where obs.voided = true order by obs.dateVoided desc");

		return query.list();
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#findObsByGroupId(java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> findObsByGroupId(Integer obsGroupId) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(
				Obs.class);
		criteria.add(Restrictions.eq("obsGroupId", obsGroupId));
		return criteria.list();
	}

	/**
	 * @see org.openmrs.api.db.ObsDAO#getObservations(org.openmrs.Person, org.openmrs.logic.Aggregation, org.openmrs.Concept, org.openmrs.logic.Constraint)
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> getObservations(Person who, Aggregation aggregation,
			Concept question, Constraint constraint) {
		Session session = sessionFactory.getCurrentSession();
		Criteria criteria = session.createCriteria(Obs.class);

		criteria.add(Restrictions.eq("person", who));
		criteria.add(Restrictions.eq("concept", question));
		criteria.add(Restrictions.eq("voided", false));

		DateConstraint dateConstraint = (DateConstraint) constraint;
		final String OBS_DATETIME = "obsDatetime";
		Criterion dateCriterion = null;
		GregorianCalendar d0, d1, d2;
		if (dateConstraint != null) {
			int dateComparison = dateConstraint.getComparison();
			switch (dateComparison) {
			case DateConstraint.EQUAL:
				dateCriterion = Restrictions.eq(OBS_DATETIME, dateConstraint
						.getDate());
				break;
			case DateConstraint.NOT_EQUAL:
				dateCriterion = Restrictions.ne(OBS_DATETIME, dateConstraint
						.getDate());
				break;
			case DateConstraint.WITHIN:
			case DateConstraint.NOT_WITHIN:
				dateCriterion = Restrictions.between(OBS_DATETIME,
						dateConstraint.getDate(), dateConstraint
								.getSecondDate());
				if (dateComparison == DateConstraint.NOT_WITHIN)
					dateCriterion = Restrictions.not(dateCriterion);
				break;
			case DateConstraint.WITHIN_PRECEDING:
			case DateConstraint.NOT_WITHIN_PRECEDING:
				d1 = new GregorianCalendar();
				d2 = new GregorianCalendar();
				d2.setTime(dateConstraint.getDate());
				d1.setTimeInMillis(d2.getTimeInMillis()
						- dateConstraint.getDuration().getDurationInMillis());
				dateCriterion = Restrictions.between(OBS_DATETIME,
						d1.getTime(), d2.getTime());
				if (dateComparison == DateConstraint.NOT_WITHIN_PRECEDING)
					dateCriterion = Restrictions.not(dateCriterion);
				break;
			case DateConstraint.WITHIN_FOLLOWING:
			case DateConstraint.NOT_WITHIN_FOLLOWING:
				d1 = new GregorianCalendar();
				d2 = new GregorianCalendar();
				d1.setTime(dateConstraint.getDate());
				d2.setTimeInMillis(d1.getTimeInMillis()
						+ dateConstraint.getDuration().getDurationInMillis());
				dateCriterion = Restrictions.between(OBS_DATETIME,
						d1.getTime(), d2.getTime());
				if (dateComparison == DateConstraint.NOT_WITHIN_FOLLOWING)
					dateCriterion = Restrictions.not(dateCriterion);
				break;
			case DateConstraint.WITHIN_SURROUNDING:
			case DateConstraint.NOT_WITHIN_SURROUNDING:
				d0 = new GregorianCalendar();
				d0.setTime(dateConstraint.getDate());
				d1 = new GregorianCalendar();
				d2 = new GregorianCalendar();
				long delta = dateConstraint.getDuration().getDurationInMillis() / 2;
				d1.setTimeInMillis(d0.getTimeInMillis() - delta);
				d2.setTimeInMillis(d0.getTimeInMillis() + delta);
				dateCriterion = Restrictions.between(OBS_DATETIME,
						d1.getTime(), d2.getTime());
				if (dateComparison == DateConstraint.NOT_WITHIN_SURROUNDING)
					dateCriterion = Restrictions.not(dateCriterion);
				break;
			case DateConstraint.WITHIN_PAST:
			case DateConstraint.NOT_WITHIN_PAST:
				d1 = new GregorianCalendar();
				d2 = new GregorianCalendar();
				d2.setTime(new Date());
				d1.setTimeInMillis(d1.getTimeInMillis()
						- dateConstraint.getDuration().getDurationInMillis());
				dateCriterion = Restrictions.between(OBS_DATETIME,
						d1.getTime(), d2.getTime());
				if (dateComparison == DateConstraint.NOT_WITHIN_PAST)
					dateCriterion = Restrictions.not(dateCriterion);
				break;
			case DateConstraint.WITHIN_SAME_DAY_AS:
			case DateConstraint.NOT_WITHIN_SAME_DAY_AS:
				d0 = new GregorianCalendar();
				d0.setTime(dateConstraint.getDate());
				d0.set(d0.get(Calendar.YEAR), d0.get(Calendar.MONTH), d0
						.get(Calendar.DATE), 0, 0, 0);
				d1 = new GregorianCalendar();
				d2 = new GregorianCalendar();
				d1.setTimeInMillis(d0.getTimeInMillis() - 1);
				d2.setTimeInMillis(d0.getTimeInMillis() + 1440000);
				dateCriterion = Restrictions.between(OBS_DATETIME,
						d1.getTime(), d2.getTime());
				if (dateComparison == DateConstraint.NOT_WITHIN_SAME_DAY_AS)
					dateCriterion = Restrictions.not(dateCriterion);
				break;
			case DateConstraint.AFTER:
				dateCriterion = Restrictions.gt(OBS_DATETIME, dateConstraint
						.getDate());
				break;
			case DateConstraint.NOT_AFTER:
				dateCriterion = Restrictions.le(OBS_DATETIME, dateConstraint
						.getDate());
				break;
			case DateConstraint.BEFORE:
				dateCriterion = Restrictions.lt(OBS_DATETIME, dateConstraint
						.getDate());
				break;
			case DateConstraint.NOT_BEFORE:
				dateCriterion = Restrictions.ge(OBS_DATETIME, dateConstraint
						.getDate());
				break;
			}
		}

		// Aggregation.EXIST - true if any results found
		// Aggregation.MEDIAN
		// Aggregation.STDDEV
		// Aggregation.VARIANCE
		// Aggregation.ANY - true if any results true, false for empty list or if any results non-boolean
		// Aggregation.ALL - true if all results true, null if any results non-boolean, true for empty list
		// Aggregation.NO - true if all results false
		// Aggregation.N_MINIMUM
		// Aggregation.N_MAXIMUM
		
		// TODO: figure out a way to return average, count, sum as "pseudo" obs
		Projection projection = null;
		boolean singleNumericResult = false;
		if (aggregation != null) {
			switch (aggregation.getType()) {
			case Aggregation.ALL:
				break;
			case Aggregation.AVERAGE:
				projection = Projections.avg("valueNumeric");
				singleNumericResult = true;
				break;
			case Aggregation.COUNT:
				projection = Projections.rowCount();
				singleNumericResult = true;
				break;
			case Aggregation.MAXIMUM:
				DetachedCriteria maxObs = DetachedCriteria.forClass(
						Obs.class).add(Restrictions.eq("person", who)).add(
						Restrictions.eq("concept", question));
				if (dateCriterion != null)
					maxObs = maxObs
						.add(dateCriterion);
				maxObs = maxObs.setProjection(
								Projections.max("valueNumeric"));
				if (dateCriterion != null)
					criteria.add(dateCriterion);
				criteria.add(
						Property.forName("valueNumeric").eq(maxObs));
				dateCriterion = null;
				break;
			case Aggregation.MINIMUM:
				DetachedCriteria minObs = DetachedCriteria.forClass(
						Obs.class).add(Restrictions.eq("person", who)).add(
						Restrictions.eq("concept", question));
				if (dateCriterion != null)
					minObs = minObs
						.add(dateCriterion);
				minObs = minObs.setProjection(
								Projections.min("valueNumeric"));
				if (dateCriterion != null)
					criteria.add(dateCriterion);
				criteria.add(
						Property.forName("valueNumeric").eq(minObs));
				dateCriterion = null;
				break;
			case Aggregation.SUM:
				projection = Projections.sum("valueNumeric");
				singleNumericResult = true;
				break;
			case Aggregation.LATEST:
			case Aggregation.LAST:
				DetachedCriteria latestObs = DetachedCriteria.forClass(
						Obs.class).add(Restrictions.eq("person", who)).add(
						Restrictions.eq("concept", question));
				if (dateCriterion != null)
					latestObs = latestObs
						.add(dateCriterion);
				latestObs = latestObs.setProjection(
								Projections.max("obsDatetime"));
				if (dateCriterion != null)
					criteria.add(dateCriterion);
				criteria.add(
						Property.forName("obsDatetime").eq(latestObs));
				dateCriterion = null;
				break;
			case Aggregation.EARLIEST:
			case Aggregation.FIRST:
				DetachedCriteria earliestObs = DetachedCriteria.forClass(
						Obs.class).add(Restrictions.eq("person", who)).add(
						Restrictions.eq("concept", question));
				if (dateCriterion != null)
					earliestObs = earliestObs
						.add(dateCriterion);
				earliestObs = earliestObs.setProjection(
								Projections.min("obsDatetime"));
				if (dateCriterion != null)
					criteria.add(dateCriterion);
				criteria.add(
						Property.forName("obsDatetime").eq(earliestObs));
				dateCriterion = null;
			}
		}

		if (dateCriterion != null)
			criteria.add(dateCriterion);
		criteria.addOrder(Order.asc(OBS_DATETIME));
		if (projection != null)
			criteria.setProjection(projection);

		List<Obs> result;
		
		if (singleNumericResult) {
			Object numericResult = criteria.uniqueResult();
			Double resultValue = Double.parseDouble(numericResult.toString());
			result = new Vector<Obs>();
			Obs obs = new Obs();
			obs.setConcept(question);
			obs.setObsDatetime(new Date());
			obs.setValueNumeric(resultValue);
			result.add(obs);
		} else {
		
			result = criteria.list();

			if (aggregation != null) {
				switch (aggregation.getType()) {
				case Aggregation.N_LAST:
				case Aggregation.N_LATEST:
					if (result.size() > aggregation.getN()) {
						List<Obs> tail = new Vector<Obs>();
						for (int i = result.size() - aggregation.getN(); i < result
								.size(); i++) {
							tail.add(result.get(i));
						}
						result = tail;
					}
					break;
				case Aggregation.N_FIRST:
				case Aggregation.N_EARLIEST:
					if (result.size() > aggregation.getN()) {
						List<Obs> head = new Vector<Obs>();
						for (int i = 0; i < aggregation.getN(); i++) {
							head.add(result.get(i));
						}
						result = head;
					}
					break;
				}
			}
			
		}
		return result;
	}

	/**
	 * @see org.openmrs.api.ObsService#getObservations(java.util.List<org.openmrs.Concept>, java.util.Date, java.util.Data, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> getObservations(List<Concept> concepts, Date fromDate, Date toDate, boolean includeVoided)
			throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		if ( concepts != null ) {
			criteria.add(Restrictions.in("concept", concepts));
		}
		if ( fromDate != null ) {
			criteria.add(Restrictions.gt("obsDatetime", fromDate));
		}
		if ( toDate != null ) {
			criteria.add(Restrictions.lt("obsDatetime", toDate));
		}
		if ( !includeVoided ) {
			criteria.add(Restrictions.eq("voided", false));
		}
		criteria.addOrder(Order.desc("obsDatetime"));
		return (List<Obs>)criteria.list();
	}

	/**
	 * @see org.openmrs.api.ObsService#getObservations(java.util.List<org.openmrs.Concept>, java.util.Date, java.util.Data, boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> getObservations(PatientSet patients, List<Concept> concepts, Date fromDate, Date toDate)
			throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		if ( patients != null ) {
			List<Patient> patientList = patients.getPatients();
			if ( patientList != null ) {
				criteria.add(Restrictions.in("person", patientList));
			}
		}
		if ( concepts != null ) {
			criteria.add(Restrictions.in("concept", concepts));
		}
		if ( fromDate != null ) {
			criteria.add(Restrictions.gt("obsDatetime", fromDate));
		}
		if ( toDate != null ) {
			criteria.add(Restrictions.lt("obsDatetime", toDate));
		}
		criteria.addOrder(Order.desc("obsDatetime"));
		return (List<Obs>)criteria.list();
	}
}
package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.ObsDAO;

//import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

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
	 * @see org.openmrs.api.db.ObsService#createObs(org.openmrs.Obs)
	 */
	public void createObs(Obs obs) throws DAOException {
		if (obs.getCreator() == null)
			obs.setCreator(Context.getAuthenticatedUser());

		if (obs.getDateCreated() == null)
			obs.setDateCreated(new Date());

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

	@SuppressWarnings("unchecked")
	public List<Obs> findObservations(Integer id, boolean includeVoided)
			throws DAOException {

		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class).createAlias(
				"patient", "p").createAlias("encounter", "e").add(
				Expression.or(Expression.eq("p.patientId", id), Expression
						.like("e.encounterId", id)));

		if (includeVoided == false) {
			criteria.add(Expression.eq("voided", new Boolean(false)));
		}

		return criteria.list();
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getMimeType(java.lang.Integer)
	 */
	public MimeType getMimeType(Integer mimeTypeId) throws DAOException {
		MimeType mimeType = new MimeType();
		mimeType = (MimeType) sessionFactory.getCurrentSession().get(MimeType.class, mimeTypeId);

		return mimeType;
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getMimeTypes()
	 */
	@SuppressWarnings("unchecked")
	public List<MimeType> getMimeTypes() throws DAOException {
		List<MimeType> mimeTypes = sessionFactory.getCurrentSession().createCriteria(MimeType.class)
				.list();

		return mimeTypes;
	}

	/**
	 * @see org.openmrs.api.db.ObsService#updateObs(org.openmrs.Obs)
	 */
	public void updateObs(Obs obs) throws DAOException {
		if (obs.getObsId() == null)
			createObs(obs);
		else {
			obs = (Obs) sessionFactory.getCurrentSession().merge(obs);
		}
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getLocation(java.lang.Integer)
	 */
	public Location getLocation(Integer locationId) throws DAOException {
		return (Location) sessionFactory.getCurrentSession().get(Location.class, locationId);
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getLocationByName(java.lang.String)
	 */
	public Location getLocationByName(String name) throws DAOException {
		List result = sessionFactory.getCurrentSession().createQuery(
				"from Location l where l.name = :name").setString("name", name)
				.list();
		if (result.size() == 0) {
			return null;
		} else {
			return (Location) result.get(0);
		}
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getLocations()
	 */
	@SuppressWarnings("unchecked")
	public List<Location> getLocations() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from Location l").list();
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getObservations(org.openmrs.Concept,org.openmrs.Location,java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> getObservations(Concept c, Location location, String sort) {
		String q = "from Obs obs where obs.location = :loc and obs.concept = :concept";
		if (sort != null && sort.length() > 0)
			q += " order by :sort";

		Query query = sessionFactory.getCurrentSession().createQuery(q);
		query.setParameter("loc", location);
		query.setParameter("concept", c);

		if (sort != null && sort.length() > 0)
			query.setParameter("sort", sort);

		return query.list();
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getObservations(org.openmrs.Encounter)
	 */
	@SuppressWarnings("unchecked")
	public Set<Obs> getObservations(Encounter whichEncounter) {
		Query query = sessionFactory.getCurrentSession()
				.createQuery("from Obs obs where obs.encounter = :e");
		query.setParameter("e", whichEncounter);
		Set<Obs> ret = new HashSet<Obs>(query.list());

		return ret;
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getObservations(org.openmrs.Patient,
	 *      org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public Set<Obs> getObservations(Patient who, Concept question) {
		Query query = sessionFactory.getCurrentSession()
				.createQuery("from Obs obs where obs.patient = :p and obs.concept = :c");
		query.setParameter("p", who);
		query.setParameter("c", question);
		Set<Obs> ret = new HashSet<Obs>(query.list());

		return ret;
	}
	
	/**
	 * @see org.openmrs.api.db.ObsService#getObservations(java.lang.Integer,org.openmrs.Patient,org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> getLastNObservations(Integer n, Patient who, Concept question) {
		Query query = sessionFactory.getCurrentSession()
				.createQuery("from Obs obs where obs.patient = :p and obs.concept = :c order by obs.obsDatetime desc");
		query.setParameter("p", who);
		query.setParameter("c", question);
		query.setMaxResults(n);
		
		return query.list();
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getObservations(org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> getObservations(Concept question, String sort) {
		Query query = sessionFactory.getCurrentSession().createQuery(
				"from Obs obs where obs.concept = :c and obs.voided = false order by "
						+ sort).setParameter("c", question);

		return query.list();
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getObservations(org.openmrs.Patient)
	 */
	@SuppressWarnings("unchecked")
	public Set<Obs> getObservations(Patient who) {
		Query query = sessionFactory.getCurrentSession()
				.createQuery("from Obs obs where obs.patient = :p");
		query.setParameter("p", who);
		Set<Obs> ret = new HashSet<Obs>(query.list());

		return ret;
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getVoidedObservations()
	 */
	@SuppressWarnings("unchecked")
	public List<Obs> getVoidedObservations() throws DAOException {
		Query query = sessionFactory.getCurrentSession()
				.createQuery("from Obs obs where obs.voided = true order by obs.dateVoided desc");

		return query.list();
	}

	@SuppressWarnings("unchecked")
	public List<Obs> findObsByGroupId(Integer obsGroupId) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		criteria.add(Restrictions.eq("obsGroupId", obsGroupId));
		return criteria.list();
	}
}
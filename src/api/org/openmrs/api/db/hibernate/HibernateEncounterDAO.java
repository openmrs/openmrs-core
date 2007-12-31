package org.openmrs.api.db.hibernate;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.EncounterDAO;

public class HibernateEncounterDAO implements EncounterDAO {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernateEncounterDAO() {
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
	 * @see org.openmrs.api.db.EncounterService#createEncounter(org.openmrs.Encounter)
	 */
	public void createEncounter(Encounter encounter) throws DAOException {
		log.debug("Creating encounter: " + encounter.getEncounterId());
		if (encounter.getCreator() == null)
			encounter.setCreator(Context.getAuthenticatedUser());

		if (encounter.getDateCreated() == null)
			encounter.setDateCreated(new Date());

		sessionFactory.getCurrentSession().save(encounter);
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#deleteEncounter(org.openmrs.Encounter)
	 */
	public void deleteEncounter(Encounter encounter) throws DAOException {
		sessionFactory.getCurrentSession().delete(encounter);
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounter(java.lang.Integer)
	 */
	public Encounter getEncounter(Integer encounterId) throws DAOException {
		return (Encounter) sessionFactory.getCurrentSession().get(Encounter.class, encounterId);
	}

	@SuppressWarnings("unchecked")
	public List<Encounter> getEncountersByPatientId(Integer patientId,
			boolean includeVoided) throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Encounter.class)
				.createAlias("patient", "p")
				.add(Expression.eq("p.patientId", patientId))
				.addOrder(Order.desc("encounterDatetime"));

		if (!includeVoided)
			crit.add(Expression.eq("voided", false));

		return crit.list();
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounterType(java.lang.Integer)
	 */
	public EncounterType getEncounterType(Integer encounterTypeId)
			throws DAOException {
		return (EncounterType) sessionFactory.getCurrentSession().get(
				EncounterType.class, encounterTypeId);
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounterType(java.lang.String)
	 */
	public EncounterType getEncounterType(String name)
			throws DAOException {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(EncounterType.class);
		crit.add(Expression.eq("name", name));
		EncounterType encounterType = (EncounterType)crit.uniqueResult();

		return encounterType;

	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounterTypes()
	 */
	@SuppressWarnings("unchecked")
	public List<EncounterType> getEncounterTypes() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery(
				"from EncounterType et order by et.name").list();
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getLocation(java.lang.Integer)
	 */
	public Location getLocation(Integer locationId) throws DAOException {
		return (Location)sessionFactory.getCurrentSession().get(Location.class, locationId);
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterService#getLocationByName(java.lang.String)
	 */
	public Location getLocationByName(String name) throws DAOException {
		List result = sessionFactory.getCurrentSession().createQuery("from Location l where l.name = :name").setString("name", name).list();
		if (result.size() == 0) {
			return null;
		} else {
			return (Location) result.get(0);
		}
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getLocations()
	 */
	@SuppressWarnings("unchecked")
	public List<Location> getLocations() throws DAOException {
		return sessionFactory.getCurrentSession().createQuery("from Location l order by l.name")
				.list();
	}
	
	/**
	 * @see org.openmrs.api.db.EncounterService#findLocations(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Location> findLocations(String name) throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(Location.class)
			.add(Expression.like("name", name, MatchMode.START))
			.addOrder(Order.asc("name"))
			.list();
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#updateEncounter(org.openmrs.Encounter)
	 */
	public void updateEncounter(Encounter encounter) throws DAOException {
		log.debug("Updating encounter: " + encounter.getEncounterId());

		if (encounter.getCreator() == null)
			createEncounter(encounter);
		else {
				Encounter e = (Encounter) sessionFactory.getCurrentSession().merge(encounter);
				sessionFactory.getCurrentSession().evict(e);
				sessionFactory.getCurrentSession().saveOrUpdate(encounter);
		}
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounters(org.openmrs.Patient,
	 *      java.util.Date, java.util.Date)
	 */
	@SuppressWarnings("unchecked")
	public Set<Encounter> getEncounters(Patient who, Date fromDate, Date toDate) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Encounter.class).add(
				Expression.eq("patient", who)).add(
				Expression.between("encounterDatetime", fromDate, toDate)).add(
				Expression.eq("voided", false));

		Set<Encounter> encounters = new HashSet<Encounter>();
		encounters.addAll(crit.list());

		return encounters;
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounters(org.openmrs.Patient,
	 *      org.openmrs.Location)
	 */
	@SuppressWarnings("unchecked")
	public Set<Encounter> getEncounters(Patient who, Location where) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Encounter.class).add(
				Expression.eq("patient", who)).add(
				Expression.eq("location", where)).add(
				Expression.eq("voided", false));

		Set<Encounter> encounters = new HashSet<Encounter>();
		encounters.addAll(crit.list());

		return encounters;

	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounters(org.openmrs.Patient,boolean)
	 */
	public Set<Encounter> getEncounters(Patient who, boolean includeVoided) {
		Set<Encounter> encounters = new HashSet<Encounter>();
		if (who != null)
			encounters.addAll(getEncountersByPatientId(who.getPatientId(), includeVoided));

		return encounters;
	}
	
    /**
     * @see org.openmrs.api.db.EncounterService#getEncounters(java.util.Date,java.util.Date)
     */
	@SuppressWarnings("unchecked")
    public Collection<Encounter> getEncounters(Date fromDate, Date toDate) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Encounter.class).add(
				Expression.between("encounterDatetime", fromDate, toDate)).add(
				Expression.eq("voided", false))
				.addOrder(Order.asc("location"))
				.addOrder(Order.asc("encounterDatetime"));

		return crit.list();
    }
	
    /**
     * @see org.openmrs.api.db.EncounterService#getEncounters(org.openmrs.Location,java.util.Date,java.util.Date)
     */
	@SuppressWarnings("unchecked")
    public Collection<Encounter> getEncounters(Location loc, Date fromDate, Date toDate) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(Encounter.class).add(
				Expression.eq("location", loc)).add(
				Expression.between("encounterDatetime", fromDate, toDate)).add(
				Expression.eq("voided", false))
				.addOrder(Order.asc("encounterDatetime"));

		return crit.list();
    }

	/**
     * @see org.openmrs.api.db.EncounterDAO#getEncounters(org.openmrs.Patient, org.openmrs.Location, java.util.Date, java.util.Date, java.util.Collection, java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    public Collection<Encounter> getEncounters(Patient who,
                                               Location loc,
                                               Date fromDate,
                                               Date toDate,
                                               Collection<Form> enteredViaForms,
                                               Collection<EncounterType> encounterTypes,
                                               boolean includeVoided) {
    	Criteria crit = sessionFactory.getCurrentSession().createCriteria(Encounter.class);
    	if (who != null)
    		crit.add(Expression.eq("patient", who));
    	if (fromDate != null)
    		crit.add(Expression.ge("encounterDatetime", fromDate));
    	if (toDate != null)
    		crit.add(Expression.le("encounterDatetime", toDate));
    	if (enteredViaForms != null && enteredViaForms.size() > 0)
    		crit.add(Expression.in("form", enteredViaForms));
    	if (encounterTypes != null && encounterTypes.size() > 0)
    		crit.add(Expression.in("encounterType", encounterTypes));
    	if (!includeVoided)
    		crit.add(Expression.eq("voided", false));
    	crit.addOrder(Order.asc("encounterDatetime"));
    	return crit.list();
    }
	
}

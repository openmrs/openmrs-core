package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.api.db.EncounterDAO;
import org.openmrs.util.OpenmrsConstants;

public class HibernateEncounterDAO implements EncounterDAO {

	protected final Log log = LogFactory.getLog(getClass());

	private Context context;

	public HibernateEncounterDAO() {
	}

	public HibernateEncounterDAO(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#createEncounter(org.openmrs.Encounter)
	 */
	public void createEncounter(Encounter encounter) throws DAOException {

		Session session = HibernateUtil.currentSession();

		encounter.setCreator(context.getAuthenticatedUser());

		// if dateCreated not set OR use lacks privilege to override, then set
		// dateCreated to current time
		if (encounter.getDateCreated() == null
				|| !context
						.hasPrivilege(OpenmrsConstants.PRIV_OVERRIDE_DATE_CREATED))
			encounter.setDateCreated(new Date());

		try {
			HibernateUtil.beginTransaction();
			session.save(encounter);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#deleteEncounter(org.openmrs.Encounter)
	 */
	public void deleteEncounter(Encounter encounter) throws DAOException {

		Session session = HibernateUtil.currentSession();

		try {
			HibernateUtil.beginTransaction();
			session.delete(encounter);
			HibernateUtil.commitTransaction();
		} catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}

	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounter(java.lang.Integer)
	 */
	public Encounter getEncounter(Integer encounterId) throws DAOException {

		Session session = HibernateUtil.currentSession();

		Encounter encounter = new Encounter();
		encounter = (Encounter) session.get(Encounter.class, encounterId);

		return encounter;
	}

	public List<Encounter> getEncountersByPatientId(Integer patientId,
			boolean includeVoided) throws DAOException {

		Session session = HibernateUtil.currentSession();

		Criteria crit = session.createCriteria(Encounter.class).createAlias(
				"patient", "p").add(Expression.eq("p.patientId", patientId));

		if (!includeVoided)
			crit.add(Expression.eq("p.voided", false));

		return crit.list();
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounterType(java.lang.Integer)
	 */
	public EncounterType getEncounterType(Integer encounterTypeId)
			throws DAOException {

		Session session = HibernateUtil.currentSession();

		EncounterType encounterType = (EncounterType) session.get(
				EncounterType.class, encounterTypeId);

		return encounterType;

	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounterTypes()
	 */
	public List<EncounterType> getEncounterTypes() throws DAOException {

		Session session = HibernateUtil.currentSession();

		List<EncounterType> encounterTypes;
		encounterTypes = session.createQuery(
				"from EncounterType et order by et.name").list();

		return encounterTypes;

	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getLocation(java.lang.Integer)
	 */
	public Location getLocation(Integer locationId) throws DAOException {

		Session session = HibernateUtil.currentSession();

		Location location = new Location();
		location = (Location) session.get(Location.class, locationId);

		return location;

	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getLocations()
	 */
	public List<Location> getLocations() throws DAOException {

		Session session = HibernateUtil.currentSession();

		List<Location> locations;
		locations = session.createQuery("from Location l order by l.name")
				.list();

		return locations;

	}

	/**
	 * @see org.openmrs.api.db.EncounterService#updateEncounter(org.openmrs.Encounter)
	 */
	public void updateEncounter(Encounter encounter) throws DAOException {

		if (encounter.getCreator() == null)
			createEncounter(encounter);
		else {
			Session session = HibernateUtil.currentSession();

			// encounter.setChangedBy(context.getAuthenticatedUser());
			// encounter.setDateChanged(new Date());
			try {
				HibernateUtil.beginTransaction();
				Encounter e = (Encounter) session.merge(encounter);
				session.evict(e);
				session.saveOrUpdate(encounter);
				HibernateUtil.commitTransaction();
			} catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounters(org.openmrs.Patient,
	 *      java.util.Date, java.util.Date)
	 */
	public Set<Encounter> getEncounters(Patient who, Date fromDate, Date toDate) {

		Session session = HibernateUtil.currentSession();

		Criteria crit = session.createCriteria(Encounter.class).add(
				Expression.eq("patient", who)).add(
				Expression.between("encounterDatetime", fromDate, toDate));

		Set<Encounter> encounters = new HashSet<Encounter>();
		encounters.addAll(crit.list());

		return encounters;
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounters(org.openmrs.Patient,
	 *      org.openmrs.Location)
	 */
	public Set<Encounter> getEncounters(Patient who, Location where) {

		Session session = HibernateUtil.currentSession();

		Criteria crit = session.createCriteria(Encounter.class).add(
				Expression.eq("patient", who)).add(
				Expression.eq("location", where));

		Set<Encounter> encounters = new HashSet<Encounter>();
		encounters.addAll(crit.list());

		return encounters;

	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounters(org.openmrs.Patient)
	 */
	public Set<Encounter> getEncounters(Patient who) {
		Session session = HibernateUtil.currentSession();

		Criteria crit = session.createCriteria(Encounter.class).add(
				Expression.eq("patient", who));

		Set<Encounter> encounters = new HashSet<Encounter>();
		encounters.addAll(crit.list());

		return encounters;
	}
}

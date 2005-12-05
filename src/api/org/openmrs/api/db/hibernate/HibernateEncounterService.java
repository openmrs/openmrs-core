package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.APIException;
import org.openmrs.api.db.EncounterService;

public class HibernateEncounterService implements
		EncounterService {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateEncounterService(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#createEncounter(org.openmrs.Encounter)
	 */
	public void createEncounter(Encounter encounter) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		encounter.setCreator(context.getAuthenticatedUser());
		encounter.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(encounter);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#deleteEncounter(org.openmrs.Encounter)
	 */
	public void deleteEncounter(Encounter encounter) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			session.delete(encounter);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
		
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounter(java.lang.Integer)
	 */
	public Encounter getEncounter(Integer encounterId) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		Encounter encounter = new Encounter();
		encounter = (Encounter)session.get(Encounter.class, encounterId);
		
		return encounter;
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounterType(java.lang.Integer)
	 */
	public EncounterType getEncounterType(Integer encounterTypeId) throws APIException {

		Session session = HibernateUtil.currentSession();
		
		EncounterType encounterType = (EncounterType)session.get(EncounterType.class, encounterTypeId);
		
		return encounterType;
		
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounterTypes()
	 */
	public List<EncounterType> getEncounterTypes() throws APIException {
	
		Session session = HibernateUtil.currentSession();
		
		List<EncounterType> encounterTypes;
		encounterTypes = session.createQuery("from EncounterType et order by et.name").list();
		
		return encounterTypes;

	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getLocation(java.lang.Integer)
	 */
	public Location getLocation(Integer locationId) throws APIException {

		Session session = HibernateUtil.currentSession();
		
		Location location = new Location();
		location = (Location)session.get(Location.class, locationId);
		
		return location;

	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getLocations()
	 */
	public List<Location> getLocations() throws APIException {

		Session session = HibernateUtil.currentSession();
		
		List<Location> locations;
		locations = session.createQuery("from Location l order by l.name").list();
		
		return locations;

	}

	/**
	 * @see org.openmrs.api.db.EncounterService#updateEncounter(org.openmrs.Encounter)
	 */
	public void updateEncounter(Encounter encounter) throws APIException {

		if (encounter.getCreator() == null)
			createEncounter(encounter);
		else {
			Session session = HibernateUtil.currentSession();
			
			//encounter.setChangedBy(context.getAuthenticatedUser());
			//encounter.setDateChanged(new Date());
			try {
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(encounter);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e);
			}
		}
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounters(org.openmrs.Patient, java.util.Date, java.util.Date)
	 */
	public Set<Encounter> getEncounters(Patient who, Date fromDate, Date toDate) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounters(org.openmrs.Patient, org.openmrs.Location)
	 */
	public Set<Encounter> getEncounters(Patient who, Location where) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.openmrs.api.db.EncounterService#getEncounters(org.openmrs.Patient)
	 */
	public Set<Encounter> getEncounters(Patient who) {
		// TODO Auto-generated method stub
		return null;
	}
}

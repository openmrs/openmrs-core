package org.openmrs.api.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.context.Context;

public class HibernateEncounterService implements
		EncounterService {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateEncounterService(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.EncounterService#createEncounter(org.openmrs.Encounter)
	 */
	public Encounter createEncounter(Encounter encounter) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		encounter.setCreator(context.getAuthenticatedUser());
		encounter.setDateCreated(new Date());
		session.save(encounter);
		
		tx.commit();
		session.disconnect();
		
		return encounter;
	}

	/**
	 * @see org.openmrs.api.EncounterService#deleteEncounter(org.openmrs.Encounter)
	 */
	public void deleteEncounter(Encounter encounter) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(encounter);
		
		tx.commit();
		session.disconnect();
		
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounter(java.lang.Integer)
	 */
	public Encounter getEncounter(Integer encounterId) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		Encounter encounter = new Encounter();
		session.load(encounter, encounterId);
		
		tx.commit();
		session.disconnect();
		
		return encounter;
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounterType(java.lang.Integer)
	 */
	public EncounterType getEncounterType(Integer encounterTypeId) throws APIException {

		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		EncounterType encounterType = new EncounterType();
		session.load(encounterType, encounterTypeId);
		
		tx.commit();
		session.disconnect();
		
		return encounterType;
		
	}

	/**
	 * @see org.openmrs.api.EncounterService#getEncounterTypes()
	 */
	public List<EncounterType> getEncounterTypes() throws APIException {
	
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		List<EncounterType> encounterTypes;
		encounterTypes = session.createQuery("from encounterType et").list();
		
		tx.commit();
		session.disconnect();
		
		return encounterTypes;

	}

	/**
	 * @see org.openmrs.api.EncounterService#getLocation(java.lang.Integer)
	 */
	public Location getLocation(Integer locationId) throws APIException {

		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		Location location = new Location();
		session.load(location, locationId);
		
		tx.commit();
		session.disconnect();
		
		return location;

	}

	/**
	 * @see org.openmrs.api.EncounterService#getLocations()
	 */
	public List<Location> getLocations() throws APIException {

		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		List<Location> locations;
		locations = session.createQuery("from Location l").list();
		
		tx.commit();
		session.disconnect();
		
		return locations;

	}

	/**
	 * @see org.openmrs.api.EncounterService#updateEncounter(org.openmrs.Encounter)
	 */
	public void updateEncounter(Encounter encounter) throws APIException {

		if (encounter.getCreator() == null)
			createEncounter(encounter);
		else {
			Session session = HibernateUtil.currentSession();
			Transaction tx = session.beginTransaction();
			
			//encounter.setChangedBy(context.getAuthenticatedUser());
			//encounter.setDateChanged(new Date());
			session.saveOrUpdate(encounter);
			
			tx.commit();
			session.disconnect();
		}
		
	}

	
}

package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
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

public class HibernateObsDAO implements
		ObsDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateObsDAO(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.db.ObsService#createObs(org.openmrs.Obs)
	 */
	public void createObs(Obs obs) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		obs.setCreator(context.getAuthenticatedUser());
		obs.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(obs);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.ObsService#deleteObs(org.openmrs.Obs)
	 */
	public void deleteObs(Obs obs) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			session.delete(obs);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
		
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getObs(java.lang.Integer)
	 */
	public Obs getObs(Integer obsId) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		Obs obs = new Obs();
		obs = (Obs)session.get(Obs.class, obsId);
		
		return obs;
	}
	
	public List<Obs> findObservations(Integer id, boolean includeVoided) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		List<Obs> obs = new Vector<Obs>();
		
		Criteria criteria = session.createCriteria(Obs.class)
			.createAlias("patient", "p")
			.createAlias("encounter", "e")
			.add(Expression.or(
				Expression.eq("p.patientId", id),
				Expression.like("e.encounterId", id)
				)
			);
		
		if (includeVoided == false) {
			criteria.add(Expression.eq("voided", new Boolean(false)));
		}

		return criteria.list();
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getMimeType(java.lang.Integer)
	 */
	public MimeType getMimeType(Integer mimeTypeId) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		MimeType mimeType = new MimeType();
		mimeType = (MimeType)session.get(MimeType.class, mimeTypeId);
		
		return mimeType;
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getMimeTypes()
	 */
	public List<MimeType> getMimeTypes() throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		List<MimeType> mimeTypes = session.createCriteria(MimeType.class).list();
		
		return mimeTypes;
	}

	/**
	 * @see org.openmrs.api.db.ObsService#unvoidObs(org.openmrs.Obs)
	 */
	public void unvoidObs(Obs obs) throws DAOException {
		
		obs.setVoided(false);
		obs.setVoidedBy(null);
		obs.setDateVoided(null);
		
		updateObs(obs);
		
	}

	/**
	 * @see org.openmrs.api.db.ObsService#updateObs(org.openmrs.Obs)
	 */
	public void updateObs(Obs obs) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		
		
		session.saveOrUpdate(obs);
	}

	/**
	 * @see org.openmrs.api.db.ObsService#voidObs(org.openmrs.Obs, java.lang.String)
	 */
	public void voidObs(Obs obs, String reason) throws DAOException {
		obs.setVoided(true);
		obs.setVoidedBy(context.getAuthenticatedUser());
		obs.setDateVoided(new Date());
		obs.setVoidReason(reason);
		
		updateObs(obs);	
	}
	
	/**
	 * @see org.openmrs.api.db.ObsService#getLocation(java.lang.Integer)
	 */
	public Location getLocation(Integer locationId) throws DAOException {

		Session session = HibernateUtil.currentSession();
		
		Location location = new Location();
		location = (Location)session.get(Location.class, locationId);
		
		return location;

	}

	/**
	 * @see org.openmrs.api.db.ObsService#getLocations()
	 */
	public List<Location> getLocations() throws DAOException {

		Session session = HibernateUtil.currentSession();
		
		List<Location> locations;
		locations = session.createQuery("from Location l").list();
		
		return locations;

	}

	/**
	 * @see org.openmrs.api.db.ObsService#getObservations(org.openmrs.Encounter)
	 */
	public Set<Obs> getObservations(Encounter whichEncounter) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getObservations(org.openmrs.Patient, org.openmrs.Concept)
	 */
	public Set<Obs> getObservations(Patient who, Concept question) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.openmrs.api.db.ObsService#getObservations(org.openmrs.Patient)
	 */
	public Set<Obs> getObservations(Patient who) {
		// TODO Auto-generated method stub
		return null;
	}
}
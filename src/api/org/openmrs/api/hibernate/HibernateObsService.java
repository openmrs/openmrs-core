package org.openmrs.api.hibernate;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openmrs.MimeType;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.ObsService;
import org.openmrs.context.Context;

//import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class HibernateObsService implements
		ObsService {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateObsService(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.ObsService#createObs(org.openmrs.Obs)
	 */
	public void createObs(Obs obs) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		obs.setCreator(context.getAuthenticatedUser());
		obs.setDateCreated(new Date());
		session.save(obs);
		
		tx.commit();
		HibernateUtil.closeSession();
	}

	/**
	 * @see org.openmrs.api.ObsService#deleteObs(org.openmrs.Obs)
	 */
	public void deleteObs(Obs obs) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(obs);
		
		tx.commit();
		HibernateUtil.closeSession();
	}

	/**
	 * @see org.openmrs.api.ObsService#getObs(java.lang.Integer)
	 */
	public Obs getObs(Integer obsId) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		Obs obs = new Obs();
		obs = (Obs)session.get(Obs.class, obsId);
		
		HibernateUtil.closeSession();
		
		return obs;
	}

	/**
	 * @see org.openmrs.api.ObsService#getMimeType(java.lang.Integer)
	 */
	public MimeType getMimeType(Integer mimeTypeId) throws APIException {
		
		Session session = HibernateUtil.currentSession();
		
		MimeType mimeType = new MimeType();
		mimeType = (MimeType)session.get(MimeType.class, mimeTypeId);
		
		HibernateUtil.closeSession();
		
		return mimeType;
	}

	/**
	 * @see org.openmrs.api.ObsService#getMimeTypes()
	 */
	public List<MimeType> getMimeTypes() throws APIException {
		Session session = HibernateUtil.currentSession();
		
		List<MimeType> mimeTypes = session.createCriteria(MimeType.class).list();
		
		HibernateUtil.closeSession();
		
		return mimeTypes;
	}

	/**
	 * @see org.openmrs.api.ObsService#unvoidObs(org.openmrs.Obs)
	 */
	public void unvoidObs(Obs obs) throws APIException {
		
		obs.setVoided(false);
		obs.setVoidedBy(null);
		obs.setDateVoided(null);
		
		updateObs(obs);
		
	}

	/**
	 * @see org.openmrs.api.ObsService#updateObs(org.openmrs.Obs)
	 */
	public void updateObs(Obs obs) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.saveOrUpdate(obs);
		
		tx.commit();
		HibernateUtil.closeSession();
	}

	/**
	 * @see org.openmrs.api.ObsService#voidObs(org.openmrs.Obs, java.lang.String)
	 */
	public void voidObs(Obs obs, String reason) throws APIException {
		obs.setVoided(true);
		obs.setVoidedBy(context.getAuthenticatedUser());
		obs.setDateVoided(new Date());
		obs.setVoidReason(reason);
		
		updateObs(obs);

		
	}

	
}
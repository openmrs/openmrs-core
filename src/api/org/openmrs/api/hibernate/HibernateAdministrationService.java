package org.openmrs.api.hibernate;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.OrderType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.RelationshipType;
import org.openmrs.Tribe;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.context.Context;

public class HibernateAdministrationService implements
		AdministrationService {

	protected final Log log = LogFactory.getLog(getClass());
	
	private Context context;
	
	public HibernateAdministrationService(Context c) {
		this.context = c;
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createEncounterType(org.openmrs.EncounterType)
	 */
	public EncounterType createEncounterType(EncounterType encounterType) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		encounterType.setCreator(context.getAuthenticatedUser());
		encounterType.setDateCreated(new Date());
		session.save(encounterType);
		
		tx.commit();
		HibernateUtil.closeSession();
		
		return encounterType;
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createFieldType(org.openmrs.FieldType)
	 */
	public FieldType createFieldType(FieldType fieldType) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		fieldType.setCreator(context.getAuthenticatedUser());
		fieldType.setDateCreated(new Date());
		session.save(fieldType);
		
		tx.commit();
		HibernateUtil.closeSession();
		
		return fieldType;
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createLocation(org.openmrs.Location)
	 */
	public Location createLocation(Location location) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		location.setCreator(context.getAuthenticatedUser());
		location.setDateCreated(new Date());
		session.save(location);
		
		tx.commit();
		HibernateUtil.closeSession();
		
		return location;
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createMimeType(org.openmrs.MimeType)
	 */
	public MimeType createMimeType(MimeType mimeType) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		//mimeType.setCreator(context.getAuthenticatedUser());
		//mimeType.setDateCreated(new Date());
		session.save(mimeType);
		
		tx.commit();
		HibernateUtil.closeSession();
		
		return mimeType;
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createOrderType(org.openmrs.OrderType)
	 */
	public OrderType createOrderType(OrderType orderType) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		orderType.setCreator(context.getAuthenticatedUser());
		orderType.setDateCreated(new Date());
		session.save(orderType);
		
		tx.commit();
		HibernateUtil.closeSession();
		
		return orderType;
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createPatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public PatientIdentifierType createPatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		patientIdentifierType.setCreator(context.getAuthenticatedUser());
		patientIdentifierType.setDateCreated(new Date());
		session.save(patientIdentifierType);
		
		tx.commit();
		HibernateUtil.closeSession();
		
		return patientIdentifierType;
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createRelationshipType(org.openmrs.RelationshipType)
	 */
	public RelationshipType createRelationshipType(RelationshipType relationshipType) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		relationshipType.setCreator(context.getAuthenticatedUser());
		relationshipType.setDateCreated(new Date());
		session.save(relationshipType);
		
		tx.commit();
		HibernateUtil.closeSession();
		
		return relationshipType;
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createTribe(org.openmrs.Tribe)
	 */
	public Tribe createTribe(Tribe tribe) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		//tribe.setCreator(context.getAuthenticatedUser());
		//tribe.setDateCreated(new Date());
		session.save(tribe);
		
		tx.commit();
		HibernateUtil.closeSession();
		
		return tribe;
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteEncounterType(org.openmrs.EncounterType)
	 */
	public void deleteEncounterType(EncounterType encounterType) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(encounterType);
		
		tx.commit();
		HibernateUtil.closeSession();
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteFieldType(org.openmrs.FieldType)
	 */
	public void deleteFieldType(FieldType fieldType) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(fieldType);
		
		tx.commit();
		HibernateUtil.closeSession();
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteLocation(org.openmrs.Location)
	 */
	public void deleteLocation(Location location) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(location);
		
		tx.commit();
		HibernateUtil.closeSession();
		
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteMimeType(org.openmrs.MimeType)
	 */
	public void deleteMimeType(MimeType mimeType) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(mimeType);
		
		tx.commit();
		HibernateUtil.closeSession();
		
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteOrderType(org.openmrs.OrderType)
	 */
	public void deleteOrderType(OrderType orderType) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(orderType);
		
		tx.commit();
		HibernateUtil.closeSession();

	}

	/**
	 * @see org.openmrs.api.AdministrationService#deletePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(patientIdentifierType);
		
		tx.commit();
		HibernateUtil.closeSession();
	
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteRelationshipType(org.openmrs.RelationshipType)
	 */
	public void deleteRelationshipType(RelationshipType relationshipType) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(relationshipType);
		
		tx.commit();
		HibernateUtil.closeSession();

	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteTribe(org.openmrs.Tribe)
	 */
	public void deleteTribe(Tribe tribe) throws APIException {
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();
		
		session.delete(tribe);
		
		tx.commit();
		HibernateUtil.closeSession();

		
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateEncounterType(org.openmrs.EncounterType)
	 */
	public void updateEncounterType(EncounterType encounterType) throws APIException {
		if (encounterType.getEncounterTypeId() == null)
			createEncounterType(encounterType);
		else {
			Session session = HibernateUtil.currentSession();
			Transaction tx = session.beginTransaction();
			
			session.saveOrUpdate(encounterType);
			
			tx.commit();
			HibernateUtil.closeSession();
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateFieldType(org.openmrs.FieldType)
	 */
	public void updateFieldType(FieldType fieldType) throws APIException {
		if (fieldType.getFieldTypeId() == null)
			createFieldType(fieldType);
		else {
			Session session = HibernateUtil.currentSession();
			Transaction tx = session.beginTransaction();
			
			session.saveOrUpdate(fieldType);
			
			tx.commit();
			HibernateUtil.closeSession();
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateLocation(org.openmrs.Location)
	 */
	public void updateLocation(Location location) throws APIException {
		if (location.getLocationId() == null)
			createLocation(location);
		else {
			Session session = HibernateUtil.currentSession();
			Transaction tx = session.beginTransaction();
			
			session.saveOrUpdate(location);
			
			tx.commit();
			HibernateUtil.closeSession();
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateMimeType(org.openmrs.MimeType)
	 */
	public void updateMimeType(MimeType mimeType) throws APIException {
		if (mimeType.getMimeTypeId() == null)
			createMimeType(mimeType);
		else {
			Session session = HibernateUtil.currentSession();
			Transaction tx = session.beginTransaction();
			
			session.saveOrUpdate(mimeType);
			
			tx.commit();
			HibernateUtil.closeSession();
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateOrderType(org.openmrs.OrderType)
	 */
	public void updateOrderType(OrderType orderType) throws APIException {
		if (orderType.getOrderTypeId() == null)
			createOrderType(orderType);
		else {
			Session session = HibernateUtil.currentSession();
			Transaction tx = session.beginTransaction();
			
			session.saveOrUpdate(orderType);
			
			tx.commit();
			HibernateUtil.closeSession();
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updatePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public void updatePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		if (patientIdentifierType.getPatientIdentifierTypeId() == null)
			createPatientIdentifierType(patientIdentifierType);
		else {
			Session session = HibernateUtil.currentSession();
			Transaction tx = session.beginTransaction();
			
			session.saveOrUpdate(patientIdentifierType);
			
			tx.commit();
			HibernateUtil.closeSession();
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateRelationshipType(org.openmrs.RelationshipType)
	 */
	public void updateRelationshipType(RelationshipType relationshipType) throws APIException {
		if (relationshipType.getRelationshipTypeId() == null)
			createRelationshipType(relationshipType);
		else {
			Session session = HibernateUtil.currentSession();
			Transaction tx = session.beginTransaction();
			
			session.saveOrUpdate(relationshipType);
			
			tx.commit();
			HibernateUtil.closeSession();
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateTribe(org.openmrs.Tribe)
	 */
	public void updateTribe(Tribe tribe) throws APIException {
		if (tribe.getTribeId() == null)
			createTribe(tribe);
		else {
			Session session = HibernateUtil.currentSession();
			Transaction tx = session.beginTransaction();
			
			session.saveOrUpdate(tribe);
			
			tx.commit();
			HibernateUtil.closeSession();
		}
	}

	
}

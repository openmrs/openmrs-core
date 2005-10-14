package org.openmrs.api.hibernate;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.OrderType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Privilege;
import org.openmrs.RelationshipType;
import org.openmrs.Role;
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
	public void createEncounterType(EncounterType encounterType) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		encounterType.setCreator(context.getAuthenticatedUser());
		encounterType.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(encounterType);
			HibernateUtil.commitTransaction();
			session.flush();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createFieldType(org.openmrs.FieldType)
	 */
	public void createFieldType(FieldType fieldType) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		fieldType.setCreator(context.getAuthenticatedUser());
		fieldType.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(fieldType);
			HibernateUtil.commitTransaction();
			session.flush();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createLocation(org.openmrs.Location)
	 */
	public void createLocation(Location location) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		location.setCreator(context.getAuthenticatedUser());
		location.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(location);
			HibernateUtil.commitTransaction();
			session.flush();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createMimeType(org.openmrs.MimeType)
	 */
	public void createMimeType(MimeType mimeType) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		//mimeType.setCreator(context.getAuthenticatedUser());
		//mimeType.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(mimeType);
			HibernateUtil.commitTransaction();
			session.flush();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createOrderType(org.openmrs.OrderType)
	 */
	public void createOrderType(OrderType orderType) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		orderType.setCreator(context.getAuthenticatedUser());
		orderType.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(orderType);
			HibernateUtil.commitTransaction();
			session.flush();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createPatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public void createPatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		patientIdentifierType.setCreator(context.getAuthenticatedUser());
		patientIdentifierType.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(patientIdentifierType);
			HibernateUtil.commitTransaction();
			session.flush();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createRelationshipType(org.openmrs.RelationshipType)
	 */
	public void createRelationshipType(RelationshipType relationshipType) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		relationshipType.setCreator(context.getAuthenticatedUser());
		relationshipType.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(relationshipType);
			HibernateUtil.commitTransaction();
			session.flush();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#createTribe(org.openmrs.Tribe)
	 */
	public void createTribe(Tribe tribe) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		//tribe.setCreator(context.getAuthenticatedUser());
		//tribe.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(tribe);
			HibernateUtil.commitTransaction();
			session.flush();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#createRole(org.openmrs.Role)
	 */
	public void createRole(Role role) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		//role.setCreator(context.getAuthenticatedUser());
		//role.setDateCreated(new Date());
		
		try {
			HibernateUtil.beginTransaction();
			session.save(role);
			HibernateUtil.commitTransaction();
			session.flush();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#createPrivilege(org.openmrs.Privilege)
	 */
	public void createPrivilege(Privilege privilege) throws APIException {
		Session session = HibernateUtil.currentSession();
		
		//privilege.setCreator(context.getAuthenticatedUser());
		//privilege.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(privilege);
			HibernateUtil.commitTransaction();
			session.flush();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e);
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteEncounterType(org.openmrs.EncounterType)
	 */
	public void deleteEncounterType(EncounterType encounterType) throws APIException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(encounterType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e.getMessage());
		}
		session.flush();
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteFieldType(org.openmrs.FieldType)
	 */
	public void deleteFieldType(FieldType fieldType) throws APIException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(fieldType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e.getMessage());
		}
		session.flush();
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteLocation(org.openmrs.Location)
	 */
	public void deleteLocation(Location location) throws APIException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(location);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e.getMessage());
		}
		session.flush();
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteMimeType(org.openmrs.MimeType)
	 */
	public void deleteMimeType(MimeType mimeType) throws APIException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(mimeType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e.getMessage());
		}
		session.flush();
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteOrderType(org.openmrs.OrderType)
	 */
	public void deleteOrderType(OrderType orderType) throws APIException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(orderType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e.getMessage());
		}
		session.flush();
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deletePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(patientIdentifierType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e.getMessage());
		}
		session.flush();
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteRelationshipType(org.openmrs.RelationshipType)
	 */
	public void deleteRelationshipType(RelationshipType relationshipType) throws APIException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(relationshipType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e.getMessage());
		}
		session.flush();
	}

	/**
	 * @see org.openmrs.api.AdministrationService#deleteTribe(org.openmrs.Tribe)
	 */
	public void deleteTribe(Tribe tribe) throws APIException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(tribe);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e.getMessage());
		}
		session.flush();
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#retireTribe(org.openmrs.Tribe)
	 */
	public void retireTribe(Tribe tribe) throws APIException {
		tribe.setRetired(true);
		updateTribe(tribe);
	}

	/**
	 * @see org.openmrs.api.AdministrationService#unretireTribe(org.openmrs.Tribe)
	 */
	public void unretireTribe(Tribe tribe) throws APIException {
		tribe.setRetired(false);
		updateTribe(tribe);
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#deleteRole(org.openmrs.Role)
	 */
	public void deleteRole(Role role) throws APIException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(role);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e.getMessage());
		}
		session.flush();
	}
	
	/**
	 * @see org.openmrs.api.AdministrationService#deletePrivilege(org.openmrs.Privilege)
	 */
	public void deletePrivilege(Privilege privilege) throws APIException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(privilege);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new APIException(e.getMessage());
		}
		session.flush();
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateEncounterType(org.openmrs.EncounterType)
	 */
	public void updateEncounterType(EncounterType encounterType) throws APIException {
		if (encounterType.getEncounterTypeId() == null)
			createEncounterType(encounterType);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(encounterType);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e.getMessage());
			}
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateFieldType(org.openmrs.FieldType)
	 */
	public void updateFieldType(FieldType fieldType) throws APIException {
		if (fieldType.getFieldTypeId() == null)
			createFieldType(fieldType);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(fieldType);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e.getMessage());
			}
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateLocation(org.openmrs.Location)
	 */
	public void updateLocation(Location location) throws APIException {
		if (location.getLocationId() == null)
			createLocation(location);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(location);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e.getMessage());
			}
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateMimeType(org.openmrs.MimeType)
	 */
	public void updateMimeType(MimeType mimeType) throws APIException {
		if (mimeType.getMimeTypeId() == null)
			createMimeType(mimeType);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(mimeType);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e.getMessage());
			}
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateOrderType(org.openmrs.OrderType)
	 */
	public void updateOrderType(OrderType orderType) throws APIException {
		if (orderType.getOrderTypeId() == null)
			createOrderType(orderType);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(orderType);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e.getMessage());
			}
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updatePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public void updatePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws APIException {
		if (patientIdentifierType.getPatientIdentifierTypeId() == null)
			createPatientIdentifierType(patientIdentifierType);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(patientIdentifierType);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e.getMessage());
			}
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateRelationshipType(org.openmrs.RelationshipType)
	 */
	public void updateRelationshipType(RelationshipType relationshipType) throws APIException {
		if (relationshipType.getRelationshipTypeId() == null)
			createRelationshipType(relationshipType);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(relationshipType);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e.getMessage());
			}
		}
	}

	/**
	 * @see org.openmrs.api.AdministrationService#updateTribe(org.openmrs.Tribe)
	 */
	public void updateTribe(Tribe tribe) throws APIException {
		if (tribe.getTribeId() == null)
			createTribe(tribe);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(tribe);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e.getMessage());
			}
		}
	}	
	
	/**
	 * @see org.openmrs.api.AdministrationService#updateRole(org.openmrs.Role)
	 */
	public void updateRole(Role role) throws APIException {
		if (role.getRole() == null)
			createRole(role);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(role);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e.getMessage());
			}
		}
	}	
	
	/**
	 * @see org.openmrs.api.AdministrationService#updatePrivilege(org.openmrs.Privilege)
	 */
	public void updatePrivilege(Privilege privilege) throws APIException {
		if (privilege.getPrivilege() == null)
			createPrivilege(privilege);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(privilege);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new APIException(e.getMessage());
			}
		}
	}	
	
}

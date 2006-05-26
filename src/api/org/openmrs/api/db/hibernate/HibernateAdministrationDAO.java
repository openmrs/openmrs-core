package org.openmrs.api.db.hibernate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptProposal;
import org.openmrs.ConceptSet;
import org.openmrs.ConceptSetDerived;
import org.openmrs.ConceptWord;
import org.openmrs.EncounterType;
import org.openmrs.FieldType;
import org.openmrs.Location;
import org.openmrs.MimeType;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.Privilege;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.Role;
import org.openmrs.Tribe;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.api.db.DAOException;
import org.openmrs.reporting.AbstractReportObject;
import org.openmrs.reporting.Report;
import org.openmrs.reporting.ReportObjectWrapper;
import org.openmrs.util.OpenmrsConstants;

public class HibernateAdministrationDAO implements
		AdministrationDAO {

	protected Log log = LogFactory.getLog(getClass());
	
	private Context context;
	private String authUserId;
	
	public HibernateAdministrationDAO() { }
	
	public HibernateAdministrationDAO(Context c) {
		this.context = c;
		if (c.isAuthenticated())
			authUserId = c.getAuthenticatedUser().getUserId().toString();
	}

	/**
	 * @see org.openmrs.api.db.AdministrationDAO#createPerson(org.openmrs.Person)
	 */
	public void createPerson(Person person) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		//person.setCreator(context.getAuthenticatedUser());
		//person.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(person);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationDAO#deletePerson(org.openmrs.Person)
	 */
	public void deletePerson(Person person) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(person);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getPerson(java.lang.Integer)
	 */
	public Person getPerson(Integer personId) throws DAOException {

		Session session = HibernateUtil.currentSession();
		
		Person person = new Person();
		person = (Person)session.get(Person.class, personId);
		
		return person;

	}
	
	public Person getPerson(Patient pat) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		/*
		Criteria crit = session.createCriteria(Person.class);
		crit = crit.createAlias("patient", "p");
		crit = crit.add(Expression.eq("p.patientId", pat.getPatientId()));
		Object o = crit.uniqueResult();
		*/
		
		
		Query query = session.createQuery("from Person p where p.patient.patientId = :patId");
		query = query.setInteger("patId", pat.getPatientId());
		Object o = query.uniqueResult();
		
		log.debug("o.class: " + o.getClass().toString());
		
		Person person = (Person)o;
		
		return person;
	}


	/**
	 * @see org.openmrs.api.db.AdministrationDAO#updatePerson(org.openmrs.Person)
	 */
	public void updatePerson(Person person) throws DAOException {
		if (person.getPersonId() == null)
			createPerson(person);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(person);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#createEncounterType(org.openmrs.EncounterType)
	 */
	public void createEncounterType(EncounterType encounterType) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		encounterType.setCreator(context.getAuthenticatedUser());
		encounterType.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(encounterType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#updateEncounterType(org.openmrs.EncounterType)
	 */
	public void updateEncounterType(EncounterType encounterType) throws DAOException {
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
				throw new DAOException(e);
			}
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteEncounterType(org.openmrs.EncounterType)
	 */
	public void deleteEncounterType(EncounterType encounterType) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(encounterType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	
	/**
	 * @see org.openmrs.api.db.AdministrationService#createFieldType(org.openmrs.FieldType)
	 */
	public void createFieldType(FieldType fieldType) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		fieldType.setCreator(context.getAuthenticatedUser());
		fieldType.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(fieldType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteFieldType(org.openmrs.FieldType)
	 */
	public void deleteFieldType(FieldType fieldType) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(fieldType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#updateFieldType(org.openmrs.FieldType)
	 */
	public void updateFieldType(FieldType fieldType) throws DAOException {
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
				throw new DAOException(e);
			}
		}
	}

	
	/**
	 * @see org.openmrs.api.db.AdministrationService#createLocation(org.openmrs.Location)
	 */
	public void createLocation(Location location) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		location.setCreator(context.getAuthenticatedUser());
		location.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(location);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#updateLocation(org.openmrs.Location)
	 */
	public void updateLocation(Location location) throws DAOException {
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
				throw new DAOException(e);
			}
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteLocation(org.openmrs.Location)
	 */
	public void deleteLocation(Location location) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(location);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	
	/**
	 * @see org.openmrs.api.db.AdministrationService#createMimeType(org.openmrs.MimeType)
	 */
	public void createMimeType(MimeType mimeType) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		//mimeType.setCreator(context.getAuthenticatedUser());
		//mimeType.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(mimeType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#updateMimeType(org.openmrs.MimeType)
	 */
	public void updateMimeType(MimeType mimeType) throws DAOException {
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
				throw new DAOException(e);
			}
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteMimeType(org.openmrs.MimeType)
	 */
	public void deleteMimeType(MimeType mimeType) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(mimeType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	
	/**
	 * @see org.openmrs.api.db.AdministrationService#createOrderType(org.openmrs.OrderType)
	 */
	public void createOrderType(OrderType orderType) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		orderType.setCreator(context.getAuthenticatedUser());
		orderType.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(orderType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#updateOrderType(org.openmrs.OrderType)
	 */
	public void updateOrderType(OrderType orderType) throws DAOException {
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
				throw new DAOException(e);
			}
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteOrderType(org.openmrs.OrderType)
	 */
	public void deleteOrderType(OrderType orderType) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(orderType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#createPatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public void createPatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		patientIdentifierType.setCreator(context.getAuthenticatedUser());
		patientIdentifierType.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(patientIdentifierType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#updatePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public void updatePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException {
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
				throw new DAOException(e);
			}
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#deletePatientIdentifierType(org.openmrs.PatientIdentifierType)
	 */
	public void deletePatientIdentifierType(PatientIdentifierType patientIdentifierType) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(patientIdentifierType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	
	/**
	 * @see org.openmrs.api.db.AdministrationService#createRelationshipType(org.openmrs.RelationshipType)
	 */
	public void createRelationshipType(RelationshipType relationshipType) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		relationshipType.setCreator(context.getAuthenticatedUser());
		relationshipType.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(relationshipType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#updateRelationshipType(org.openmrs.RelationshipType)
	 */
	public void updateRelationshipType(RelationshipType relationshipType) throws DAOException {
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
				throw new DAOException(e);
			}
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteRelationshipType(org.openmrs.RelationshipType)
	 */
	public void deleteRelationshipType(RelationshipType relationshipType) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(relationshipType);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	
	/**
	 * @see org.openmrs.api.db.AdministrationService#createTribe(org.openmrs.Tribe)
	 */
	public void createTribe(Tribe tribe) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		//tribe.setCreator(context.getAuthenticatedUser());
		//tribe.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(tribe);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#updateTribe(org.openmrs.Tribe)
	 */
	public void updateTribe(Tribe tribe) throws DAOException {
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
				throw new DAOException(e);
			}
		}
	}	

	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteTribe(org.openmrs.Tribe)
	 */
	public void deleteTribe(Tribe tribe) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(tribe);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#retireTribe(org.openmrs.Tribe)
	 */
	public void retireTribe(Tribe tribe) throws DAOException {
		tribe.setRetired(true);
		updateTribe(tribe);
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#unretireTribe(org.openmrs.Tribe)
	 */
	public void unretireTribe(Tribe tribe) throws DAOException {
		tribe.setRetired(false);
		updateTribe(tribe);
	}

	
	/**
	 * @see org.openmrs.api.db.AdministrationService#createRelationship(org.openmrs.Relationship)
	 */
	public void createRelationship(Relationship relationship) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		relationship.setCreator(context.getAuthenticatedUser());
		relationship.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(relationship);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#updateRelationship(org.openmrs.Relationship)
	 */
	public void updateRelationship(Relationship relationship) throws DAOException {
		if (relationship.getRelationshipId() == null)
			createRelationship(relationship);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(relationship);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}	

	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteRelationship(org.openmrs.Relationship)
	 */
	public void deleteRelationship(Relationship relationship) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(relationship);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#voidRelationship(org.openmrs.Relationship)
	 */
	public void voidRelationship(Relationship relationship) throws DAOException {
		relationship.setVoided(true);
		updateRelationship(relationship);
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#unvoidRelationship(org.openmrs.Relationship)
	 */
	public void unvoidRelationship(Relationship relationship) throws DAOException {
		relationship.setVoided(false);
		updateRelationship(relationship);
	}

	
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#createRole(org.openmrs.Role)
	 */
	public void createRole(Role role) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		//role.setCreator(context.getAuthenticatedUser());
		//role.setDateCreated(new Date());
		
		try {
			HibernateUtil.beginTransaction();
			session.save(role);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteRole(org.openmrs.Role)
	 */
	public void deleteRole(Role role) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(role);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#updateRole(org.openmrs.Role)
	 */
	public void updateRole(Role role) throws DAOException {
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
				throw new DAOException(e);
			}
		}
	}	
	
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#createPrivilege(org.openmrs.Privilege)
	 */
	public void createPrivilege(Privilege privilege) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		//privilege.setCreator(context.getAuthenticatedUser());
		//privilege.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(privilege);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#updatePrivilege(org.openmrs.Privilege)
	 */
	public void updatePrivilege(Privilege privilege) throws DAOException {
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
				throw new DAOException(e);
			}
		}
	}	

	/**
	 * @see org.openmrs.api.db.AdministrationService#deletePrivilege(org.openmrs.Privilege)
	 */
	public void deletePrivilege(Privilege privilege) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(privilege);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#createConceptClass(org.openmrs.ConceptClass)
	 */
	public void createConceptClass(ConceptClass cc) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		cc.setCreator(context.getAuthenticatedUser());
		cc.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(cc);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#updateConceptClass(org.openmrs.ConceptClass)
	 */
	public void updateConceptClass(ConceptClass cc) throws DAOException {
		if (cc.getConceptClassId() == null)
			createConceptClass(cc);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(cc);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}	
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteConceptClass(org.openmrs.ConceptClass)
	 */
	public void deleteConceptClass(ConceptClass cc) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(cc);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	
	/**
	 * @see org.openmrs.api.db.AdministrationService#createConceptClass(org.openmrs.ConceptClass)
	 */
	public void createConceptDatatype(ConceptDatatype cd) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		cd.setCreator(context.getAuthenticatedUser());
		cd.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(cd);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#updateConceptDatatype(org.openmrs.ConceptDatatype)
	 */
	public void updateConceptDatatype(ConceptDatatype cd) throws DAOException {
		if (cd.getConceptDatatypeId() == null)
			createConceptDatatype(cd);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(cd);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}	
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteConceptDatatype(org.openmrs.ConceptDatatype)
	 */
	public void deleteConceptDatatype(ConceptDatatype cd) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(cd);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#createReport(org.openmrs.reporting.Report)
	 */
	public void createReport(Report r) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		r.setCreator(context.getAuthenticatedUser());
		r.setDateCreated(new Date());
		try {
			HibernateUtil.beginTransaction();
			session.save(r);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	/**
	 * @see org.openmrs.api.db.AdministrationService#updateReport(org.openmrs.reporting.Report)
	 */
	public void updateReport(Report r) throws DAOException {
		if (r.getReportId() == null)
			createReport(r);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(r);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}	
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#deleteReport(org.openmrs.reporting.Report)
	 */
	public void deleteReport(Report r) throws DAOException {
		Session session = HibernateUtil.currentSession();
		try {
			HibernateUtil.beginTransaction();
			session.delete(r);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	public void updateConceptWord(Concept concept) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		// remove all old words
		deleteConceptWord(concept);
		
		// add all new words
		Collection<ConceptWord> words = ConceptWord.makeConceptWords(concept);
		log.debug("words: " + words);
		try {
			HibernateUtil.beginTransaction();
			
			for (ConceptWord word : words) {
				try {
					session.save(word);
				}
				catch (NonUniqueObjectException e) {
					ConceptWord tmp  = (ConceptWord)session.merge(word);
					session.evict(tmp);
					session.save(word);
				}
			}
			
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			log.error(e);
			throw new DAOException(e);
		}
	}

	public void deleteConceptWord(Concept concept) throws DAOException {
		
		Session session = HibernateUtil.currentSession();
		
		Criteria crit = session.createCriteria(ConceptWord.class);
		crit.add(Expression.eq("concept", concept));
		
		List<ConceptWord> words = crit.list();
		
		log.debug(authUserId + "|ConceptWord|" + words);
		
		HibernateUtil.beginTransaction();
			session.createQuery("delete from ConceptWord where concept_id = :c")
				.setInteger("c", concept.getConceptId())
				.executeUpdate();
		HibernateUtil.commitTransaction();
	}
	
	public void updateConceptWords() throws DAOException {
		Set<Concept> concepts = new HashSet<Concept>();
		concepts.addAll(context.getConceptService().getConceptsByName(""));
		for (Concept concept : concepts) {
			updateConceptWord(concept);
		}
	}
	
	public void updateConceptSetDerived(Concept concept) throws DAOException {
		Session session = HibernateUtil.currentSession();
		log.debug("Updating concept set derivisions for #" + concept.getConceptId().toString());
		
		HibernateUtil.beginTransaction();
		// deletes current concept's sets and matching parent's sets

		HibernateUtil.commitTransaction();
		
		//try {
			HibernateUtil.beginTransaction();
			
			//recursively get all parents
			List<Concept> parents = getParents(concept);
			
			// delete this concept's children and their bursted parents
			for (Concept parent : parents) {
				session.createQuery("delete from ConceptSetDerived csd where csd.concept in (select cs.concept from ConceptSet cs where cs.conceptSet = :c) and csd.conceptSet = :parent)")
						.setParameter("c", concept)
						.setParameter("parent", parent)
						.executeUpdate();
			}
			
			//set of updates to be passed to the server (unique list)
			Set<ConceptSetDerived> updates = new HashSet<ConceptSetDerived>();
			
			//add parents as sets of parents below
			ConceptSetDerived csd;
			for (Integer a = 0; a < parents.size() - 1; a++) {
				Concept set = parents.get(a);
				for (Integer b = a + 1; b < parents.size(); b++) {
					Concept conc = parents.get(b);
					csd = new ConceptSetDerived(set, conc, Double.valueOf(b.doubleValue()));
					updates.add(csd);
				}
			}
			
			//recursively add parents to children
			updates.addAll(deriveChildren(parents, concept));
			
			for (ConceptSetDerived c : updates) {
				session.saveOrUpdate(c);
			}
			
			HibernateUtil.commitTransaction();
		//}
		//catch (Exception e) {
		//	HibernateUtil.rollbackTransaction();
		//	log.error(e);
		//	throw new DAOException(e);
		//}
	}
	
	private Set<ConceptSetDerived> deriveChildren(List<Concept> parents, Concept current) {
		Set<ConceptSetDerived> updates = new HashSet<ConceptSetDerived>();
		
		ConceptSetDerived derivedSet = null;
		// make each child a direct child of each parent/grandparent
		for (ConceptSet childSet : current.getConceptSets()) {
			Concept child = childSet.getConcept();
			log.debug("Deriving child: " + child.getConceptId());
			Double sort_weight = childSet.getSortWeight();
			for (Concept parent : parents) {
				log.debug("Matching child: " + child.getConceptId() + " with parent: " + parent.getConceptId());
				derivedSet = new ConceptSetDerived(parent, child, sort_weight++);
				updates.add(derivedSet);
			}
			
			//recurse if this child is a set as well
			if (child.isSet()) {
				log.debug("Concept id: " + child.getConceptId() + " is a set");
				List<Concept> new_parents = new Vector<Concept>();
				new_parents.addAll(parents);
				new_parents.add(child);
				updates.addAll(deriveChildren(new_parents, child));
			}
		}
		
		return updates;
	}
	
	
	private List<Concept> getParents(Concept current) {
		Session session = HibernateUtil.currentSession();
		List<Concept> parents = new Vector<Concept>();
		
		if (current != null) {
			
			Query query = session.createQuery("from Concept c join c.conceptSets sets where sets.concept = ?")
									.setEntity(0, current);
			List<Concept> immed_parents = query.list();
			
			for (Concept c : immed_parents) {
				parents.addAll(getParents(c));
			}
			
			parents.add(current);
			
			if (log.isDebugEnabled()) {
				log.debug("parents found: ");
				for (Concept c : parents) {
					log.debug("id: " + c.getConceptId());
				}
			}
		}
		
		return parents;
		
	}
	
	public void updateConceptSetDerived() throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		HibernateUtil.beginTransaction();
		
		// remove all of the rows in the derived table
		session.createQuery("delete from ConceptSetDerived").executeUpdate();
		
		try {
			// remake the derived table by copying over the basic concept_set table
			session.connection().prepareStatement("insert into concept_set_derived (concept_id, concept_set, sort_weight) select cs.concept_id, cs.concept_set, cs.sort_weight from concept_set cs where not exists (select concept_id from concept_set_derived csd where csd.concept_id = cs.concept_id and csd.concept_set = cs.concept_set)").execute();
		
			// burst the concept sets -- make grandchildren direct children of grandparents
			session.connection().prepareStatement("insert into concept_set_derived (concept_id, concept_set, sort_weight) select cs1.concept_id, cs2.concept_set, cs1.sort_weight from concept_set cs1 join concept_set cs2 where cs2.concept_id = cs1.concept_set and not exists (select concept_id from concept_set_derived csd where csd.concept_id = cs1.concept_id and csd.concept_set = cs2.concept_set)").execute();
			
			// burst the concept sets -- make greatgrandchildren direct child of greatgrandparents
			session.connection().prepareStatement("insert into concept_set_derived (concept_id, concept_set, sort_weight) select cs1.concept_id, cs3.concept_set, cs1.sort_weight from concept_set cs1 join concept_set cs2 join concept_set cs3 where cs1.concept_set = cs2.concept_id and cs2.concept_set = cs3.concept_id and not exists (select concept_id from concept_set_derived csd where csd.concept_id = cs1.concept_id and csd.concept_set = cs3.concept_set)").execute();
			
			// TODO This 'algorithm' only solves three layers of children.  Ooptions for correction:
			//	1) Add a few more join statements to cover 5 layers (conceivable upper limit of layers)
			//	2) Find the deepest layer and programmaticly create the sql statements
			//	3) Run the joins on 
		}
		catch (SQLException e) {
			throw new DAOException (e);
		}
		
		HibernateUtil.commitTransaction();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#addConceptProposal(org.openmrs.ConceptProposal)
	 */
	public void createConceptProposal(ConceptProposal cp) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		try {
			HibernateUtil.beginTransaction();
			session.save(cp);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
		HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#updateConceptProposal(org.openmrs.ConceptProposal)
	 */
	public void updateConceptProposal(ConceptProposal cp) throws DAOException {
		if (cp.getConceptProposalId() == null)
			createConceptProposal(cp);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				HibernateUtil.beginTransaction();
				session.update(cp);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#mrnGeneratorLog(java.lang.String,java.lang.Integer,java.lang.Integer)
	 */
	public void mrnGeneratorLog(String site, Integer start, Integer count) {
		try {
			Session session = HibernateUtil.currentSession();
			HibernateUtil.beginTransaction();
			
			String sql = "insert into ";
			sql += OpenmrsConstants.DATABASE_BUSINESS_NAME + ".ext_mrn_log ";
			sql += "(date_generated, generated_by, site, mrn_first, mrn_count) values (?, ?, ?, ?, ?)";
			
			PreparedStatement ps = session.connection().prepareStatement(sql);
			
			ps.setTimestamp(1, new Timestamp(new Date().getTime()));
			ps.setInt(2, context.getAuthenticatedUser().getUserId());
			ps.setString(3, site);
			ps.setInt(4, start);
			ps.setInt(5, count);
			ps.execute();
			
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationService#getMRNGeneratorLog()
	 */
	public Collection getMRNGeneratorLog() {
		Collection log = new Vector<Map>();
		
		try {
			Session session = HibernateUtil.currentSession();
			
			Map<String, Object> row;
			
			String sql = "select * from ";
			sql += OpenmrsConstants.DATABASE_BUSINESS_NAME + ".ext_mrn_log ";
			sql += "order by mrn_log_id desc";
			
			PreparedStatement ps = session.connection().prepareStatement(sql);
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				row = new HashMap<String, Object>();
				row.put("date", rs.getTimestamp("date_generated"));
				row.put("user", rs.getString("generated_by"));
				row.put("site", rs.getString("site"));
				row.put("first", rs.getInt("mrn_first"));
				row.put("count", rs.getInt("mrn_count"));
				log.add(row);
			}
		}
		catch (Exception e) {
			throw new DAOException(e);
		}
		
		return log;
	}

	public void createReportObject(AbstractReportObject ro) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		ro.setCreator(context.getAuthenticatedUser());
		ro.setDateCreated(new Date());
		ro.setVoided(new Boolean(false));
		
		ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper(ro);

		try {
			HibernateUtil.beginTransaction();
			session.save(wrappedReportObject);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}

	public void updateReportObject(AbstractReportObject ro) throws DAOException {
		if (ro.getReportObjectId() == null)
			createReportObject(ro);
		else {
			try {
				Session session = HibernateUtil.currentSession();
				session.clear();
				ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper(ro);
				HibernateUtil.beginTransaction();
				session.saveOrUpdate(wrappedReportObject);
				HibernateUtil.commitTransaction();
			}
			catch (Exception e) {
				HibernateUtil.rollbackTransaction();
				throw new DAOException(e);
			}
		}
	}	
	
	public void deleteReportObject(Integer reportObjectId) throws DAOException {
		Session session = HibernateUtil.currentSession();
		
		ReportObjectWrapper wrappedReportObject = new ReportObjectWrapper();
		wrappedReportObject = (ReportObjectWrapper)session.get(ReportObjectWrapper.class, reportObjectId);
		
		try {
			HibernateUtil.beginTransaction();
			//System.out.println("\n\n\nGOING TO DELETE NOW: " + wrappedReportObject.getReportObjectId() + "\n\n\n");
			session.delete(wrappedReportObject);
			HibernateUtil.commitTransaction();
		}
		catch (Exception e) {
			HibernateUtil.rollbackTransaction();
			throw new DAOException(e);
		}
	}
}
/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.jdbc.Work;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.StringType;
import org.hibernate.type.TextType;
import org.hibernate.type.Type;
import org.openmrs.GlobalProperty;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.APIException;
import org.openmrs.api.db.AdministrationDAO;
import org.openmrs.api.db.DAOException;
import org.openmrs.util.DatabaseUtil;
import org.openmrs.util.HandlerUtil;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Hibernate specific database methods for the AdministrationService
 *
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.db.AdministrationDAO
 * @see org.openmrs.api.AdministrationService
 */
public class HibernateAdministrationDAO implements AdministrationDAO, ApplicationContextAware {
	
	private static final Logger log = LoggerFactory.getLogger(HibernateAdministrationDAO.class);
	private static final String PROPERTY = "property";
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;

	private Metadata metadata;
	
	public HibernateAdministrationDAO() {
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
	 * @see org.openmrs.api.db.AdministrationDAO#getGlobalProperty(java.lang.String)
	 */
	@Override
	public String getGlobalProperty(String propertyName) throws DAOException {
		GlobalProperty gp = getGlobalPropertyObject(propertyName);
		
		// if no gp exists, return a null value
		if (gp == null) {
			return null;
		}
		
		return gp.getPropertyValue();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getGlobalPropertyObject(java.lang.String)
	 */
	@Override
	public GlobalProperty getGlobalPropertyObject(String propertyName) {
		if (isDatabaseStringComparisonCaseSensitive()) {
			Criteria criteria = sessionFactory.getCurrentSession().createCriteria(GlobalProperty.class);
			return (GlobalProperty) criteria.add(Restrictions.eq(PROPERTY, propertyName).ignoreCase())
			        .uniqueResult();
		} else {
			return (GlobalProperty) sessionFactory.getCurrentSession().get(GlobalProperty.class, propertyName);
		}
	}
	
	@Override
	public GlobalProperty getGlobalPropertyByUuid(String uuid) throws DAOException {

		return (GlobalProperty) sessionFactory.getCurrentSession()
		        .createQuery("from GlobalProperty t where t.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getAllGlobalProperties()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<GlobalProperty> getAllGlobalProperties() throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(GlobalProperty.class);
		return criteria.addOrder(Order.asc(PROPERTY)).list();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getGlobalPropertiesByPrefix(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<GlobalProperty> getGlobalPropertiesByPrefix(String prefix) {
		return sessionFactory.getCurrentSession().createCriteria(GlobalProperty.class)
		        .add(Restrictions.ilike(PROPERTY, prefix, MatchMode.START)).list();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#getGlobalPropertiesBySuffix(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<GlobalProperty> getGlobalPropertiesBySuffix(String suffix) {
		return sessionFactory.getCurrentSession().createCriteria(GlobalProperty.class)
		        .add(Restrictions.ilike(PROPERTY, suffix, MatchMode.END)).list();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#deleteGlobalProperty(GlobalProperty)
	 */
	@Override
	public void deleteGlobalProperty(GlobalProperty property) throws DAOException {
		sessionFactory.getCurrentSession().delete(property);
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#saveGlobalProperty(org.openmrs.GlobalProperty)
	 */
	@Override
	public GlobalProperty saveGlobalProperty(GlobalProperty gp) throws DAOException {
		GlobalProperty gpObject = getGlobalPropertyObject(gp.getProperty());
		if (gpObject != null) {
			gpObject.setPropertyValue(gp.getPropertyValue());
			gpObject.setDescription(gp.getDescription());
			sessionFactory.getCurrentSession().update(gpObject);
			return gpObject;
		} else {
			sessionFactory.getCurrentSession().save(gp);
			return gp;
		}
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#executeSQL(java.lang.String, boolean)
	 */
	@Override
	public List<List<Object>> executeSQL(String sql, boolean selectOnly) throws DAOException {
		
		// (solution for junit tests that usually use hsql
		// hsql does not like the backtick.  Replace the backtick with the hsql
		// escape character: the double quote (or nothing).
		if (HibernateUtil.isHSQLDialect(sessionFactory)) {
			sql = sql.replace("`", "");
		}
		return DatabaseUtil.executeSQL(sessionFactory.getCurrentSession(), sql, selectOnly);
	}
	
	@Override
	public int getMaximumPropertyLength(Class<? extends OpenmrsObject> aClass, String fieldName) {
		PersistentClass persistentClass = metadata.getEntityBinding(aClass.getName().split("_")[0]);
		if (persistentClass == null) {
			throw new APIException("Couldn't find a class in the hibernate configuration named: " + aClass.getName());
		} else {
			int fieldLength;
			try {
				fieldLength = ((Column) persistentClass.getProperty(fieldName).getColumnIterator().next()).getLength();
			}
			catch (Exception e) {
				log.debug("Could not determine maximum length", e);
				return -1;
			}
			return fieldLength;
		}
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		HibernateSessionFactoryBean sessionFactoryBean = (HibernateSessionFactoryBean) applicationContext
		        .getBean("&sessionFactory");
		metadata = sessionFactoryBean.getMetadata();
	}
	
	/**
	 * @see org.openmrs.api.db.AdministrationDAO#validate(java.lang.Object, Errors)
	 * <strong>Should</strong> Pass validation if field lengths are correct
	 * <strong>Should</strong> Fail validation if field lengths are not correct
	 * <strong>Should</strong> Fail validation for location class if field lengths are not correct
	 * <strong>Should</strong> Pass validation for location class if field lengths are correct
	 */
	
	//@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
	@Override
	public void validate(Object object, Errors errors) throws DAOException {
		Class entityClass = object.getClass();
		ClassMetadata metadata = null;
		try {
			metadata = sessionFactory.getClassMetadata(entityClass);
		}
		catch (MappingException ex) {
			log.debug(entityClass + " is not a hibernate mapped entity", ex);
		}
		if (metadata != null) {
			String[] propNames = metadata.getPropertyNames();
			Object identifierType = metadata.getIdentifierType();
			String identifierName = metadata.getIdentifierPropertyName();
			if (identifierType instanceof StringType || identifierType instanceof TextType) {
				int maxLength = getMaximumPropertyLength(entityClass, identifierName);
				String identifierValue = (String) metadata.getIdentifier(object,
				    (SessionImplementor) sessionFactory.getCurrentSession());
				if (identifierValue != null) {
					int identifierLength = identifierValue.length();
					if (identifierLength > maxLength) {
						
						errors.rejectValue(identifierName, "error.exceededMaxLengthOfField", new Object[] { maxLength },
						    null);
					}
				}
			}
			for (String propName : propNames) {
				Type propType = metadata.getPropertyType(propName);
				if (propType instanceof StringType || propType instanceof TextType) {
					String propertyValue = (String) metadata.getPropertyValue(object, propName);
					if (propertyValue != null) {
						int maxLength = getMaximumPropertyLength(entityClass, propName);
						int propertyValueLength = propertyValue.length();
						if (propertyValueLength > maxLength) {
							errors.rejectValue(propName, "error.exceededMaxLengthOfField", new Object[] { maxLength },
									null);
						}
					}
				}
			}
		}
		FlushMode previousFlushMode = sessionFactory.getCurrentSession().getHibernateFlushMode();
		sessionFactory.getCurrentSession().setHibernateFlushMode(FlushMode.MANUAL);
		try {
			for (Validator validator : getValidators(object)) {
				validator.validate(object, errors);
			}
			
		}
		
		finally {
			sessionFactory.getCurrentSession().setHibernateFlushMode(previousFlushMode);
		}
		
	}
	
	/**
	 * Fetches all validators that are registered
	 *
	 * @param obj the object that will be validated
	 * @return list of compatible validators
	 */
	protected List<Validator> getValidators(Object obj) {
		List<Validator> matchingValidators = new ArrayList<>();

		List<Validator> validators = HandlerUtil.getHandlersForType(Validator.class, obj.getClass());
		
		for (Validator validator : validators) {
			if (validator.supports(obj.getClass())) {
				matchingValidators.add(validator);
			}
		}
		
		return matchingValidators;
	}
	
	@Override
	public boolean isDatabaseStringComparisonCaseSensitive() {
		GlobalProperty gp = (GlobalProperty) sessionFactory.getCurrentSession().get(GlobalProperty.class,
		    OpenmrsConstants.GP_CASE_SENSITIVE_DATABASE_STRING_COMPARISON);
		if (gp != null) {
			return Boolean.valueOf(gp.getPropertyValue());
		} else {
			return true;
		}
	}
	
	/**
	 * Updates PostgreSQL Sequences after core data insertion
	 * 
	 * @see org.openmrs.api.db.AdministrationDAO#updatePostgresSequence()
	 */
	@Override
	public void updatePostgresSequence() throws DAOException {
		
		if (HibernateUtil.isPostgreSQLDialect(sessionFactory)) {
			
			// All the required PostgreSQL sequences that need to be updated
			String postgresSequences = "SELECT setval('person_person_id_seq', (SELECT MAX(person_id) FROM person)+1);"
			        + "SELECT setval('person_name_person_name_id_seq', (SELECT MAX(person_name_id) FROM person_name)+1);"
			        + "SELECT setval('person_attribute_type_person_attribute_type_id_seq', (SELECT MAX(person_attribute_type_id) FROM person_attribute_type)+1);"
			        + "SELECT setval('relationship_type_relationship_type_id_seq', (SELECT MAX(relationship_type_id) FROM relationship_type)+1);"
			        + "SELECT setval('users_user_id_seq', (SELECT MAX(user_id) FROM users)+1);"
			        + "SELECT setval('care_setting_care_setting_id_seq', (SELECT MAX(care_setting_id) FROM care_setting)+1);"
			        + "SELECT setval('concept_datatype_concept_datatype_id_seq', (SELECT MAX(concept_datatype_id) FROM concept_datatype)+1);"
			        + "SELECT setval('concept_map_type_concept_map_type_id_seq', (SELECT MAX(concept_map_type_id) FROM concept_map_type)+1);"
			        + "SELECT setval('concept_stop_word_concept_stop_word_id_seq', (SELECT MAX(concept_stop_word_id) FROM concept_stop_word)+1);"
			        + "SELECT setval('concept_concept_id_seq', (SELECT MAX(concept_id) FROM concept)+1);"
			        + "SELECT setval('concept_name_concept_name_id_seq', (SELECT MAX(concept_name_id) FROM concept_name)+1);"
			        + "SELECT setval('concept_class_concept_class_id_seq', (SELECT MAX(concept_class_id) FROM concept_class)+1);"
			        + "SELECT setval('concept_reference_source_concept_source_id_seq', (SELECT MAX(concept_source_id) FROM concept_reference_source)+1);"
			        + "SELECT setval('encounter_role_encounter_role_id_seq', (SELECT MAX(encounter_role_id) FROM encounter_role)+1);"
			        + "SELECT setval('field_type_field_type_id_seq', (SELECT MAX(field_type_id) FROM field_type)+1);"
			        + "SELECT setval('hl7_source_hl7_source_id_seq', (SELECT MAX(hl7_source_id) FROM hl7_source)+1);"
			        + "SELECT setval('location_location_id_seq', (SELECT MAX(location_id) FROM location)+1);"
			        + "SELECT setval('encounter_encounter_id_seq', (SELECT MAX(encounter_id) FROM encounter)+1);"
			        + "SELECT setval('concept_description_concept_description_id_seq', (SELECT MAX(concept_description_id) FROM concept_description)+1);"
			        + "SELECT setval('conditions_condition_id_seq', (SELECT MAX(condition_id) FROM conditions)+1);"
			        + "SELECT setval('encounter_diagnosis_diagnosis_id_seq', (SELECT MAX(diagnosis_id) FROM encounter_diagnosis)+1);"
			        + "SELECT setval('diagnosis_attribute_type_diagnosis_attribute_type_id_seq', (SELECT MAX(diagnosis_attribute_type_id) FROM diagnosis_attribute_type)+1);"
			        + "SELECT setval('visit_visit_id_seq', (SELECT MAX(visit_id) FROM visit)+1);"
			        + "SELECT setval('concept_reference_term_concept_reference_term_id_seq', (SELECT MAX(concept_reference_term_id) FROM concept_reference_term)+1);"
			        + "SELECT setval('orders_order_id_seq', (SELECT MAX(order_id) FROM orders)+1);"
			        + "SELECT setval('order_group_order_group_id_seq', (SELECT MAX(order_group_id) FROM order_group)+1);"
			        + "SELECT setval('concept_reference_map_concept_map_id_seq', (SELECT MAX(concept_map_id) FROM concept_reference_map)+1);"
			        + "SELECT setval('order_group_attribute_type_order_group_attribute_type_id_seq', (SELECT MAX(order_group_attribute_type_id) FROM order_group_attribute_type)+1);"
			        + "SELECT setval('encounter_provider_encounter_provider_id_seq', (SELECT MAX(encounter_provider_id) FROM encounter_provider)+1);"
			        + "SELECT setval('provider_attribute_type_provider_attribute_type_id_seq', (SELECT MAX(provider_attribute_type_id) FROM provider_attribute_type)+1);"
			        + "SELECT setval('program_attribute_type_program_attribute_type_id_seq', (SELECT MAX(program_attribute_type_id) FROM program_attribute_type)+1);"
			        + "SELECT setval('concept_state_conversion_concept_state_conversion_id_seq', (SELECT MAX(concept_state_conversion_id) FROM concept_state_conversion)+1);"
			        + "SELECT setval('program_program_id_seq', (SELECT MAX(program_id) FROM program)+1);"
			        + "SELECT setval('concept_attribute_type_concept_attribute_type_id_seq', (SELECT MAX(concept_attribute_type_id) FROM concept_attribute_type)+1);"
			        + "SELECT setval('concept_name_tag_concept_name_tag_id_seq', (SELECT MAX(concept_name_tag_id) FROM concept_name_tag)+1);"
			        + "SELECT setval('allergy_reaction_allergy_reaction_id_seq', (SELECT MAX(allergy_reaction_id) FROM allergy_reaction)+1);"
			        + "SELECT setval('cohort_cohort_id_seq', (SELECT MAX(cohort_id) FROM cohort)+1);"
			        + "SELECT setval('cohort_member_cohort_member_id_seq', (SELECT MAX(cohort_member_id) FROM cohort_member)+1);"
			        + "SELECT setval('visit_type_visit_type_id_seq', (SELECT MAX(visit_type_id) FROM visit_type)+1);"
			        + "SELECT setval('visit_attribute_type_visit_attribute_type_id_seq', (SELECT MAX(visit_attribute_type_id) FROM visit_attribute_type)+1);"
			        + "SELECT setval('order_attribute_type_order_attribute_type_id_seq', (SELECT MAX(order_attribute_type_id) FROM order_attribute_type)+1);"
			        + "SELECT setval('medication_dispense_medication_dispense_id_seq', (SELECT MAX(medication_dispense_id) FROM medication_dispense)+1);"
			        + "SELECT setval('order_set_attribute_type_order_set_attribute_type_id_seq', (SELECT MAX(order_set_attribute_type_id) FROM order_set_attribute_type)+1);"
			        + "SELECT setval('person_address_person_address_id_seq', (SELECT MAX(person_address_id) FROM person_address)+1);"
			        + "SELECT setval('patient_identifier_patient_identifier_id_seq', (SELECT MAX(patient_identifier_id) FROM patient_identifier)+1);"
			        + "SELECT setval('relationship_relationship_id_seq', (SELECT MAX(relationship_id) FROM relationship)+1);"
			        + "SELECT setval('provider_provider_id_seq', (SELECT MAX(provider_id) FROM provider)+1);"
			        + "SELECT setval('encounter_type_encounter_type_id_seq', (SELECT MAX(encounter_type_id) FROM encounter_type)+1);"
			        + "SELECT setval('person_attribute_person_attribute_id_seq', (SELECT MAX(person_attribute_id) FROM person_attribute)+1);"
			        + "SELECT setval('allergy_allergy_id_seq', (SELECT MAX(allergy_id) FROM allergy)+1);"
			        + "SELECT setval('location_attribute_type_location_attribute_type_id_seq', (SELECT MAX(location_attribute_type_id) FROM location_attribute_type)+1);"
			        + "SELECT setval('order_frequency_order_frequency_id_seq', (SELECT MAX(order_frequency_id) FROM order_frequency)+1);"
			        + "SELECT setval('patient_program_patient_program_id_seq', (SELECT MAX(patient_program_id) FROM patient_program)+1);"
			        + "SELECT setval('form_field_form_field_id_seq', (SELECT MAX(form_field_id) FROM form_field)+1);"
			        + "SELECT setval('concept_proposal_concept_proposal_id_seq', (SELECT MAX(concept_proposal_id) FROM concept_proposal)+1);"
			        + "SELECT setval('program_workflow_program_workflow_id_seq', (SELECT MAX(program_workflow_id) FROM program_workflow)+1);"
			        + "SELECT setval('program_workflow_state_program_workflow_state_id_seq', (SELECT MAX(program_workflow_state_id) FROM program_workflow_state)+1);"
			        + "SELECT setval('patient_state_patient_state_id_seq', (SELECT MAX(patient_state_id) FROM patient_state)+1);"
			        + "SELECT setval('obs_obs_id_seq', (SELECT MAX(obs_id) FROM obs)+1);"
			        + "SELECT setval('order_type_order_type_id_seq', (SELECT MAX(order_type_id) FROM order_type)+1);"
			        + "SELECT setval('patient_identifier_type_patient_identifier_type_id_seq', (SELECT MAX(patient_identifier_type_id) FROM patient_identifier_type)+1);"
			        + "SELECT setval('scheduler_task_config_task_config_id_seq', (SELECT MAX(task_config_id) FROM scheduler_task_config)+1);"
			        + "SELECT setval('scheduler_task_config_property_task_config_property_id_seq', (SELECT MAX(task_config_property_id) FROM scheduler_task_config_property)+1)"
			        + "";
			Session session = sessionFactory.getCurrentSession();
			
			session.doWork(new Work() {
				
				@Override
				public void execute(Connection con) throws SQLException {
					Statement stmt = con.createStatement();
					stmt.addBatch(postgresSequences);
					stmt.executeBatch();
				}
			});
		}
	}
}

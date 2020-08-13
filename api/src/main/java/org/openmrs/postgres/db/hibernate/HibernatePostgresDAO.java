/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.postgres.db.hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.openmrs.api.db.CohortDAO;
import org.openmrs.postgres.db.PostgresDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

/**
 * Hibernate implementation of the PostgresDAO
 *
 * @see PostgresDAO
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.postgres.PostgresService
 */
public class HibernatePostgresDAO implements PostgresDAO {
	
	private static final Logger log = LoggerFactory.getLogger(HibernatePostgresDAO.class);
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	public HibernatePostgresDAO() {
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
	 * @see org.openmrs.postgres.db.PostgresDAO#updateSequence()
	 */
	@Override
	public void updateSequence() {
		String dialect = sessionFactory.getProperties().getOrDefault("hibernate.dialect", "").toString();
		if (StringUtils.containsIgnoreCase(dialect, "postgresql")) {
			
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
			        + "SELECT setval('order_type_order_type_id_seq', (SELECT MAX(order_type_id) FROM order_type)+1);"
			        + "SELECT setval('patient_identifier_type_patient_identifier_type_id_seq', (SELECT MAX(patient_identifier_type_id) FROM patient_identifier_type)+1);"
			        + "SELECT setval('scheduler_task_config_task_config_id_seq', (SELECT MAX(task_config_id) FROM scheduler_task_config)+1);"
			        + "SELECT setval('scheduler_task_config_property_task_config_property_id_seq', (SELECT MAX(task_config_property_id) FROM scheduler_task_config_property)+1)"
			        + "";
			Session session = sessionFactory.getCurrentSession();
			for (String postgresSequence : postgresSequences.split(";"))
				session.createNativeQuery(postgresSequence).list();
		}
	}
	
}

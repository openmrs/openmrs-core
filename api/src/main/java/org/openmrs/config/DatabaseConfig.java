/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.config;

import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories("org.openmrs.api.db")
@PropertySource("classpath:hibernate.default.properties")
public class DatabaseConfig {
	
	@Bean
	LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource, Environment env) {
		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
		entityManagerFactoryBean.setDataSource(dataSource);
		entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		entityManagerFactoryBean.setMappingResources("org/openmrs/api/db/hibernate/ActiveListItem.hbm.xml",
		    "org/openmrs/api/db/hibernate/ActiveListType.hbm.xml", "org/openmrs/api/db/hibernate/ActiveListAllergy.hbm.xml",
		    "org/openmrs/api/db/hibernate/ActiveListProblem.hbm.xml", "org/openmrs/api/db/hibernate/Concept.hbm.xml",
		    "org/openmrs/api/db/hibernate/ConceptAnswer.hbm.xml", "org/openmrs/api/db/hibernate/ConceptDescription.hbm.xml",
		    "org/openmrs/api/db/hibernate/ConceptName.hbm.xml", "org/openmrs/api/db/hibernate/ConceptNameTag.hbm.xml",
		    "org/openmrs/api/db/hibernate/ConceptClass.hbm.xml", "org/openmrs/api/db/hibernate/ConceptDatatype.hbm.xml",
		    "org/openmrs/api/db/hibernate/ConceptProposal.hbm.xml",
		    "org/openmrs/api/db/hibernate/ConceptStateConversion.hbm.xml",
		    "org/openmrs/api/db/hibernate/ConceptSet.hbm.xml", "org/openmrs/api/db/hibernate/ConceptMap.hbm.xml",
		    "org/openmrs/api/db/hibernate/ConceptStopWord.hbm.xml", "org/openmrs/api/db/hibernate/ConceptSource.hbm.xml",
		    "org/openmrs/api/db/hibernate/ConceptReferenceTerm.hbm.xml",
		    "org/openmrs/api/db/hibernate/ConceptMapType.hbm.xml",
		    "org/openmrs/api/db/hibernate/ConceptReferenceTermMap.hbm.xml", "org/openmrs/api/db/hibernate/Drug.hbm.xml",
		    "org/openmrs/api/db/hibernate/DrugIngredient.hbm.xml", "org/openmrs/api/db/hibernate/DrugReferenceMap.hbm.xml",
		    "org/openmrs/api/db/hibernate/Field.hbm.xml", "org/openmrs/api/db/hibernate/FieldAnswer.hbm.xml",
		    "org/openmrs/api/db/hibernate/FieldType.hbm.xml", "org/openmrs/api/db/hibernate/Form.hbm.xml",
		    "org/openmrs/api/db/hibernate/FormField.hbm.xml", "org/openmrs/api/db/hibernate/FormResource.hbm.xml",
		    "org/openmrs/api/db/hibernate/GlobalProperty.hbm.xml", "org/openmrs/api/db/hibernate/Obs.hbm.xml",
		    "org/openmrs/api/db/hibernate/Person.hbm.xml", "org/openmrs/api/db/hibernate/PersonAttribute.hbm.xml",
		    "org/openmrs/api/db/hibernate/PersonAttributeType.hbm.xml",
		    "org/openmrs/api/db/hibernate/PersonAddress.hbm.xml", "org/openmrs/api/db/hibernate/PersonMergeLog.hbm.xml",
		    "org/openmrs/api/db/hibernate/PersonName.hbm.xml", "org/openmrs/api/db/hibernate/User.hbm.xml",
		    "org/openmrs/api/db/hibernate/LoginCredential.hbm.xml", "org/openmrs/api/db/hibernate/Privilege.hbm.xml",
		    "org/openmrs/api/db/hibernate/Role.hbm.xml", "org/openmrs/api/db/hibernate/Patient.hbm.xml",
		    "org/openmrs/api/db/hibernate/PatientIdentifier.hbm.xml",
		    "org/openmrs/api/db/hibernate/PatientIdentifierType.hbm.xml",
		    "org/openmrs/api/db/hibernate/Relationship.hbm.xml", "org/openmrs/api/db/hibernate/RelationshipType.hbm.xml",
		    "org/openmrs/api/db/hibernate/OrderType.hbm.xml", "org/openmrs/api/db/hibernate/Order.hbm.xml",
		    "org/openmrs/api/db/hibernate/CareSetting.hbm.xml", "org/openmrs/api/db/hibernate/Location.hbm.xml",
		    "org/openmrs/api/db/hibernate/LocationTag.hbm.xml",
		    "org/openmrs/api/db/hibernate/LocationAttributeType.hbm.xml",
		    "org/openmrs/api/db/hibernate/LocationAttribute.hbm.xml", "org/openmrs/api/db/hibernate/EncounterType.hbm.xml",
		    "org/openmrs/api/db/hibernate/Encounter.hbm.xml", "org/openmrs/api/db/hibernate/EncounterProvider.hbm.xml",
		    "org/openmrs/api/db/hibernate/EncounterRole.hbm.xml", "org/openmrs/api/db/hibernate/Program.hbm.xml",
		    "org/openmrs/api/db/hibernate/ProgramWorkflow.hbm.xml",
		    "org/openmrs/api/db/hibernate/ProgramWorkflowState.hbm.xml",
		    "org/openmrs/api/db/hibernate/PatientProgram.hbm.xml", "org/openmrs/api/db/hibernate/PatientState.hbm.xml",
		    "org/openmrs/api/db/hibernate/Cohort.hbm.xml", "org/openmrs/api/db/hibernate/SerializedObject.hbm.xml",
		    "org/openmrs/api/db/hibernate/OrderFrequency.hbm.xml", "org/openmrs/hl7/db/hibernate/HL7Source.hbm.xml",
		    "org/openmrs/hl7/db/hibernate/HL7InQueue.hbm.xml", "org/openmrs/hl7/db/hibernate/HL7InArchive.hbm.xml",
		    "org/openmrs/hl7/db/hibernate/HL7InError.hbm.xml", "org/openmrs/notification/db/hibernate/Template.hbm.xml",
		    "org/openmrs/notification/db/hibernate/Alert.hbm.xml",
		    "org/openmrs/notification/db/hibernate/AlertRecipient.hbm.xml",
		    "org/openmrs/scheduler/db/hibernate/TaskDefinition.hbm.xml", "org/openmrs/api/db/hibernate/VisitType.hbm.xml",
		    "org/openmrs/api/db/hibernate/VisitAttributeType.hbm.xml",
		    "org/openmrs/api/db/hibernate/VisitAttribute.hbm.xml", "org/openmrs/api/db/hibernate/Visit.hbm.xml",
		    "org/openmrs/api/db/hibernate/Provider.hbm.xml", "org/openmrs/api/db/hibernate/ProviderAttribute.hbm.xml",
		    "org/openmrs/api/db/hibernate/ProviderAttributeType.hbm.xml",
		    "org/openmrs/api/db/hibernate/ClobDatatypeStorage.hbm.xml");
		Properties jpaProperties = new Properties();
		
		jpaProperties.put("hibernate.dialect", env.getRequiredProperty("hibernate.dialect"));
		jpaProperties.put("hibernate.show_sql", env.getRequiredProperty("hibernate.show_sql"));
		jpaProperties.put("hibernate.format_sql", env.getRequiredProperty("hibernate.format_sql"));
		jpaProperties.put("hibernate.cache.use_second_level_cache", env
		        .getRequiredProperty("hibernate.cache.use_second_level_cache"));
		jpaProperties.put("hibernate.cache.use_query_cache", env.getRequiredProperty("hibernate.cache.use_query_cache"));
		jpaProperties.put("hibernate.cache.provider_class", env.getRequiredProperty("hibernate.cache.provider_class"));
		jpaProperties.put("hibernate.hbm2ddl.auto", env.getRequiredProperty("hibernate.hbm2ddl.auto"));
		jpaProperties.put("hibernate.ejb.naming_strategy", env.getRequiredProperty("hibernate.ejb.naming_strategy"));
		
		entityManagerFactoryBean.setJpaProperties(jpaProperties);
		
		return entityManagerFactoryBean;
	}
	
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/openmrs?autoReconnect=true");

		dataSource.setUsername("root");
		dataSource.setPassword("root");

		return dataSource;
	}
	
	@Bean
	JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(entityManagerFactory);
		return transactionManager;
	}
	
}

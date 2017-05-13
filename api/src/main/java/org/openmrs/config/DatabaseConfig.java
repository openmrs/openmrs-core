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

import org.hibernate.Interceptor;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.ChainingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableJpaRepositories("org.openmrs.api.db")
public class DatabaseConfig {

    @Autowired(required = false)
    public Map<String, Interceptor> interceptors = new HashMap<String, Interceptor>();

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws IOException {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setPackagesToScan("org.openmrs");
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        entityManagerFactoryBean.setMappingResources("org/openmrs/api/db/hibernate/Allergy.hbm.xml",
                "org/openmrs/api/db/hibernate/AllergyReaction.hbm.xml",
                "org/openmrs/api/db/hibernate/Concept.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptAnswer.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptAttribute.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptAttributeType.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptDescription.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptName.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptNameTag.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptClass.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptDatatype.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptProposal.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptStateConversion.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptSet.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptMap.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptStopWord.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptSource.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptReferenceTerm.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptMapType.hbm.xml",
                "org/openmrs/api/db/hibernate/ConceptReferenceTermMap.hbm.xml",
                "org/openmrs/api/db/hibernate/Drug.hbm.xml",
                "org/openmrs/api/db/hibernate/DrugIngredient.hbm.xml",
                "org/openmrs/api/db/hibernate/DrugReferenceMap.hbm.xml",
                "org/openmrs/api/db/hibernate/Field.hbm.xml",
                "org/openmrs/api/db/hibernate/FieldAnswer.hbm.xml",
                "org/openmrs/api/db/hibernate/FieldType.hbm.xml",
                "org/openmrs/api/db/hibernate/Form.hbm.xml",
                "org/openmrs/api/db/hibernate/FormField.hbm.xml",
                "org/openmrs/api/db/hibernate/FormResource.hbm.xml",
                "org/openmrs/api/db/hibernate/GlobalProperty.hbm.xml",
                "org/openmrs/api/db/hibernate/Obs.hbm.xml",
                "org/openmrs/api/db/hibernate/Person.hbm.xml",
                "org/openmrs/api/db/hibernate/PersonAttribute.hbm.xml",
                "org/openmrs/api/db/hibernate/PersonAttributeType.hbm.xml",
                "org/openmrs/api/db/hibernate/PersonAddress.hbm.xml",
                "org/openmrs/api/db/hibernate/PersonMergeLog.hbm.xml",
                "org/openmrs/api/db/hibernate/PersonName.hbm.xml",
                "org/openmrs/api/db/hibernate/User.hbm.xml",
                "org/openmrs/api/db/hibernate/LoginCredential.hbm.xml",
                "org/openmrs/api/db/hibernate/Privilege.hbm.xml",
                "org/openmrs/api/db/hibernate/Role.hbm.xml",
                "org/openmrs/api/db/hibernate/Patient.hbm.xml",
                "org/openmrs/api/db/hibernate/PatientIdentifier.hbm.xml",
                "org/openmrs/api/db/hibernate/PatientIdentifierType.hbm.xml",
                "org/openmrs/api/db/hibernate/Relationship.hbm.xml",
                "org/openmrs/api/db/hibernate/RelationshipType.hbm.xml",
                "org/openmrs/api/db/hibernate/OrderType.hbm.xml",
                "org/openmrs/api/db/hibernate/Order.hbm.xml",
                "org/openmrs/api/db/hibernate/OrderSet.hbm.xml",
                "org/openmrs/api/db/hibernate/OrderSetMember.hbm.xml",
                "org/openmrs/api/db/hibernate/OrderGroup.hbm.xml",
                "org/openmrs/api/db/hibernate/CareSetting.hbm.xml",
                "org/openmrs/api/db/hibernate/Location.hbm.xml",
                "org/openmrs/api/db/hibernate/LocationTag.hbm.xml",
                "org/openmrs/api/db/hibernate/LocationAttributeType.hbm.xml",
                "org/openmrs/api/db/hibernate/LocationAttribute.hbm.xml",
                "org/openmrs/api/db/hibernate/EncounterType.hbm.xml",
                "org/openmrs/api/db/hibernate/Encounter.hbm.xml",
                "org/openmrs/api/db/hibernate/EncounterProvider.hbm.xml",
                "org/openmrs/api/db/hibernate/EncounterRole.hbm.xml",
                "org/openmrs/api/db/hibernate/Program.hbm.xml",
                "org/openmrs/api/db/hibernate/ProgramWorkflow.hbm.xml",
                "org/openmrs/api/db/hibernate/ProgramWorkflowState.hbm.xml",
                "org/openmrs/api/db/hibernate/PatientProgram.hbm.xml",
                "org/openmrs/api/db/hibernate/PatientState.hbm.xml",
                "org/openmrs/api/db/hibernate/Cohort.hbm.xml",
                "org/openmrs/api/db/hibernate/SerializedObject.hbm.xml",
                "org/openmrs/api/db/hibernate/OrderFrequency.hbm.xml",
                "org/openmrs/hl7/db/hibernate/HL7Source.hbm.xml",
                "org/openmrs/hl7/db/hibernate/HL7InQueue.hbm.xml",
                "org/openmrs/hl7/db/hibernate/HL7InArchive.hbm.xml",
                "org/openmrs/hl7/db/hibernate/HL7InError.hbm.xml",
//                "org/openmrs/notificati/on/db/hibernate/Template.hbm.xml",
                "org/openmrs/notification/db/hibernate/Alert.hbm.xml",
                "org/openmrs/notification/db/hibernate/AlertRecipient.hbm.xml",
                "org/openmrs/scheduler/db/hibernate/TaskDefinition.hbm.xml",
                "org/openmrs/api/db/hibernate/VisitType.hbm.xml" ,
                "org/openmrs/api/db/hibernate/VisitAttributeType.hbm.xml" ,
                "org/openmrs/api/db/hibernate/VisitAttribute.hbm.xml" ,
                "org/openmrs/api/db/hibernate/Visit.hbm.xml" ,
                "org/openmrs/api/db/hibernate/Provider.hbm.xml" ,
                "org/openmrs/api/db/hibernate/ProviderAttribute.hbm.xml" ,
                "org/openmrs/api/db/hibernate/ProviderAttributeType.hbm.xml" ,
                "org/openmrs/api/db/hibernate/ClobDatatypeStorage.hbm.xml");

        PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = patternResolver.getResources("classpath*:*.hbm.xml");
        for(Resource resource: resources){
            entityManagerFactoryBean.setMappingResources(resource.getFile().getAbsolutePath());
        }

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.ejb.interceptor", chainingInterceptor());
        jpaProperties.load(new ClassPathResource("hibernate.default.properties").getInputStream());
        jpaProperties.putAll(Context.getRuntimeProperties());
        entityManagerFactoryBean.setJpaProperties(jpaProperties);

        return entityManagerFactoryBean;
    }

    @Bean
    public DataSource dataSource() {
        Properties properties = Context.getRuntimeProperties();
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(properties.getProperty("hibernate.connection.driver_class"));
        dataSource.setUrl(properties.getProperty("hibernate.connection.url"));
        dataSource.setUsername(properties.getProperty("hibernate.connection.username"));
        dataSource.setPassword(properties.getProperty("hibernate.connection.password"));
        return dataSource;
    }

    @Bean
    ChainingInterceptor chainingInterceptor(){
        ChainingInterceptor chainingInterceptor = new ChainingInterceptor();
        List<String> keys = new ArrayList<String>(interceptors.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            chainingInterceptor.addInterceptor(interceptors.get(key));
        }
        return chainingInterceptor;
    }


}
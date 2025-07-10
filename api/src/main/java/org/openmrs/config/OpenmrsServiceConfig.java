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

import org.hibernate.SessionFactory;
import org.openmrs.aop.AuthorizationAdvice;
import org.openmrs.aop.LoggingAdvice;
import org.openmrs.aop.RequiredDataAdvice;
import org.openmrs.api.PatientService;
import org.openmrs.api.db.hibernate.HibernatePatientDAO;
import org.openmrs.api.db.hibernate.search.session.SearchSessionFactory;
import org.openmrs.api.impl.PatientServiceImpl;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.patient.impl.VerhoeffIdentifierValidator;
import org.springframework.cache.interceptor.CacheInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
public class OpenmrsServiceConfig {

	@Bean
	public PatientDAO patientDAO(SessionFactory sessionFactory, SearchSessionFactory searchSessionFactory) {
		HibernatePatientDAO dao = new HibernatePatientDAO();
		dao.setSessionFactory(sessionFactory);
		dao.setSearchSessionFactory(searchSessionFactory);
		
		return dao;
	}

	@Bean
	public PatientService patientServiceTarget(PatientDAO patientDAO) {
		Map<Class<? extends IdentifierValidator>, IdentifierValidator> validators = new LinkedHashMap<>();
		validators.put(LuhnIdentifierValidator.class, new LuhnIdentifierValidator());
		validators.put(VerhoeffIdentifierValidator.class, new VerhoeffIdentifierValidator());

		PatientServiceImpl service = new PatientServiceImpl();
		service.setPatientDAO(patientDAO);
		service.setIdentifierValidators(validators);
		
		return service;
	}

	@Bean
	public Object patientService(HibernateTransactionManager transactionManager, PatientService patientServiceTarget,
		AnnotationTransactionAttributeSource transactionAttributeSource, AuthorizationAdvice authorizationAdvice,
		RequiredDataAdvice requiredDataAdvice, LoggingAdvice loggingAdvice, CacheInterceptor cacheInterceptor) {
		TransactionProxyFactoryBean proxy = new TransactionProxyFactoryBean();
		proxy.setTransactionManager(transactionManager);
		proxy.setTarget(patientServiceTarget);
		proxy.setTransactionAttributeSource(transactionAttributeSource);
		proxy.setPreInterceptors(new Object[] { authorizationAdvice, requiredDataAdvice, loggingAdvice, cacheInterceptor });
		
		return proxy;
	}
}

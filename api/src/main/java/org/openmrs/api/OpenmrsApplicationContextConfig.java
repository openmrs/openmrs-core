/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import ca.uhn.hl7v2.app.Application;
import ca.uhn.hl7v2.app.MessageTypeRouter;
import ca.uhn.hl7v2.parser.GenericParser;
import org.hibernate.SessionFactory;
import org.openmrs.hl7.handler.ADTA28Handler;
import org.openmrs.hl7.handler.ORUR01Handler;
import org.openmrs.messagesource.impl.MutableResourceBundleMessageSource;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.BinaryDataHandler;
import org.openmrs.obs.handler.BinaryStreamHandler;
import org.openmrs.obs.handler.ImageHandler;
import org.openmrs.obs.handler.TextHandler;
import org.openmrs.patient.IdentifierValidator;
import org.openmrs.patient.impl.LuhnIdentifierValidator;
import org.openmrs.patient.impl.VerhoeffIdentifierValidator;
import org.openmrs.serialization.OpenmrsSerializer;
import org.openmrs.serialization.SimpleXStreamSerializer;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.TransactionManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Provides OpenMRS Application Context Spring Configuration.
 * 
 * It's a replacement for applicationContext-service.xml from which we gradually migrate away.
 * 
 * @see org.openmrs.aop.AOPConfig
 * @see org.openmrs.api.cache.CacheConfig
 * 
 * @since 3.0.0
 */
@Configuration
public class OpenmrsApplicationContextConfig {
	
	@Bean
	public TransactionManager transactionManager(SessionFactory sessionFactory) {
		return new HibernateTransactionManager(sessionFactory);
	}
	
	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(1);
		executor.setMaxPoolSize(20);
		executor.setQueueCapacity(1000);
		return executor;
	}
	
	@Bean
	public Map<String, ComplexObsHandler> handlers(ImageHandler imageHandler, TextHandler textHandler, 
												   BinaryDataHandler binaryDataHandler,
												   BinaryStreamHandler binaryStreamHandler) {
		Map<String, ComplexObsHandler> map = new LinkedHashMap<>();
		map.put("ImageHandler", imageHandler);
		map.put("TextHandler", textHandler);
		map.put("BinaryDataHandler", binaryDataHandler);
		map.put("BinaryStreamHandler", binaryStreamHandler);
		return map;	
	}
	
	@Bean
	public Map<Class<?>, IdentifierValidator> identifierValidators(LuhnIdentifierValidator luhnIdentifierValidator,
																   VerhoeffIdentifierValidator verhoeffIdentifierValidator) {
		Map<Class<?>, IdentifierValidator> map = new LinkedHashMap<>();
		map.put(LuhnIdentifierValidator.class, luhnIdentifierValidator);
		map.put(VerhoeffIdentifierValidator.class, verhoeffIdentifierValidator);
		return map;
	}
	
	@Bean
	public List<OpenmrsSerializer> serializerList(SimpleXStreamSerializer simpleXStreamSerializer) {
		List<OpenmrsSerializer> serializers = new ArrayList<>();
		serializers.add(simpleXStreamSerializer);
		return serializers;
	}
	
	@Bean
	public MutableResourceBundleMessageSource mutableResourceBundleMessageSource() {
		MutableResourceBundleMessageSource messageSource = new MutableResourceBundleMessageSource();
		messageSource.setBasenames("classpath:custom_messages", "classpath:messages");
		messageSource.setUseCodeAsDefaultMessage(true);
		messageSource.setCacheSeconds(5);
		messageSource.setDefaultEncoding("UTF-8");
		return messageSource;
	}

	/**
	 * Provides a PropertySourcesPlaceholderConfigurer that uses OpenmrsUtil.getApplicationDataDirectory()
	 * to resolve the runtime properties file location, ensuring the property is always set.
	 *  
	 * @return configurer
	 */
    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		String appDataDir = OpenmrsUtil.getApplicationDataDirectory();
		Properties props = new Properties();
		props.setProperty(OpenmrsConstants.KEY_OPENMRS_APPLICATION_DATA_DIRECTORY, appDataDir);
		configurer.setProperties(props);
		configurer.setLocations(new ClassPathResource("hibernate.default.properties"),
			new FileSystemResource(appDataDir + "/openmrs-runtime.properties"));
		configurer.setIgnoreResourceNotFound(true);
		configurer.setLocalOverride(true);
		return configurer;
	}

	@Bean
	public GenericParser hL7Parser() {
		return new GenericParser();
	}

	@Bean
	public MessageTypeRouter hL7Router() {
		return new MessageTypeRouter();
	}

	@Bean
	public Map<String, Application> hL7Handlers(ORUR01Handler orur01Handler, ADTA28Handler adta28Handler) {
		Map<String, Application> map = new LinkedHashMap<>();
		map.put("ORU_R01", orur01Handler);
		map.put("ADT_A28", adta28Handler);
		return map;
	}
}

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

import org.openmrs.util.OpenmrsBeanRegistrar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * Spring Java-based configuration class for initializing the core OpenMRS application context.
 * <p>
 * This configuration class bootstraps the application by:
 * </p>
 * <ul>
 *   <li>Importing a comprehensive list of Java-configured beans via {@code @Import}, including:
 *     <ul>
 *       <li>API configuration classes such as {@code OpenmrsPropertyConfig}, {@code CacheConfig}</li>
 *       <li>Interceptors like {@code DropMillisecondsHibernateInterceptor}, {@code ImmutableOrderInterceptor}</li>
 *       <li>Search engine configurations for both Lucene and Elasticsearch</li>
 *       <li>Custom datatype definitions used by the system</li>
 *       <li>Complex obs handlers (e.g., {@code BinaryDataHandler}, {@code ImageHandler})</li>
 *       <li>Utility classes and serializer definitions</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * @since 3.0.0
 */
@Configuration
@ImportResource({
	"classpath:applicationContext-service.xml",
	"classpath*:moduleApplicationContext.xml",
	"classpath*:webModuleApplicationContext.xml",
	"classpath*:openmrs-servlet.xml"
})
@Import({
	org.openmrs.api.OpenmrsPropertyConfig.class,
	org.openmrs.api.cache.CacheConfig.class,
	org.openmrs.AllergyProperties.class,
	org.openmrs.ObsPostLoadEventListener.class,
	org.openmrs.api.db.hibernate.DropMillisecondsHibernateInterceptor.class,
	org.openmrs.api.db.hibernate.ImmutableObsInterceptor.class,
	org.openmrs.api.db.hibernate.ImmutableOrderInterceptor.class,
	org.openmrs.api.db.hibernate.search.elasticsearch.ElasticsearchConfig.class,
	org.openmrs.api.db.hibernate.search.lucene.LuceneConfig.class,
	org.openmrs.api.db.hibernate.search.session.SearchSessionFactoryImpl.class,
	org.openmrs.api.storage.LocalStorageService.class,
	org.openmrs.api.stream.StreamDataService.class,
	org.openmrs.customdatatype.datatype.ConceptDatatype.class,
	org.openmrs.customdatatype.datatype.DateDatatype.class,
	org.openmrs.customdatatype.datatype.FloatDatatype.class,
	org.openmrs.customdatatype.datatype.FreeTextDatatype.class,
	org.openmrs.customdatatype.datatype.LocationDatatype.class,
	org.openmrs.customdatatype.datatype.LongFreeTextDatatype.class,
	org.openmrs.customdatatype.datatype.ProgramDatatype.class,
	org.openmrs.customdatatype.datatype.ProviderDatatype.class,
	org.openmrs.customdatatype.datatype.RegexValidatedTextDatatype.class,
	org.openmrs.customdatatype.datatype.SpecifiedTextOptionsDatatype.class,
	org.openmrs.obs.handler.BinaryDataHandler.class,
	org.openmrs.obs.handler.BinaryStreamHandler.class,
	org.openmrs.obs.handler.ImageHandler.class,
	org.openmrs.obs.handler.MediaHandler.class,
	org.openmrs.obs.handler.TextHandler.class,
	org.openmrs.serialization.SimpleXStreamSerializer.class,
	org.openmrs.util.HandlerUtil.class,
})
public class OpenmrsAppConfig {

	@Bean(name = "openmrsBeanRegistrar")
	public static OpenmrsBeanRegistrar openmrsBeanRegistrar() {
		return new OpenmrsBeanRegistrar();
	}
}

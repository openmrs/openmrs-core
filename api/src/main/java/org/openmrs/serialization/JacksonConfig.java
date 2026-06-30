/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.serialization;

import org.jspecify.annotations.NonNull;
import org.openmrs.OpenmrsObject;
import org.openmrs.util.OpenmrsJacksonLocaleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.hibernate7.Hibernate7Module;

/**
 * Default Jackson object mapper configuration.
 * <p>
 * Used for example by {@link org.openmrs.scheduler.jobrunr.JobRunrConfig} or
 * {@link org.openmrs.event.outbox.OutboxEventInterceptor}.
 *
 * @since 2.9.x
 */
@Configuration
public class JacksonConfig {

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		Hibernate7Module hibernate7Module = new Hibernate7Module();
		hibernate7Module.enable(Hibernate7Module.Feature.FORCE_LAZY_LOADING);
		hibernate7Module.enable(Hibernate7Module.Feature.REPLACE_PERSISTENT_COLLECTIONS);

		mapper.registerModule(hibernate7Module);
		mapper.registerModule(new OpenmrsJacksonLocaleModule());

		SimpleModule keyDeserializingModule = newKeyDeserializingModule();
		mapper.registerModule(keyDeserializingModule);

		// Prevent infinite recursion on bidirectional entity relationships
		mapper.addMixIn(OpenmrsObject.class, OpenmrsObjectMixIn.class);

		// Prevent type info serialization for Collections and Maps
		mapper.addMixIn(java.util.Collection.class, IgnoreTypeInfoMixIn.class);
		mapper.addMixIn(java.util.Map.class, IgnoreTypeInfoMixIn.class);

		return mapper;
	}

	/**
	 * Allows to deserialize Maps that have OpenmrsObjects as keys.
	 *
	 * @return module
	 */
	private static @NonNull SimpleModule newKeyDeserializingModule() {
		SimpleModule openmrsModule = new SimpleModule();
		openmrsModule.setKeyDeserializers(new SimpleKeyDeserializers() {

			@Override
			public KeyDeserializer findKeyDeserializer(JavaType type, DeserializationConfig config,
			        BeanDescription beanDesc) {
				if (OpenmrsObject.class.isAssignableFrom(type.getRawClass())) {
					return new KeyDeserializer() {

						@Override
						public Object deserializeKey(String key, DeserializationContext ctxt) {
							try {
								OpenmrsObject obj = (OpenmrsObject) type.getRawClass().getDeclaredConstructor()
								        .newInstance();
								obj.setUuid(key);
								return obj;
							} catch (Exception e) {
								return null;
							}
						}
					};
				}
				return null;
			}
		});
		return openmrsModule;
	}

	@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "uuid")
	public abstract static class OpenmrsObjectMixIn {}

	@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
	public abstract static class IgnoreTypeInfoMixIn {}
}

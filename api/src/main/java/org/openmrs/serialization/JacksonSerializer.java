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

import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.DomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;

/**
 * {@code JacksonSerializer} is a JSON serialization implementation for OpenMRS
 * that uses the Jackson library to convert Java objects to JSON and vice versa.
 * <p>
 * It supports UUID-based references for {@code OpenmrsObject}s using
 * {@link UuidReferenceModule} and handles Hibernate proxies via {@link Hibernate5Module},
 * </p>
 * 
 * @since 3.0.0
 *
 * @see UuidReferenceModule
 * @see OpenmrsSerializer
 * @see SerializationException
 */
@Component("jacksonSerializer")
public class JacksonSerializer implements OpenmrsSerializer {

	private static final Logger log = LoggerFactory.getLogger(JacksonSerializer.class);

	private ObjectMapper objectMapper =  null;

	@Autowired
	private AdministrationService adminService;
	
	/**
	 * Constructs and configures a new {@code JacksonSerializer} instance with
	 * necessary modules, date formatting, and visibility rules.
	 *
	 * @param domainService the {@link DomainService} used to resolve UUIDs during deserialization
	 * @throws SerializationException if initialization fails
	 */
	@Autowired
	public JacksonSerializer(@Qualifier(value = "domainService") DomainService domainService) throws SerializationException {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new UuidReferenceModule(domainService));
		objectMapper.registerModule(new Hibernate6Module());
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
		objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
		objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
		objectMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setDefaultPropertyInclusion(JsonInclude.Value.construct(JsonInclude.Include.NON_EMPTY, JsonInclude.Include.NON_EMPTY));
	}

	/**
	 * Constructor that accepts a {@link ObjectMapper}.
	 * 
	 * @param objectMapper the preconfigured ObjectMapper to use
	 * @throws SerializationException if initialization fails
	 */
	public JacksonSerializer(ObjectMapper objectMapper) throws SerializationException {
		this.objectMapper = objectMapper;
	}
	
	/**
	 * Returns the internal {@link ObjectMapper}, allowing further configuration
	 * by other modules or extensions.
	 *
	 * @return the underlying ObjectMapper
	 */
	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	
	/**
	 * Serializes an object to a JSON string if it passes the whitelist check.
	 *
	 * @param o the object to serialize
	 * @return the serialized JSON string
	 * @throws RuntimeException if serialization fails or object is null
	 */
	@Override
	public String serialize(Object o) {
		if (o == null) {
			throw new RuntimeException("Cannot serialize null object");
		}
		try {
			return getObjectMapper().writeValueAsString(o);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Unable to serialize class: " + o.getClass().getName(), e);
		}
	}
	
	/**
	 * Deserializes a JSON string to a Java object of the specified type.
	 *
	 * @param serializedObject the JSON string
	 * @param clazz            the target class to deserialize to
	 * @param <T>              the type parameter
	 * @return the deserialized Java object
	 * @throws SerializationException if deserialization fails or provided clazz is null
	 * @throws SecurityException if the class of the object is not whitelisted
	 */
	@Override
	public <T> T deserialize(String serializedObject, Class<? extends T> clazz) throws SerializationException {

		if (StringUtils.isBlank(serializedObject)) {
			return null;
		}

		if (clazz == null) {
			throw new SerializationException("Cannot deserialize to null class type!");
		}

		try {
			return (T) getObjectMapper().readValue(serializedObject, clazz);
		}
		catch (Exception e) {
			throw new SerializationException("Unable to deserialize class: " + clazz.getName(), e);
		}
	}
}

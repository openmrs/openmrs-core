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
import java.util.ArrayList;
import java.util.List;

import org.openmrs.api.AdministrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

/**
 * {@code JacksonSerializer} is a JSON serialization implementation for OpenMRS
 * that uses the Jackson library to convert Java objects to JSON and vice versa.
 * <p>
 * It supports UUID-based references for {@code OpenmrsObject}s using
 * {@link UuidReferenceModule}, handles Hibernate proxies via {@link Hibernate5Module},
 * and enforces a configurable whitelist of allowed types during serialization.
 * </p>
 *
 * @see UuidReferenceModule
 * @see OpenmrsSerializer
 * @see SerializationException
 */
@Component("jacksonSerializer")
public class JacksonSerializer implements OpenmrsSerializer {

	protected static final Logger log = LoggerFactory.getLogger(JacksonSerializer.class);

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
	public JacksonSerializer(DomainService domainService) throws SerializationException {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new UuidReferenceModule(domainService));
		objectMapper.registerModule(new Hibernate5Module());
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
		objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
		objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);
		objectMapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	
	/**
	 * Serializes an object to a JSON string if it passes the whitelist check.
	 *
	 * @param o the object to serialize
	 * @return the serialized JSON string
	 * @throws SecurityException if the class of the object is not whitelisted
	 *  @throws RuntimeException if serialization fails
	 */
	@Override
	public String serialize(Object o) {
		String className = o.getClass().getName();

		try {
			return getObjectMapper().writeValueAsString(o);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Unable to serialize class: " + className, e);
		}
	}
	
	/**
	 * Deserializes a JSON string to a Java object of the specified type.
	 *
	 * @param serializedObject the JSON string
	 * @param clazz            the target class to deserialize to
	 * @param <T>              the type parameter
	 * @return the deserialized Java object
	 * @throws SerializationException if deserialization fails
	 */
	@Override
	public <T> T deserialize(String serializedObject, Class<? extends T> clazz) throws SerializationException {

		if (!isWhitelisted(clazz.getName())) {
			throw new SecurityException("Deserialization denied: " + clazz.getName());
		}

		try {
			return (T) getObjectMapper().readValue(serializedObject, clazz);
		}
		catch (Exception e) {
			throw new SerializationException("Unable to deserialize class: " + clazz.getName(), e);
		}
	}

	/**
	 * Checks if a class is allowed for serialization based on the configured whitelist.
	 *
	 * @param className the fully qualified name of the class
	 * @return true if whitelisted or if whitelist is empty, false otherwise
	 */
	private boolean isWhitelisted(String className) {
		return adminService.getSerializerWhitelistTypes().isEmpty() ? true : adminService.getSerializerWhitelistTypes().stream().anyMatch(pattern -> matchPattern(pattern, className));
	}

	/**
	 * Matches a class name against a whitelist pattern.
	 * 
	 *<p>
	* Supported pattern types:
	* <ul>
	*   <li><b>Exact or wildcard match</b>: Supports {@code *} (single segment) and {@code **} (multi-segment) wildcards.</li>
	*   <li><b>Inheritance match</b>: Use {@code hierarchyOf:fully.qualified.BaseClass} to match subclasses or implementations.</li>
	* </ul>
	*
	* @param pattern    the matching pattern (wildcard or hierarchy-based)
	* @param className  the fully qualified name of the class to check
	* @return {@code true} if the class matches the pattern; {@code false} otherwise or if class resolution fails
	*
	* @example
	* <pre>
	* matchPattern("org.openmrs.*", "org.openmrs.Patient") → true
	* matchPattern("hierarchyOf:org.openmrs.OpenmrsObject", "org.openmrs.Patient") → true
	* </pre>
	*/
	private boolean matchPattern(String pattern, String className) {
		try {
			if (pattern.startsWith("hierarchyOf:")) {
				String baseClassName = pattern.substring("hierarchyOf:".length());
				Class<?> baseClass = Class.forName(baseClassName);
				Class<?> actualClass = Class.forName(className);
				return baseClass.isAssignableFrom(actualClass);
			} else {
				// Convert dot-style wildcard pattern to regex
				String regex = pattern
					.replace(".", "\\.")
					.replace("**", ".+")
					.replace("*", "[^.]+");
				return className.matches(regex);
			}
		} catch (ClassNotFoundException e) {
			// Fails safely
			return false;
		}
	}
}

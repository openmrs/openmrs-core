/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.module.webservices.rest.util.SimpleObjectConverter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

/**
 * This is the Map returned for all objects. The properties are just key/value pairs. If an object
 * has subobjects those are just lists of SimpleObjects
 */
@XStreamAlias("object")
@XStreamConverter(SimpleObjectConverter.class)
public class SimpleObject extends LinkedHashMap<String, Object> {
	
	private static final long serialVersionUID = 1L;
	
	public SimpleObject() {
		super();
	}
	
	public SimpleObject(int initialCapacity) {
		super(initialCapacity);
	}
	
	/**
	 * Puts a property in this map, and returns the map itself (for chained method calls)
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public SimpleObject add(String key, Object value) {
		put(key, value);
		return this;
	}
	
	/**
	 * Removes a property from the map, and returns the map itself (for chained method calls)
	 * 
	 * @param key
	 * @return
	 */
	public SimpleObject removeProperty(String key) {
		remove(key);
		return this;
	}
	
	/**
	 * Creates an instance from the given json string.
	 * 
	 * @param json
	 * @return the simpleObject
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static SimpleObject parseJson(String json) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json, SimpleObject.class);
	}
	
	/**
	 * Returns the value to which the specified key is mapped, or null if this map contains no
	 * mapping for the key.
	 * 
	 * @param key
	 * @return
	 */
	public <T> T get(String key) {
		return (T) super.get(key);
	}
	
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.webservices.rest.web.resource.api;

import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.response.ConversionException;

/**
 * Can convert from String -> T. Can convert from T -> json-friendly version of a given
 * Representation
 */
public interface Converter<T> {
	
	/**
	 * @param type user-friendly type name, if relevant for this converter (@see
	 *            DelegatingSubclassHandler)
	 * @return a new instance of the given type
	 */
	T newInstance(String type);
	
	/**
	 * @param string
	 * @return the result of converting the String input to a T
	 */
	T getByUniqueId(String string);
	
	/**
	 * @param instance
	 * @param rep
	 * @return a convertible-to-json object for instance in the given representation
	 * @throws Exception
	 */
	SimpleObject asRepresentation(T instance, Representation rep) throws ConversionException;
	
	/**
	 * @param instance
	 * @param propertyName
	 * @return
	 * @throws ConversionException
	 */
	Object getProperty(T instance, String propertyName) throws ConversionException;
	
	/**
	 * @param instance
	 * @param propertyName
	 * @param value
	 * @throws ConversionException
	 */
	void setProperty(Object instance, String propertyName, Object value) throws ConversionException;
	
}

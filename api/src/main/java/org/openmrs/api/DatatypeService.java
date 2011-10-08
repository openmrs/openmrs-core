/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api;

import java.util.List;
import java.util.Set;

import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeException;
import org.openmrs.customdatatype.CustomDatatypeHandler;

/**
 * API methods related to {@link CustomDatatype} and {@link CustomDatatypeHandler}.
 * @since 1.9
 */
public interface DatatypeService extends OpenmrsService {
	
	/**
	 * @return all datatypes registered by core code and modules
	 */
	Set<Class<? extends CustomDatatype<?>>> getAllDatatypeClasses();
	
	/**
	 * @return all handlers registered by core code and modules
	 */
	Set<Class<? extends CustomDatatypeHandler<?, ?>>> getAllHandlerClasses();
	
	/**
	 * @param clazz
	 * @param config
	 * @return an instantiated {@link CustomDatatype}, with a configuration set
	 * @throws
	 */
	<T extends CustomDatatype<?>> T getDatatype(Class<T> clazz, String config) throws CustomDatatypeException;
	
	/**
	 * Gets the default handler for a {@link CustomDatatype}, and sets its configuration
	 * TODO probably remove the config argument since it doesn't make sense to let people provide handlerConfig to the default handler. If we remove this argument, we also need to change BaseAttributeTypeValidator  
	 * 
	 * @param datatype
	 * @param handlerConfig
	 * @return
	 */
	CustomDatatypeHandler<?, ?> getHandler(CustomDatatype<?> datatype, String handlerConfig);
	
	/**
	 * @param datatypeClass
	 * @return all handlers suitable for the given {@link CustomDatatype} class
	 */
	@SuppressWarnings("rawtypes")
	List<Class<? extends CustomDatatypeHandler>> getHandlerClasses(Class<? extends CustomDatatype<?>> datatypeClass);
	
}

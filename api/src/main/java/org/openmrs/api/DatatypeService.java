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

import java.util.List;
import java.util.Set;

import org.openmrs.api.db.ClobDatatypeStorage;
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
	
	/**
	 * Gets a clob storage object by its id
	 * 
	 * @param id
	 * @return
	 */
	ClobDatatypeStorage getClobDatatypeStorage(Integer id);
	
	/**
	 * Gets a clob storage object by its uuid
	 * 
	 * @param uuid
	 * @return
	 */
	ClobDatatypeStorage getClobDatatypeStorageByUuid(String uuid);
	
	/**
	 * Creates or updates a clob storage object
	 * 
	 * @param storage
	 * @return the saved object
	 */
	ClobDatatypeStorage saveClobDatatypeStorage(ClobDatatypeStorage storage);
	
	/**
	 * Deletes a clob storage object from the database
	 * 
	 * @param storage the object to delete
	 */
	void deleteClobDatatypeStorage(ClobDatatypeStorage storage);
	
}

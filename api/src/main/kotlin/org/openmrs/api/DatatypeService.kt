/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api

import org.openmrs.api.db.ClobDatatypeStorage
import org.openmrs.customdatatype.CustomDatatype
import org.openmrs.customdatatype.CustomDatatypeException
import org.openmrs.customdatatype.CustomDatatypeHandler

/**
 * API methods related to [CustomDatatype] and [CustomDatatypeHandler].
 * @since 1.9
 */
interface DatatypeService : OpenmrsService {

    /**
     * @return all datatypes registered by core code and modules
     */
    fun getAllDatatypeClasses(): Set<Class<out CustomDatatype<*>>>

    /**
     * @return all handlers registered by core code and modules
     */
    fun getAllHandlerClasses(): Set<Class<out CustomDatatypeHandler<*, *>>>

    /**
     * @param clazz the datatype class
     * @param config the configuration string
     * @return an instantiated [CustomDatatype], with a configuration set
     * @throws CustomDatatypeException if there is an error creating the datatype
     */
    @Throws(CustomDatatypeException::class)
    fun <T : CustomDatatype<*>> getDatatype(clazz: Class<T>, config: String?): T

    /**
     * Gets the default handler for a [CustomDatatype], and sets its configuration.
     *
     * @param datatype the datatype
     * @param handlerConfig the handler configuration
     * @return default handler with the given configuration
     */
    fun getHandler(datatype: CustomDatatype<*>, handlerConfig: String?): CustomDatatypeHandler<*, *>?

    /**
     * @param datatypeClass the datatype class
     * @return all handlers suitable for the given [CustomDatatype] class
     */
    fun getHandlerClasses(datatypeClass: Class<out CustomDatatype<*>>): List<Class<out CustomDatatypeHandler<*, *>>>

    /**
     * Gets a clob storage object by its id.
     *
     * @param id the clob storage id
     * @return clob storage object
     */
    fun getClobDatatypeStorage(id: Int?): ClobDatatypeStorage?

    /**
     * Gets a clob storage object by its uuid.
     *
     * @param uuid the clob storage uuid
     * @return clob storage object
     */
    fun getClobDatatypeStorageByUuid(uuid: String): ClobDatatypeStorage?

    /**
     * Creates or updates a clob storage object.
     *
     * @param storage the storage object to save
     * @return the saved object
     */
    fun saveClobDatatypeStorage(storage: ClobDatatypeStorage): ClobDatatypeStorage

    /**
     * Deletes a clob storage object from the database.
     *
     * @param storage the object to delete
     */
    fun deleteClobDatatypeStorage(storage: ClobDatatypeStorage)
}

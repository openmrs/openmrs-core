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

import org.openmrs.api.storage.ObjectMetadata
import org.openmrs.api.stream.StreamDataWriter
import java.io.IOException
import java.io.InputStream
import java.io.UncheckedIOException
import java.util.regex.Pattern
import java.util.stream.Stream

/**
 * Storage service to persist data that does not fit well in DB.
 *
 * Data objects are stored under generated or provided unique keys. Data and keys are immutable.
 *
 * [ObjectMetadata] can be passed in save methods. It is implementation dependent on how the metadata is
 * used or persisted.
 *
 * @since 2.8.0, 2.7.5, 2.6.16, 2.5.15
 */
interface StorageService : OpenmrsService {

    companion object {
        @JvmField
        val moduleIdOrGroupPattern: Pattern = Pattern.compile("[\\w-./]+")
    }

    /**
     * Get InputStream to read data for the given key.
     *
     * @param key unique key
     * @return data
     * @throws IOException wrong key or IO error
     */
    @Throws(IOException::class)
    fun getData(key: String): InputStream

    /**
     * Get InputStream to read temporary data for the given key.
     *
     * @param key unique key
     * @return data
     * @throws IOException wrong key or IO error
     */
    @Throws(IOException::class)
    fun getTempData(key: String): InputStream

    /**
     * Returns metadata for the file associated under the given key.
     *
     * @param key the key
     * @return the metadata
     * @throws IOException wrong key or IO error
     */
    @Throws(IOException::class)
    fun getMetadata(key: String): ObjectMetadata

    /**
     * Returns keys starting with the given prefix (the order depends on the implementation).
     *
     * Remember to close the stream when done iterating.
     *
     * @param moduleIdOrGroup module id or group (allowed characters `[\w-./]+`)
     * @param keyPrefix key prefix
     * @return stream of keys
     * @throws IOException IO error
     */
    @Throws(IOException::class)
    fun getKeys(moduleIdOrGroup: String, keyPrefix: String): Stream<String>

    /**
     * Saves the given InputStream with auto-generated key (should be preferred to avoid key collision and use
     * the best key structure).
     *
     * @param inputStream data
     * @param metadata object metadata or null
     * @param moduleIdOrGroup module id or group (allowed characters `[\w-./]+`)
     * @return unique key
     * @throws IOException IO error
     */
    @Throws(IOException::class)
    fun saveData(inputStream: InputStream, metadata: ObjectMetadata?, moduleIdOrGroup: String): String

    /**
     * Saves the given InputStream as a temporary file. Temporary files are kept, if possible, in local
     * storage for fast IO. You shall assume that they are not accessible between HTTP requests and different replicas.
     *
     * @param inputStream data
     * @param metadata object metadata or null
     * @return unique key
     * @throws IOException IO error
     */
    @Throws(IOException::class)
    fun saveTempData(inputStream: InputStream, metadata: ObjectMetadata?): String

    /**
     * Saves data as a temporary file. Temporary files are kept, if possible, in local
     * storage for fast IO. You shall assume that they are not accessible between HTTP requests and different replicas.
     *
     * It uses pipes internally to avoid copying data into memory and writes directly to storage in a separate thread.
     *
     * @param writer lambda to write to stream
     * @param metadata object metadata or null
     * @return unique key
     * @throws IOException IO error
     */
    @Throws(IOException::class)
    fun saveTempData(writer: StreamDataWriter, metadata: ObjectMetadata?): String

    /**
     * Saves the given InputStream under the given key suffix.
     *
     * [saveData] with auto-generated key should be preferred to avoid key collision and use the
     * best key structure.
     *
     * @param inputStream data
     * @param metadata object metadata or null
     * @param moduleIdOrGroup module id or group (allowed characters `[\w-./]+`)
     * @param keySuffix for the unique key
     * @return unique key
     * @throws IOException if key exists or IO error
     */
    @Throws(IOException::class)
    fun saveData(inputStream: InputStream, metadata: ObjectMetadata?, moduleIdOrGroup: String, keySuffix: String): String

    /**
     * Saves the data under the given key suffix.
     *
     * [saveData] with auto-generated key should be preferred to avoid key collision and use
     * the best key structure.
     *
     * It uses pipes internally to avoid copying data into memory and writes directly to storage in a separate thread.
     *
     * @param writer lambda to write to stream
     * @param metadata object metadata or null
     * @param moduleIdOrGroup module id or group (allowed characters `[\w-./]+`)
     * @param keySuffix for the unique key
     * @return unique key
     * @throws IOException if key exists or IO error
     */
    @Throws(IOException::class)
    fun saveData(writer: StreamDataWriter, metadata: ObjectMetadata?, moduleIdOrGroup: String, keySuffix: String): String

    /**
     * Saves the data with auto-generated key (should be preferred to avoid key collision and use the best key
     * structure).
     *
     * It uses pipes internally to avoid copying data into memory and writes directly to storage in a separate thread.
     *
     * @param writer lambda to write to stream
     * @param metadata object metadata or null
     * @param moduleIdOrGroup module id or group (allowed characters `[\w-./]+`)
     * @return unique key
     * @throws IOException if key exists or IO error
     */
    @Throws(IOException::class)
    fun saveData(writer: StreamDataWriter, metadata: ObjectMetadata?, moduleIdOrGroup: String): String

    /**
     * Marks data for deletion. The key may be freed up after some period.
     *
     * @param key unique key
     * @return true if marked for deletion, false if not exists
     * @throws IOException IO error
     */
    @Throws(IOException::class)
    fun purgeData(key: String): Boolean

    /**
     * Returns true if object with the given key exists.
     *
     * @param key unique key
     * @return true if exists
     * @throws UncheckedIOException IO error
     */
    @Throws(UncheckedIOException::class)
    fun exists(key: String): Boolean
}

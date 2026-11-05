/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.obs;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.openmrs.api.StorageService;
import org.openmrs.api.storage.DataWithMetadata;
import org.openmrs.api.storage.ObjectMetadata;
import org.openmrs.api.stream.StreamDataWriter;

/**
 * Delegating {@link StorageService} that counts the calls complex obs handlers make, so tests can
 * assert that reading an obs does not perform redundant storage operations.
 */
class CountingStorageService implements StorageService {

	private final StorageService delegate;

	int getDataCalls;

	int getDataWithMetadataCalls;

	int getMetadataCalls;

	int existsCalls;

	CountingStorageService(StorageService delegate) {
		this.delegate = delegate;
	}

	void reset() {
		getDataCalls = 0;
		getDataWithMetadataCalls = 0;
		getMetadataCalls = 0;
		existsCalls = 0;
	}

	@Override
	public InputStream getData(String key) throws IOException {
		getDataCalls++;
		return delegate.getData(key);
	}

	@Override
	public InputStream getTempData(String key) throws IOException {
		return delegate.getTempData(key);
	}

	@Override
	public DataWithMetadata getDataWithMetadata(String key) throws IOException {
		getDataWithMetadataCalls++;
		return delegate.getDataWithMetadata(key);
	}

	@Override
	public ObjectMetadata getMetadata(String key) throws IOException {
		getMetadataCalls++;
		return delegate.getMetadata(key);
	}

	@Override
	public Stream<String> getKeys(String moduleIdOrGroup, String keyPrefix) throws IOException {
		return delegate.getKeys(moduleIdOrGroup, keyPrefix);
	}

	@Override
	public String saveData(InputStream inputStream, ObjectMetadata metadata, String moduleIdOrGroup) throws IOException {
		return delegate.saveData(inputStream, metadata, moduleIdOrGroup);
	}

	@Override
	public String saveTempData(InputStream inputStream, ObjectMetadata metadata) throws IOException {
		return delegate.saveTempData(inputStream, metadata);
	}

	@Override
	public String saveTempData(StreamDataWriter writer, ObjectMetadata metadata) throws IOException {
		return delegate.saveTempData(writer, metadata);
	}

	@Override
	public String saveData(InputStream inputStream, ObjectMetadata metadata, String moduleIdOrGroup, String keySuffix)
	        throws IOException {
		return delegate.saveData(inputStream, metadata, moduleIdOrGroup, keySuffix);
	}

	@Override
	public String saveData(StreamDataWriter writer, ObjectMetadata metadata, String moduleIdOrGroup, String keySuffix)
	        throws IOException {
		return delegate.saveData(writer, metadata, moduleIdOrGroup, keySuffix);
	}

	@Override
	public String saveData(StreamDataWriter writer, ObjectMetadata metadata, String moduleIdOrGroup) throws IOException {
		return delegate.saveData(writer, metadata, moduleIdOrGroup);
	}

	@Override
	public boolean purgeData(String key) throws IOException {
		return delegate.purgeData(key);
	}

	@Override
	public boolean exists(String key) {
		existsCalls++;
		return delegate.exists(key);
	}

	@Override
	public void onShutdown() {
		delegate.onShutdown();
	}

	@Override
	public void onStartup() {
		delegate.onStartup();
	}
}

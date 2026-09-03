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
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.StorageService;
import org.openmrs.api.storage.DataWithMetadata;
import org.openmrs.api.storage.ObjectMetadata;
import org.openmrs.api.stream.StreamDataWriter;
import org.openmrs.obs.handler.AbstractHandler;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AbstractHandlerTest extends BaseContextSensitiveTest {

	private final String FILENAME = "mytxtfile.txt";

	private AbstractHandler handler;

	@Autowired
	private AdministrationService adminService;

	@Autowired
	private StorageService storageService;

	private static class CountingStorageService implements StorageService {

		private final StorageService delegate;

		int getDataWithMetadataCalls;

		int getMetadataCalls;

		int existsCalls;

		CountingStorageService(StorageService delegate) {
			this.delegate = delegate;
		}

		@Override
		public InputStream getData(String key) throws IOException {
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

	@BeforeEach
	public void initializeContext() throws APIException {
		handler = new AbstractHandler(adminService, storageService);

		adminService.saveGlobalProperty(new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR, "obs"));
	}

	@Test
	public void saveObs_shouldNeverOverwritePreviousFiles() {
		String content1 = "A";
		String content2 = "B";

		for (int i = 0; i <= 101; i++) {
			String currentData = (i % 2 == 0) ? content1 : content2;

			ComplexData complexData = new ComplexData(FILENAME, currentData.getBytes(StandardCharsets.UTF_8));

			Obs obs = new Obs();
			obs.setComplexData(complexData);

			handler.saveObs(obs);

			Obs fetchedObs = handler.getObs(obs, null);

			assertEquals(currentData, new String((byte[]) fetchedObs.getComplexData().getData()));
		}
	}

	@Test
	public void saveObs_shouldPreserveTitleWithoutExtension() {
		ComplexData complexDataWithTitle = new ComplexData(FILENAME, "A".getBytes(StandardCharsets.UTF_8));

		Obs obsWithTitle = new Obs();
		obsWithTitle.setComplexData(complexDataWithTitle);

		handler.saveObs(obsWithTitle);

		String[] nameWithTitle = obsWithTitle.getValueComplex().split("_|\\.");

		String titlePart = nameWithTitle[0];

		assertEquals(titlePart, FilenameUtils.removeExtension(FILENAME));
	}

	@Test
	public void saveObs_shouldCorrectlySaveFileWithoutTitle() {
		ComplexData complexDataWithNullTitle = new ComplexData(null, "test".getBytes(StandardCharsets.UTF_8));

		Obs obsWithNullTitle = new Obs();
		obsWithNullTitle.setComplexData(complexDataWithNullTitle);

		handler.saveObs(obsWithNullTitle);

		String[] nameWithNullTitle = obsWithNullTitle.getValueComplex().split("\\|");

		String filename = nameWithNullTitle[0];
		String key = nameWithNullTitle[1];

		assertEquals(filename, key);
	}

	@Test
	public void getObs_shouldNotIssueRedundantStorageCallsForLocalStorage() throws APIException {
		CountingStorageService counting = new CountingStorageService(storageService);
		AbstractHandler countingHandler = new AbstractHandler(adminService, counting);

		ComplexData complexData = new ComplexData(FILENAME, "test".getBytes(StandardCharsets.UTF_8));
		Obs obs = new Obs();
		obs.setComplexData(complexData);
		countingHandler.saveObs(obs);

		Obs fetched = countingHandler.getObs(obs, null);

		assertEquals("test", new String((byte[]) fetched.getComplexData().getData()));
		assertEquals(1, counting.getDataWithMetadataCalls, "data+metadata must be fetched together");
		assertEquals(0, counting.getMetadataCalls, "no separate metadata request when data provides it");
		assertEquals(0, counting.existsCalls, "no existence probe when resolving key layout");
		assertTrue(fetched.getComplexData().getLength() > 0, "metadata length should be populated");
	}

}

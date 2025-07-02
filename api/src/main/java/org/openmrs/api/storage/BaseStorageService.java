/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.openmrs.api.StorageService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.api.stream.StreamDataService;
import org.openmrs.api.stream.StreamDataWriter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements temporary storage.
 * 
 * @since 2.8.0, 2.7.5, 2.6.16, 2.5.15
 */
public abstract class BaseStorageService extends BaseOpenmrsService implements StorageService {
	private final StreamDataService streamService;
	
	private final Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
	
	public BaseStorageService(@Autowired StreamDataService streamService) {
		this.streamService = streamService;
	}

	@Override
	public InputStream getTempData(String key) throws IOException {
		Path tempFile = tempDir.resolve(key);
		if (!Files.exists(tempFile)) {
			throw new IOException("Temp file does not exist: " + key);
		}
		return Files.newInputStream(tempFile);
	}

	@Override
	public String saveData(InputStream inputStream, ObjectMetadata metadata, String moduleIdOrGroup) 
		throws IOException {
		return saveData(inputStream, metadata, moduleIdOrGroup, null);
	}
	

	public String saveData(StreamDataWriter dataWriter, ObjectMetadata metadata, String moduleIdOrGroup) 
		throws IOException {
		return saveData(dataWriter, metadata, moduleIdOrGroup, null);
	}

	public String saveData(StreamDataWriter dataWriter, ObjectMetadata metadata, String moduleIdOrGroup,
						   String keySuffix) throws IOException {
		return saveData(streamService.streamData(dataWriter, metadata != null ? metadata.getLength() : null), metadata, 
			moduleIdOrGroup, keySuffix);
	}

	public String saveTempData(InputStream inputStream, ObjectMetadata metadata) throws IOException {
		Path tempFile = Files.createTempFile("openmrs-temp", ".tmp");
		Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
		tempFile.toFile().deleteOnExit();
		return tempFile.getFileName().toString();
	}

	public String saveTempData(StreamDataWriter writer, ObjectMetadata metadata) throws IOException {
		return saveTempData(streamService.streamData(writer, metadata != null ? metadata.getLength() : null), metadata);
	}
}

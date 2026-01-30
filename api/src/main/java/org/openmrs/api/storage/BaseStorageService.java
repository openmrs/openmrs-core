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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.RandomStringUtils;
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

	private final DateTimeFormatter keyDateTimeFormat = DateTimeFormatter.ofPattern("yyyy/MM-dd/yyyy-MM-dd-HH-mm-ss-SSS-");
	
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

	protected String newKey(String moduleIdOrGroup, String keySuffix, String filename) {
		if (keySuffix == null) {
			keySuffix = LocalDateTime.now().format(keyDateTimeFormat) + RandomStringUtils.insecure().nextAlphanumeric(8);
		}
		if (filename != null) {
			keySuffix += '-' + filename.replace(File.separator, "");
		}
		
		if (moduleIdOrGroup == null) {
			return keySuffix;
		} else {
			if (!moduleIdOrGroupPattern.matcher(moduleIdOrGroup).matches()) {
				throw new IllegalArgumentException("moduleIdOrGroup '" + moduleIdOrGroup + "' does not match [\\w-./]+");
			}
			return moduleIdOrGroup + '/' + keySuffix;
		}
	}

	protected String decodeKey(String key) {
		try {
			return URLDecoder.decode(key, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	protected String encodeKey(String key) {
		try {
			return URLEncoder.encode(key, "UTF-8").replace(".", "%2E")
				.replace("*", "%2A").replace("%2F", "/");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}

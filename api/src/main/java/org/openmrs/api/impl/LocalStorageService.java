/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.openmrs.api.StorageService;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Value;

/**
 * Used to persist data in a local file system or volumes.
 * <p>
 * It is the default implementation of StorageService.
 */
public class LocalStorageService extends BaseOpenmrsService implements StorageService {
	
	private final String storageDir;
	
	private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM-dd/HH-mm/yyyy-MM-dd-HH-mm-ss-SSS-");
	
	public LocalStorageService() {
		this.storageDir = Paths.get(OpenmrsUtil.getApplicationDataDirectory(), "storage").toString();
	}
	
	public LocalStorageService(@Value("${STORAGE_DIR}") String storageDir) {
		this.storageDir = storageDir;
	}
	
	@Override
	public InputStream getData(final String key) throws IOException {
		return Files.newInputStream(getPath(key));
	}
	
	public Path getPath(final String key) {
		Path path = Paths.get(key);
		if (path.isAbsolute()) {
			return path;
		} else {
			return Paths.get(storageDir, key);
		}
	}
	
	@Override
	public Stream<String> getKeys(final String moduleId, final String prefix) throws IOException {
		List<String> dirs = new ArrayList<>();
		final Path storagePath;
		if (moduleId != null) {
			dirs.add("modules");
			dirs.add(moduleId);
			storagePath = Paths.get(storageDir, "modules", moduleId);
		} else {
			storagePath = Paths.get(storageDir);
		}
		
		Path pathPrefix = Paths.get(prefix);
		final Path parent;
		final String filename;
		if (prefix.endsWith("/") && Files.isDirectory(storagePath.resolve(pathPrefix))) {
			parent = pathPrefix;
			filename = "";
		} else {
			parent = pathPrefix.getParent();
			filename = pathPrefix.getFileName().toString();
		}
		
		if (parent != null) {
			dirs.add(parent.toString());
		}
		@SuppressWarnings("resource")
		Stream<Path> stream = Files.list(Paths.get(storageDir, dirs.toArray(new String[0])));
		// Filter out files that start with dot (hidden files)
		return stream.filter(
		    path -> path.getFileName().toString().startsWith(filename) && !path.getFileName().toString().startsWith("."))
		        .map(path -> storagePath.relativize(path) + ((Files.isDirectory(path)) ? File.separator : ""));
	}
	
	@Override
	public String saveData(InputStream inputStream, String moduleId) throws IOException {
		Path key = newKey(moduleId, null);
		Path path = newPath(key);
		Files.copy(inputStream, path);
		return key.toString();
	}
	
	private Path newPath(Path key) throws IOException {
		Path newPath = Paths.get(storageDir).resolve(key);
		Files.createDirectories(newPath.getParent());
		
		return newPath;
	}
	
	private Path newKey(String moduleId, String keySuffix) {
		if (keySuffix == null) {
			Date date = new Date();
			keySuffix = dateFormat.format(date) + UUID.randomUUID().toString().substring(0, 8);
		}
		
		if (moduleId == null) {
			return Paths.get(keySuffix);
		} else {
			return Paths.get("modules", moduleId, keySuffix);
		}
	}
	
	@Override
	public String saveTempData(InputStream inputStream) throws IOException {
		Path tempFile = Files.createTempFile("openmrs-temp", ".tmp");
		Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
		tempFile.toFile().deleteOnExit();
		return tempFile.toString();
	}
	
	@Override
	public String saveData(InputStream inputStream, String moduleId, String keySuffix) throws IOException {
		Path key = newKey(moduleId, keySuffix);
		Path target = newPath(key);
		Files.copy(inputStream, target);
		return key.toString();
	}
	
	@Override
	public boolean purgeData(String key) throws IOException {
		try {
			return Files.deleteIfExists(getPath(key));
		}
		catch (IOException e) {
			File file = getPath(key).toFile();
			file.deleteOnExit();
			return file.exists();
		}
	}
	
	@Override
	public boolean exists(String key) {
		return Files.exists(getPath(key));
	}
}

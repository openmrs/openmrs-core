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

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.openmrs.api.StorageService;
import org.openmrs.api.stream.StreamDataService;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

/**
 * Used to persist data in a local file system or volumes.
 * <p>
 * It is the default implementation of StorageService.
 * 
 * @since 2.8.0, 2.7.2, 2.6.16, 2.5.15
 */
@Service
@Conditional(StorageServiceCondition.class)
@Qualifier("local")
public class LocalStorageService extends BaseStorageService implements StorageService {
	
	private final Path storageDir;
	
	private final DateTimeFormatter keyDateTimeFormat = DateTimeFormatter.ofPattern("yyyy/MM-dd/HH-mm/yyyy-MM-dd-HH-mm-ss-SSS-");
	
	private final MimetypesFileTypeMap mimetypes = new MimetypesFileTypeMap();
	
	public LocalStorageService(@Value("${storage_dir}") String storageDir, @Autowired StreamDataService streamService) {
		super(streamService);
		this.storageDir = "${storage_dir}".equals(storageDir) ? Paths.get(OpenmrsUtil.getApplicationDataDirectory(), 
			"storage").toAbsolutePath() : Paths.get(storageDir).toAbsolutePath();
	}
	
	@Override
	public InputStream getData(final String key) throws IOException {
		return Files.newInputStream(getPath(key));
	}

	/**
	 * It needs to be evaluated each time as it changes over time in tests...
	 * 
	 * @return the legacy storage dir
	 * @deprecated since 2.8.0
	 */
	@Deprecated
	private Path getLegacyStorageDir() {
		return Paths.get(OpenmrsUtil.getApplicationDataDirectory());
	}

	@Override
	public ObjectMetadata getMetadata(final String key) throws IOException {
		Path path = getPath(key);

		BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
		String fileName = decodeKey(path.getFileName().toString());
		
		return ObjectMetadata.builder()
				.setLength(attributes.size())
				.setMimeType(mimetypes.getContentType(fileName))
				.setFilename(fileName)
				.setCreationTime(attributes.creationTime().toInstant()).build();
	}

	Path getPath(String key) {
		key = key.replace('/', File.separatorChar); //MS Windows support
		Path legacyStorageDir = getLegacyStorageDir();
		Path legacyPath = legacyStorageDir.resolve(key);
		if (Files.exists(legacyPath)) {
			if (!legacyPath.normalize().startsWith(legacyStorageDir)) {
				throw new IllegalArgumentException("Key must not point outside legacy storage dir. Wrong key: " + key);
			}
			return legacyPath;
		} else {
			Path path = storageDir.resolve(encodeKey(key));
			assertKeyInStorageDir(path, key);
			return path;
		}
	}
	
	@Override
	public Stream<String> getKeys(final String moduleIdOrGroup, final String keyPrefix) throws IOException {
		List<String> dirs = new ArrayList<>();
		final Path storagePath;
		if (moduleIdOrGroup != null) {
			dirs.add(moduleIdOrGroup);
			storagePath = storageDir.resolve(moduleIdOrGroup);
		} else {
			storagePath = storageDir;
		}
		
		String encodedPrefix = encodeKey(keyPrefix);
		encodedPrefix = encodedPrefix.replace('/', File.separatorChar);
		
		Path pathPrefix = Paths.get(encodedPrefix);
		final Path parent;
		final String filename;
		if (encodedPrefix.endsWith(File.separator) && Files.isDirectory(storagePath.resolve(pathPrefix))) {
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
		Stream<Path> stream = Files.list(Paths.get(storageDir.toString(), dirs.toArray(new String[0])));
		// Filter out files that start with dot (hidden files)
		return stream.filter(
		    path -> path.getFileName().toString().startsWith(filename) && !path.getFileName().toString().startsWith("."))
		        .map(path -> {
					String key = storagePath.relativize(path).toString();
					key = decodeKey(key);
					key += (Files.isDirectory(path)) ? File.separator : "";
					key = key.replace(File.separatorChar, '/'); //MS Windows support
					return key;
				});
	}

	static String decodeKey(String key) {
		try {
			return URLDecoder.decode(key, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	static String encodeKey(String key) {
		try {
			return URLEncoder.encode(key, "UTF-8").replace(".", "%2E")
				.replace("*", "%2A").replace("%2F", "/");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	Path newPath(String key) throws IOException {
		key = encodeKey(key);
		key = key.replace('/', File.separatorChar);
		Path newPath = storageDir.resolve(key);
		assertKeyInStorageDir(newPath, key);
		
		Files.createDirectories(newPath.getParent());
		
		return newPath;
	}
	
	void assertKeyInStorageDir(Path path, String key) {
		if (!path.normalize().startsWith(storageDir)) {
			throw new IllegalArgumentException("Key must not point outside storage dir. Wrong key: " + key);
		}
	}
	
	String newKey(String moduleIdOrGroup, String keySuffix) {
		if (keySuffix == null) {
			keySuffix = LocalDateTime.now().format(keyDateTimeFormat) + RandomStringUtils.insecure().nextAlphanumeric(8);
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
	
	@Override
	public String saveData(InputStream inputStream, ObjectMetadata metadata, String moduleIdOrGroup, String keySuffix) throws IOException {
		String key = newKey(moduleIdOrGroup, keySuffix);
		Path target = newPath(key);
		Files.copy(inputStream, target);
		return key;
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

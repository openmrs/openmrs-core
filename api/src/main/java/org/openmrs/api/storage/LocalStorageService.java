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
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import jakarta.activation.MimetypesFileTypeMap;

import org.apache.commons.io.function.IOFunction;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.StorageService;
import org.openmrs.api.stream.StreamDataService;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @since 2.8.0, 2.7.5, 2.6.16, 2.5.15
 */
@Service
@Conditional(StorageServiceCondition.class)
@Qualifier("local")
public class LocalStorageService extends BaseStorageService implements StorageService {

	protected static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

	private final Path storageDir;

	private final MimetypesFileTypeMap mimetypes = new MimetypesFileTypeMap();

	public LocalStorageService(@Value("${storage.local.dir:}") String storageDir,
	    @Autowired StreamDataService streamService) {
		super(streamService);
		this.storageDir = StringUtils.isBlank(storageDir)
		        ? Paths.get(OpenmrsUtil.getApplicationDataDirectory(), "storage").toAbsolutePath()
		        : Paths.get(storageDir).toAbsolutePath();
	}

	@Override
	public DataWithMetadata getDataWithMetadata(String key) throws IOException {
		// Note: the metadata must be read before the data stream is opened and this ordering must be
		// maintained. If the reads were swapped and the metadata lookup failed, the already opened
		// stream would never be closed.
		return read(key, path -> {
			ObjectMetadata metadata = getMetadataInternal(path);
			return new DataWithMetadata(getDataInternal(path), metadata);
		});
	}

	@Override
	public InputStream getData(final String key) throws IOException {
		return read(key, this::getDataInternal);
	}

	/**
	 * Reads from the current storage location, falling back to the legacy location only if the file is
	 * not found there.
	 * <p>
	 * The current location is read rather than probed, so a hit costs no extra
	 * <code>Files.exists</code> call compared to reading from a single location. Only a miss pays for a
	 * probe of the legacy location.
	 *
	 * @param key the storage key
	 * @param reader the read to perform against the resolved path
	 * @return the value read by the given function
	 * @throws IOException if the file cannot be read from either location
	 * @throws IllegalArgumentException if the key points outside both storage locations
	 */
	private <T> T read(String key, IOFunction<Path, T> reader) throws IOException {
		Path path = storageDir.resolve(encodeKey(key));
		if (isInStorageDir(path)) {
			try {
				return reader.apply(path);
			} catch (NoSuchFileException e) {
				// The file is not in the current location, so fall back to the legacy location below.
			}
		}

		Path legacyPath = getLegacyStorageDir().resolve(key);
		if (fileExists(legacyPath)) {
			assertKeyInLegacyStorageDir(legacyPath, key);
			return reader.apply(legacyPath);
		}

		if (!isInStorageDir(path)) {
			throw new IllegalArgumentException("Key must not point outside storage dir. Wrong key: " + key);
		}
		throw new NoSuchFileException(path.toString(), null, "No such file or directory");
	}

	private InputStream getDataInternal(Path path) throws IOException {
		return Files.newInputStream(path);
	}

	/**
	 * It needs to be evaluated each time as it changes over time in tests...
	 * <p>
	 * It's only added to support legacy storage location, which will be removed in some later version.
	 *
	 * @return the legacy storage dir
	 */
	private Path getLegacyStorageDir() {
		return Paths.get(OpenmrsUtil.getApplicationDataDirectory());
	}

	@Override
	public ObjectMetadata getMetadata(final String key) throws IOException {
		return read(key, this::getMetadataInternal);
	}

	private ObjectMetadata getMetadataInternal(Path path) throws IOException {
		BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
		String filename = decodeKey(path.getFileName().toString());
		return ObjectMetadata.builder().setLength(attributes.size()).setMimeType(mimetypes.getContentType(filename))
		        .setFilename(filename).setCreationTime(attributes.creationTime().toInstant()).build();
	}

	/**
	 * Resolves the path a key is stored at, preferring the legacy location over the current one.
	 * <p>
	 * This is only used for deleting data, where the legacy location must win so that data saved before
	 * the current storage layout was introduced is still removed. Reads go through
	 * {@link #read(String, IOFunction)} instead, which avoids the extra existence check this requires.
	 *
	 * @param key the storage key
	 * @return the path the key is stored at
	 * @see #read(String, IOFunction)
	 */
	Path getPath(String key) {
		Path legacyStorageDir = getLegacyStorageDir();
		Path legacyPath = legacyStorageDir.resolve(key);
		if (fileExists(legacyPath)) {
			assertKeyInLegacyStorageDir(legacyPath, key);
			return legacyPath;
		} else {
			Path path = storageDir.resolve(encodeKey(key));
			assertKeyInStorageDir(path, key);
			return path;
		}
	}

	@Override
	public Stream<String> getKeys(final String moduleIdOrGroup, final String keyPrefix) throws IOException {
		String key = encodeKey(newKey(moduleIdOrGroup, keyPrefix, null));

		int lastDirIndex = key.lastIndexOf("/");
		String lastDir = "";
		if (lastDirIndex != -1) {
			lastDir = key.substring(0, lastDirIndex + 1);
		}

		Path searchDir = storageDir.resolve(lastDir);

		if (!searchDir.toFile().isDirectory()) {
			return Stream.empty();
		}

		@SuppressWarnings("resource")
		Stream<Path> stream = Files.list(searchDir);
		// Filter out files that start with dot (hidden files)
		return stream.filter(path -> !path.getFileName().toString().startsWith(".")).map(path -> {
			String foundKey = storageDir.relativize(path).toString();
			foundKey = decodeKey(foundKey);
			foundKey += (Files.isDirectory(path)) ? File.separator : "";
			foundKey = foundKey.replace(File.separatorChar, '/'); //MS Windows support
			return foundKey;
		}).filter(foundKey -> foundKey.startsWith(key));
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
		if (!isInStorageDir(path)) {
			throw new IllegalArgumentException("Key must not point outside storage dir. Wrong key: " + key);
		}
	}

	private boolean isInStorageDir(Path path) {
		return path.normalize().startsWith(storageDir);
	}

	void assertKeyInLegacyStorageDir(Path path, String key) {
		if (!path.normalize().startsWith(getLegacyStorageDir())) {
			throw new IllegalArgumentException("Key must not point outside legacy storage dir. Wrong key: " + key);
		}
	}

	/**
	 * Exists as a separate method so that tests can count the number of existence checks a storage
	 * operation performs.
	 *
	 * @param path the path to check
	 * @return true if the path exists
	 * @see #exists(String)
	 */
	boolean fileExists(Path path) {
		return Files.exists(path);
	}

	@Override
	public String saveData(InputStream inputStream, ObjectMetadata metadata, String moduleIdOrGroup, String keySuffix)
	        throws IOException {
		String key = newKey(moduleIdOrGroup, keySuffix, metadata != null ? metadata.getFilename() : null);
		Path target = newPath(key);
		try {
			Files.copy(inputStream, target);
		} catch (IOException e) {
			purgeData(key);
			throw e;
		}
		return key;
	}

	@Override
	public boolean purgeData(String key) throws IOException {
		if (key == null)
			return false;

		try {
			return Files.deleteIfExists(getPath(key));
		} catch (Exception e) {
			log.error("Error deleting key: {}", key, e);
			try {
				File file = getPath(key).toFile();
				if (file.exists()) {
					file.deleteOnExit();
					return true;
				} else {
					return false;
				}
			} catch (Exception deleteException) {
				log.error("Error marking key for deletion: {}", key, deleteException);
			}
			return false;
		}
	}

	@Override
	public boolean exists(String key) {
		return fileExists(storageDir.resolve(encodeKey(key))) || fileExists(getLegacyStorageDir().resolve(key));
	}
}

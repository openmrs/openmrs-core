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
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.openmrs.api.StorageService;
import org.openmrs.util.OpenmrsUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LocalStorageServiceTest extends BaseStorageServiceTest {

	/**
	 * Counts the number of existence checks a storage operation performs, which is what TRUNK-6682
	 * bounds to one per read.
	 */
	private class CountingLocalStorageService extends LocalStorageService {

		int existenceChecks;

		CountingLocalStorageService() {
			super(tempDir.toAbsolutePath().toString(), streamService);
		}

		@Override
		boolean fileExists(Path path) {
			existenceChecks++;
			return super.fileExists(path);
		}
	}

	@Override
	public StorageService newStorageService() {
		return new LocalStorageService(tempDir.toAbsolutePath().toString(), streamService);
	}

	@Test
	public void getDataWithMetadata_shouldNotCheckExistenceWhenFileIsInStorageDir() throws IOException {
		CountingLocalStorageService counting = new CountingLocalStorageService();

		String key = null;
		try {
			key = counting.saveData(testFile, null, null, "counted_key");

			try (DataWithMetadata dwm = counting.getDataWithMetadata(key)) {
				assertEquals(testFileContent, IOUtils.toString(dwm.data(), Charset.defaultCharset()));
			}

			assertThat(counting.existenceChecks, is(0));
		} finally {
			if (key != null) {
				counting.purgeData(key);
			}
		}
	}

	@Test
	public void getData_shouldCheckExistenceOnceWhenFallingBackToLegacyFile() throws IOException {
		CountingLocalStorageService counting = new CountingLocalStorageService();

		Path legacyPath = null;
		try {
			Path dir = Files.createDirectories(Paths.get(OpenmrsUtil.getApplicationDataDirectory(), "storage"));
			legacyPath = Files.createFile(dir.resolve(RandomStringUtils.insecure().nextAlphanumeric(8)));

			try (OutputStream out = Files.newOutputStream(legacyPath)) {
				IOUtils.write("test", out, Charset.defaultCharset());
			}

			try (InputStream data = counting.getData(legacyPath.toAbsolutePath().toString())) {
				assertEquals("test", IOUtils.toString(data, Charset.defaultCharset()));
			}

			assertThat(counting.existenceChecks, is(1));
		} finally {
			if (legacyPath != null) {
				Files.deleteIfExists(legacyPath);
			}
		}
	}

	@Test
	public void exists_shouldCheckExistenceOnceWhenFileIsInStorageDir() throws IOException {
		CountingLocalStorageService counting = new CountingLocalStorageService();

		String key = null;
		try {
			key = counting.saveData(testFile, null, null, "counted_key");

			assertThat(counting.exists(key), is(true));
			assertThat(counting.existenceChecks, is(1));
		} finally {
			if (key != null) {
				counting.purgeData(key);
			}
		}
	}

	/**
	 * This is the read path {@code AbstractHandler} performs: it probes {@code exists} to resolve the
	 * key layout, then reads the data and metadata together. Together they must cost a single existence
	 * check.
	 */
	@Test
	public void readAsHandler_shouldPerformOnlyOneExistenceCheck() throws IOException {
		CountingLocalStorageService counting = new CountingLocalStorageService();

		String key = null;
		try {
			key = counting.saveData(testFile, null, null, "counted_key");

			if (counting.exists(key)) {
				try (DataWithMetadata dwm = counting.getDataWithMetadata(key)) {
					assertEquals(testFileContent, IOUtils.toString(dwm.data(), Charset.defaultCharset()));
				}
			}

			assertThat(counting.existenceChecks, is(1));
		} finally {
			if (key != null) {
				counting.purgeData(key);
			}
		}
	}

	@Test
	public void getData_shouldReturnDataWhenLegacyFileExists() throws IOException {
		Path legacyPath = null;
		try {
			Path dir = Files.createDirectories(Paths.get(OpenmrsUtil.getApplicationDataDirectory(), "storage"));
			legacyPath = Files.createFile(dir.resolve(RandomStringUtils.insecure().nextAlphanumeric(8)));

			try (OutputStream out = Files.newOutputStream(legacyPath)) {
				IOUtils.write("test", out, Charset.defaultCharset());
			}

			try (InputStream data = storageService.getData(legacyPath.toAbsolutePath().toString())) {
				assertEquals("test", IOUtils.toString(data, Charset.defaultCharset()));
			}
		} finally {
			if (legacyPath != null) {
				Files.deleteIfExists(legacyPath);
			}
		}
	}

	@Test
	public void purgeData_shouldScheduleDeletionIfFileOpen() throws IOException {
		saveTestData(null, null, (key) -> {
			try (InputStream ignored = storageService.getData(key)) {
				boolean deleted = storageService.purgeData(key);
				assertThat(deleted, is(true));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void saveData_shouldNotAllowToWriteFilesOutsideOfStorageDir() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> {
			storageService.saveData((out) -> {}, null, null, "/test");
		});

		String key = null;
		try {
			key = storageService.saveData((out) -> {}, null, null, "../test");
			assertThat(key, is("../test"));
			Path testFile = tempDir.resolve("test");
			assertThat(Files.exists(testFile), is(false));
			assertThat(storageService.exists(key), is(true));
		} finally {
			if (key != null) {
				storageService.purgeData(key);
			}
		}
	}

	@Test
	public void getData_shouldFailIfKeyTriesToAccessFilesOutsideOfStorageDir() throws IOException {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
			storageService.getData("/test");
		});
		assertThat(e.getMessage(), is("Key must not point outside storage dir. Wrong key: /test"));

		Path testFile = Paths.get(OpenmrsUtil.getApplicationDataDirectory(), "../test");
		try {
			testFile.toFile().createNewFile();
			IllegalArgumentException e2 = assertThrows(IllegalArgumentException.class, () -> {
				storageService.getData("../test");
			});
			assertThat(e2.getMessage(), is("Key must not point outside legacy storage dir. Wrong key: ../test"));

			IllegalArgumentException e3 = assertThrows(IllegalArgumentException.class, () -> {
				storageService.exists("../test");
			});
			assertThat(e3.getMessage(), is("Key must not point outside legacy storage dir. Wrong key: ../test"));
		} finally {
			if (testFile.toFile().exists()) {
				testFile.toFile().delete();
			}
		}
	}
}

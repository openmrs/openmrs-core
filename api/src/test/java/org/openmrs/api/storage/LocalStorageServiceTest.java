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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

public class LocalStorageServiceTest extends BaseStorageServiceTest {

	@Override
	public StorageService newStorageService() {
		return new LocalStorageService(tempDir.toAbsolutePath().toString(), streamService);
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
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void saveData_shouldNotAllowToWriteFilesOutsideOfStorageDir() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> {
			storageService.saveData((out) -> {}, null, null,
				"/test");
		});

		String key = null;
		try {
			key = storageService.saveData((out) -> {
					}, null, null,
					"../test");
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
		} finally {
			if (testFile.toFile().exists()) {
				testFile.toFile().delete();
			}
		}
	}
}

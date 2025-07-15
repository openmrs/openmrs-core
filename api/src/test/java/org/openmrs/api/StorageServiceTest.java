/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.api.storage.LocalStorageService;
import org.openmrs.api.stream.StreamDataService;
import org.openmrs.api.stream.StreamDataWriter;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;

class StorageServiceTest extends BaseContextSensitiveTest {
	
	final String testFileContent = "This is a test file";
	
	final String testFile2Content = "This is another test file";
	
	InputStream testFile;
	
	InputStream testFile2;
	
	LocalStorageService localStorageService;
	
	final SimpleDateFormat dirFormat = new SimpleDateFormat("yyyy/MM");
	
	@Autowired
	StreamDataService streamService;
	
	@TempDir
	Path tempDir;
	
	@BeforeEach
	void setUp() {
		localStorageService = new LocalStorageService(tempDir.toAbsolutePath().toString(), streamService);
		testFile = IOUtils.toInputStream(testFileContent, Charset.defaultCharset());
		testFile2 = IOUtils.toInputStream(testFile2Content, Charset.defaultCharset());
	}
	
	@Test
	void getDataShouldThrowExceptionWhenFileDoesNotExist() {
		assertThrows(IOException.class, () -> localStorageService.getData("none"));
	}
	
	@Test
	void getDataShouldReturnDataWhenFileExists() throws IOException {
		saveTestData(null, "key", (key) -> {
			try (InputStream data = localStorageService.getData(key)) {
				assertEquals(testFileContent, IOUtils.toString(data, Charset.defaultCharset()));
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void getDataShouldReturnDataWhenLegacyFileExists() throws IOException {
		Path legacyPath = null;
		try {
			Path dir = Files.createDirectories(Paths.get(OpenmrsUtil.getApplicationDataDirectory(), "storage"));
			legacyPath = Files.createFile(dir.resolve(RandomStringUtils.insecure().nextAlphanumeric(8)));

			try (OutputStream out = Files.newOutputStream(legacyPath)) {
				IOUtils.write("test", out, Charset.defaultCharset());
			}

			try (InputStream data = localStorageService.getData(legacyPath.toAbsolutePath().toString())) {
				assertEquals("test", IOUtils.toString(data, Charset.defaultCharset()));
			}
		} finally {
			if (legacyPath != null) {
				Files.deleteIfExists(legacyPath);
			}
		}
	}
	
	void saveTestData(String moduleId, String keySuffix, Consumer<String> verify) throws IOException {
		saveTestData(moduleId, keySuffix, null, verify);
	}
	
	void saveTestData(String moduleId, String keySuffix, InputStream testData, Consumer<String> verify) throws IOException {
		String key = null;
		try {
			if (testData == null) {
				testData = testFile;
			}
			if (keySuffix != null) {
				key = localStorageService.saveData(testData, null, moduleId, keySuffix);
			} else {
				key = localStorageService.saveData(testData, null, moduleId);
			}
			verify.accept(key);
		}
		finally {
			if (key != null) {
				localStorageService.purgeData(key);
			}
		}
	}
	
	@Test
	void saveTempDataShouldPersistData() throws IOException {
		String key = null;
		try {
			key = localStorageService.saveTempData(testFile, null);
			
			try (InputStream data = localStorageService.getTempData(key)) {
				assertEquals(testFileContent, IOUtils.toString(data, Charset.defaultCharset()));
			}
		}
		finally {
			if (key != null) {
				localStorageService.purgeData(key);
			}
		}
		
	}
	
	@Test
	void saveDataShouldPersistDataIfNoModuleIdAndKeySuffix() throws IOException {
		saveTestData(null, null, (key) -> {
			try (InputStream data = localStorageService.getData(key)) {
				assertEquals(testFileContent, IOUtils.toString(data, Charset.defaultCharset()));
				assertThat(key, startsWith(dirFormat.format(new Date())));
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void saveDataShouldPersistDataWithModuleId() throws IOException {
		saveTestData("test_module", null, (key) -> {
			try (InputStream data = localStorageService.getData(key)) {
				assertEquals(testFileContent, IOUtils.toString(data, Charset.defaultCharset()));
				assertThat(key, startsWith("test_module/"));
				assertThat(key, containsString(dirFormat.format(new Date())));
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void saveDataShouldPersistDataWithModuleIdAndKeySuffix() throws IOException {
		saveTestData("test_module", "test_key", (key) -> {
			try (InputStream data = localStorageService.getData(key)) {
				assertEquals(testFileContent, IOUtils.toString(data, Charset.defaultCharset()));
				assertThat(key, startsWith("test_module/"));
				assertThat(key, not(containsString(dirFormat.format(new Date()))));
				assertThat(key, endsWith("test_key"));
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void saveDataShouldFailIfModuleIdAndKeySuffixExists() throws IOException {
		String keySuffix = newKeySuffix();
		saveTestData("test_module", keySuffix, (key) -> {
			assertThrows(FileAlreadyExistsException.class, () -> saveTestData("test_module", keySuffix, (newKey) -> {}));
		});
	}
	
	@Test
	void saveDataShouldPersistDataIfModuleIdDiffersButKeySuffixSame() throws IOException {
		String keySuffix = newKeySuffix();
		saveTestData("test_module", keySuffix, (key) -> {
			try {
				saveTestData("test_another_module", keySuffix, testFile2, (newKey) -> {
					try (InputStream data = localStorageService.getData(key)) {
						assertEquals(testFileContent, IOUtils.toString(data, Charset.defaultCharset()));
						assertThat(newKey, startsWith("test_another_module/"));
						assertThat(newKey, not(containsString(dirFormat.format(new Date()))));
						assertThat(newKey, endsWith(keySuffix));
					}
					catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	private static @NotNull String newKeySuffix() {
		return UUID.randomUUID().toString().substring(0, 8);
	}
	
	@Test
	void getKeysShouldListFilesWithGivenModuleIdAndKeySuffix() throws IOException {
		saveTestData("test_module", "test/test_key", (key) -> {
			try {
				saveTestData("test_module", "test/test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = localStorageService.getKeys("test_module", "test/test_ke")) {
						assertThat(keys.collect(Collectors.toList()),
						    containsInAnyOrder(equalTo("test/test_key"), equalTo("test/test_key_2")));
					}
					catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void getKeysShouldListFilesWithGivenModuleIdAndKeySuffixWithoutDirs() throws IOException {
		saveTestData("test_module", "test_key", (key) -> {
			try {
				saveTestData("test_module", "test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = localStorageService.getKeys("test_module", "test_ke")) {
						assertThat(keys.collect(Collectors.toList()),
						    containsInAnyOrder(equalTo("test_key"), equalTo("test_key_2")));
					}
					catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void getKeysShouldListFilesWithoutGivenModuleIdAndWithKeySuffix() throws IOException {
		saveTestData(null, "test_key", (key) -> {
			try {
				saveTestData(null, "test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = localStorageService.getKeys(null, "test_ke")) {
						assertThat(keys.collect(Collectors.toList()),
						    containsInAnyOrder(equalTo("test_key"), equalTo("test_key_2")));
					}
					catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void getKeysShouldListFilesOnlyForGivenModule() throws IOException {
		saveTestData("test_module", "test_key", (key) -> {
			try {
				saveTestData(null, "test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = localStorageService.getKeys("test_module", "test_ke")) {
						assertThat(keys.collect(Collectors.toList()), containsInAnyOrder(equalTo("test_key")));
					}
					catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void getKeysShouldListFilesOnlyForGlobal() throws IOException {
		saveTestData("test_module", "test_key", (key) -> {
			try {
				saveTestData(null, "test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = localStorageService.getKeys(null, "test_ke")) {
						assertThat(keys.collect(Collectors.toList()), containsInAnyOrder(equalTo("test_key_2")));
					}
					catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void getKeysShouldListFilesAndDirsOnlyForCurrentDir() throws IOException {
		saveTestData("test_module", "test_parent/test/test_key", (key) -> {
			try {
				saveTestData("test_module", "test_parent/test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = localStorageService.getKeys("test_module", "test_parent/test")) {
						assertThat(keys.collect(Collectors.toList()),
						    containsInAnyOrder(equalTo("test_parent/test_key_2"), equalTo("test_parent/test/")));
					}
					catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void getKeysShouldListNoFilesIfNoMatches() throws IOException {
		saveTestData("test_module", "test/test_key", (key) -> {
			try {
				saveTestData("test_module", "test/test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = localStorageService.getKeys("test_module", "test2")) {
						assertThat(keys.collect(Collectors.toList()), is(emptyIterable()));
					}
					catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void getKeysShouldListAllFilesAndDirsFromDir() throws IOException {
		saveTestData("test_module", "test/test_key", (key) -> {
			try {
				saveTestData("test_module", "test/test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = localStorageService.getKeys("test_module", "test/")) {
						assertThat(keys.collect(Collectors.toList()),
						    containsInAnyOrder(equalTo("test/test_key_2"), equalTo("test/test_key")));
					}
					catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void getKeysShouldListAllFilesAndDirsFromRoot() throws IOException {
		saveTestData("test_module", "test/test_key", (key) -> {
			try {
				saveTestData("test_module", "test/test_key_2", testFile2, (key2) -> {
					try {
						saveTestData(null, "test", (key3) -> {
							try (Stream<String> keys = localStorageService.getKeys(null, "/")) {
								assertThat(keys.collect(Collectors.toList()),
								    containsInAnyOrder(equalTo("test"), equalTo("test_module/")));
							}
							catch (IOException e) {
								throw new UncheckedIOException(e);
							}
						});
					}
					catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void getKeysShouldListAllFilesAndDirsFromParentDirOnly() throws IOException {
		saveTestData("test_module", "test/test_key", (key) -> {
			try {
				saveTestData("test_module", "test/test/test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = localStorageService.getKeys("test_module", "test/")) {
						assertThat(keys.collect(Collectors.toList()),
						    containsInAnyOrder(equalTo("test/test_key"), equalTo("test/test/")));
					}
					catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void purgeDataShouldReturnTrueWhenDeleted() throws IOException {
		saveTestData(null, null, (key) -> {
			try {
				boolean deleted = localStorageService.purgeData(key);
				boolean exists = localStorageService.exists(key);
				assertThat(deleted, is(true));
				assertThat(exists, is(false));
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void purgeDataShouldScheduleDeletionIfFileOpen() throws IOException {
		saveTestData(null, null, (key) -> {
			try (InputStream ignored = localStorageService.getData(key)) {
				boolean deleted = localStorageService.purgeData(key);
				assertThat(deleted, is(true));
			}
			catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	
	@Test
	void purgeDataShouldReturnFalseIfNotExists() throws IOException {
		boolean exists = localStorageService.exists(newKeySuffix());
		boolean deleted = localStorageService.purgeData(newKeySuffix());
		assertThat(exists, is(false));
		assertThat(deleted, is(false));
	}
	
	@Test
	void existsShouldReturnTrueWhenFileExists() throws IOException {
		saveTestData(null, null, (key) -> {
			boolean exists = localStorageService.exists(key);
			assertThat(exists, is(true));
		});
	}
	
	@Test
	void existsShouldReturnFalseWhenFileMissing() throws IOException {
		boolean exists = localStorageService.exists(newKeySuffix());
		assertThat(exists, is(false));
	}

	@Test
	void saveDataShouldHandleWindowsPathSeparatorInKey() throws IOException {
		saveTestData("test_module", "test_key/test", (key) -> {
			assertThat(key, is("test_module/test_key/test"));
			assertThat(localStorageService.exists(key), is(true));
		});

		saveTestData("test_module", "test_key\\test", (key) -> {
			assertThat(key, is("test_module/test_key\\test"));
			assertThat(localStorageService.exists(key), is(true));
		});

		saveTestData(null, null, (key) -> {
			assertThat(key, not(containsString("\\")));
			assertThat(localStorageService.exists(key), is(true));
		});
			
	}
	
	@Test
	void saveDataShouldNotFailIfModuleIdOrGroupContainsAllowedCharacters() throws IOException {
		String key = null;
		try {
			key = localStorageService.saveData((out) -> {
				out.write(1);
			}, null, "test10-.a/10");
		} finally {
			if (key != null) {
				localStorageService.purgeData(key);
			}
		}
	}

	@Test
	void saveDataShouldFailIfModuleIdOrGroupContainsBadCharacters() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> {
			localStorageService.saveData((out) -> {
				out.write(1);
			}, null, "test10$-.a/10");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			localStorageService.saveData((out) -> {
				out.write(1);
			}, null, "test10-.a/10,");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			localStorageService.saveData((out) -> {
				out.write(1);
			}, null, "test10-.a/10=");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			localStorageService.saveData((out) -> {
				out.write(1);
			}, null, "test10-.a/10\\");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			localStorageService.saveData((out) -> {
				out.write(1);
			}, null, "@test10-.a/10");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			localStorageService.saveData((out) -> {
				out.write(1);
			}, null, "test!10-.a/10");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			localStorageService.saveData((out) -> {
				out.write(1);
			}, null, "t[est10-.a/10=");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			localStorageService.saveData((out) -> {
				out.write(1);
			}, null, "test10-.a/10=");
		});
	}
	
	@Test
	void saveDataShouldNotAllowToWriteFilesOutsideStorageDir() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> {
			localStorageService.saveData((out) -> {}, null, null,
				"/test");
		});

		String key = null;
		try {
			key = localStorageService.saveData((out) -> {
					}, null, null,
					"../test");
			assertThat(key, is("../test"));
			Path testFile = tempDir.resolve("test");
			assertThat(Files.exists(testFile), is(false));
			assertThat(localStorageService.exists(key), is(true));
		} finally {
			if (key != null) {
				localStorageService.purgeData(key);
			}
		}
	}

	@Test
	void saveDataShouldNotCreateFileIfErrorOccursWhenCopyingData() throws IOException {
		StreamDataWriter writer = (OutputStream outputStream) -> {
			outputStream.write(1);
			outputStream.flush();
			throw new IOException("Failure during writing");
		};

		InputStream inputStream = streamService.streamData(writer, null);
		assertThrows(IOException.class, () -> {
			localStorageService.saveData(inputStream, null, null, "test");
		});
	}

	@Test
	void getDataShouldFailIfKeyTriesToAccessFilesOutsideStorageDir() throws IOException {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
			localStorageService.getData("/test");
		});
		assertThat(e.getMessage(), is("Key must not point outside storage dir. Wrong key: /test"));

		Path testFile = Paths.get(OpenmrsUtil.getApplicationDataDirectory(), "../test");
		try {
			testFile.toFile().createNewFile();
			IllegalArgumentException e2 = assertThrows(IllegalArgumentException.class, () -> {
				localStorageService.getData("../test");
			});
			assertThat(e2.getMessage(), is("Key must not point outside legacy storage dir. Wrong key: ../test"));
		} finally {
			if (testFile.toFile().exists()) {
				testFile.toFile().delete();
			}
		}
	}
}

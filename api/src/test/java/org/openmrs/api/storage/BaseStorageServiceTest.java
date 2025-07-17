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
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmrs.api.StorageService;
import org.openmrs.api.stream.StreamDataService;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseStorageServiceTest extends BaseContextSensitiveTest {
	protected final String testFileContent = "This is a test file";
	protected final String testFile2Content = "This is another test file";
	protected final SimpleDateFormat dirFormat = new SimpleDateFormat("yyyy/MM");
	protected InputStream testFile;
	protected InputStream testFile2;
	protected StorageService storageService;
	@Autowired
	protected StreamDataService streamService;
	@TempDir
	protected Path tempDir;

	protected static String newKeySuffix() {
		return UUID.randomUUID().toString().substring(0, 8);
	}

	@BeforeEach
	void setUp() {
		storageService = newStorageService();
		testFile = IOUtils.toInputStream(testFileContent, Charset.defaultCharset());
		testFile2 = IOUtils.toInputStream(testFile2Content, Charset.defaultCharset());
	}

	public abstract StorageService newStorageService();

	@Test
	public void getData_shouldThrowExceptionWhenFileDoesNotExist() {
		assertThrows(IOException.class, () -> storageService.getData("none"));
	}

	@Test
	public void getData_shouldReturnDataWhenFileExists() throws IOException {
		saveTestData(null, "key", (key) -> {
			try (InputStream data = storageService.getData(key)) {
				assertEquals(testFileContent, IOUtils.toString(data, Charset.defaultCharset()));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	public void saveTestData(String moduleId, String keySuffix, Consumer<String> verify) throws IOException {
		saveTestData(moduleId, keySuffix, null, verify);
	}

	public void saveTestData(String moduleId, String keySuffix, InputStream testData, Consumer<String> verify) throws IOException {
		String key = null;
		try {
			if (testData == null) {
				testData = testFile;
				testData.reset();
			}
			if (keySuffix != null) {
				key = storageService.saveData(testData, null, moduleId, keySuffix);
			} else {
				key = storageService.saveData(testData, null, moduleId);
			}
			verify.accept(key);
		} finally {
			if (key != null) {
				storageService.purgeData(key);
			}
		}
	}

	@Test
	public void saveTempData_shouldPersistData() throws IOException {
		String key = null;
		try {
			key = storageService.saveTempData(testFile, null);

			try (InputStream data = storageService.getTempData(key)) {
				assertEquals(testFileContent, IOUtils.toString(data, Charset.defaultCharset()));
			}
		} finally {
			if (key != null) {
				storageService.purgeData(key);
			}
		}

	}

	@Test
	public void saveData_shouldPersistDataIfNoModuleIdAndKeySuffix() throws IOException {
		saveTestData(null, null, (key) -> {
			try (InputStream data = storageService.getData(key)) {
				assertEquals(testFileContent, IOUtils.toString(data, Charset.defaultCharset()));
				assertThat(key, startsWith(dirFormat.format(new Date())));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void saveData_shouldPersistDataWithModuleId() throws IOException {
		saveTestData("test_module", null, (key) -> {
			try (InputStream data = storageService.getData(key)) {
				assertEquals(testFileContent, IOUtils.toString(data, Charset.defaultCharset()));
				assertThat(key, startsWith("test_module/"));
				assertThat(key, containsString(dirFormat.format(new Date())));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void saveData_shouldPersistDataWithModuleIdAndKeySuffix() throws IOException {
		saveTestData("test_module", "test_key", (key) -> {
			try (InputStream data = storageService.getData(key)) {
				assertEquals(testFileContent, IOUtils.toString(data, Charset.defaultCharset()));
				assertThat(key, startsWith("test_module/"));
				assertThat(key, not(containsString(dirFormat.format(new Date()))));
				assertThat(key, endsWith("test_key"));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void saveData_shouldFailIfModuleIdAndKeySuffixExists() throws IOException {
		String keySuffix = BaseStorageServiceTest.newKeySuffix();
		saveTestData("test_module", keySuffix, (key) -> {
			assertThrows(FileAlreadyExistsException.class, () -> 
				saveTestData("test_module", keySuffix, (newKey) -> {
			}));
		});
	}

	@Test
	public void saveData_shouldPersistDataIfModuleIdDiffersButKeySuffixSame() throws IOException {
		String keySuffix = BaseStorageServiceTest.newKeySuffix();
		saveTestData("test_module", keySuffix, (key) -> {
			try {
				saveTestData("test_another_module", keySuffix, testFile2, (newKey) -> {
					try (InputStream data = storageService.getData(key)) {
						assertEquals(testFileContent, IOUtils.toString(data, Charset.defaultCharset()));
						assertThat(newKey, startsWith("test_another_module/"));
						assertThat(newKey, not(containsString(dirFormat.format(new Date()))));
						assertThat(newKey, endsWith(keySuffix));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void getKeys_shouldListFilesWithGivenModuleIdAndKeySuffix() throws IOException {
		saveTestData("test_module", "test/test_key", (key) -> {
			try {
				saveTestData("test_module", "test/test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = storageService.getKeys("test_module", "test/test_ke")) {
						assertThat(keys.collect(Collectors.toList()),
							containsInAnyOrder(equalTo("test_module/test/test_key"), 
								equalTo("test_module/test/test_key_2")));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void getKeys_shouldListFilesWithGivenModuleIdAndKeySuffixWithoutDirs() throws IOException {
		saveTestData("test_module", "test_key", (key) -> {
			try {
				saveTestData("test_module", "test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = storageService.getKeys("test_module", "test_ke")) {
						assertThat(keys.collect(Collectors.toList()),
							containsInAnyOrder(equalTo("test_module/test_key"), 
								equalTo("test_module/test_key_2")));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void getKeys_shouldListFilesWithoutGivenModuleIdAndWithKeySuffix() throws IOException {
		saveTestData(null, "test_key", (key) -> {
			try {
				saveTestData(null, "test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = storageService.getKeys(null, "test_ke")) {
						assertThat(keys.collect(Collectors.toList()),
							containsInAnyOrder(equalTo("test_key"), equalTo("test_key_2")));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void getKeys_shouldListFilesOnlyForGivenModule() throws IOException {
		saveTestData("test_module", "test_key", (key) -> {
			try {
				saveTestData(null, "test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = storageService.getKeys("test_module", "test_ke")) {
						assertThat(keys.collect(Collectors.toList()), 
							containsInAnyOrder(equalTo("test_module/test_key")));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void getKeys_shouldListFilesOnlyForGlobal() throws IOException {
		saveTestData("test_module", "test_key", (key) -> {
			try {
				saveTestData(null, "test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = storageService.getKeys(null, "test_ke")) {
						assertThat(keys.collect(Collectors.toList()), containsInAnyOrder(
							equalTo("test_key_2")));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void getKeys_shouldListFilesAndDirsOnlyForCurrentDir() throws IOException {
		saveTestData("test_module", "test_parent/test/test_key", (key) -> {
			try {
				saveTestData("test_module", "test_parent/test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = storageService.getKeys("test_module", "test_parent/test")) {
						assertThat(keys.collect(Collectors.toList()),
							containsInAnyOrder(equalTo("test_module/test_parent/test_key_2"), 
								equalTo("test_module/test_parent/test/")));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void getKeys_shouldListNoFilesIfNoMatches() throws IOException {
		saveTestData("test_module", "test/test_key", (key) -> {
			try {
				saveTestData("test_module", "test/test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = storageService.getKeys("test_module", "test2")) {
						assertThat(keys.collect(Collectors.toList()), is(emptyIterable()));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void getKeys_shouldListAllFilesAndDirsFromDir() throws IOException {
		saveTestData("test_module", "test/test_key", (key) -> {
			try {
				saveTestData("test_module", "test/test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = storageService.getKeys("test_module", "test/")) {
						assertThat(keys.collect(Collectors.toList()),
							containsInAnyOrder(equalTo("test_module/test/test_key_2"), 
								equalTo("test_module/test/test_key")));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void getKeys_shouldListAllFilesAndDirsFromRoot() throws IOException {
		saveTestData("test_module", "test/test_key", (key) -> {
			try {
				saveTestData("test_module", "test/test_key_2", testFile2, (key2) -> {
					try {
						saveTestData(null, "test", (key3) -> {
							try (Stream<String> keys = storageService.getKeys(null, "")) {
								assertThat(keys.collect(Collectors.toList()),
									containsInAnyOrder(equalTo("test_module/"), 
										equalTo("test")));
							} catch (IOException e) {
								throw new UncheckedIOException(e);
							}
						});
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void getKeys_shouldListAllFilesAndDirsFromParentDirOnly() throws IOException {
		saveTestData("test_module", "test/test_key", (key) -> {
			try {
				saveTestData("test_module", "test/test/test_key_2", testFile2, (key2) -> {
					try (Stream<String> keys = storageService.getKeys("test_module", "test/")) {
						assertThat(keys.collect(Collectors.toList()),
							containsInAnyOrder(equalTo("test_module/test/test_key"), 
								equalTo("test_module/test/test/")));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void purgeData_shouldReturnTrueWhenDeleted() throws IOException {
		saveTestData(null, null, (key) -> {
			try {
				boolean deleted = storageService.purgeData(key);
				boolean exists = storageService.exists(key);
				assertThat(deleted, is(true));
				assertThat(exists, is(false));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	@Test
	public void purgeData_shouldReturnFalseIfNotExists() throws IOException {
		boolean exists = storageService.exists(BaseStorageServiceTest.newKeySuffix());
		boolean deleted = storageService.purgeData(BaseStorageServiceTest.newKeySuffix());
		assertThat(exists, is(false));
		assertThat(deleted, is(false));
	}

	@Test
	public void exists_shouldReturnTrueWhenFileExists() throws IOException {
		saveTestData(null, null, (key) -> {
			boolean exists = storageService.exists(key);
			assertThat(exists, is(true));
		});
	}

	@Test
	public void exists_shouldReturnFalseWhenFileMissing() throws IOException {
		boolean exists = storageService.exists(BaseStorageServiceTest.newKeySuffix());
		assertThat(exists, is(false));
	}

	@Test
	public void saveData_shouldHandleWindowsPathSeparatorInKey() throws IOException {
		saveTestData("test_module", "test_key/test", (key) -> {
			assertThat(key, is("test_module/test_key/test"));
			assertThat(storageService.exists(key), is(true));
		});

		saveTestData("test_module", "test_key\\test", (key) -> {
			assertThat(key, is("test_module/test_key\\test"));
			assertThat(storageService.exists(key), is(true));
		});

		saveTestData(null, null, (key) -> {
			assertThat(key, not(containsString("\\")));
			assertThat(storageService.exists(key), is(true));
		});

	}

	@Test
	public void saveData_shouldNotFailIfModuleIdOrGroupContainsAllowedCharacters() throws IOException {
		String key = null;
		try {
			key = storageService.saveData((out) -> {
				out.write(1);
			}, null, "test10-.a/10");
		} finally {
			if (key != null) {
				storageService.purgeData(key);
			}
		}
	}

	@Test
	public void saveData_shouldFailIfModuleIdOrGroupContainsBadCharacters() throws IOException {
		assertThrows(IllegalArgumentException.class, () -> {
			storageService.saveData((out) -> {
				out.write(1);
			}, null, "test10$-.a/10");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			storageService.saveData((out) -> {
				out.write(1);
			}, null, "test10-.a/10,");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			storageService.saveData((out) -> {
				out.write(1);
			}, null, "test10-.a/10=");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			storageService.saveData((out) -> {
				out.write(1);
			}, null, "test10-.a/10\\");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			storageService.saveData((out) -> {
				out.write(1);
			}, null, "@test10-.a/10");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			storageService.saveData((out) -> {
				out.write(1);
			}, null, "test!10-.a/10");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			storageService.saveData((out) -> {
				out.write(1);
			}, null, "t[est10-.a/10=");
		});
		assertThrows(IllegalArgumentException.class, () -> {
			storageService.saveData((out) -> {
				out.write(1);
			}, null, "test10-.a/10=");
		});
	}

	@Test
	public void saveData_shouldNotCreateFileIfErrorOccursWhenCopyingData() {
		assertThrows(IOException.class, () -> {
			storageService.saveData((out) -> {
					out.write(1);
					throw new IOException("Failure during writing");
				}, null, null,
				"test");
		});

		assertThat(storageService.exists("test"), is(false));
	}
}

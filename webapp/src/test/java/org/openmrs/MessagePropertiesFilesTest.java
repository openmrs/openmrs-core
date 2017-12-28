/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Check if all messages*.properties file contain duplicated keys.
 *
 */
@RunWith(value = Parameterized.class)
public class MessagePropertiesFilesTest {

	private PropertiesFileValidator propertiesFileValidator;

	private String messagePropertiesFilename;

	@Parameters(name = " filename: {0}")
	public static Collection<Object[]> data() {
		
		List<String> messagePropertiesFiles = MessagePropertiesHelper.listMessagePropertiesFiles();
		
		final int filesCount = messagePropertiesFiles.size();
		
		Object[][] data = new Object[filesCount][];
		
		for(int i = 0; i < filesCount; i++){
			data[i] = new Object[]{messagePropertiesFiles.get(i)};
		}
		
		return Arrays.asList(data);
	}

	public MessagePropertiesFilesTest(String messagePropertiesFilename) {
		this.messagePropertiesFilename = messagePropertiesFilename;
	}

	@Before
	public void beforeClass() {
		propertiesFileValidator = new PropertiesFileValidator();
	}

	@Test
	public void checkDuplicatesInFiles() throws FileNotFoundException,
			IOException {
		checkDuplicatesKeysForFile(messagePropertiesFilename);
	}

	private void checkDuplicatesKeysForFile(String messagePropertiesFileName)
			throws FileNotFoundException, IOException {
		FileInputStream fileInputStream = new FileInputStream(
				messagePropertiesFileName);

		List<String> duplicatedKeys;
		try {
			duplicatedKeys = propertiesFileValidator.getDuplicatedKeys(fileInputStream);
			Assert.assertTrue(duplicatedKeys.isEmpty());
		}catch(Exception e){
			Assert.fail("Problem with checking messages properties file.");
		} finally {
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		}
		
	}
}

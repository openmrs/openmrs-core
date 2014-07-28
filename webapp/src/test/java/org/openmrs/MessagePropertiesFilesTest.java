/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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

		List<String> duplicatedKeys = null;
		try {
			duplicatedKeys = propertiesFileValidator.getDuplicatedKeys(fileInputStream);
			Assert.assertTrue(duplicatedKeys.isEmpty());
			return;
		}catch(Exception e){
			Assert.fail("Problem with checking messages properties file.");
		} finally {
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		}
		
	}
}

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to ease list message properties filepaths.
 *
 */
public class MessagePropertiesHelper {

	private static final String MESSAGE_PROPERTIES_FILENAME_REGEX = ".*.properties";
	private static final String MESSAGES_FILE_FOLDER = "src/main/webapp/WEB-INF/";
	
	private MessagePropertiesHelper(){
		
	}
	
	/**
	 * Return all filepaths that matches to message properties file regex.
	 * @return list of filenames
	 */
	public static  List<String> listMessagePropertiesFiles(){
		
		String[] allFilesInDirectory = new File(MESSAGES_FILE_FOLDER).list();
		
		List<String> result = new ArrayList<String>();
		for (String fileName : allFilesInDirectory) {
			if(fileName.matches(MESSAGE_PROPERTIES_FILENAME_REGEX)){
				result.add(MESSAGES_FILE_FOLDER + fileName);
			}
		}
		return result;
	}
}

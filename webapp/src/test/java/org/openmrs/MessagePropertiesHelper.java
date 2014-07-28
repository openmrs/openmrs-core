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

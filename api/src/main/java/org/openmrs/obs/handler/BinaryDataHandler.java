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
package org.openmrs.obs.handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * Handler for storing generic binary data for complex obs to the file system.
 * 
 * @see OpenmrsConstants#GLOBAL_PROPERTY_COMPLEX_OBS_DIR
 * @since 1.8
 */
public class BinaryDataHandler extends AbstractHandler implements ComplexObsHandler {
	
	public static final Log log = LogFactory.getLog(BinaryDataHandler.class);
	
	/**
	 * Constructor initializes formats for alternative file names to protect from unintentionally
	 * overwriting existing files.
	 */
	public BinaryDataHandler() {
		super();
	}
	
	/**
	 * Returns the same ComplexData for all views. The title is the original filename, and the data
	 * is the raw byte[] of data
	 *
	 * (If the view is set to "download", all commas and whitespace are stripped out of the filename to
	 * fix an issue where the browser wasn't handling a filename with whitespace properly)
	 * 
	 * @see ComplexObsHandler#getObs(Obs, String)
	 */
	@Override
	public Obs getObs(Obs obs, String view) {
		
		try {
			File file = getComplexDataFile(obs);
			String[] names = obs.getValueComplex().split("\\|");
			String originalFilename = names[0];
			if ("download".equals(view)) {
				originalFilename = originalFilename.replace(",", "").replace(" ", "");
			}
			FileInputStream fileInputStream = new FileInputStream(file);
			obs.setComplexData(new ComplexData(originalFilename, new BufferedInputStream(fileInputStream)));
			fileInputStream.close();
		}
		catch (Exception e) {
			throw new APIException("An error occurred while trying to get binary complex obs.", e);
		}
		return obs;
	}
	
	/**
	 * @see ComplexObsHandler#saveObs(Obs)
	 */
	public Obs saveObs(Obs obs) throws APIException {
		try {
			// Write the File to the File System
			String fileName = obs.getComplexData().getTitle();
			InputStream in = (InputStream) obs.getComplexData().getData();
			File outfile = getOutputFileToWrite(obs);
			OutputStream out = new FileOutputStream(outfile, false);
			OpenmrsUtil.copyFile(in, out);
			
			// Store the filename in the Obs
			obs.setComplexData(null);
			obs.setValueComplex(fileName + "|" + outfile.getName());
			
			// close the stream
			out.close();
		}
		catch (Exception e) {
			throw new APIException("Error writing binary data complex obs to the file system. ", e);
		}
		
		return obs;
	}

}

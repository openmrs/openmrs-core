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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.util.OpenmrsUtil;

/**
 * Handler for storing files for complex obs to the file system. Files are stored in the location
 * specified by the global property: "obs.complex_obs_dir"
 * 
 * @since 1.5
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
	 * Currently supports all views
	 * 
	 * @see org.openmrs.obs.ComplexObsHandler#getObs(org.openmrs.Obs, java.lang.String)
	 */
	public Obs getObs(Obs obs, String view) {
		File file = getComplexDataFile(obs);
		log.debug("value complex: " + obs.getValueComplex());
		log.debug("file path: " + file.getAbsolutePath());
		ComplexData complexData = null;
		
		// to handle problem with downloading/saving files with blank spaces or commas in their names
		// also need to remove the "file" text appended to the end of the file name
		String[] names = obs.getValueComplex().split("\\|");
		String originalFilename = names[0];
		if ("download".equals(view)) {
			originalFilename = originalFilename.replaceAll(",", "").replaceAll(" ", "").replaceAll("file$","");	
		}
		
		try {
			complexData = new ComplexData(originalFilename, OpenmrsUtil.getFileAsBytes(file));
		}
		catch (IOException e) {
			log.error("Trying to read file: " + file.getAbsolutePath(), e);
		}
		
		obs.setComplexData(complexData);
		
		return obs;
	}
	
	/**
	 * TODO should this support a StringReader too?
	 * 
	 * @see org.openmrs.obs.ComplexObsHandler#saveObs(org.openmrs.Obs)
	 */
	public Obs saveObs(Obs obs) throws APIException {
		// Get the buffered file  from the ComplexData.
		ComplexData complexData = obs.getComplexData();
		if (complexData == null) {
			log.error("Cannot save complex data where obsId=" + obs.getObsId() + " because its ComplexData is null.");
			return obs;
		}
		
		FileOutputStream fout = null;
		try {
			File outfile = getOutputFileToWrite(obs);
			fout = new FileOutputStream(outfile);
			
			Object data = obs.getComplexData().getData();
			if (data instanceof byte[]) {
				fout.write((byte[]) data);
			} else if (InputStream.class.isAssignableFrom(data.getClass())) {
				try {
					OpenmrsUtil.copyFile((InputStream) data, fout);
				}
				catch (IOException e) {
					throw new APIException(
					        "Unable to convert complex data to a valid input stream and then read it into a buffered image");
				}
			}
			
			// Set the Title and URI for the valueComplex
			obs.setValueComplex(outfile.getName() + " file |" + outfile.getName());
			
			// Remove the ComplexData from the Obs
			obs.setComplexData(null);
			
		}
		catch (IOException ioe) {
			throw new APIException("Trying to write complex obs to the file system. ", ioe);
		}
		finally {
			try {
				fout.close();
			}
			catch (Throwable t) {
				// pass
			}
		}
		
		return obs;
	}
	
}

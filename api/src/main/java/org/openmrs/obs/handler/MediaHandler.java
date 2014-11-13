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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.util.OpenmrsUtil;

/**
 * Handler for storing audio and video for complex obs to the file system. The mime type used is
 * taken from the file name. Media are stored in the location specified by the global property: "obs.complex_obs_dir"
 *
 * @see org.openmrs.util.OpenmrsConstants#GLOBAL_PROPERTY_COMPLEX_OBS_DIR
 * @since 1.12
 */
public class MediaHandler extends AbstractHandler implements ComplexObsHandler {
	
	/** Views supported by this handler */
	private static final String[] supportedViews = { ComplexObsHandler.RAW_VIEW, };
	
	public static final Log log = LogFactory.getLog(MediaHandler.class);
	
	public MediaHandler() {
		super();
	}
	
	/**
	 * Currently supports all views and puts the media file data into the ComplexData object
	 *
	 * @see org.openmrs.obs.ComplexObsHandler#getObs(org.openmrs.Obs, java.lang.String)
	 */
	public Obs getObs(Obs obs, String view) {
		File file = getComplexDataFile(obs);
		
		// Raw media
		if (ComplexObsHandler.RAW_VIEW.equals(view)) {
			try {
				String[] names = obs.getValueComplex().split("\\|");
				String originalFilename = names[0];
				originalFilename = originalFilename.replace(",", "").replace(" ", "");
				
				FileInputStream mediaStream = new FileInputStream(file);
				ComplexData complexData = new ComplexData(originalFilename, mediaStream);
				
				complexData.setMIMEType(OpenmrsUtil.getFileMimeType(file));
				
				complexData.setLength(file.length());
				
				obs.setComplexData(complexData);
			}
			catch (FileNotFoundException e) {
				log.error("Trying to create media file stream from " + file.getAbsolutePath(), e);
			}
		}
		// No other view supported
		// NOTE: if adding support for another view, don't forget to update supportedViews list above
		else {
			return null;
		}
		
		return obs;
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#getSupportedViews()
	 */
	@Override
	public String[] getSupportedViews() {
		return supportedViews;
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#saveObs(org.openmrs.Obs)
	 */
	public Obs saveObs(Obs obs) throws APIException {
		
		try {
			// Write the File to the File System
			String fileName = obs.getComplexData().getTitle();
			File outfile = getOutputFileToWrite(obs);
			OutputStream out = new FileOutputStream(outfile, false);
			FileInputStream mediaStream = (FileInputStream) obs.getComplexData().getData();
			OpenmrsUtil.copyFile(mediaStream, out);
			
			// Store the filename in the Obs
			obs.setComplexData(null);
			obs.setValueComplex(fileName + "|" + outfile.getName());
			
			// close the stream
			out.close();
		}
		catch (IOException ioe) {
			throw new APIException("Trying to write complex obs to the file system. ", ioe);
		}
		
		return obs;
	}
	
}

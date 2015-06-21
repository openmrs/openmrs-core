/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.obs.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.Assert;

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
public class BinaryStreamHandler extends AbstractHandler implements ComplexObsHandler {
	
	/** Views supported by this handler */
	private static final String[] supportedViews = { ComplexObsHandler.RAW_VIEW, };
	
	public static final Log log = LogFactory.getLog(BinaryStreamHandler.class);
	
	/**
	 * Constructor initializes formats for alternative file names to protect from unintentionally
	 * overwriting existing files.
	 */
	public BinaryStreamHandler() {
		super();
	}
	
	/**
	 * Returns the same ComplexData for all views. The title is the original filename, and the data
	 * is the raw byte[] of data (If the view is set to "download", all commas and whitespace are
	 * stripped out of the filename to fix an issue where the browser wasn't handling a filename
	 * with whitespace properly) Note that if the method cannot find the file associated with the
	 * obs, it returns the obs with the ComplexData = null
	 * 
	 * @see ComplexObsHandler#getObs(Obs, String)
	 */
	@Override
	public Obs getObs(Obs obs, String view) {
		ComplexData complexData = null;
		
		// Raw stream
		if (ComplexObsHandler.RAW_VIEW.equals(view)) {
			try {
				File file = getComplexDataFile(obs);
				String[] names = obs.getValueComplex().split("\\|");
				String originalFilename = names[0];
				originalFilename = originalFilename.replace(",", "").replace(" ", "");
				
				if (file.exists()) {
					FileInputStream fileInputStream = new FileInputStream(file);
					complexData = new ComplexData(originalFilename, fileInputStream);
				} else {
					log.error("Unable to find file associated with complex obs " + obs.getId());
				}
			}
			catch (Exception e) {
				throw new APIException("Obs.error.while.trying.get.binary.complex", null, e);
			}
		} else {
			// No other view supported
			// NOTE: if adding support for another view, don't forget to update supportedViews list above
			return null;
		}
		
		Assert.notNull(complexData, "Complex data must not be null");
		complexData.setMimeType("application/octet-stream");
		obs.setComplexData(complexData);
		
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
			throw new APIException("Obs.error.writing.binary.data.complex", null, e);
		}
		
		return obs;
	}
	
}

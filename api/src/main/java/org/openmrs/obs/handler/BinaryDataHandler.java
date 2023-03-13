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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Handler for storing files for complex obs to the file system. Files are stored in the location
 * specified by the global property: "obs.complex_obs_dir"
 * 
 * @since 1.5
 */
public class BinaryDataHandler extends AbstractHandler implements ComplexObsHandler {
	
	/** Views supported by this handler */
	private static final String[] supportedViews = { ComplexObsHandler.RAW_VIEW, };
	
	private static final Logger log = LoggerFactory.getLogger(BinaryDataHandler.class);
	
	/**
	 * Constructor initializes formats for alternative file names to protect from unintentionally
	 * overwriting existing files.
	 */
	public BinaryDataHandler() {
		super();
	}
	
	/**
	 * Currently supports the following views: org.openmrs.obs.ComplexObsHandler#RAW_VIEW
	 * 
	 * @see org.openmrs.obs.ComplexObsHandler#getObs(org.openmrs.Obs, java.lang.String)
	 */
	@Override
	public Obs getObs(Obs obs, String view) {
		File file = getComplexDataFile(obs);
		log.debug("value complex: " + obs.getValueComplex());
		log.debug("file path: " + file.getAbsolutePath());
		ComplexData complexData = null;
		
		// Raw view (i.e. the file as is)
		if (ComplexObsHandler.RAW_VIEW.equals(view)) {
			// to handle problem with downloading/saving files with blank spaces or commas in their names
			// also need to remove the "file" text appended to the end of the file name
			String[] names = obs.getValueComplex().split("\\|");
			String originalFilename = names[0];
			originalFilename = originalFilename.replaceAll(",", "").replaceAll(" ", "").replaceAll("file$", "");
			
			try {
				complexData = new ComplexData(originalFilename, OpenmrsUtil.getFileAsBytes(file));
			}
			catch (IOException e) {
				log.error("Trying to read file: " + file.getAbsolutePath(), e);
			}
		} else {
			// No other view supported
			// NOTE: if adding support for another view, don't forget to update supportedViews list above
			return null;
		}
		
		Assert.notNull(complexData, "Complex data must not be null");
		
		// Get the Mime Type and set it
		String mimeType = OpenmrsUtil.getFileMimeType(file);
		complexData.setMimeType(mimeType);
		
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
	 * TODO should this support a StringReader too?
	 * 
	 * @see org.openmrs.obs.ComplexObsHandler#saveObs(org.openmrs.Obs)
	 */
	@Override
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
					throw new APIException("Obs.error.unable.convert.complex.data", new Object[] { "input stream" }, e);
				}
			}
			
			// Set the Title and URI for the valueComplex
			obs.setValueComplex(outfile.getName() + " file |" + outfile.getName());
			
			// Remove the ComplexData from the Obs
			obs.setComplexData(null);
			
		}
		catch (IOException ioe) {
			throw new APIException("Obs.error.trying.write.complex", null, ioe);
		}
		finally {
			try {
				fout.close();
			}
			catch (Exception e) {
				// pass
			}
		}
		
		return obs;
	}
	
}

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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.Assert;

import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.util.OpenmrsUtil;

/**
 * Handler for storing files for complex obs to the file system. Files are stored in the location
 * specified by the global property: "obs.complex_obs_dir"
 * The in coming data are either char[] or java.io.Reader
 *
 */
public class TextHandler extends AbstractHandler implements ComplexObsHandler {
	
	/** Views supported by this handler */
	private static final String[] supportedViews = { ComplexObsHandler.TEXT_VIEW, ComplexObsHandler.RAW_VIEW,
	        ComplexObsHandler.URI_VIEW };
	
	public static final Log log = LogFactory.getLog(TextHandler.class);
	
	/**
	 * Constructor initializes formats for alternative file names to protect from unintentionally
	 * overwriting existing files.
	 */
	public TextHandler() {
		super();
	}
	
	/**
	 * 
	 * 
	 * @see org.openmrs.obs.ComplexObsHandler#getObs(org.openmrs.Obs, java.lang.String)
	 */
	public Obs getObs(Obs obs, String view) {
		File file = getComplexDataFile(obs);
		log.debug("value complex: " + obs.getValueComplex());
		log.debug("file path: " + file.getAbsolutePath());
		ComplexData complexData = null;
		
		if (ComplexObsHandler.TEXT_VIEW.equals(view) || ComplexObsHandler.RAW_VIEW.equals(view)) {
			// to handle problem with downloading/saving files with blank spaces or commas in their names
			// also need to remove the "file" text appended to the end of the file name
			String[] names = obs.getValueComplex().split("\\|");
			String originalFilename = names[0];
			originalFilename = originalFilename.replaceAll(",", "").replaceAll(" ", "").replaceAll("file$", "");
			
			try {
				complexData = ComplexObsHandler.RAW_VIEW.equals(view) ? new ComplexData(originalFilename, OpenmrsUtil
				        .getFileAsBytes(file)) : new ComplexData(originalFilename, OpenmrsUtil.getFileAsString(file));
			}
			catch (IOException e) {
				log.error("Trying to read file: " + file.getAbsolutePath(), e);
			}
		} else if (ComplexObsHandler.URI_VIEW.equals(view)) {
			complexData = new ComplexData(file.getName(), file.getPath());
		} else {
			// No other view supported
			// NOTE: if adding support for another view, don't forget to update supportedViews list above
			return null;
		}
		
		Assert.notNull(complexData, "Complex data must not be null");
		complexData.setMimeType("text/plain");
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
	 * 
	 * 
	 * @see org.openmrs.obs.ComplexObsHandler#saveObs(org.openmrs.Obs)
	 */
	@Override
	public Obs saveObs(Obs obs) throws APIException {
		ComplexData complexData = obs.getComplexData();
		if (complexData == null) {
			log.error("Cannot save complex data where obsId=" + obs.getObsId() + " because its ComplexData is null.");
			return obs;
		}
		BufferedWriter fout = null;
		try {
			File outfile = getOutputFileToWrite(obs);
			fout = new BufferedWriter(new FileWriter(outfile));
			Reader tempRd = null;
			Object data = obs.getComplexData().getData();
			if (data instanceof char[]) {
				fout.write((char[]) data);
			} else if (Reader.class.isAssignableFrom(data.getClass())) {
				try {
					tempRd = new BufferedReader((Reader) data);
					while (true) {
						int character = tempRd.read();
						if (character == -1) {
							break;
						}
						fout.write(character);
					}
					tempRd.close();
				}
				catch (IOException e) {
					throw new APIException("Obs.error.unable.convert.complex.data", new Object[] { "Reader" }, e);
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

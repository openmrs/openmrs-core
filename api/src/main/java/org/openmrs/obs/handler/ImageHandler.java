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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.util.OpenmrsConstants;

/**
 * Handler for storing basic images for complex obs to the file system. The image mime type used is
 * taken from the image name. if the .* image name suffix matches
 * {@link javax.imageio.ImageIO#getWriterFormatNames()} then that mime type will be used to save the
 * image. Images are stored in the location specified by the global property: "obs.complex_obs_dir"
 * 
 * @see OpenmrsConstants#GLOBAL_PROPERTY_COMPLEX_OBS_DIR
 * @since 1.5
 */
public class ImageHandler extends AbstractHandler implements ComplexObsHandler {
	
	public static final Log log = LogFactory.getLog(ImageHandler.class);
	
	private Set<String> extensions;
	
	/**
	 * Constructor initializes formats for alternative file names to protect from unintentionally
	 * overwriting existing files.
	 */
	public ImageHandler() {
		super();
		
		// Create a HashSet to quickly check for supported extensions.
		extensions = new HashSet<String>();
		for (String mt : ImageIO.getWriterFormatNames()) {
			extensions.add(mt);
		}
	}
	
	/**
	 * Currently supports all views and puts the Image file data into the ComplexData object
	 * 
	 * @see org.openmrs.obs.ComplexObsHandler#getObs(org.openmrs.Obs, java.lang.String)
	 */
	public Obs getObs(Obs obs, String view) {
		File file = getComplexDataFile(obs);
		BufferedImage img = null;
		try {
			img = ImageIO.read(file);
		}
		catch (IOException e) {
			log.error("Trying to read file: " + file.getAbsolutePath(), e);
		}
		
		ComplexData complexData = new ComplexData(file.getName(), img);
		
		obs.setComplexData(complexData);
		
		return obs;
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#saveObs(org.openmrs.Obs)
	 */
	public Obs saveObs(Obs obs) throws APIException {
		// Get the buffered image from the ComplexData.
		BufferedImage img = null;
		
		Object data = obs.getComplexData().getData();
		if (data instanceof BufferedImage) {
			img = (BufferedImage) obs.getComplexData().getData();
		} else if (data instanceof InputStream) {
			try {
				img = ImageIO.read((InputStream) data);
				if (img == null) {
					throw new IllegalArgumentException("Invalid image file");
				}
			}
			catch (IOException e) {
				throw new APIException(
				        "Unable to convert complex data to a valid input stream and then read it into a buffered image", e);
			}
		}
		
		if (img == null) {
			throw new APIException("Cannot save complex obs where obsId=" + obs.getObsId()
			        + " because its ComplexData.getData() is null.");
		}
		
		File outfile = null;
		try {
			outfile = getOutputFileToWrite(obs);
			
			String extension = getExtension(obs.getComplexData().getTitle());
			
			// TODO: Check this extension against the registered extensions for validity
			
			// Write the file to the file system.
			ImageIO.write(img, extension, outfile);
			
			// Set the Title and URI for the valueComplex
			obs.setValueComplex(extension + " image |" + outfile.getName());
			
			// Remove the ComlexData from the Obs
			obs.setComplexData(null);
			
		}
		catch (IOException ioe) {
			if (outfile != null && outfile.length() == 0) {
				outfile.delete(); // OpenJDK 7 & 8 may leave a 0-byte file when ImageIO.write(..) fails.
			}
			throw new APIException("Trying to write complex obs to the file system. ", ioe);
		}
		
		return obs;
	}
}

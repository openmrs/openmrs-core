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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;

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
	
	/** Views supported by this handler */
	private static final String[] supportedViews = { ComplexObsHandler.RAW_VIEW };
	
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
		
		// Raw image
		if (ComplexObsHandler.RAW_VIEW.equals(view)) {
			BufferedImage img = null;
			try {
				img = ImageIO.read(file);
			}
			catch (IOException e) {
				log.error("Trying to read file: " + file.getAbsolutePath(), e);
			}
			
			ComplexData complexData = new ComplexData(file.getName(), img);
			
			// Image MIME type
			try {
				FileImageInputStream imgStream = new FileImageInputStream(file);
				Iterator<ImageReader> imgReader = ImageIO.getImageReaders(imgStream);
				imgStream.close();
				if (imgReader.hasNext()) {
					complexData.setMimeType("image/" + imgReader.next().getFormatName().toLowerCase());
				} else {
					log.warn("MIME type of " + file.getAbsolutePath() + " is not known");
				}
			}
			catch (FileNotFoundException e) {
				log.error("Image " + file.getAbsolutePath() + " was not found", e);
			}
			catch (IOException e) {
				log.error("Trying to determine MIME type of " + file.getAbsolutePath(), e);
			}
			
			obs.setComplexData(complexData);
		} else {
			// No other view supported
			// NOTE: if adding support for another view, don't forget to update supportedViews list above
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
				throw new APIException("Obs.error.unable.convert.complex.data", new Object[] { "input stream" }, e);
			}
		}
		
		if (img == null) {
			throw new APIException("Obs.error.cannot.save.complex", new Object[] { obs.getObsId() });
		}
		
		try {
			File outfile = getOutputFileToWrite(obs);
			
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
			throw new APIException("Obs.error.trying.write.complex", null, ioe);
		}
		
		return obs;
	}
	
}

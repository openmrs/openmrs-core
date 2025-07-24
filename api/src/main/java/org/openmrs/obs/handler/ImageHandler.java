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

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.storage.ObjectMetadata;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for storing basic images for complex obs to the file system. The image mime type used is
 * taken from the image name. if the .* image name suffix matches
 * {@link javax.imageio.ImageIO#getWriterFormatNames()} then that mime type will be used to save the
 * image. Images are stored in the location specified by the global property: "obs.complex_obs_dir"
 * 
 * @see org.openmrs.util.OpenmrsConstants#GLOBAL_PROPERTY_COMPLEX_OBS_DIR
 * @since 1.5
 */
@Component
public class ImageHandler extends AbstractHandler implements ComplexObsHandler {
	
	/** Views supported by this handler */
	private static final String[] supportedViews = { ComplexObsHandler.RAW_VIEW };
	
	private static final Logger log = LoggerFactory.getLogger(ImageHandler.class);
	
	private Set<String> extensions;
	
	/**
	 * Constructor initializes formats for alternative file names to protect from unintentionally
	 * overwriting existing files.
	 */
	public ImageHandler() {
		super();
		
		// Create a HashSet to quickly check for supported extensions.
		extensions = new HashSet<>();
		Collections.addAll(extensions, ImageIO.getWriterFormatNames());
	}
	
	/**
	 * Currently supports all views and puts the Image file data into the ComplexData object
	 * 
	 * @see org.openmrs.obs.ComplexObsHandler#getObs(org.openmrs.Obs, java.lang.String)
	 */
	@Override
	public Obs getObs(Obs obs, String view) {
		String key = parseDataKey(obs);
		
		// Raw image
		if (ComplexObsHandler.RAW_VIEW.equals(view)) {
			String mimeType = null;
			BufferedImage img = null;
			try (InputStream in = storageService.getData(key)) {
				ImageInputStream imageIn = ImageIO.createImageInputStream(in);
				Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageIn);
				if (imageReaders.hasNext()) {
					ImageReader imgReader = imageReaders.next();
					mimeType = "image/" + imgReader.getFormatName().toLowerCase();
					ImageReadParam param = imgReader.getDefaultReadParam();
					imgReader.setInput(imageIn, true, true);
					try {
						img = imgReader.read(0, param);
					} finally {
						imgReader.dispose();
					}
				}
			} catch (IOException e) {
				log.error("Trying to read file: {}", key, e);
				// Do not fail if image is missing
			}
			
			ComplexData complexData = new ComplexData(key, img);
			complexData.setMimeType(mimeType); // Set mimeType based on file content and not filename
			if (img != null) { // Do not inject if image is missing
				injectMissingMetadata(key, complexData);
			}
			complexData.setLength(null); // Reset as loaded image size is not equal to file size
			
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
	@Override
	public Obs saveObs(Obs obs) throws APIException {
		try {
			String[] splitTitle = obs.getComplexData().getTitle().split("\\|");
			String filename = splitTitle[0];
			if (splitTitle.length > 1) {
				filename = splitTitle[1];
			}
			String extension = FilenameUtils.getExtension(filename);
			String assignedKey = storageService.saveData((out) -> {
				Object data = obs.getComplexData().getData();
				
				InputStream in = null;
				if (data instanceof byte[]) {
					in = new ByteArrayInputStream((byte[]) data);
				} else if (data instanceof InputStream) {
					in = (InputStream) data;
				}
				
				BufferedImage img = null;
				if (in != null) {
					img = ImageIO.read(in);
				} else if (data instanceof BufferedImage) {
					img = (BufferedImage) data;
				}
				
				if (img == null) {
					throw new APIException("Obs.error.cannot.save.complex", new Object[] { obs.getObsId() });
				}
				ImageIO.write(img, extension, out);
				out.flush();
			}, ObjectMetadata.builder().setFilename(filename).build(), getObsDir());

			// Set the Title and URI for the valueComplex
			obs.setValueComplex(extension + " image |" + assignedKey);

			// Remove the ComlexData from the Obs
			obs.setComplexData(null);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		return obs;
	}
	
}

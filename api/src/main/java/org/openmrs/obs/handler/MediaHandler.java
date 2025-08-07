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

import jakarta.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.storage.ObjectMetadata;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for storing audio and video for complex obs to the file system. The mime type used is
 * probed from the file if possible. Media are stored in the location specified by the global
 * property: "obs.complex_obs_dir"
 *
 * @see org.openmrs.util.OpenmrsConstants#GLOBAL_PROPERTY_COMPLEX_OBS_DIR
 * @since 1.12
 */
@Component
public class MediaHandler extends AbstractHandler implements ComplexObsHandler {
	
	/** Views supported by this handler */
	private static final String[] supportedViews = { ComplexObsHandler.RAW_VIEW, };
	
	private static final Logger log = LoggerFactory.getLogger(MediaHandler.class);

	private final MimetypesFileTypeMap mimetypes = new MimetypesFileTypeMap();
	
	public MediaHandler() {
		super();
	}
	
	/**
	 * Currently supports all views and puts the media file data into the ComplexData object
	 *
	 * @see org.openmrs.obs.ComplexObsHandler#getObs(org.openmrs.Obs, java.lang.String)§
	 */
	@Override
	public Obs getObs(Obs obs, String view) {
		String key = parseDataKey(obs);
		
		// Raw media
		if (ComplexObsHandler.RAW_VIEW.equals(view)) {
			try {
				String[] names = obs.getValueComplex().split("\\|");
				String originalFilename = names[0];
				originalFilename = originalFilename.replace(",", "")
					.replace(" ", "");

				InputStream in = storageService.getData(key);
				ComplexData complexData = new ComplexData(originalFilename, in);
				
				complexData.setMimeType(mimetypes.getContentType(originalFilename));
				
				// Get the Mime Type and set it
				injectMissingMetadata(key, complexData);
				obs.setComplexData(complexData);
			}
			catch (IOException e) {
				log.error("Trying to create media file stream from {}", key, e);
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
	@Override
	public Obs saveObs(Obs obs) throws APIException {
		try {
			String filename = obs.getComplexData().getTitle();
			String key = storageService.saveData(outputStream -> {
				IOUtils.copy((InputStream) obs.getComplexData().getData(), outputStream);
				outputStream.flush();
			}, ObjectMetadata.builder().setFilename(filename).build(), getObsDir());
			
			obs.setComplexData(null);
			obs.setValueComplex(filename + "|" + key);
		}
		catch (IOException ioe) {
			throw new APIException("Obs.error.trying.write.complex", null, ioe);
		}
		
		return obs;
	}
	
}

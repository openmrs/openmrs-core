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

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.storage.ObjectMetadata;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.util.OpenmrsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Handler for storing generic binary data for complex obs to the file system.
 * 
 * @see OpenmrsConstants#GLOBAL_PROPERTY_COMPLEX_OBS_DIR
 * @since 1.8
 */
@Component
public class BinaryStreamHandler extends AbstractHandler implements ComplexObsHandler {
	
	/** Views supported by this handler */
	private static final String[] supportedViews = { ComplexObsHandler.RAW_VIEW, };
	
	private static final Logger log = LoggerFactory.getLogger(BinaryStreamHandler.class);
	
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
		String key = parseDataKey(obs);
			
		ComplexData complexData = null;
		// Raw stream
		if (ComplexObsHandler.RAW_VIEW.equals(view)) {
			try {
				String[] names = obs.getValueComplex().split("\\|");
				String originalFilename = names[0];
				originalFilename = originalFilename.replace(",", "").replace(" ", "");
					
				if (storageService.exists(key)) {
					InputStream in = storageService.getData(key);
					complexData = new ComplexData(originalFilename, in);
				} else {
					log.error("Unable to find file associated with complex obs {}", obs.getId());
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

		injectMissingMetadata(key, complexData);
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
	@Override
	public Obs saveObs(Obs obs) throws APIException {
		try {
			String key = storageService.saveData(outputStream -> {
				InputStream in = (InputStream) obs.getComplexData().getData();
				IOUtils.copy(in, outputStream);
				outputStream.flush();
			}, ObjectMetadata.builder().setFilename(obs.getComplexData().getTitle()).build(), getObsDir());
			// Store the filename in the Obs
			obs.setValueComplex(StringUtils.defaultIfBlank(obs.getComplexData().getTitle(), key) + "|" + key);
			obs.setComplexData(null);
		}
		catch (Exception e) {
			throw new APIException("Obs.error.writing.binary.data.complex", null, e);
		}
		
		return obs;
	}
	
}

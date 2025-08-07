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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.storage.ObjectMetadata;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Handler for storing files for complex obs to the file system. Files are stored in the location
 * specified by the global property: "obs.complex_obs_dir"
 * 
 * @since 1.5
 */
@Component("binaryDataHandler")
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
		String key = parseDataKey(obs);
		
		log.debug("value complex: {}", obs.getValueComplex());
		log.debug("file path: {}", key);
		ComplexData complexData = null;
		
		// Raw view (i.e. the file as is)
		if (ComplexObsHandler.RAW_VIEW.equals(view)) {
			// to handle problem with downloading/saving files with blank spaces or commas in their names
			// also need to remove the "file" text appended to the end of the file name
			String[] names = obs.getValueComplex().split("\\|");
			String originalFilename = names[0];
			originalFilename = originalFilename.replaceAll(",", "")
				.replaceAll(" ", "").replaceAll("file$", "");
			
			try (InputStream in = storageService.getData(key)){
				complexData = new ComplexData(originalFilename, IOUtils.toByteArray(in));
			}
			catch (IOException e) {
				log.error("Trying to read file: {}", key, e);
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
	 * TODO should this support a StringReader too?
	 * 
	 * @see org.openmrs.obs.ComplexObsHandler#saveObs(org.openmrs.Obs)
	 */
	@Override
	public Obs saveObs(Obs obs) throws APIException {
		// Get the buffered file  from the ComplexData.
		ComplexData complexData = obs.getComplexData();
		if (complexData == null) {
			log.error("Cannot save complex data where obsId={} because its ComplexData is null.", obs.getObsId());
			return obs;
		}
		try {
			Object data = obs.getComplexData().getData();
			ObjectMetadata metadata = new ObjectMetadata();
			if (data instanceof byte[]) {
				metadata.setLength((long) ((byte[]) data).length);	
			}
			metadata.setFilename(obs.getComplexData().getTitle());
			
			String key = storageService.saveData(outputStream -> {
				if (data instanceof byte[]) {
					IOUtils.write((byte[]) data, outputStream);
				} else if (InputStream.class.isAssignableFrom(data.getClass())) {
					IOUtils.copy((InputStream) data, outputStream);
				}
				outputStream.flush();
			}, metadata, getObsDir());

			// Set the Title and URI for the valueComplex
			obs.setValueComplex(StringUtils.defaultIfBlank(obs.getComplexData().getTitle(), key) + " file |" + key);

			// Remove the ComplexData from the Obs
			obs.setComplexData(null);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		return obs;
	}
	
}

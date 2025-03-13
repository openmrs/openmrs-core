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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
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
 * The in coming data are either char[] or java.io.Reader
 *
 */
@Component
public class TextHandler extends AbstractHandler implements ComplexObsHandler {
	
	/** Views supported by this handler */
	private static final String[] supportedViews = { ComplexObsHandler.TEXT_VIEW, ComplexObsHandler.RAW_VIEW,
	        ComplexObsHandler.URI_VIEW };
	
	private static final Logger log = LoggerFactory.getLogger(TextHandler.class);
	
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
	@Override
	public Obs getObs(Obs obs, String view) {
		String key = parseDataKey(obs);
		
		log.debug("value complex: {}", obs.getValueComplex());
		log.debug("file path: {}", key);
		ComplexData complexData = null;
		
		if (ComplexObsHandler.TEXT_VIEW.equals(view) || ComplexObsHandler.RAW_VIEW.equals(view)) {
			// to handle problem with downloading/saving files with blank spaces or commas in their names
			// also need to remove the "file" text appended to the end of the file name
			String[] names = obs.getValueComplex().split("\\|");
			String originalFilename = names[0];
			originalFilename = originalFilename.replaceAll(",", "")
				.replaceAll(" ", "").replaceAll("file$", "");
			
			try (InputStream is = storageService.getData(key)){
				complexData = ComplexObsHandler.RAW_VIEW.equals(view) ? new ComplexData(originalFilename, 
					IOUtils.toByteArray(is)) : new ComplexData(originalFilename, 
					IOUtils.toString(is, StandardCharsets.UTF_8));
			}
			catch (IOException e) {
				log.error("Trying to read file: {}", key, e);
			}
		} else if (ComplexObsHandler.URI_VIEW.equals(view)) {
			complexData = new ComplexData(parseDataTitle(obs), key);
		} else {
			// No other view supported
			// NOTE: if adding support for another view, don't forget to update supportedViews list above
			return null;
		}
		Assert.notNull(complexData, "Complex data must not be null");
		
		// Get the Mime Type and set it
		ObjectMetadata metadata;
		try {
			metadata = storageService.getMetadata(key);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		String mimeType = metadata.getMimeType();
		mimeType = !(mimeType.equals("application/octet-stream")) ? mimeType : "text/plain";
		complexData.setMimeType(mimeType);
		complexData.setLength(metadata.getLength());
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
			log.error("Cannot save complex data where obsId={} because its ComplexData is null.", obs.getObsId());
			return obs;
		}
		try {
			String assignedKey = storageService.saveData((out) -> {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

				Object data = obs.getComplexData().getData();
				if (data instanceof char[]) {
					writer.write((char[]) data);
				} else if (Reader.class.isAssignableFrom(data.getClass())) {
					try (Reader reader = new BufferedReader((Reader) data)){
						IOUtils.copy(reader, writer);
					}
					catch (IOException e) {
						throw new APIException("Obs.error.unable.convert.complex.data", new Object[] { "Reader" }, e);
					}
				} else if (InputStream.class.isAssignableFrom(data.getClass())) {
					try (Reader reader = new BufferedReader(new InputStreamReader((InputStream) data, 
						StandardCharsets.UTF_8))) {
						IOUtils.copy(reader, writer);
					}
					catch (IOException e) {
						throw new APIException("Obs.error.unable.convert.complex.data", new Object[] { "input stream" }, e);
					}
				}
				writer.flush();
			}, ObjectMetadata.builder().setFilename(obs.getComplexData().getTitle()).build(),  getObsDir());
			
			// Set the Title and URI for the valueComplex
			obs.setValueComplex(obs.getComplexData().getTitle() + " file |" + assignedKey);
			
			// Remove the ComplexData from the Obs
			obs.setComplexData(null);
			
		}
		catch (IOException ioe) {
			throw new APIException("Obs.error.trying.write.complex", null, ioe);
		}
		
		return obs;
	}
	
}

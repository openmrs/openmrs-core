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
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.StorageService;
import org.openmrs.api.context.Context;
import org.openmrs.api.storage.ObjectMetadata;
import org.openmrs.obs.ComplexData;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract handler for some convenience methods Files are stored in the location specified by the
 * global property: "obs.complex_obs_dir"
 * 
 * @since 1.5
 */
public class AbstractHandler {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractHandler.class);
	
	@Autowired
	StorageService storageService;
	
	@Autowired
	AdministrationService adminService;
	
	public AbstractHandler() {
	}
	
	public AbstractHandler(AdministrationService adminService, StorageService storageService) {
		this();
		this.adminService = adminService;
		this.storageService = storageService;
	}

	/**
	 * @return obs dir
	 * @since 2.8.0
	 */
	public String getObsDir() {
		return adminService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR);
	}

	/**
	 * @see org.openmrs.obs.ComplexObsHandler#getObs(Obs, String)
	 */
	public Obs getObs(Obs obs, String view) {
		String key = parseNewOrLegacyKey(obs);
		
		byte[] bytes;
		try (InputStream is = storageService.getData(key)) {
			bytes = IOUtils.toByteArray(is);
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		ComplexData complexData = new ComplexData(parseDataTitle(obs), bytes);
		injectMissingMetadata(key, complexData);
		obs.setComplexData(complexData);
		return obs;
	}
	
	public Obs saveObs(Obs obs) throws APIException {
		try {
			byte[] data = (byte[]) obs.getComplexData().getData();
			
			String key = storageService.saveData(outputStream -> {
				if (outputStream == null) System.out.println("outputStream is null");
				if (obs == null) System.out.println("obs is null");
				if (obs.getComplexData() == null) System.out.println("obs.complexData is null");
				IOUtils.write( data, outputStream);
			}, ObjectMetadata.builder().setLength((long) data.length).build(), getObsDir());
			// Store the filename in the Obs
			obs.setValueComplex(StringUtils.defaultIfBlank(obs.getComplexData().getTitle(), key) + "|" + key);
			obs.setComplexData(null);
		}
		catch (Exception e) {
			throw new APIException("Obs.error.writing.binary.data.complex", null, e);
		}

		return obs;
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#purgeComplexData(org.openmrs.Obs)
	 */
	public boolean purgeComplexData(Obs obs) {
		String key = parseNewOrLegacyKey(obs);
		
		try {
			if (storageService.purgeData(key)) {
				obs.setComplexData(null);
				return true;
			} else {
				log.warn("Could not delete complex data object for obsId={} located at {}", obs.getObsId(), key);
				return false;
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Should be replaced with just parseDataKey once migration is over.
	 * 
	 * @param obs complex obs
	 * @return key
	 * @deprecated 2.8.0
	 * @since 2.8.0
	 */
	@Deprecated
	public String parseNewOrLegacyKey(Obs obs) {
		String key = parseDataKey(obs);
		if (!storageService.exists(key)) {
			key = getObsDir() + File.separator + key;
		}
		return key;
	}

	/**
	 * Convenience method to create and return a file for the stored ComplexData.data Object
	 * 
	 * @param obs
	 * @return File object
	 * @deprecated since 2.8.0
	 */
	public static File getComplexDataFile(Obs obs) {
		log.warn("Deprecated getComplexDataFile() called. Accessing legacy storage.");
		String filename = parseDataKey(obs);
		File dir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(
		    Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
		return new File(dir, filename);
	}

	/**
	 * @param obs the obs
	 * @return file key
	 * @since 2.8.0
	 */
	public static String parseDataKey(Obs obs) {
		String[] names = obs.getValueComplex().split("\\|");
		return names.length < 2 ? names[0] : names[names.length - 1];
	}

	/**
	 * @param obs the obs
	 * @return file title
	 * @since 2.8.0
	 */
	public static String parseDataTitle(Obs obs) {
		String[] names = obs.getValueComplex().split("\\|");
		return names[0];
	}

	protected void injectMissingMetadata(String key, ComplexData complexData) {
		try {
			ObjectMetadata metadata = storageService.getMetadata(key);
			
			if (complexData.getMimeType() == null) {
				complexData.setMimeType(metadata.getMimeType());
			}
			complexData.setLength(metadata.getLength());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * @see org.openmrs.obs.ComplexObsHandler#getSupportedViews()
	 */
	public String[] getSupportedViews() {
		return new String[0];
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#supportsView(java.lang.String)
	 */
	public boolean supportsView(String view) {
		return Arrays.asList(getSupportedViews()).contains(view);
	}
	
}

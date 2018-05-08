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
import java.text.NumberFormat;
import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.obs.ComplexData;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract handler for some convenience methods Files are stored in the location specified by the
 * global property: "obs.complex_obs_dir"
 * 
 * @since 1.5
 */
public class AbstractHandler {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractHandler.class);
	
	protected NumberFormat nf;
	
	/**
	 * Constructor initializes formats for alternative file names to protect from unintentionally
	 * overwriting existing files.
	 */
	public AbstractHandler() {
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(0);
		nf.setMinimumIntegerDigits(2);
	}
	
	/**
	 * Returns a {@link File} for the given obs complex data to be written to. The output file
	 * location is determined off of the {@link OpenmrsConstants#GLOBAL_PROPERTY_COMPLEX_OBS_DIR}
	 * and the file name is determined off the current obs.getComplexData().getTitle().
	 * 
	 * @param obs the Obs with a non-null complex data on it
	 * @return File that the complex data should be written to
	 */
	public File getOutputFileToWrite(Obs obs) throws IOException {
		String title = obs.getComplexData().getTitle();
		String titleWithoutExtension = FilenameUtils.removeExtension(title);
		String extension = "." + StringUtils.defaultIfEmpty(FilenameUtils.getExtension(title), "dat");
		String uuid = obs.getUuid();
		String filename;
		
		if (StringUtils.isNotBlank(titleWithoutExtension)) {
			filename = titleWithoutExtension + "_" + uuid + extension;
		} else {
			filename = uuid + extension;
		}
		
		File dir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(
		    Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
		File outputfile = new File(dir, filename);
		
		return outputfile;
	}
	
	/**
	 * Get the extension for a given filename if it exists, else return the filename. If there is no
	 * filename in the input string, "raw" is returned. 
	 * 
	 * If given "asdf.jpg", will return "jpg".
	 * If given "asdf", will return "asdf". 
	 * If given "" or "a/b/c/" will return "raw".
	 * 
	 * @param filename
	 * @return the part after the period in the given filename, the filename, or "raw"
	 * @deprecated since 2.1.3 use {@link org.apache.commons.io.FilenameUtils#getExtension(String)}
	 *             instead.
	 */
	@Deprecated
	public String getExtension(String filename) {
		String result = FilenameUtils.getExtension(filename);
		
		if (StringUtils.isEmpty(result)) {
			result = FilenameUtils.getBaseName(filename);
			
			if (StringUtils.isEmpty(result)) {
				result = "raw";
			}
		}
		
		return result;
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#getObs(Obs, String)
	 */
	public Obs getObs(Obs obs, String view) {
		File file = BinaryDataHandler.getComplexDataFile(obs);
		log.debug("value complex: " + obs.getValueComplex());
		log.debug("file path: " + file.getAbsolutePath());
		ComplexData complexData = null;
		try {
			complexData = new ComplexData(file.getName(), OpenmrsUtil.getFileAsBytes(file));
		}
		catch (IOException e) {
			log.error("Trying to read file: " + file.getAbsolutePath(), e);
		}
		String mimeType = OpenmrsUtil.getFileMimeType(file);
		complexData.setMimeType(mimeType);
		obs.setComplexData(complexData);
		
		return obs;
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#purgeComplexData(org.openmrs.Obs)
	 */
	public boolean purgeComplexData(Obs obs) {
		File file = getComplexDataFile(obs);
		if (!file.exists()) {
			return true;
		} else if (file.delete()) {
			obs.setComplexData(null);
			return true;
		}
		
		log.warn(
		    "Could not delete complex data object for obsId=" + obs.getObsId() + " located at " + file.getAbsolutePath());
		return false;
	}
	
	/**
	 * Convenience method to create and return a file for the stored ComplexData.data Object
	 * 
	 * @param obs
	 * @return File object
	 */
	public static File getComplexDataFile(Obs obs) {
		String[] names = obs.getValueComplex().split("\\|");
		String filename = names.length < 2 ? names[0] : names[names.length - 1];
		File dir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(
		    Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
		return new File(dir, filename);
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

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.obs.ComplexData;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * Abstract handler for some convenience methods Files are stored in the location specified by the
 * global property: "obs.complex_obs_dir"
 * 
 * @since 1.5
 */
public class AbstractHandler {
	
	public static final Log log = LogFactory.getLog(AbstractHandler.class);
	
	protected NumberFormat nf;
	
	protected SimpleDateFormat longfmt;
	
	/**
	 * Constructor initializes formats for alternative file names to protect from unintentionally
	 * overwriting existing files.
	 */
	public AbstractHandler() {
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(0);
		nf.setMinimumIntegerDigits(2);
		longfmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
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
		// Get the title and remove the extension.
		String t = obs.getComplexData().getTitle();
		
		String extension = getExtension(t);
		String title = obs.getComplexData().getTitle();
		
		File dir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
		File outputfile = null;
		
		// Get the output stream
		if (null == title) {
			String now = longfmt.format(new Date());
			outputfile = new File(dir, now);
		} else {
			title = title.replace("." + extension, "");
			outputfile = new File(dir, title + "." + extension);
			// outputfile = new File(dir, title);
		}
		
		int i = 0;
		String tmp = null;
		
		// If the Obs does not exist, but the File does, append a two-digit
		// count number to the filename and save it.
		while (obs.getObsId() == null && outputfile.exists() && i < 100) {
			tmp = null;
			// Remove the extension from the filename.
			tmp = String.valueOf(outputfile.getAbsolutePath().replace("." + extension, ""));
			outputfile = null;
			// Append two-digit count number to the filename.
			String filename = (i < 1) ? tmp + "_" + nf.format(Integer.valueOf(++i)) : tmp.replace(nf.format(Integer
			        .valueOf(i)), nf.format(Integer.valueOf(++i)));
			// Append the extension to the filename.
			outputfile = new File(filename + "." + extension);
		}
		
		return outputfile;
		
	}
	
	/**
	 * Get the extension for a given filename. <br/>
	 * If given "asdf.jpg", will return "jpg". <br/>
	 * If given "asdf", will return "asdf". <br/>
	 * 
	 * @param filename
	 * @return the filepart after the period in the given filename
	 */
	public String getExtension(String filename) {
		String[] filenameParts = filename.split("\\.");
		
		log.debug("titles length: " + filenameParts.length);
		
		String extension = (filenameParts.length < 2) ? filenameParts[0] : filenameParts[filenameParts.length - 1];
		extension = StringUtils.isNotEmpty(extension) ? extension : "raw";
		
		return extension;
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
		
		obs.setComplexData(complexData);
		
		return obs;
	}
	
	/**
	 * @see org.openmrs.obs.ComplexObsHandler#purgeComplexData(org.openmrs.Obs)
	 */
	public boolean purgeComplexData(Obs obs) {
		File file = getComplexDataFile(obs);
		if (file.exists() && file.delete()) {
			obs.setComplexData(null);
			// obs.setValueComplex(null);
			return true;
		}
		
		log.warn("Could not delete complex data object for obsId=" + obs.getObsId() + " located at "
		        + file.getAbsolutePath());
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
		File dir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(Context.getAdministrationService().getGlobalProperty(
		    OpenmrsConstants.GLOBAL_PROPERTY_COMPLEX_OBS_DIR));
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

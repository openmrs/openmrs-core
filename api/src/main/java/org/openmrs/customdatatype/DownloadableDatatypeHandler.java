/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.customdatatype;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Indicates that a CustomDatatypeHandler allows underlying custom values to be downloaded or streamed in some
 * meaningful way.
 */
public interface DownloadableDatatypeHandler<T> extends CustomDatatypeHandler<CustomDatatype<T>, T> {
	
	/**
	 * @param dt the datatype this handler handles
	 * @param valueReference custom value to be downloaded
	 * @return the MIME type that should be communicated to any client downloading the given custom value.
	 * Should not return null.
	 */
	String getContentType(CustomDatatype<T> dt, String valueReference);
	
	/**
	 * @param dt the datatype this handler handles
	 * @param valueReference custom value to be downloaded
	 * @return the filename that should be communicated to any client downloading the given custom value.
	 * May return null, in which case the framework will choose a meaningless filename.
	 */
	String getFilename(CustomDatatype<T> dt, String valueReference);
	
	/**
	 * Writes the custom value to os. Implementations should assume that the caller has set up the stream,
	 * calling getMimeType and getFilename if necessary.
	 * Implementations should assume that the caller has taken care of any needed buffering on os.
	 * 
	 * @param dt the datatype this handler handles 
	 * @param valueReference custom value to be written to os 
	 * @param os the output stream to be written to
	 * @throws IOException if there is an IO error writing to the stream 
	 */
	void writeToStream(CustomDatatype<T> dt, String valueReference, OutputStream os) throws IOException;
	
}

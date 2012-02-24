/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.customdatatype;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

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

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.obs;

import java.io.InputStream;

/**
 * ComplexObs is a transient Object that extends Obs but is not itself persisted in the database. It
 * has a data Object and a title. Alternatively, it can have a byte array in the Object. <br>
 * <br>
 * Most handlers should support this data Object being an {@link InputStream}, at least prior to
 * saving the object.<br>
 * <br>
 * On pulling data out, the format is per-handler defined and the page doing the viewing should know
 * how to handle it.
 */
public class ComplexData implements java.io.Serializable {
	
	public static final long serialVersionUID = 345734100L;
	
	private Object data;
	
	private String title;
	
	private String mimeType;
	
	private Long length;
	
	/**
	 * Default constructor requires title and data.
	 * 
	 * @param title Name or brief description of ComplexData.
	 * @param data The complex data for an Obs
	 */
	public ComplexData(String title, Object data) {
		setTitle(title);
		setData(data);
	}
	
	/**
	 * Set the title for this ComplexData
	 * 
	 * @param title
	 */
	private void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Get the title for this ComplexData
	 * 
	 * @return the title as a <code>String</code>
	 */
	public String getTitle() {
		return this.title;
	}
	
	/**
	 * Set the data Object.
	 * 
	 * @param data
	 */
	private void setData(Object data) {
		this.data = data;
	}
	
	/**
	 * Get the data Object. If this was initialized with a byte array, the output may not be
	 * reliable. TODO: Should this even try to return the byte array?
	 * 
	 * @return the data as an <code>Object</code>
	 */
	public Object getData() {
		return this.data;
	}
	
	/**
	 * Set the data MIME type
	 * 
	 * @param mimeType
	 * @since 1.12
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	/**
	 * Get the data MIME type
	 * 
	 * @return data MIME type
	 * @since 1.12
	 */
	public String getMimeType() {
		return this.mimeType;
	}
	
	/**
	 * Set the data length
	 *
	 * @param length
	 * @since 1.12
	 */
	public void setLength(Long length) {
		this.length = length;
	}
	
	/**
	 * Get the data length
	 *
	 * @return data length
	 * @since 1.12
	 */
	public Long getLength() {
		return this.length;
	}
	
}

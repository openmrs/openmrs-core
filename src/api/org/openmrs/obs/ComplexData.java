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
package org.openmrs.obs;

import java.io.InputStream;

/**
 * ComplexObs is a transient Object that extends Obs but is not itself persisted in the database. It
 * has a data Object and a title. Alternatively, it can have a byte array in the Object. <br/>
 * <br/>
 * Most handlers should support this data Object being an {@link InputStream}, at least prior to
 * saving the object.<br/>
 * <br/>
 * On pulling data out, the format is per-handler defined and the page doing the viewing should know
 * how to handle it.
 */
public class ComplexData implements java.io.Serializable {
	
	public static final long serialVersionUID = 345734100L;
	
	private Object data;
	
	private String title;
	
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
	
}

/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

/**
 * MimeType
 * 
 * @deprecated This class is no longer used. Mimetypes are determined by the handler for a
 *             ConceptComplex now.
 */
@Deprecated
public class MimeType extends BaseOpenmrsMetadata implements java.io.Serializable {
	
	public static final long serialVersionUID = 8765L;
	
	// Fields
	
	private Integer mimeTypeId;
	
	private String mimeType;
	
	// Constructors
	
	/** default constructor */
	public MimeType() {
	}
	
	/** constructor with id */
	public MimeType(Integer mimeTypeId) {
		this.mimeTypeId = mimeTypeId;
	}
	
	// Property accessors
	
	/**
	 * @return Returns the mimeTypeId.
	 */
	public Integer getMimeTypeId() {
		return mimeTypeId;
	}
	
	/**
	 * @param mimeTypeId The mimeTypeId to set.
	 */
	public void setMimeTypeId(Integer mimeTypeId) {
		this.mimeTypeId = mimeTypeId;
	}
	
	/**
	 * @return Returns the mimeType.
	 */
	public String getMimeType() {
		return mimeType;
	}
	
	/**
	 * @param mimeType The mimeType to set.
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		
		return getMimeTypeId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setMimeTypeId(id);
		
	}
	
}

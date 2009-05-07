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
package org.openmrs.hl7;

import java.io.Serializable;

import org.openmrs.BaseOpenmrsMetadata;

/**
 * Names a unique location that hl7 messages could be coming from.
 */
public class HL7Source extends BaseOpenmrsMetadata implements Serializable {
	
	private static final long serialVersionUID = 3062136520728193223L;
	
	private Integer hl7SourceId;
	
	/**
	 * @return Returns the hl7SourceId.
	 */
	public Integer getHL7SourceId() {
		return hl7SourceId;
	}
	
	/**
	 * @param hl7SourceId The hl7SourceId to set.
	 */
	public void setHL7SourceId(Integer hl7SourceId) {
		this.hl7SourceId = hl7SourceId;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		return getHL7SourceId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setHL7SourceId(id);
	}
	
}

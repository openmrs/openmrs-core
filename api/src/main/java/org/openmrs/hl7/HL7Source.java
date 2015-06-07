/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
	 * Empty constructor
	 */
	public HL7Source() {
	}
	
	/**
	 * Generic constructor
	 * 
	 * @param hl7SourceId primary key id
	 */
	public HL7Source(Integer hl7SourceId) {
		this.hl7SourceId = hl7SourceId;
	}
	
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

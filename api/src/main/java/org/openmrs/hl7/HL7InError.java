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

import org.openmrs.api.APIException;

/**
 * Represents a error in processing an hl7 message.
 *
 * @see HL7InQueue
 * @see HL7Service
 */
public class HL7InError extends HL7QueueItem {
	
	private static final int MAX_ERROR_DETAILS_LENGTH = 16777215;
	
	private Integer hl7InErrorId;
	
	private String error;
	
	private String errorDetails;
	
	/**
	 * Default constructor
	 */
	public HL7InError() {
		
	}
	
	/**
	 * Convenience constructor to help convert a queue item into an exception
	 */
	public HL7InError(HL7InQueue hl7InQueue) {
		setHL7Source(hl7InQueue.getHL7Source());
		setHL7SourceKey(hl7InQueue.getHL7SourceKey());
		setHL7Data(hl7InQueue.getHL7Data());
	}
	
	/**
	 * @return Returns the hl7InErrorId.
	 */
	public Integer getHL7InErrorId() {
		return hl7InErrorId;
	}
	
	/**
	 * @param hl7InErrorId The hl7InExceptionId to set.
	 */
	public void setHL7InErrorId(Integer hl7InErrorId) {
		this.hl7InErrorId = hl7InErrorId;
	}
	
	/**
	 * @return Returns the error.
	 */
	public String getError() {
		return error;
	}
	
	/**
	 * @param error The error to set.
	 */
	public void setError(String error) {
		this.error = error;
	}
	
	/**
	 * @return Returns the errorDetails.
	 */
	public String getErrorDetails() {
		return errorDetails;
	}
	
	/**
	 * @param errorDetails The errorDetails to set.
	 */
	public void setErrorDetails(String errorDetails) {
		if (errorDetails != null && errorDetails.length() > MAX_ERROR_DETAILS_LENGTH) {
			throw new APIException("Hl7inError.error.details.length", new Object[] { errorDetails.length(),
			        MAX_ERROR_DETAILS_LENGTH });
		}
		this.errorDetails = errorDetails;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 * @since 1.5
	 */
	public Integer getId() {
		return getHL7InErrorId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 * @since 1.5
	 */
	public void setId(Integer id) {
		setHL7InErrorId(id);
	}
	
}

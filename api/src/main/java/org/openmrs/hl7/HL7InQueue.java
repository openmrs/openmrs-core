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

/**
 * Represents an hl7 message that has yet to be processed.
 * 
 * @see HL7Service
 */
public class HL7InQueue extends HL7QueueItem {
	
	private static final long serialVersionUID = 8882704913734764446L;
	
	private Integer hl7InQueueId;
	
	private String errorMessage;
	
	private Integer messageState;
	
	/**
	 * Default constructor
	 */
	public HL7InQueue() {
	}
	
	/**
	 * Convenience constructor to build queue from a previously deleted queue entry
	 * 
	 * @param hl7InArchive deleted entry from which queue entry will be constructed
	 * @since 1.5
	 */
	public HL7InQueue(HL7InArchive hl7InArchive) {
		setHL7Source(hl7InArchive.getHL7Source());
		setHL7SourceKey(hl7InArchive.getHL7SourceKey());
		setHL7Data(hl7InArchive.getHL7Data());
		setMessageState(HL7Constants.HL7_STATUS_PENDING);
	}
	
	/**
	 * Convenience constructor to build queue from a previously erred queue entry
	 * 
	 * @param hl7InError erred entry from which queue entry will be constructed
	 */
	public HL7InQueue(HL7InError hl7InError) {
		setHL7Source(hl7InError.getHL7Source());
		setHL7SourceKey(hl7InError.getHL7SourceKey());
		setHL7Data(hl7InError.getHL7Data());
		
	}
	
	/**
	 * @return Returns the hl7InQueueId.
	 */
	public Integer getHL7InQueueId() {
		return hl7InQueueId;
	}
	
	/**
	 * @param hl7InQueueId The hl7InQueueId to set.
	 */
	public void setHL7InQueueId(Integer hl7InQueueId) {
		this.hl7InQueueId = hl7InQueueId;
	}
	
	/**
	 * @return Returns the errorMessage.
	 * @since 1.5
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	
	/**
	 * @param errorMessage The errorMessage to set.
	 * @since 1.5
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	/**
	 * Can be one of the states in the {@link HL7Constants} file.
	 * 
	 * @return Returns the message State.
	 * @see HL7Constants#HL7_STATUS_PENDING
	 * @see HL7Constants#HL7_STATUS_ERROR
	 * @see HL7Constants#HL7_STATUS_PROCESSED
	 * @see HL7Constants#HL7_STATUS_PROCESSING
	 * @see HL7Constants#HL7_STATUS_DELETED
	 */
	public Integer getMessageState() {
		return messageState;
	}
	
	/**
	 * @param messageState The message State to set.
	 */
	public void setMessageState(Integer messageState) {
		this.messageState = messageState;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 * @since 1.5
	 */
	@Override
	public Integer getId() {
		return getHL7InQueueId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 * @since 1.5
	 */
	@Override
	public void setId(Integer id) {
		setHL7InQueueId(id);
	}
	
}

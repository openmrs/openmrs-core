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
import java.util.Date;

import org.openmrs.BaseOpenmrsObject;

/**
 * Represents an hl7 message that has yet to be processed.
 * 
 * @see HL7Service
 */
public class HL7InQueue extends BaseOpenmrsObject implements Serializable {
	
	private static final long serialVersionUID = 8882704913734764446L;
	
	private Integer hl7InQueueId;
	
	private HL7Source hl7Source;
	
	private String hl7SourceKey;
	
	private String hl7Data;
	
	private String errorMessage;
	
	private Integer messageState;
	
	private Date dateCreated;
	
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
	
	public boolean equals(Object obj) {
		if (obj instanceof HL7InQueue) {
			HL7InQueue hl7InQueue = (HL7InQueue) obj;
			if (this.getHL7InQueueId() != null && hl7InQueue.getHL7InQueueId() != null)
				return this.getHL7InQueueId().equals(hl7InQueue.getHL7InQueueId());
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getHL7InQueueId() == null)
			return super.hashCode();
		int hash = 7;
		hash = 37 * hash + this.getHL7InQueueId().hashCode();
		return hash;
	}
	
	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}
	
	/**
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @return Returns the hl7Data.
	 */
	public String getHL7Data() {
		return hl7Data;
	}
	
	/**
	 * @param hl7Data The hl7Data to set.
	 */
	public void setHL7Data(String hl7Data) {
		this.hl7Data = hl7Data;
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
	 * @return Returns the hl7Source.
	 */
	public HL7Source getHL7Source() {
		return hl7Source;
	}
	
	/**
	 * @param hl7Source The hl7Source to set.
	 */
	public void setHL7Source(HL7Source hl7Source) {
		this.hl7Source = hl7Source;
	}
	
	/**
	 * @return Returns the hl7SourceKey.
	 */
	public String getHL7SourceKey() {
		return hl7SourceKey;
	}
	
	/**
	 * @param hl7SourceKey The hl7SourceKey to set.
	 */
	public void setHL7SourceKey(String hl7SourceKey) {
		this.hl7SourceKey = hl7SourceKey;
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
	public Integer getId() {
		return getHL7InQueueId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 * @since 1.5
	 */
	public void setId(Integer id) {
		setHL7InQueueId(id);
	}
	
}

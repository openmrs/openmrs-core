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

import java.util.Date;

import org.openmrs.BaseOpenmrsObject;

/**
 * Represents a successfully processed hl7 message.
 * 
 * @see HL7InQueue
 * @see HL7Service
 */
public class HL7InArchive extends BaseOpenmrsObject {
	
	private int hl7InArchiveId;
	
	private HL7Source hl7Source;
	
	private String hl7SourceKey;
	
	private String hl7Data;
	
	private Integer messageState;
	
	private Date dateCreated;

	private boolean loaded = false;
	
	/**
	 * Default constructor
	 */
	public HL7InArchive() {
	}
	
	/**
	 * Convenience constructor to build archive from an existing queue entry
	 * 
	 * @param hl7InQueue queue entry from which archive entry will be constructed
	 */
	public HL7InArchive(HL7InQueue hl7InQueue) {
		setHL7Source(hl7InQueue.getHL7Source());
		setHL7SourceKey(hl7InQueue.getHL7SourceKey());
		setHL7Data(hl7InQueue.getHL7Data());
		setMessageState(HL7Constants.HL7_STATUS_PROCESSED);
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
	 * @return Returns the hl7InArchiveId.
	 */
	public int getHL7InArchiveId() {
		return hl7InArchiveId;
	}
	
	/**
	 * @param hl7InArchiveId The hl7InArchiveId to set.
	 */
	public void setHL7InArchiveId(int hl7InArchiveId) {
		this.hl7InArchiveId = hl7InArchiveId;
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
	 * @return Returns message state.
	 * @since 1.5
	 */
	public Integer getMessageState() {
		return messageState;
	}
	
	/**
	 * @param messageState The message source to set.
	 * @since 1.5
	 */
	public void setMessageState(Integer messageState) {
		this.messageState = messageState;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 * @since 1.5
	 */
	public Integer getId() {
		return getHL7InArchiveId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 * @since 1.5
	 */
	public void setId(Integer id) {
		setHL7InArchiveId(id);
	}

	/**
	 * describes whether hl7 data has been loaded from the filesystem
	 * 
	 * @since 1.7
	 * @return the loaded status (true or false)
	 */
	public boolean isLoaded() {
		return loaded;
	}

	/**
	 * sets the flag for hl7 data having been loaded from the filesystem
	 * 
	 * @since 1.7
	 * @param the status to set 
	 */
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

}

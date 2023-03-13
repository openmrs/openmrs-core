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
 * Represents a successfully processed hl7 message.
 * 
 * @see HL7InQueue
 * @see HL7Service
 */
public class HL7InArchive extends HL7QueueItem {
	
	private int hl7InArchiveId;
	
	private Integer messageState;
	
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
	@Override
	public Integer getId() {
		return getHL7InArchiveId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 * @since 1.5
	 */
	@Override
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
	 * @param loaded status to set
	 */
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}
	
}

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

import java.util.Date;

import org.hibernate.envers.Audited;
import org.openmrs.BaseOpenmrsObject;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

/**
 * Common representation of {@link HL7InQueue}, {@link HL7InArchive} and {@link HL7InError}.
 * 
 * @since 1.9
 * @see HL7InQueue
 * @see HL7InArchive
 * @see HL7InError
 */
@MappedSuperclass
@Audited
public abstract class HL7QueueItem extends BaseOpenmrsObject {

	@ManyToOne(optional = false)
	@JoinColumn(name = "hl7_source")
	private HL7Source hl7Source;
	
	@Column(name = "hl7_source_key", length = 1024)
	private String hl7SourceKey;
	
	@Column(name = "hl7_data", nullable = false, length = 65535)
	@Lob
	private String hl7Data;

	@Column(name = "date_created", nullable = false, length = 19)
	private Date dateCreated;
	
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
	
}

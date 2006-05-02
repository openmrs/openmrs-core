package org.openmrs.hl7;

import java.io.Serializable;
import java.util.Date;

public class HL7InQueue implements Serializable {

	private static final long serialVersionUID = 8882704913734764446L;

	private Integer hl7InQueueId;
	private HL7Source hl7Source;
	private String hl7SourceKey;
	private String hl7Data;
	private Date dateCreated;

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated
	 *            The dateCreated to set.
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
	 * @param hl7Data
	 *            The hl7Data to set.
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
	 * @param hl7InQueueId
	 *            The hl7InQueueId to set.
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
	 * @param hl7Source
	 *            The hl7Source to set.
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
	 * @param hl7SourceKey
	 *            The hl7SourceKey to set.
	 */
	public void setHL7SourceKey(String hl7SourceKey) {
		this.hl7SourceKey = hl7SourceKey;
	}

}

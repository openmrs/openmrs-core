package org.openmrs.hl7;

import java.io.Serializable;
import java.util.Date;

import org.openmrs.User;

public class HL7Source implements Serializable {

	private static final long serialVersionUID = 3062136520728193223L;

	private Integer hl7SourceId;
	private String name;
	private String description;
	private User creator;
	private Date dateCreated;

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator
	 *            The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

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
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the hl7SourceId.
	 */
	public Integer getHL7SourceId() {
		return hl7SourceId;
	}

	/**
	 * @param hl7SourceId
	 *            The hl7SourceId to set.
	 */
	public void setHL7SourceId(Integer hl7SourceId) {
		this.hl7SourceId = hl7SourceId;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

}

package org.openmrs;

import java.util.Set;

/**
 * MimeType 
 */
public class MimeType implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private String mimeType;
	private String description;
	private Set complexObs;

	// Constructors

	/** default constructor */
	public MimeType() {
	}

	/** constructor with id */
	public MimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	// Property accessors

	/**
	 * 
	 */
	public String getMimeType() {
		return this.mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * 
	 */
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 
	 */
	public Set getComplexObs() {
		return this.complexObs;
	}

	public void setComplexObs(Set complexObs) {
		this.complexObs = complexObs;
	}

}
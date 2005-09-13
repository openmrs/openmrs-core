package org.openmrs;

/**
 * MimeType 
 */
public class MimeType implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private String mimeType;
	private String description;

	// Constructors

	/** default constructor */
	public MimeType() {
	}

	/** constructor with id */
	public MimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public boolean equals(Object obj) {
		if (obj instanceof MimeType) {
			MimeType m = (MimeType)obj;
			return (this.getMimeType().matches(m.getMimeType()) &&
					this.getDescription().matches(m.getDescription()));
		}
		return false;
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
}
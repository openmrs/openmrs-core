package org.openmrs;

/**
 * MimeType 
 */
public class MimeType implements java.io.Serializable {

	public static final long serialVersionUID = 8765L;

	// Fields

	private Integer mimeTypeId;
	private String mimeType;
	private String description;

	// Constructors

	/** default constructor */
	public MimeType() {
	}

	/** constructor with id */
	public MimeType(Integer mimeTypeId) {
		this.mimeTypeId = mimeTypeId;
	}

	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof MimeType) {
			MimeType m = (MimeType)obj;
			return (mimeTypeId.equals(m.getMimeTypeId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getMimeTypeId() == null) return super.hashCode();
		return this.getMimeTypeId().hashCode();
	}

	// Property accessors

	/**
	 * @return Returns the mimeTypeId.
	 */
	public Integer getMimeTypeId() {
		return mimeTypeId;
	}

	/**
	 * @param mimeTypeId The mimeTypeId to set.
	 */
	public void setMimeTypeId(Integer mimeTypeId) {
		this.mimeTypeId = mimeTypeId;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the mimeType.
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @param mimeType The mimeType to set.
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

}
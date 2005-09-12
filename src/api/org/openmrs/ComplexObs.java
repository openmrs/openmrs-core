package org.openmrs;

/**
 * ComplexObs 
 */
public class ComplexObs extends Obs implements java.io.Serializable{
	
	public static final long serialVersionUID = 1L;

	// Fields

	private MimeType mimeType;
	private String urn;
	private String complexValue; 

	// Constructors

	/** default constructor */
	public ComplexObs() {
	}

	/** constructor with id */
	public ComplexObs(Integer obsId) {
		this.obsId = obsId;
	}
	
	/** 
	 * overriding parent 
	 * @return true/false whether this is a complex observation
	 * @see org.openmrs.Obs#isComplexObs
	 */
	
	public Boolean isComplexObs() {
		return true;
	}

	// Property accessors

	/**
	 * Gets the Universal Resource Number for this complex obs
	 * @return urn string
	 */
	public String getUrn() {
		return this.urn;
	}

	/**
	 * Sets the Universal Resource Number for this complex obs
	 * @param urn string
	 */
	public void setUrn(String urn) {
		this.urn = urn;
	}

	/**
	 * gets the value of this complex observation
	 * @return complex value string
	 */
	public String getComplexValue() {
		return this.complexValue;
	}

	/**
	 * Sets the value of this complex observation
	 * @param complex value string
	 */
	public void setComplexValue(String complexValue) {
		this.complexValue = complexValue;
	}

	/**
	 * gets the mime type object of this complex observation
	 * @return MimeType object
	 */
	public MimeType getMimeType() {
		return this.mimeType;
	}

	/**
	 * Sets mime type object of this complex observation
	 * @param MimeType object
	 */
	public void setMimeType(MimeType mimeType) {
		this.mimeType = mimeType;
	}

}
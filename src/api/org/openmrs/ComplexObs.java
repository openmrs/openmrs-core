package org.openmrs;

/**
 * ComplexObs 
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class ComplexObs extends Obs implements java.io.Serializable{
	
	public static final long serialVersionUID = 345734087L;

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
	 * Compares two ComplexObs objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ComplexObs){
			//ComplexObs c = (ComplexObs)obj;
			return (super.equals((Obs)obj)); /*&&
					this.getMimeType().equals(c.getMimeType()) &&
					this.getUrn().matches(c.getUrn()) &&
					this.getComplexValue().matches(c.getComplexValue())); */
		}
		return false;
	}
	
	/** 
	 * overriding parent function isComplexObs function to allow an easier check of 
	 * complex vs noncomplex
	 * 
	 * @return true/false whether this is a complex observation
	 * @see org.openmrs.Obs#isComplexObs
	 */
	
	public boolean isComplexObs() {
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
	 * @param complexValue value string
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
	 * @param mimeType object
	 */
	public void setMimeType(MimeType mimeType) {
		this.mimeType = mimeType;
	}

}
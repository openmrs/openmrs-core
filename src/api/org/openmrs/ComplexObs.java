package org.openmrs;

/**
 * ComplexObs 
 */
public class ComplexObs implements java.io.Serializable {
	
	public static final long serialVersionUID = 1L;

	// Fields

	private Integer obsId;
	private String urn;
	private String complexValue;
	private Obs obs;

	// Constructors

	/** default constructor */
	public ComplexObs() {
	}

	/** constructor with id */
	public ComplexObs(Integer obsId) {
		this.obsId = obsId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getObsId() {
		return this.obsId;
	}

	public void setObsId(Integer obsId) {
		this.obsId = obsId;
	}

	/**
	 * 
	 */
	public String getUrn() {
		return this.urn;
	}

	public void setUrn(String urn) {
		this.urn = urn;
	}

	/**
	 * 
	 */
	public String getComplexValue() {
		return this.complexValue;
	}

	public void setComplexValue(String complexValue) {
		this.complexValue = complexValue;
	}

	/**
	 * 
	 */
	public Obs getObs() {
		return this.obs;
	}

	public void setObs(Obs obs) {
		this.obs = obs;
	}

}
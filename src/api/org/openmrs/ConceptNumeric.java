package org.openmrs;


public class ConceptNumeric extends Concept implements java.io.Serializable {

	public static final long serialVersionUID = 47323L;

	// Fields

	private Double hiAbsolute;
	private Double hiCritical;
	private Double hiNormal;
	private Double lowAbsolute;
	private Double lowCritical;
	private Double lowNormal;
	private String units;
	private Boolean precise = false;

	// Constructors

	/** default constructor */
	public ConceptNumeric() {
	}
	
	public ConceptNumeric(Integer conceptId) {
		setConceptId(conceptId);
	}

	public boolean equals(Object obj) {
		if (obj instanceof ConceptNumeric) {
			ConceptNumeric c = (ConceptNumeric)obj;
			return (this.getConceptId().equals(c.getConceptId()));
		}
		return false;
	}
	
	public int hashCode() {
		if (getConceptId() == null) return super.hashCode();
		int hash = 6;
		if (getConceptId() != null)
			hash = hash + getConceptId().hashCode() * 31;
		return hash;
	}

	// Property accessors

	/**
	 * 
	 */
	public Double getHiAbsolute() {
		return this.hiAbsolute;
	}

	public void setHiAbsolute(Double hiAbsolute) {
		this.hiAbsolute = hiAbsolute;
	}

	/**
	 * 
	 */
	public Double getHiCritical() {
		return this.hiCritical;
	}

	public void setHiCritical(Double hiCritical) {
		this.hiCritical = hiCritical;
	}

	/**
	 * 
	 */
	public Double getHiNormal() {
		return this.hiNormal;
	}

	public void setHiNormal(Double hiNormal) {
		this.hiNormal = hiNormal;
	}

	/**
	 * 
	 */
	public Double getLowAbsolute() {
		return this.lowAbsolute;
	}

	public void setLowAbsolute(Double lowAbsolute) {
		this.lowAbsolute = lowAbsolute;
	}

	/**
	 * 
	 */
	public Double getLowCritical() {
		return this.lowCritical;
	}

	public void setLowCritical(Double lowCritical) {
		this.lowCritical = lowCritical;
	}

	/**
	 * 
	 */
	public Double getLowNormal() {
		return this.lowNormal;
	}

	public void setLowNormal(Double lowNormal) {
		this.lowNormal = lowNormal;
	}

	/**
	 * 
	 */
	public String getUnits() {
		return this.units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * 
	 */
	public Boolean getPrecise() {
		return (precise == null ? false : precise);
	}

	public void setPrecise(Boolean precise) {
		this.precise = precise;
	}
	
	public boolean isNumeric() {
		return (getDatatype().getName().equals("Numeric"));
	}
}
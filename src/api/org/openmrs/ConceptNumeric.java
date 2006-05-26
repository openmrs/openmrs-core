package org.openmrs;


/**
 * ConceptNumeric
 * 
 * @author Burke Mamlin
 * @author Ben Wolfe
 * @version 1.0
 */
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
	
	/**
	 * Optional constructor for turning a Concept into a ConceptNumeric
	 * Note: This cannot copy over numeric specific values
	 * @param c
	 */
	public ConceptNumeric(Concept c) {
		this.setAnswers(c.getAnswers(true));
		this.setChangedBy(c.getChangedBy());
		this.setConceptClass(c.getConceptClass());
		this.setConceptId(c.getConceptId());
		this.setConceptSets(c.getConceptSets());
		this.setCreator(c.getCreator());
		this.setDatatype(c.getDatatype());
		this.setDateChanged(c.getDateChanged());
		this.setDateCreated(c.getDateCreated());
		this.setIcd10(c.getIcd10());
		this.setLoinc(c.getLoinc());
		this.setSet(c.isSet());
		this.setNames(c.getNames());
		this.setRetired(c.getRetired());
		this.setSynonyms(c.getSynonyms());
		this.setVersion(c.getVersion());
		
		this.hiAbsolute  = null;
		this.hiCritical  = null;
		this.hiNormal    = null;
		this.lowAbsolute = null;
		this.lowCritical = null;
		this.lowNormal   = null;
		this.units     = "";
		this.precise   = false;
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

	public Boolean isPrecise() {
		return (precise == null ? false : precise);
	}
	
	public Boolean getPrecise() {
		return isPrecise();
	}

	public void setPrecise(Boolean precise) {
		this.precise = precise;
	}
	
	public boolean isNumeric() {
		return (getDatatype().getName().equals("Numeric"));
	}
}
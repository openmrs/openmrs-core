package org.openmrs;

import java.util.Date;
import java.util.Locale;

/**
 * Drug 
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class Drug implements java.io.Serializable {

	public static final long serialVersionUID = 285L;

	// Fields

	private Integer drugId;
	private String name;
	private Boolean combination = false;
	private Double dailyMgPerKg;
	private String dosageForm;
	private Double doseStrength;
	private String inn;
	private Double maximumDose;
	private Double minimumDose;
	private String route;
	private Integer shelfLife;
	private Integer therapyClass;
	private String units;
	private Date dateCreated;
	private Concept concept;
	private User creator;

	// Constructors

	/** default constructor */
	public Drug() {
	}

	/** constructor with id */
	public Drug(Integer drugId) {
		this.drugId = drugId;
	}
	
	/** 
	 * Compares two Drug objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Drug))
			return false;
		
		Drug drug = (Drug) obj;
		return (this.drugId.equals(drug.getDrugId()));
	}
	
	public int hashCode() {
		if (this.getDrugId() == null) return super.hashCode();
		return this.getDrugId().hashCode();
	}

	// Property accessors

	/**
	 * Gets the internal identification number for this drug
	 * 
	 * @return Integer
	 */
	public Integer getDrugId() {
		return this.drugId;
	}

	/**
	 * Sets the internal identification number for this drug
	 * @param drugId
	 */
	public void setDrugId(Integer drugId) {
		this.drugId = drugId;
	}

	/**
	 * Gets the entires concept drug name in the form of
	 * CONCEPTNAME (Drug: DRUGNAME)
	 * @param locale
	 * @return full drug name (with concept name appended)
	 */
	public String getFullName(Locale locale) {
		if (concept == null)
			return name;
		else
			return name + " (" + concept.getName(locale).getName() + ")"; 
	}
	/**
	 * Gets the name of this drug
	 * @return String
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name of this drug
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets whether or not this is a combination drug
	 * @return Boolean
	 */
	public Boolean isCombination() {
		return this.combination;
	}
	
	public Boolean getCombination() {
		return isCombination();
	}

	/**
	 * Sets whether or not this is a combination drug
	 * @param combination
	 */
	public void setCombination(Boolean combination) {
		this.combination = combination;
	}

	/**
	 * Gets the daily milligrams per kilograms of body weight for this drug 
	 * @return Double
	 */
	public Double getDailyMgPerKg() {
		return this.dailyMgPerKg;
	}

	/**
	 * Sets the daily milligrams per kilogram of body weight for this drug
	 * @param dailyMgPerKg
	 */
	public void setDailyMgPerKg(Double dailyMgPerKg) {
		this.dailyMgPerKg = dailyMgPerKg;
	}

	/**
	 * Gets the form of the dosage
	 * @return String
	 */
	public String getDosageForm() {
		return this.dosageForm;
	}

	/**
	 * Sets the dosage form
	 * @param String dosage form
	 */
	public void setDosageForm(String dosageForm) {
		this.dosageForm = dosageForm;
	}

	/**
	 * Gets the dose strength of this drug
	 * @return Double
	 */
	public Double getDoseStrength() {
		return this.doseStrength;
	}

	/**
	 * Sets the dose strength
	 * @param Double
	 */
	public void setDoseStrength(Double doseStrength) {
		this.doseStrength = doseStrength;
	}

	/**
	 * Gets the International Nonproprietary Name for this drug
	 * @return String
	 */
	public String getInn() {
		return this.inn;
	}

	/**
	 * Sets the International Nonproprietary Name for this drug
	 * @param String
	 */
	public void setInn(String inn) {
		this.inn = inn;
	}

	/**
	 * Gets the maximum dosage for this drug
	 * @return Double
	 */
	public Double getMaximumDose() {
		return this.maximumDose;
	}

	/**
	 * Sets the maximum dosage for this drug
	 * @param Double
	 */
	public void setMaximumDose(Double maximumDose) {
		this.maximumDose = maximumDose;
	}

	/**
	 * Gets the minimum dosage for this drug
	 * @return Double
	 */
	public Double getMinimumDose() {
		return this.minimumDose;
	}

	/**
	 * Sets the minimum dosage for this drug
	 * @param Double
	 */
	public void setMinimumDose(Double minimumDose) {
		this.minimumDose = minimumDose;
	}

	/**
	 * Gets the route
	 * @return String
	 */
	public String getRoute() {
		return this.route;
	}

	/**
	 * Sets the route
	 * @param String
	 */
	public void setRoute(String route) {
		this.route = route;
	}

	/**
	 * Gets the shelf life
	 * @return Integer
	 */
	public Integer getShelfLife() {
		return this.shelfLife;
	}

	/**
	 * Sets the shelf life
	 * @param Integer
	 */
	public void setShelfLife(Integer shelfLife) {
		this.shelfLife = shelfLife;
	}

	/**
	 * Gets the therapy class recs
	 * @return Integer
	 */
	public Integer getTherapyClass() {
		return this.therapyClass;
	}

	/**
	 * Sets the therapy class recs
	 * @param Integer
	 */
	public void setTherapyClass(Integer therapyClass) {
		this.therapyClass = therapyClass;
	}

	/**
	 * Gets the units
	 * @return String
	 */
	public String getUnits() {
		return this.units;
	}

	/**
	 * Sets the units
	 * @param String
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * Gets the date created
	 * @return Date
	 */
	public Date getDateCreated() {
		return this.dateCreated;
	}

	/**
	 * Sets the date this was created
	 * @param Date
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * Gets the concept this drug is tied to
	 * @return Concept
	 */
	public Concept getConcept() {
		return this.concept;
	}

	/**
	 * Sets the concept this drug is tied to
	 * @param Concept
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * Gets the creator of this drug record
	 * @return User
	 */
	public User getCreator() {
		return this.creator;
	}

	/**
	 * Sets the creator of this drug record
	 * @param User
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

}
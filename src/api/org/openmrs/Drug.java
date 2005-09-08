package org.openmrs;

import java.util.Date;
import java.util.Set;

/**
 * Drug 
 */
public class Drug implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer drugId;
	private String name;
	private Boolean combination;
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
	private Set drugOrders;
	private User user;

	// Constructors

	/** default constructor */
	public Drug() {
	}

	/** constructor with id */
	public Drug(Integer drugId) {
		this.drugId = drugId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getDrugId() {
		return this.drugId;
	}

	public void setDrugId(Integer drugId) {
		this.drugId = drugId;
	}

	/**
	 * 
	 */
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 */
	public Boolean getCombination() {
		return this.combination;
	}

	public void setCombination(Boolean combination) {
		this.combination = combination;
	}

	/**
	 * 
	 */
	public Double getDailyMgPerKg() {
		return this.dailyMgPerKg;
	}

	public void setDailyMgPerKg(Double dailyMgPerKg) {
		this.dailyMgPerKg = dailyMgPerKg;
	}

	/**
	 * 
	 */
	public String getDosageForm() {
		return this.dosageForm;
	}

	public void setDosageForm(String dosageForm) {
		this.dosageForm = dosageForm;
	}

	/**
	 * 
	 */
	public Double getDoseStrength() {
		return this.doseStrength;
	}

	public void setDoseStrength(Double doseStrength) {
		this.doseStrength = doseStrength;
	}

	/**
	 * 
	 */
	public String getInn() {
		return this.inn;
	}

	public void setInn(String inn) {
		this.inn = inn;
	}

	/**
	 * 
	 */
	public Double getMaximumDose() {
		return this.maximumDose;
	}

	public void setMaximumDose(Double maximumDose) {
		this.maximumDose = maximumDose;
	}

	/**
	 * 
	 */
	public Double getMinimumDose() {
		return this.minimumDose;
	}

	public void setMinimumDose(Double minimumDose) {
		this.minimumDose = minimumDose;
	}

	/**
	 * 
	 */
	public String getRoute() {
		return this.route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	/**
	 * 
	 */
	public Integer getShelfLife() {
		return this.shelfLife;
	}

	public void setShelfLife(Integer shelfLife) {
		this.shelfLife = shelfLife;
	}

	/**
	 * 
	 */
	public Integer getTherapyClass() {
		return this.therapyClass;
	}

	public void setTherapyClass(Integer therapyClass) {
		this.therapyClass = therapyClass;
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
	public Date getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * 
	 */
	public Concept getConcept() {
		return this.concept;
	}

	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * 
	 */
	public Set getDrugOrders() {
		return this.drugOrders;
	}

	public void setDrugOrders(Set drugOrders) {
		this.drugOrders = drugOrders;
	}

	/**
	 * 
	 */
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
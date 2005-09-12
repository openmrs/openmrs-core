package org.openmrs;

import java.util.Date;

/**
 * DrugOrder 
 */
public class DrugOrder extends Order implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer dose;
	private String units;
	private String frequency;
	private Boolean prn;
	private Boolean complex;
	private Integer quantity;
	private Drug drug;
	private User creator;
	private Date dateCreated;
	
	// Constructors

	/** default constructor */
	public DrugOrder() {
	}

	/** constructor with id */
	public DrugOrder(Integer orderId) {
		this.setOrderId(orderId);
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getDose() {
		return this.dose;
	}

	public void setDose(Integer dose) {
		this.dose = dose;
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
	public String getFrequency() {
		return this.frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	/**
	 * 
	 */
	public Boolean isPrn() {
		return this.prn;
	}

	public void iPrn(Boolean prn) {
		this.prn = prn;
	}

	/**
	 * 
	 */
	public Boolean isComplex() {
		return this.complex;
	}

	public void setComplex(Boolean complex) {
		this.complex = complex;
	}

	/**
	 * 
	 */
	public Integer getQuantity() {
		return this.quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
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
	public Drug getDrug() {
		return this.drug;
	}

	public void setDrug(Drug drug) {
		this.drug = drug;
	}

	/**
	 * 
	 */
	public User getCreator() {
		return this.creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

}
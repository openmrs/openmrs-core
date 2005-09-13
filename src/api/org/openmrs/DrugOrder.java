package org.openmrs;

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
	
	// Constructors

	/** default constructor */
	public DrugOrder() {
	}

	/** constructor with id */
	public DrugOrder(Integer orderId) {
		this.setOrderId(orderId);
	}

	public boolean equals(Object obj) {
		if (obj instanceof DrugOrder) {
			DrugOrder d = (DrugOrder)obj;
			return (super.equals((Order)obj) &&
				this.getDrug().equals(d.getDrug()) &&
				this.getDose() == d.getDose());
		}
		return false;
	}
	
	public boolean isDrugOrder() {
		return true;
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

	public void setPrn(Boolean prn) {
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
	public Drug getDrug() {
		return this.drug;
	}

	public void setDrug(Drug drug) {
		this.drug = drug;
	}
}
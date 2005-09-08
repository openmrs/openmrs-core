package org.openmrs;

/**
 * DrugOrder 
 */
public class DrugOrder implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer orderId;
	private Integer dose;
	private String units;
	private String frequency;
	private Boolean prn;
	private Boolean complex;
	private Integer quantity;
	private String dateCreated;
	private Order order;
	private Drug drug;
	private User user;

	// Constructors

	/** default constructor */
	public DrugOrder() {
	}

	/** constructor with id */
	public DrugOrder(Integer orderId) {
		this.orderId = orderId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getOrderId() {
		return this.orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

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
	public Boolean getPrn() {
		return this.prn;
	}

	public void setPrn(Boolean prn) {
		this.prn = prn;
	}

	/**
	 * 
	 */
	public Boolean getComplex() {
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
	public String getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * 
	 */
	public Order getOrder() {
		return this.order;
	}

	public void setOrder(Order order) {
		this.order = order;
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
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
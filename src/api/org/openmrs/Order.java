package org.openmrs;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Order 
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class Order implements java.io.Serializable {

	protected final Log log = LogFactory.getLog(getClass());
	public static final long serialVersionUID = 4334343L;

	// Fields

	private Integer orderId;
	private Patient patient;
	private OrderType orderType;
	private Concept concept;
	private String instructions;
	private Date startDate;
	private Date autoExpireDate;
	private Encounter encounter;
	private User orderer;
	private User creator;
	private Date dateCreated;
	private Boolean discontinued = false;
	private User discontinuedBy;
	private Date discontinuedDate;
	private Concept discontinuedReason;
	private Boolean voided = false;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;

	
	// Constructors

	/** default constructor */
	public Order() {
	}

	/** constructor with id */
	public Order(Integer orderId) {
		this.orderId = orderId;
	}

	/** 
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Order) {
			Order o = (Order)obj;
			if (this.getOrderId() != null && o.getOrderId() != null)
				return (this.getOrderId().equals(o.getOrderId()));
			/*return (this.getOrderType().equals(o.getOrderType()) &&
					this.getConcept().equals(o.getConcept()) &&
					this.getEncounter().equals(o.getEncounter()) &&
					this.getInstructions().matches(o.getInstructions())); */
		}
		return false;
	}
	
	public int hashCode() {
		if (this.getOrderId() == null) return super.hashCode();
		return this.getOrderId().hashCode();
	}

	/**
	 * true/false whether or not this is a drug order
	 * overridden in extending class drugOrders. 
	 */
	public boolean isDrugOrder() {
		return false;
	}
	
	// Property accessors

	/**
	 * @return Returns the autoExpireDate.
	 */
	public Date getAutoExpireDate() {
		return autoExpireDate;
	}

	/**
	 * @param autoExpireDate The autoExpireDate to set.
	 */
	public void setAutoExpireDate(Date autoExpireDate) {
		this.autoExpireDate = autoExpireDate;
	}

	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}

	/**
	 * @param concept The concept to set.
	 */
	public void setConcept(Concept concept) {
		this.concept = concept;
	}

	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return Returns the discontinued status.
	 */
	public Boolean getDiscontinued() {
		return discontinued;
	}

	/**
	 * @param discontinued The discontinued status to set.
	 */
	public void setDiscontinued(Boolean discontinued) {
		this.discontinued = discontinued;
	}

	/**
	 * @return Returns the discontinuedBy.
	 */
	public User getDiscontinuedBy() {
		return discontinuedBy;
	}

	/**
	 * @param discontinuedBy The discontinuedBy to set.
	 */
	public void setDiscontinuedBy(User discontinuedBy) {
		this.discontinuedBy = discontinuedBy;
	}

	/**
	 * @return Returns the discontinuedDate.
	 */
	public Date getDiscontinuedDate() {
		return discontinuedDate;
	}

	/**
	 * @param discontinuedDate The discontinuedDate to set.
	 */
	public void setDiscontinuedDate(Date discontinuedDate) {
		this.discontinuedDate = discontinuedDate;
	}

	/**
	 * @return Returns the discontinuedReason.
	 */
	public Concept getDiscontinuedReason() {
		return discontinuedReason;
	}

	/**
	 * @param discontinuedReason The discontinuedReason to set.
	 */
	public void setDiscontinuedReason(Concept discontinuedReason) {
		this.discontinuedReason = discontinuedReason;
	}

	/**
	 * @return Returns the dateVoided.
	 */
	public Date getDateVoided() {
		return dateVoided;
	}

	/**
	 * @param dateVoided The dateVoided to set.
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}

	/**
	 * @return Returns the voided.
	 */
	public Boolean getVoided() {
		return voided;
	}

	/**
	 * @param voided The voided to set.
	 */
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}

	/**
	 * @return Returns the voidedBy.
	 */
	public User getVoidedBy() {
		return voidedBy;
	}

	/**
	 * @param voidedBy The voidedBy to set.
	 */
	public void setVoidedBy(User voidedBy) {
		this.voidedBy = voidedBy;
	}

	/**
	 * @return Returns the voidReason.
	 */
	public String getVoidReason() {
		return voidReason;
	}

	/**
	 * @param voidReason The voidReason to set.
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}

	/**
	 * @return Returns the encounter.
	 */
	public Encounter getEncounter() {
		return encounter;
	}

	/**
	 * @param encounter The encounter to set.
	 */
	public void setEncounter(Encounter encounter) {
		this.encounter = encounter;
	}

	/**
	 * @return Returns the instructions.
	 */
	public String getInstructions() {
		return instructions;
	}

	/**
	 * @param instructions The instructions to set.
	 */
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	/**
	 * @return Returns the orderer.
	 */
	public User getOrderer() {
		return orderer;
	}

	/**
	 * @param orderer The orderer to set.
	 */
	public void setOrderer(User orderer) {
		this.orderer = orderer;
	}

	/**
	 * @return Returns the orderId.
	 */
	public Integer getOrderId() {
		return orderId;
	}

	/**
	 * @param orderId The orderId to set.
	 */
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	/**
	 * @return Returns the orderType.
	 */
	public OrderType getOrderType() {
		return orderType;
	}

	/**
	 * @param orderType The orderType to set.
	 */
	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}

	/**
	 * @return Returns the startDate.
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate The startDate to set.
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	/**
	 * Convenience method to determine if order is current
	 * @param checkDate - the date on which to check order. if null, will use current date
	 * @return boolean indicating whether the order was current on the input date
	 */
	public boolean isCurrent(Date checkDate) {
		if (checkDate == null) {
			checkDate = new Date();
		}
		if (startDate == null || checkDate.before(startDate)) {
			return false;
		}
		if (discontinuedDate != null && discontinuedDate.before(checkDate)) {
			return false;
		}
		if (autoExpireDate != null && autoExpireDate.before(checkDate)) {
			return false;
		}
		if (this.voided || this.discontinued) {
			return false;
		}
		return true;
	}
	
	public boolean isCurrent() {
		return isCurrent(new Date());
	}

	public boolean isFuture(Date checkDate) {
		log.debug("Check if this is in the future");
		if ( checkDate == null ) {
			checkDate = new Date();
		}
		
		if ( startDate != null && checkDate.before(startDate) && !voided && !discontinued){
			log.debug("Looks like this order IS in the future");
			return true;
		}
		
		log.debug("Looks like this order is not in the future");
		return false;
	}

	public boolean isFuture() {
		return isFuture(new Date());
	}

	
	/**
	 * Convenience method to determine if order is discontinued
	 * @param checkDate - the date on which to check order. if null, will use current date
	 * @return boolean indicating whether the order was discontinued on the input date
	 */
	public boolean isDiscontinued(Date checkDate) {
		if (checkDate == null) {
			checkDate = new Date();
		}
		if (startDate == null || checkDate.before(startDate)) {
			return false;
		}
		if (discontinuedDate != null && discontinuedDate.after(checkDate)) {
			return false;
		}
		if (discontinuedDate == null) {
			return false;
		}
		if (this.voided || !this.discontinued) {
			return false;
		}
		return true;
	}
	
	public boolean isDiscontinued() {
		return isDiscontinued(new Date());
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}
}
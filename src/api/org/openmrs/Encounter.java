package org.openmrs;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Encounter 
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class Encounter implements java.io.Serializable {

	public static final long serialVersionUID = 7844L;

	// Fields

	private Integer encounterId;
	private Date encounterDatetime;
	private Date dateCreated;
	private Patient patient;
	private Location location;
	private Form form;
	private EncounterType encounterType;
	private User creator;
	private User provider;
	private Set<Order> orders;
	private Set<Obs> obs;
	// Constructors

	/** default constructor */
	public Encounter() {
	}

	/** constructor with id */
	public Encounter(Integer encounterId) {
		this.encounterId = encounterId;
	}

	/** 
	 * Compares two Encounter objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof Encounter) {
			Encounter enc = (Encounter) obj;
			if (this.getEncounterId() != null && enc.getEncounterId() != null)
				return (this.getEncounterId().equals(enc.getEncounterId()));
			/*return (this.getEncounterType().equals(enc.getEncounterType()) &&
					this.getPatient().equals(enc.getPatient()) &&
					this.getProvider().equals(enc.getProvider()) &&
					this.getLocation().equals(enc.getLocation()) &&
					this.getEncounterDatetime().equals(enc.getEncounterDatetime())); */
		}
		return false;
			
	}
	
	public int hashCode() {
		if (this.getEncounterId() == null) return super.hashCode();
		return this.getEncounterId().hashCode();
	}

	// Property accessors

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
	 * @return Returns the encounterDatetime.
	 */
	public Date getEncounterDatetime() {
		return encounterDatetime;
	}

	/**
	 * @param encounterDatetime The encounterDatetime to set.
	 */
	public void setEncounterDatetime(Date encounterDatetime) {
		this.encounterDatetime = encounterDatetime;
	}

	/**
	 * @return Returns the encounterId.
	 */
	public Integer getEncounterId() {
		return encounterId;
	}

	/**
	 * @param encounterId The encounterId to set.
	 */
	public void setEncounterId(Integer encounterId) {
		this.encounterId = encounterId;
	}

	/**
	 * @return Returns the encounterType.
	 */
	public EncounterType getEncounterType() {
		return encounterType;
	}

	/**
	 * @param encounterType The encounterType to set.
	 */
	public void setEncounterType(EncounterType encounterType) {
		this.encounterType = encounterType;
	}

	/**
	 * @return Returns the location.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location The location to set.
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * @return Returns the obs.
	 */
	public Set<Obs> getObs() {
		return obs;
	}

	/**
	 * @param obs The obs to set.
	 */
	public void setObs(Set<Obs> obs) {
		this.obs = obs;
	}
	
	/**
	 * Add the given Obs to the list of obs for this Encounter
	 * @param observation
	 */
	public void addObs(Obs observation) {
		observation.setEncounter(this);
		if (obs == null)
			obs = new HashSet<Obs>();
		if (!obs.contains(observation) && observation != null)
			obs.add(observation);
	}

	/**
	 * Remove the given obervation from the list of obs for this Encounter
	 * @param observation
	 */
	public void removeObs(Obs observation) {
		if (obs != null)
			obs.remove(observation);
	}

	/**
	 * @return Returns the orders
	 */
	public Set<Order> getOrders() {
		return orders;
	}

	
	/**
	 * @param orders The orders to set.
	 */
	public void setOrders(Set<Order> orders) {
		this.orders = orders;
	}
	
	/**
	 * Add the given Order to the list of orders for this Encounter
	 * @param order
	 */
	public void addOrder(Order order) {
		order.setEncounter(this);
		if (orders == null)
			orders = new HashSet<Order>();
		if (!orders.contains(order) && order != null)
			orders.add(order);
	}

	/**
	 * Remove the given obervation from the list of orders for this Encounter
	 * @param order
	 */
	public void removeOrder(Order order) {
		if (orders != null)
			orders.remove(order);
	}

	/**
	 * @return Returns the patient.
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * @param patient The patient to set.
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	/**
	 * @return Returns the provider.
	 */
	public User getProvider() {
		return provider;
	}

	/**
	 * @param provider The provider to set.
	 */
	public void setProvider(User provider) {
		this.provider = provider;
	}

	/**
	 * @return Returns the form.
	 */
	public Form getForm() {
		return form;
	}

	/**
	 * @param form The form to set.
	 */
	public void setForm(Form form) {
		this.form = form;
	}
	
	@Override
	public String toString() {
		if (encounterId == null)
			return null;
		return "Encounter #" + encounterId;
	}

}
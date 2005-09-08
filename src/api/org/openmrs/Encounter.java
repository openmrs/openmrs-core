package org.openmrs;

import java.util.Date;
import java.util.Set;

/**
 * Encounter 
 */
public class Encounter implements java.io.Serializable {

	public static final long serialVersionUID = 1L;

	// Fields

	private Integer encounterId;
	private Date encounterDatetime;
	private Date dateCreated;
	private Patient patient;
	private Location location;
	private Set orders;
	private Set obs;
	private EncounterType encounterType;
	private User creator;
	private User provider;

	// Constructors

	/** default constructor */
	public Encounter() {
	}

	/** constructor with id */
	public Encounter(Integer encounterId) {
		this.encounterId = encounterId;
	}

	// Property accessors

	/**
	 * 
	 */
	public Integer getEncounterId() {
		return this.encounterId;
	}

	public void setEncounterId(Integer encounterId) {
		this.encounterId = encounterId;
	}

	/**
	 * 
	 */
	public Date getEncounterDatetime() {
		return this.encounterDatetime;
	}

	public void setEncounterDatetime(Date encounterDatetime) {
		this.encounterDatetime = encounterDatetime;
	}

	/**
	 * 
	 */
	public Patient getPatient() {
		return this.patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	/**
	 * 
	 */
	public Location getLocation() {
		return this.location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * 
	 */
	public Set getOrders() {
		return this.orders;
	}

	public void setOrders(Set orders) {
		this.orders = orders;
	}

	/**
	 * 
	 */
	public Set getObs() {
		return this.obs;
	}

	public void setObs(Set obs) {
		this.obs = obs;
	}

	/**
	 * 
	 */
	public EncounterType getEncounterType() {
		return this.encounterType;
	}

	public void setEncounterType(EncounterType encounterType) {
		this.encounterType = encounterType;
	}

	/**
	 * 
	 */
	public User getProvider() {
		return provider;
	}

	public void setProvider(User provider) {
		this.provider = provider;
	}

	/**
	 * 
	 */
	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
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

}
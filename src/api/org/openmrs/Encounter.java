/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An Encounter represents one visit or interaction of a patient with a healthcare worker.
 * 
 * Every encounter can have 0 to n Observations associated with it
 * Every encounter can have 0 to n Orders associated with it
 * 
 * The patientId attribute should be equal to patient.patientId and is
 * only included this second time for performance increases on bulk calls. 
 * 
 * @see Obs 
 * @see Order
 */
public class Encounter implements java.io.Serializable {

	public static final long serialVersionUID = 7844L;

	// Fields

	private Integer encounterId;
	private Date encounterDatetime;
	private Date dateCreated;
	private Patient patient;
	private Integer patientId;
	private Location location;
	private Form form;
	private EncounterType encounterType;
	private User creator;
	private User provider;
	private Set<Order> orders;
	private Set<Obs> obs;
	private Boolean voided = false;
	private User voidedBy;
	private Date dateVoided;
	private String voidReason;
	
	
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
	 * @param obj Encounter object to compare to
	 * @return boolean true/false whether or not they are the same objects
	 * @see java.lang.Object#equals(java.lang.Object)
	 * 
	 * @should equal encounter with same encounter id
	 * @should not equal encounter with different encounter id
	 * @should fail on null
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
		return this == obj;
			
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 * 
	 * @should have same hashcode when equal
	 * @should have different hash code when not equal
	 */
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
	 * @return Returns a Set<Obs> of all non-voided, non-obsGroup children Obs
	 *         of this Encounter
	 */
	public Set<Obs> getObs() {
		Set<Obs> ret = new HashSet<Obs>();
		
		if (this.obs != null) {
			for (Obs o : this.obs)
				ret.addAll(getObsLeaves(o));
				// this should be all thats needed unless the encounter has been built by hand
				//if (o.isVoided() == false && o.isObsGrouping() == false)
				//	ret.add(o);
		}
		
			
		return ret;
	}
	
	/**
	 * Convenience method to recursively get all leaf obs 
	 * of this encounter.  This method goes down into
	 * each obs and adds all non-grouping obs to the return
	 * list
	 * 
	 * @param obsParent current obs to loop over
	 * @return list of leaf obs
	 */
	private List<Obs> getObsLeaves(Obs obsParent) {
		List<Obs> leaves = new ArrayList<Obs>();
		
		if (obsParent.hasGroupMembers()) {
			for (Obs child : obsParent.getGroupMembers()) {
				if (child.isVoided() == false) {
					if (child.isObsGrouping() == false)
						leaves.add(child);
					else
						// recurse if this is a grouping obs
						leaves.addAll(getObsLeaves(child));
				}
			}
		}
		else if (obsParent.isVoided() == false) {
			leaves.add(obsParent);
		}
		
		return leaves;
	}
	
	/**
	 * Returns all Obs where Obs.encounterId = Encounter.encounterId
	 * In practice, this method should not be used very often...
	 * @param boolean includeVoided specifies whether or not to include voided Obs
	 * @return Returns the all Obs.
	 */
	public Set<Obs> getAllObs(boolean includeVoided) {
		if (includeVoided && obs != null)
			return obs;
		
		Set<Obs> ret = new HashSet<Obs>();
		
		if (this.obs != null) {
			for (Obs o : this.obs) {
				if (includeVoided)
					ret.add(o);
				else if (!o.isVoided())
					ret.add(o);
			}
		}
		return ret;
	}
	
	/**
	 * Convenience method to call {@link #getAllObs(boolean)}
	 * with a false parameter
	 * 
	 * @return all non-voided obs
	 */
	public Set<Obs> getAllObs() {
		return getAllObs(false);
	}
	
	/**
	 * Returns a Set<Obs> of all root-level Obs of an Encounter, including obsGroups
	 * 
	 * @param boolean includeVoided specifies whether or not to include voided Obs
	 * @return Returns all obs at top level -- will not be null
	 */
	public Set<Obs> getObsAtTopLevel(boolean includeVoided) {
		Set<Obs> ret = new HashSet<Obs>();
		for (Obs o : getAllObs(includeVoided)) {
			if (o.getObsGroup() == null) 
				ret.add(o);
		}
		return ret;
	}
	
	/**
	 * @param obs The obs to set.
	 */
	public void setObs(Set<Obs> obs) {
		this.obs = obs;
	}
	
	/**
	 * Add the given Obs to the list of obs for this Encounter.
	 * 
	 * @param observation the Obs to add to this encounter
	 */
	public void addObs(Obs observation) {
		if (obs == null)
			obs = new HashSet<Obs>();
		if (observation != null) {
			observation.setEncounter(this);
			
			if (observation.getObsDatetime() == null)
				observation.setObsDatetime(getEncounterDatetime());
			if (observation.getPerson() == null)
				observation.setPerson(getPatient());
			if (observation.getLocation() == null)
				observation.setLocation(getLocation());
			obs.add(observation);
		}
	}

	/**
	 * Remove the given observation from the list of obs for this Encounter
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
		if (orders == null) {
			return new HashSet<Order>();
		}
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
		if (orders == null)
			orders = new HashSet<Order>();
		if (order != null) {
			order.setEncounter(this);
			orders.add(order);
		}
	}

	/**
	 * Remove the given observation from the list of orders for this Encounter
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
     * @return the patientId
     */
    public Integer getPatientId() {
    	return patientId;
    }

	/**
     * @param patientId the patientId to set
     */
    public void setPatientId(Integer patientId) {
    	this.patientId = patientId;
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
	
	/**
	 * @return Returns the voided.
	 */
	public Boolean isVoided() {
		return voided;
	}

	/**
	 * @return Returns the voided.
	 */
	public Boolean getVoided() {
		return voided;
	}

	/**
	 * @param voided
	 *            The voided status to set.
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
	 * @param voidedBy
	 *            The voidedBy to set.
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
	 * @param voidReason
	 *            The voidReason to set.
	 */
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
	
	/**
	 * @return Returns the dateVoided.
	 */
	public Date getDateVoided() {
		return dateVoided;
	}

	/**
	 * @param dateVoided
	 *            The dateVoided to set.
	 */
	public void setDateVoided(Date dateVoided) {
		this.dateVoided = dateVoided;
	}
	
	@Override
	public String toString() {
		String ret = "";
		ret += encounterId == null ? "(no ID) " : encounterId.toString() + " ";
		ret += this.getEncounterDatetime() == null ? "(no Date) " : this.getEncounterDatetime().toString() + " ";
		ret += this.getEncounterType() == null ? "(no Type) " : this.getEncounterType().getName() + " ";
		ret += this.getLocation() == null ? "(no Location) " : this.getLocation().getName() + " ";
		ret += this.getPatient() == null ? "(no Patient) " : this.getPatient().getPatientId().toString() + " ";
		ret += this.getForm() == null ? "(no Form) " : this.getForm().getName() + " ";
		ret += this.getObsAtTopLevel(false) == null ? "(no Obss) " : "num Obs: " + this.getObsAtTopLevel(false) + " ";
		ret += this.getOrders() == null ? "(no Orders) " : "num Orders: " + this.getOrders().size() + " ";
		return "Encounter: [" + ret + "]";
	}

}

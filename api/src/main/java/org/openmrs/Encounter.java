/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.annotation.AllowDirectAccess;
import org.openmrs.annotation.DisableHandlers;
import org.openmrs.api.context.Context;
import org.openmrs.api.handler.VoidHandler;

/**
 * An Encounter represents one visit or interaction of a patient with a healthcare worker. Every
 * encounter can have 0 to n Observations associated with it Every encounter can have 0 to n Orders
 * associated with it The patientId attribute should be equal to patient.patientId and is only
 * included this second time for performance increases on bulk calls.
 *
 * @see Obs
 * @see Order
 */
public class Encounter extends BaseOpenmrsData implements java.io.Serializable {
	
	public static final long serialVersionUID = 2L;
	
	// Fields
	
	private Integer encounterId;
	
	private Date encounterDatetime;
	
	private Patient patient;
	
	private Integer patientId;
	
	private Location location;
	
	private Form form;
	
	private EncounterType encounterType;
	
	private Set<Order> orders;
	
	@AllowDirectAccess
	private Set<Obs> obs;
	
	private Visit visit;
	
	@DisableHandlers(handlerTypes = { VoidHandler.class })
	private Set<EncounterProvider> encounterProviders = new LinkedHashSet<EncounterProvider>();
	
	// Constructors
	
	/** default constructor */
	public Encounter() {
	}
	
	/**
	 * @param encounterId
	 * @should set encounter id
	 */
	public Encounter(Integer encounterId) {
		this.encounterId = encounterId;
	}
	
	// Property accessors
	
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
	 * @return Returns a Set<Obs> of all non-voided, non-obsGroup children Obs of this Encounter
	 * @should not return null with null obs set
	 * @should get obs
	 * @should not get voided obs
	 * @should only get child obs
	 * @should not get child obs if child also on encounter
	 * @should get both child and parent obs after removing child from parent grouping
	 * @should get obs with two levels of hierarchy
	 * @should get obs with three levels of hierarchy
	 * @should not get voided obs with three layers of hierarchy
	 */
	public Set<Obs> getObs() {
		Set<Obs> ret = new HashSet<Obs>();
		
		if (this.obs != null) {
			for (Obs o : this.obs) {
				ret.addAll(getObsLeaves(o));
			}
			// this should be all thats needed unless the encounter has been built by hand
			//if (o.isVoided() == false && o.isObsGrouping() == false)
			//	ret.add(o);
		}
		
		return ret;
	}
	
	/**
	 * Convenience method to recursively get all leaf obs of this encounter. This method goes down
	 * into each obs and adds all non-grouping obs to the return list
	 *
	 * @param obsParent current obs to loop over
	 * @return list of leaf obs
	 */
	private List<Obs> getObsLeaves(Obs obsParent) {
		List<Obs> leaves = new ArrayList<Obs>();
		
		if (obsParent.hasGroupMembers()) {
			for (Obs child : obsParent.getGroupMembers()) {
				if (!child.isVoided()) {
					if (!child.isObsGrouping()) {
						leaves.add(child);
					} else {
						// recurse if this is a grouping obs
						leaves.addAll(getObsLeaves(child));
					}
				}
			}
		} else if (!obsParent.isVoided()) {
			leaves.add(obsParent);
		}
		
		return leaves;
	}
	
	/**
	 * Returns all Obs where Obs.encounterId = Encounter.encounterId In practice, this method should
	 * not be used very often...
	 *
	 * @param includeVoided specifies whether or not to include voided Obs
	 * @return Returns the all Obs.
	 * @should not return null with null obs set
	 * @should get obs
	 * @should get both parent and child obs
	 * @should get both parent and child with child directly on encounter
	 * @should get both child and parent obs after removing child from parent grouping
	 */
	public Set<Obs> getAllObs(boolean includeVoided) {
		if (includeVoided && obs != null) {
			return obs;
		}
		
		Set<Obs> ret = new HashSet<Obs>();
		
		if (this.obs != null) {
			for (Obs o : this.obs) {
				if (includeVoided) {
					ret.add(o);
				} else if (!o.isVoided()) {
					ret.add(o);
				}
			}
		}
		return ret;
	}
	
	/**
	 * Convenience method to call {@link #getAllObs(boolean)} with a false parameter
	 *
	 * @return all non-voided obs
	 * @should not get voided obs
	 */
	public Set<Obs> getAllObs() {
		return getAllObs(false);
	}
	
	/**
	 * Returns a Set<Obs> of all root-level Obs of an Encounter, including obsGroups
	 *
	 * @param includeVoided specifies whether or not to include voided Obs
	 * @return Returns all obs at top level -- will not be null
	 * @should not return null with null obs set
	 * @should get obs
	 * @should not get voided obs
	 * @should only get parents obs
	 * @should only return the grouped top level obs
	 * @should get both child and parent obs after removing child from parent grouping
	 */
	public Set<Obs> getObsAtTopLevel(boolean includeVoided) {
		Set<Obs> ret = new HashSet<Obs>();
		for (Obs o : getAllObs(includeVoided)) {
			if (o.getObsGroup() == null) {
				ret.add(o);
			}
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
	 * @should add obs with null values
	 * @should not fail with null obs
	 * @should set encounter attribute on obs
	 * @should add obs to non null initial obs set
	 * @should add encounter attrs to obs if attributes are null
	 * @should add encounter attrs to obs groupMembers if attributes are null
	 */
	public void addObs(Obs observation) {
		if (obs == null) {
			obs = new HashSet<Obs>();
		}
		
		if (observation != null) {
			obs.add(observation);
			
			//Propagate some attributes to the obs and any groupMembers
			
			// a Deque is a two-ended queue, that lets us add to the end, and fetch from the beginning
			Deque<Obs> obsToUpdate = new ArrayDeque<Obs>();
			obsToUpdate.add(observation);
			
			//prevent infinite recursion if an obs is its own group member
			Set<Obs> seenIt = new HashSet<Obs>();
			
			while (!obsToUpdate.isEmpty()) {
				Obs o = obsToUpdate.removeFirst();
				
				//has this obs already been processed?
				if (o == null || seenIt.contains(o)) {
					continue;
				}
				seenIt.add(o);
				
				o.setEncounter(this);
				
				//if the attribute was already set, preserve it
				//if not, inherit the values sfrom the encounter
				if (o.getObsDatetime() == null) {
					o.setObsDatetime(getEncounterDatetime());
				}
				if (o.getPerson() == null) {
					o.setPerson(getPatient());
				}
				if (o.getLocation() == null) {
					o.setLocation(getLocation());
				}
				
				//propagate attributes to  all group members as well
				if (o.getGroupMembers(true) != null) {
					obsToUpdate.addAll(o.getGroupMembers());
				}
			}
			
		}
	}
	
	/**
	 * Remove the given observation from the list of obs for this Encounter
	 *
	 * @param observation
	 * @should remove obs successfully
	 * @should not throw error when removing null obs from empty set
	 * @should not throw error when removing null obs from non empty set
	 */
	public void removeObs(Obs observation) {
		if (obs != null) {
			obs.remove(observation);
		}
	}
	
	/**
	 * @return Returns the orders
	 */
	public Set<Order> getOrders() {
		if (orders == null) {
			orders = new LinkedHashSet<Order>();
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
	 *
	 * @param order
	 * @should add order with null values
	 * @should not fail with null obs passed to add order
	 * @should set encounter attribute
	 * @should add order to non null initial order set
	 * @should add order to encounter when adding order to set returned from getOrders
	 */
	public void addOrder(Order order) {
		if (order != null) {
			order.setEncounter(this);
			getOrders().add(order);
		}
	}
	
	/**
	 * Remove the given observation from the list of orders for this Encounter
	 *
	 * @param order
	 * @should remove order from encounter
	 * @should not fail when removing null order
	 * @should not fail when removing non existent order
	 */
	public void removeOrder(Order order) {
		if (orders != null) {
			orders.remove(order);
		}
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
		this.patientId = patient.getPersonId();
	}
	
	/**
	 * @return the patientId
	 * @deprecated due to duplication. Use Encounter.Patient instead
	 */
	@Deprecated
	public Integer getPatientId() {
		return patientId;
	}
	
	/**
	 * @param patientId the patientId to set
	 * @deprecated due to duplication. Use Encounter.Patient instead
	 */
	@Deprecated
	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}
	
	/**
	 * Basic property accessor for encounterProviders. The convenience methods getProvidersByRoles
	 * and getProvidersByRole are the preferred methods for getting providers. This getter is 
	 * provided as a convenience for treating this like a DTO
	 *
	 * @return list of all existing providers on this encounter
	 * @see #getProvidersByRole(EncounterRole)
	 * @see #getProvidersByRoles()
	 * @since 1.9.1
	 */
	public Set<EncounterProvider> getEncounterProviders() {
		return encounterProviders;
	}
	
	/**
	 * Basic property setter for encounterProviders. The convenience methods addProvider,
	 * removeProvider, and setProvider are the preferred methods for adding/removing providers. This
	 * setter is provided as a convenience for treating this like a DTO
	 *
	 * @param encounterProviders the list of EncounterProvider objects to set. Overwrites list as
	 *            normal setter is inclined to do
	 * @see #addProvider(EncounterRole, Provider)
	 * @see #removeProvider(EncounterRole, Provider)
	 * @see #setProvider(EncounterRole, Provider)
	 * @since 1.9.1
	 */
	public void setEncounterProviders(Set<EncounterProvider> encounterProviders) {
		this.encounterProviders = encounterProviders;
	}
	
	/**
	 * @return Returns the provider.
	 * @since 1.6 (used to return User)
	 * @deprecated since 1.9, use {@link #getProvidersByRole(EncounterRole)}
	 * @should return null if there is no providers
	 * @should return provider for person
	 * @should return null if there is no provider for person
	 * @should return same provider for person if called twice
	 * @should not return a voided provider
	 */
	@Deprecated
	public Person getProvider() {
		if (encounterProviders == null || encounterProviders.isEmpty()) {
			return null;
		} else {
			for (EncounterProvider encounterProvider : encounterProviders) {
				// Return the first non-voided provider associated with a person in the list
				if (!encounterProvider.isVoided() && encounterProvider.getProvider().getPerson() != null) {
					return encounterProvider.getProvider().getPerson();
				}
			}
		}
		return null;
	}
	
	/**
	 * @param provider The provider to set.
	 * @deprecated use {@link #setProvider(Person)}
	 */
	@Deprecated
	public void setProvider(User provider) {
		setProvider(provider.getPerson());
	}
	
	/**
	 * @param provider The provider to set.
	 * @deprecated since 1.9, use {@link #setProvider(EncounterRole, Provider)}
	 * @should set existing provider for unknown role
	 */
	@Deprecated
	public void setProvider(Person provider) {
		EncounterRole unknownRole = Context.getEncounterService().getEncounterRoleByUuid(
		    EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID);
		if (unknownRole == null) {
			throw new IllegalStateException("No 'Unknown' encounter role with uuid "
			        + EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID + ".");
		}
		Collection<Provider> providers = Context.getProviderService().getProvidersByPerson(provider);
		if (providers == null || providers.isEmpty()) {
			throw new IllegalArgumentException("No provider with personId " + provider.getPersonId());
		}
		setProvider(unknownRole, providers.iterator().next());
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
	 * @see java.lang.Object#toString()
	 * @should not fail with empty object
	 */
	@Override
	public String toString() {
		String ret = "";
		ret += encounterId == null ? "(no ID) " : encounterId.toString() + " ";
		ret += this.getEncounterDatetime() == null ? "(no Date) " : this.getEncounterDatetime().toString() + " ";
		ret += this.getEncounterType() == null ? "(no Type) " : this.getEncounterType().getName() + " ";
		ret += this.getLocation() == null ? "(no Location) " : this.getLocation().getName() + " ";
		ret += this.getPatient() == null ? "(no Patient) " : this.getPatient().getPatientId().toString() + " ";
		ret += this.getForm() == null ? "(no Form) " : this.getForm().getName() + " ";
		ret += "num Obs: " + this.getObsAtTopLevel(false) + " ";
		ret += "num Orders: " + this.getOrders().size() + " ";
		return "Encounter: [" + ret + "]";
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		
		return getEncounterId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setEncounterId(id);
		
	}
	
	/**
	 * Gets the visit.
	 *
	 * @return the visit.
	 * @since 1.9
	 */
	public Visit getVisit() {
		return visit;
	}
	
	/**
	 * Sets the visit
	 *
	 * @param visit the visit to set.
	 * @since 1.9
	 */
	public void setVisit(Visit visit) {
		this.visit = visit;
	}
	
	/**
	 * Gets all unvoided providers, grouped by role.
	 *
	 * @return map of unvoided providers keyed by roles
	 * @since 1.9
	 * @should return empty map if no unvoided providers
	 * @should return all roles and unvoided providers
	 */
	public Map<EncounterRole, Set<Provider>> getProvidersByRoles() {
		return getProvidersByRoles(false);
	}
	
	/**
	 * Gets all providers, grouped by role.
	 *
	 * @param includeVoided set to true to include voided providers, else set to false
	 * @return map of providers keyed by roles
	 * @since 1.9
	 * @should return empty map if no providers
	 * @should return all roles and providers
	 */
	public Map<EncounterRole, Set<Provider>> getProvidersByRoles(boolean includeVoided) {
		
		Map<EncounterRole, Set<Provider>> providers = new HashMap<EncounterRole, Set<Provider>>();
		for (EncounterProvider encounterProvider : encounterProviders) {
			
			if (!includeVoided && encounterProvider.getVoided()) {
				continue;
			}
			
			Set<Provider> list = providers.get(encounterProvider.getEncounterRole());
			if (list == null) {
				list = new LinkedHashSet<Provider>();
				providers.put(encounterProvider.getEncounterRole(), list);
			}
			
			list.add(encounterProvider.getProvider());
		}
		
		return providers;
	}
	
	/**
	 * Gets unvoided providers who had the given role in this encounter.
	 *
	 * @param role
	 * @return unvoided providers or empty set if none was found
	 * @since 1.9
	 * @should return unvoided providers for role
	 * @should return empty set for no role
	 * @should return empty set for null role
	 */
	public Set<Provider> getProvidersByRole(EncounterRole role) {
		return getProvidersByRole(role, false);
	}
	
	/**
	 * Gets providers who had the given role in this encounter.
	 *
	 * @param role
	 * @param includeVoided set to true to include voided providers, else set to false
	 * @return providers or empty set if none was found
	 * @since 1.9
	 * @should return providers for role
	 * @should return empty set for no role
	 * @should return empty set for null role
	 */
	public Set<Provider> getProvidersByRole(EncounterRole role, boolean includeVoided) {
		Set<Provider> providers = new LinkedHashSet<Provider>();
		
		for (EncounterProvider encounterProvider : encounterProviders) {
			if (encounterProvider.getEncounterRole().equals(role)) {
				if (!includeVoided && encounterProvider.getVoided()) {
					continue;
				}
				
				providers.add(encounterProvider.getProvider());
			}
		}
		
		return providers;
	}
	
	/**
	 * Adds a new provider for the encounter, with the given role.
	 *
	 * @param role
	 * @param provider
	 * @since 1.9
	 * @should add provider for new role
	 * @should add second provider for role
	 * @should not add same provider twice for role
	 */
	public void addProvider(EncounterRole role, Provider provider) {
		// first, make sure the provider isn't already there
		for (EncounterProvider ep : encounterProviders) {
			if (ep.getEncounterRole().equals(role) && ep.getProvider().equals(provider) && !ep.isVoided()) {
				return;
			}
		}
		EncounterProvider encounterProvider = new EncounterProvider();
		encounterProvider.setEncounter(this);
		encounterProvider.setEncounterRole(role);
		encounterProvider.setProvider(provider);
		encounterProvider.setDateCreated(new Date());
		encounterProvider.setCreator(Context.getAuthenticatedUser());
		encounterProviders.add(encounterProvider);
	}
	
	/**
	 * Sets the provider for the given role.
	 * <p>
	 * If the encounter already had any providers for the given role, those are removed.
	 *
	 * @param role
	 * @param provider
	 * @since 1.9
	 * @should set provider for new role
	 * @should clear providers and set provider for role
	 * @should void existing EncounterProvider
	 */
	public void setProvider(EncounterRole role, Provider provider) {
		boolean hasProvider = false;
		for (Iterator<EncounterProvider> it = encounterProviders.iterator(); it.hasNext();) {
			EncounterProvider encounterProvider = it.next();
			if (encounterProvider.getEncounterRole().equals(role)) {
				if (!encounterProvider.getProvider().equals(provider)) {
					encounterProvider.setVoided(true);
					encounterProvider.setDateVoided(new Date());
					encounterProvider.setVoidedBy(Context.getAuthenticatedUser());
				} else if (!encounterProvider.isVoided()) {
					hasProvider = true;
				}
			}
		}
		
		if (!hasProvider) {
			addProvider(role, provider);
		}
	}
	
	/**
	 * Removes the provider for a given role.
	 *
	 * @param role the role.
	 * @param provider the provider.
	 * @since 1.9
	 * @should void existing EncounterProvider
	 */
	public void removeProvider(EncounterRole role, Provider provider) {
		for (EncounterProvider encounterProvider : encounterProviders) {
			if (encounterProvider.getEncounterRole().equals(role) && encounterProvider.getProvider().equals(provider)) {
				encounterProvider.setVoided(true);
				encounterProvider.setDateVoided(new Date());
				encounterProvider.setVoidedBy(Context.getAuthenticatedUser());
				return;
			}
		}
	}
	
	/**
	 * Copied encounter will not have visit field copied.
	 *
	 * @param patient the Patient that will be assign to copied Encounter
	 * @return copied encounter
	 *
	 * @should copy all Encounter data except visit and assign copied Encounter to given Patient
	 */
	public Encounter copyAndAssignToAnotherPatient(Patient patient) {
		Map<Order, Order> oldNewOrderMap = new HashMap<Order, Order>();
		Encounter target = new Encounter();
		
		target.setChangedBy(getChangedBy());
		target.setCreator(getCreator());
		target.setDateChanged(getDateChanged());
		target.setDateCreated(getDateCreated());
		target.setDateVoided(getDateVoided());
		target.setVoided(getVoided());
		target.setVoidedBy(getVoidedBy());
		target.setVoidReason(getVoidReason());
		
		// Encounter specific data
		target.setEncounterDatetime(getEncounterDatetime());
		target.setEncounterType(getEncounterType());
		target.setForm(getForm());
		target.setLocation(getLocation());
		target.setPatient(patient);
		
		//encounter providers
		for (EncounterProvider encounterProvider : getEncounterProviders()) {
			EncounterProvider encounterProviderCopy = encounterProvider.copy();
			encounterProviderCopy.setEncounter(target);
			target.getEncounterProviders().add(encounterProviderCopy);
		}
		
		//orders
		for (Order order : getOrders()) {
			Order orderCopy = order.copy();
			orderCopy.setEncounter(target);
			orderCopy.setPatient(patient);
			target.addOrder(orderCopy);
			oldNewOrderMap.put(order, orderCopy);
		}
		
		Context.getEncounterService().saveEncounter(target);
		
		//obs
		for (Obs obs : getAllObs()) {
			Obs obsCopy = Obs.newInstance(obs);
			obsCopy.setEncounter(target);
			obsCopy.setPerson(patient);
			//refresh order reference
			Order oldOrder = obsCopy.getOrder();
			Order newOrder = oldNewOrderMap.get(oldOrder);
			obsCopy.setOrder(newOrder);
			target.addObs(obsCopy);
		}
		
		return target;
	}
}

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
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cascade;
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
@Entity
@Table(name = "encounter")
@BatchSize(size = 25)
public class Encounter extends BaseChangeableOpenmrsData {
	
	public static final long serialVersionUID = 2L;
	
	// Fields
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "encounter_id")
	private Integer encounterId;
	
	@Column(name = "encounter_datetime", nullable = false, length = 19)
	private Date encounterDatetime;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "patient_id")
	private Patient patient;
	
	@ManyToOne
	@JoinColumn(name = "location_id")
	private Location location;
	
	@ManyToOne
	@JoinColumn(name = "form_id")
	private Form form;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "encounter_type")
	private EncounterType encounterType;
	
	@OneToMany(mappedBy = "encounter")
	private Set<Order> orders;
	
	@OneToMany(mappedBy = "encounter")
	private Set<Diagnosis> diagnoses;
	
	@OneToMany(mappedBy = "encounter")
	private Set<Condition> conditions;
	
	@OneToMany(mappedBy = "encounter")
	@Access(AccessType.FIELD)
	@OrderBy("concept_id")
	@BatchSize(size = 25)
	@AllowDirectAccess
	private Set<Obs> obs;
	
	@ManyToOne
	@Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	@JoinColumn(name = "visit_id")
	private Visit visit;
	
	@OneToMany(mappedBy = "encounter", cascade = CascadeType.ALL)
	@OrderBy("provider_id")
	@DisableHandlers(handlerTypes = { VoidHandler.class })
	private Set<EncounterProvider> encounterProviders = new LinkedHashSet<>();
	
	@OneToMany(mappedBy = "encounter")
	private Set<Allergy> allergies;
	
	// Constructors
	
	/** default constructor */
	public Encounter() {
	}
	
	/**
	 * @param encounterId
	 * <strong>Should</strong> set encounter id
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
	 * @return Returns a Set&lt;Obs&gt; of all non-voided, non-obsGroup children Obs of this
	 *         Encounter
	 * <strong>Should</strong> not return null with null obs set
	 * <strong>Should</strong> get obs
	 * <strong>Should</strong> not get voided obs
	 * <strong>Should</strong> only get child obs
	 * <strong>Should</strong> not get child obs if child also on encounter
	 * <strong>Should</strong> get both child and parent obs after removing child from parent grouping
	 * <strong>Should</strong> get obs with two levels of hierarchy
	 * <strong>Should</strong> get obs with three levels of hierarchy
	 * <strong>Should</strong> not get voided obs with three layers of hierarchy
	 */
	public Set<Obs> getObs() {
		Set<Obs> ret = new LinkedHashSet<>();
		
		if (this.obs != null) {
			for (Obs o : this.obs) {
				ret.addAll(getObsLeaves(o));
			}
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
		List<Obs> leaves = new ArrayList<>();
		
		if (obsParent.hasGroupMembers()) {
			for (Obs child : obsParent.getGroupMembers()) {
				if (!child.getVoided()) {
					if (!child.isObsGrouping()) {
						leaves.add(child);
					} else {
						// recurse if this is a grouping obs
						leaves.addAll(getObsLeaves(child));
					}
				}
			}
		} else if (!obsParent.getVoided()) {
			leaves.add(obsParent);
		}
		
		return leaves;
	}

	/**
	 * Convenience method to recursively get all leaf obs for a given parent obs.
	 * @param obsParent indicates the parent Obs
	 * @param includedVoided indicates whether or not to include voided Obs
	 * @return a Set of all leaves Obs including the parent Obs
	 * @since 2.2.1
	 */
	private Set<Obs> getFlattenedObsLeaves(Obs obsParent, boolean includedVoided) {
		Set<Obs> leaves = new LinkedHashSet<>();

		if (includedVoided || (!obsParent.getVoided())) {
			leaves.add(obsParent);
			if (obsParent.hasGroupMembers()) {
				for (Obs child : obsParent.getGroupMembers(includedVoided)) {
					leaves.addAll(getFlattenedObsLeaves(child, includedVoided));
				}
			}
		}
		return leaves;
	}

	/**
	 * Returns all Obs where Obs.encounterId = Encounter.encounterId In practice, this method should
	 * not be used very often...
	 *
	 * @param includeVoided specifies whether or not to include voided Obs
	 * @return Returns the all Obs.
	 * <strong>Should</strong> not return null with null obs set
	 * <strong>Should</strong> get obs
	 * <strong>Should</strong> get both parent and child obs
	 * <strong>Should</strong> get both parent and child with child directly on encounter
	 * <strong>Should</strong> get both child and parent obs after removing child from parent grouping
	 */
	public Set<Obs> getAllObs(boolean includeVoided) {
		if (includeVoided && obs != null) {
			return obs;
		}
		
		Set<Obs> ret = new LinkedHashSet<>();
		
		if (this.obs != null) {
			ret = this.obs.stream().filter(o -> includeVoided || !o.getVoided()).collect(Collectors.toSet());
		}
		return ret;
	}
	
	/**
	 * Convenience method to call {@link #getAllObs(boolean)} with a false parameter
	 *
	 * @return all non-voided obs
	 * <strong>Should</strong> not get voided obs
	 */
	public Set<Obs> getAllObs() {
		return getAllObs(false);
	}

	/**
	 * Returns all encounter obs as a flatten list(Set) of obs.
	 * @param includeVoided indicates whether or not to include voided obs
	 * @return a Set of all encounter' Obs
	 */
	public Set<Obs> getAllFlattenedObs(boolean includeVoided) {

		Set<Obs> ret = new LinkedHashSet<>();

		if (this.obs != null) {
			for (Obs o : this.obs) {
				if (includeVoided || (!o.getVoided())) {
					ret.addAll(getFlattenedObsLeaves(o, includeVoided));
				}
			}
		}
		return ret;
	}
	
	/**
	 * Returns a Set&lt;Obs&gt; of all root-level Obs of an Encounter, including obsGroups
	 *
	 * @param includeVoided specifies whether or not to include voided Obs
	 * @return Returns all obs at top level -- will not be null
	 * <strong>Should</strong> not return null with null obs set
	 * <strong>Should</strong> get obs
	 * <strong>Should</strong> not get voided obs
	 * <strong>Should</strong> only get parents obs
	 * <strong>Should</strong> only return the grouped top level obs
	 * <strong>Should</strong> get both child and parent obs after removing child from parent grouping
	 */
	public Set<Obs> getObsAtTopLevel(boolean includeVoided) {
		
		return getAllObs(includeVoided).stream().filter(o -> o.getObsGroup() == null)
		        .collect(Collectors.toCollection(LinkedHashSet::new));
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
	 * <strong>Should</strong> add obs with null values
	 * <strong>Should</strong> not fail with null obs
	 * <strong>Should</strong> set encounter attribute on obs
	 * <strong>Should</strong> add obs to non null initial obs set
	 * <strong>Should</strong> add encounter attrs to obs if attributes are null
	 * <strong>Should</strong> add encounter attrs to obs groupMembers if attributes are null
	 */
	public void addObs(Obs observation) {
		if (obs == null) {
			obs = new LinkedHashSet<>();
		}
		
		if (observation != null) {
			obs.add(observation);
			
			//Propagate some attributes to the obs and any groupMembers
			
			// a Deque is a two-ended queue, that lets us add to the end, and fetch from the beginning
			Deque<Obs> obsToUpdate = new ArrayDeque<>();
			obsToUpdate.add(observation);
			
			//prevent infinite recursion if an obs is its own group member
			Set<Obs> seenIt = new LinkedHashSet<>();
			
			while (!obsToUpdate.isEmpty()) {
				Obs o = obsToUpdate.removeFirst();
				
				//has this obs already been processed?
				if (o == null || seenIt.contains(o)) {
					continue;
				}
				seenIt.add(o);
				
				o.setEncounter(this);
				
				//if the attribute was already set, preserve it
				//if not, inherit the values from the encounter
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
	 * <strong>Should</strong> remove obs successfully
	 * <strong>Should</strong> not throw error when removing null obs from empty set
	 * <strong>Should</strong> not throw error when removing null obs from non empty set
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
			orders = new LinkedHashSet<>();
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
	 * <strong>Should</strong> add order with null values
	 * <strong>Should</strong> not fail with null obs passed to add order
	 * <strong>Should</strong> set encounter attribute
	 * <strong>Should</strong> add order to non null initial order set
	 * <strong>Should</strong> add order to encounter when adding order to set returned from getOrders
	 */
	public void addOrder(Order order) {
		if (order != null) {
			order.setEncounter(this);
			getOrders().add(order);
		}
	}
	
	/**
	 * Remove the given order from the list of orders for this Encounter
	 *
	 * @param order
	 * <strong>Should</strong> remove order from encounter
	 * <strong>Should</strong> not fail when removing null order
	 * <strong>Should</strong> not fail when removing non existent order
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
	}
	
	/**
	 * Gets the set of diagnoses
	 * 
	 * @return diagnoses - the set of diagnoses.
	 * @since 2.2
	 */
	public Set<Diagnosis> getDiagnoses() {
		return diagnoses;
	}
	
	/**
	 * Sets a set of diagnoses for the current Encounter
	 * 
	 * @param diagnoses the set of Diagnosis to set.
	 * @since 2.2
	 */
	public void setDiagnoses(Set<Diagnosis> diagnoses) {
		this.diagnoses = diagnoses;
	}
	
	/**
	 * Basic property getter for the encounter's non-voided conditions.
	 * 
	 * @return all non-voided conditions
	 * @since 2.4.0, 2.3.1
	 */
	public Set<Condition> getConditions() {
		return getConditions(false);
	}

	/**
	 * Returns all conditions where 'Condition.encounterId = Encounter.encounterId'.
	 *
	 * @param includeVoided - Specifies whether or not to include voided conditions.
	 * @return The set of conditions, or an empty set if there are no conditions to return.
	 * @since 2.3.3, 2.4.0, 2.5.0
	 */
	public Set<Condition> getConditions(boolean includeVoided) {
		return Optional.ofNullable(conditions).orElse(new LinkedHashSet<>())
			.stream().filter(c -> includeVoided || !c.getVoided()).collect(Collectors.toSet());
	}
		
	/**
	 * Basic property setter for conditions
	 *  
	 * @param conditions - set of conditions
	 * @since 2.4.0, 2.3.1
	 */
	public void setConditions(Set<Condition> conditions) {
		this.conditions = conditions;
	}

	/**
	 * Add the given condition to the set of conditions for this encounter.
	 *
	 * @param condition - the condition to add
	 */
	public void addCondition(Condition condition) {
		if (conditions == null) {
			conditions = new LinkedHashSet<>();
		}

		if (condition != null) {
			condition.setEncounter(this);
			conditions.add(condition);
		}
	}

	/**
	 * Remove the given condition from the set of conditions for this encounter.
	 * In practise the condition is not removed but rather voided.
	 *
	 * @param condition - the condition to remove
	 */
	public void removeCondition(Condition condition) {
		Optional.ofNullable(conditions).orElse(new LinkedHashSet<>()).stream().filter(c -> !c.getVoided() && c.equals(condition)).forEach(c -> {
			c.setVoided(true);
			c.setDateVoided(new Date());
			c.setVoidReason("Voided by the API");
			c.setVoidedBy(Context.getAuthenticatedUser());
		});
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
	 * Returns only the non-voided encounter providers for this encounter. If you want <u>all</u>
	 * encounter providers, use {@link #getEncounterProviders()}
	 *
	 * @return list of non-voided encounter providers for this encounter
	 * @see #getEncounterProviders()
	 */
	public Set<EncounterProvider> getActiveEncounterProviders() {
		Set<EncounterProvider> activeProviders = new LinkedHashSet<>();
		Set<EncounterProvider> providers = getEncounterProviders();
		if (providers != null && !providers.isEmpty()) {
			activeProviders = providers.stream().filter(p -> !p.getVoided()).collect(Collectors.toSet());
		}
		return activeProviders;
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
	 * <strong>Should</strong> not fail with empty object
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
	@Override
	public Integer getId() {
		
		return getEncounterId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
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
	 * <strong>Should</strong> return empty map if no unvoided providers
	 * <strong>Should</strong> return all roles and unvoided providers
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
	 * <strong>Should</strong> return empty map if no providers
	 * <strong>Should</strong> return all roles and providers
	 */
	public Map<EncounterRole, Set<Provider>> getProvidersByRoles(boolean includeVoided) {
		
		return encounterProviders
		        .stream()
		        .filter(ep -> includeVoided || !ep.getVoided())
		        .collect(
		            Collectors.groupingBy(EncounterProvider::getEncounterRole,
		                Collectors.mapping(EncounterProvider::getProvider, Collectors.toSet())));
		
	}
	
	/**
	 * Gets unvoided providers who had the given role in this encounter.
	 *
	 * @param role
	 * @return unvoided providers or empty set if none was found
	 * @since 1.9
	 * <strong>Should</strong> return unvoided providers for role
	 * <strong>Should</strong> return empty set for no role
	 * <strong>Should</strong> return empty set for null role
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
	 * <strong>Should</strong> return providers for role
	 * <strong>Should</strong> return empty set for no role
	 * <strong>Should</strong> return empty set for null role
	 */
	public Set<Provider> getProvidersByRole(EncounterRole role, boolean includeVoided) {
		
		return encounterProviders.stream()
		        .filter(ep -> ep.getEncounterRole().equals(role) && (includeVoided || !ep.getVoided()))
		        .map(EncounterProvider::getProvider).collect(Collectors.toSet());
	}
	
	/**
	 * Adds a new provider for the encounter, with the given role.
	 *
	 * @param role
	 * @param provider
	 * @since 1.9
	 * <strong>Should</strong> add provider for new role
	 * <strong>Should</strong> add second provider for role
	 * <strong>Should</strong> not add same provider twice for role
	 */
	public void addProvider(EncounterRole role, Provider provider) {
		// first, make sure the provider isn't already there
		for (EncounterProvider ep : encounterProviders) {
			if (ep.getEncounterRole().equals(role) && ep.getProvider().equals(provider) && !ep.getVoided()) {
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
	 * <strong>Should</strong> set provider for new role
	 * <strong>Should</strong> clear providers and set provider for role
	 * <strong>Should</strong> void existing EncounterProvider
	 */
	public void setProvider(EncounterRole role, Provider provider) {
		boolean hasProvider = false;
		for (EncounterProvider encounterProvider : encounterProviders) {
			if (encounterProvider.getEncounterRole().equals(role)) {
				if (!encounterProvider.getProvider().equals(provider)) {
					encounterProvider.setVoided(true);
					encounterProvider.setDateVoided(new Date());
					encounterProvider.setVoidedBy(Context.getAuthenticatedUser());
				} else if (!encounterProvider.getVoided()) {
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
	 * <strong>Should</strong> void existing EncounterProvider
	 */
	public void removeProvider(EncounterRole role, Provider provider) {
		for (EncounterProvider encounterProvider : encounterProviders) {
			if (encounterProvider.getEncounterRole().equals(role) && encounterProvider.getProvider().equals(provider)
			        && !encounterProvider.getVoided()) {
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
	 * <strong>Should</strong> copy all Encounter data except visit and assign copied Encounter to given Patient
	 */
	public Encounter copyAndAssignToAnotherPatient(Patient patient) {
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
		
		Context.getEncounterService().saveEncounter(target);
		
		//obs
		for (Obs obs : getAllObs()) {
			Obs obsCopy = Obs.newInstance(obs);
			obsCopy.setEncounter(target);
			obsCopy.setPerson(patient);
			target.addObs(obsCopy);
		}
		
		return target;
	}
	
	/**
	 * Takes in a list of orders and pulls out the orderGroups within them
	 *
	 * @since 1.12
	 * @return list of orderGroups
	 */
	public List<OrderGroup> getOrderGroups() {
		Map<String, OrderGroup> orderGroups = new HashMap<>();
		for (Order order : orders) {
			if (order.getOrderGroup() != null) {
				orderGroups.computeIfAbsent(order.getOrderGroup().getUuid(), k -> order.getOrderGroup());
				order.getOrderGroup().addOrder(order, null);
			}
		}
		return new ArrayList<>(orderGroups.values());
	}
	
	/**
	 * Takes in a list of orders and filters out the orders which have orderGroups
	 * 
	 * @since 1.12
	 * @return list of orders not having orderGroups
	 */
	public List<Order> getOrdersWithoutOrderGroups() {
		return orders.stream().filter(o -> o.getOrderGroup() == null).collect(Collectors.toList());
	}
	
	/**
	 * Check if encounter has a particular diagnosis
	 *
	 * @since 2.2
	 * @param diagnosis the diagnosis to check if it belongs to this given encounter
	 * @return true if this encounter has the given diagnosis, else false
	 */
	public Boolean hasDiagnosis(Diagnosis diagnosis) {
		for (Diagnosis diagnosis1 : getDiagnoses()) {
			if (diagnosis.equals(diagnosis1)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Basic property getter for the encounter's non-voided allergies.
	 * 
	 * @return all non-voided allergies
	 * @since 2.5.0
	 */
	public Set<Allergy> getAllergies() {
		return getAllergies(false);
	}

	/**
	 * Returns all allergies where 'Allergy.encounterId = Encounter.encounterId'.
	 *
	 * @param includeVoided - Specifies whether or not to include voided allergies.
	 * @return The set of allergies, or an empty set if there are no allergies to return.
	 * @since 2.5.0
	 */
	public Set<Allergy> getAllergies(boolean includeVoided) {
		return Optional.ofNullable(allergies).orElse(new LinkedHashSet<>())
			.stream().filter(c -> includeVoided || !c.getVoided()).collect(Collectors.toSet());
	}
		
	/**
	 * Basic property setter for allergies
	 *  
	 * @param allergies - set of allergies
	 * @since 2.5.0
	 */
	public void setAllergies(Set<Allergy> allergies) {
		this.allergies = allergies;
	}

	/**
	 * Add the given allergy to the set of allergies for this encounter.
	 *
	 * @param allergy - the allergy to add
	 */
	public void addAllergy(Allergy allergy) {
		if (allergies == null) {
			allergies = new LinkedHashSet<>();
		}

		if (allergy != null) {
			allergy.setEncounter(this);
			allergies.add(allergy);
		}
	}

	/**
	 * Remove the given allergy from the set of allergies for this encounter.
	 * In practice the allergy is not removed but rather voided.
	 *
	 * @param allergy - the allergy to remove
	 */
	public void removeAllergy(Allergy allergy) {
		Optional.ofNullable(allergies).orElse(new LinkedHashSet<>()).stream().filter(c -> !c.getVoided() && c.equals(allergy)).forEach(c -> {
			c.setVoided(true);
			c.setDateVoided(new Date());
			c.setVoidReason("Voided by the API");
			c.setVoidedBy(Context.getAuthenticatedUser());
		});
	}
}

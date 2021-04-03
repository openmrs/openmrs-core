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

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * This class represents a list of patientIds.
 */
public class Cohort extends BaseChangeableOpenmrsData {
	
	public static final long serialVersionUID = 0L;
	
	private Integer cohortId;
	
	private String name;
	
	private String description;
	
	private Collection<CohortMembership> memberships;
	
	public Cohort() {
		memberships = new TreeSet<>();
	}
	
	/**
	 * Convenience constructor to create a Cohort object that has an primarykey/internal identifier
	 * of <code>cohortId</code>
	 *
	 * @param cohortId the internal identifier for this cohort
	 */
	public Cohort(Integer cohortId) {
		this();
		this.cohortId = cohortId;
	}
	
	/**
	 * This constructor does not check whether the database contains patients with the given ids,
	 * but {@link org.openmrs.api.CohortService#saveCohort(Cohort)} will.
	 * 
	 * @param name
	 * @param description optional description
	 * @param ids option array of Integer ids
	 */
	public Cohort(String name, String description, Integer[] ids) {
		this();
		this.name = name;
		this.description = description;
		if (ids != null) {
			Arrays.stream(ids).forEach(this::addMember);
		}
	}
	
	/**
	 * This constructor does not check whether the database contains patients with the given ids,
	 * but {@link org.openmrs.api.CohortService#saveCohort(Cohort)} will.
	 * 
	 * @param name
	 * @param description optional description
	 * @param patients optional array of patients
	 */
	public Cohort(String name, String description, Patient[] patients) {
		this(name, description, (Integer[]) null);
		if (patients != null) {
			Arrays.stream(patients).forEach(p -> addMembership(new CohortMembership(p.getPatientId())));
		}
	}
	
	/**
	 * This constructor does not check whether the database contains patients with the given ids,
	 * but {@link org.openmrs.api.CohortService#saveCohort(Cohort)} will.
	 * 
	 * @param patientsOrIds optional collection which may contain Patients, or patientIds which may
	 *            be Integers, Strings, or anything whose toString() can be parsed to an Integer.
	 */
	public Cohort(Collection<?> patientsOrIds) {
		this(null, null, patientsOrIds);
	}
	
	/**
	 * This constructor does not check whether the database contains patients with the given ids,
	 * but {@link org.openmrs.api.CohortService#saveCohort(Cohort)} will.
	 * 
	 * @param name
	 * @param description optional description
	 * @param patientsOrIds optional collection which may contain Patients, or patientIds which may
	 *            be Integers, Strings, or anything whose toString() can be parsed to an Integer.
	 */
	public Cohort(String name, String description, Collection<?> patientsOrIds) {
		this(name, description, (Integer[]) null);
		if (patientsOrIds != null) {
			for (Object o : patientsOrIds) {
				if (o instanceof Patient) {
					addMembership(new CohortMembership(((Patient) o).getPatientId()));
				} else if (o instanceof Integer) {
					addMembership(new CohortMembership((Integer) o));
				}
			}
		}
	}
	
	/**
	 * Convenience constructor taking in a string that is a list of comma separated patient ids This
	 * constructor does not check whether the database contains patients with the given ids, but
	 * {@link org.openmrs.api.CohortService#saveCohort(Cohort)} will.
	 * 
	 * @param commaSeparatedIds
	 */
	public Cohort(String commaSeparatedIds) {
		this();
		String[] ids = StringUtils.split(commaSeparatedIds, ',');
		Arrays.stream(ids).forEach(id -> addMembership(new CohortMembership(Integer.valueOf(id.trim()))));
	}
	
	/**
	 * @deprecated since 2.1.0 cohorts are more complex than just a set of patient ids, so there is no one-line replacement
	 * @return Returns a comma-separated list of patient ids in the cohort.
	 */
	@Deprecated
	public String getCommaSeparatedPatientIds() {
		return StringUtils.join(getMemberIds(), ',');
	}
	
	public boolean contains(Integer patientId) {
		return getMemberships() != null
		        && getMemberships().stream().anyMatch(m -> m.getPatientId().equals(patientId) && !m.getVoided());
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Cohort id=" + getCohortId());
		if (getName() != null) {
			sb.append(" name=").append(getName());
		}
		if (getMemberships() != null) {
			sb.append(" size=").append(getMemberships().size());
		}
		return sb.toString();
	}
	
	public void addMember(Integer memberId) {
		this.addMembership(new CohortMembership(memberId));
	}
	
	/**
	 * @since 2.1.0
	 */
	public boolean addMembership(CohortMembership cohortMembership) {
		if (cohortMembership != null) {
			cohortMembership.setCohort(this);
			return getMemberships().add(cohortMembership);
		}
		return false;
	}
	
	/**
	 * @since 2.1.0
	 */
	public boolean removeMembership(CohortMembership cohortMembership) {
		return getMemberships().remove(cohortMembership);
	}
	
	/**
	 * @since 2.1.0
	 * @param includeVoided boolean true/false to include/exclude voided memberships
	 * @return Collection of cohort memberships
	 */
	public Collection<CohortMembership> getMemberships(boolean includeVoided) {
		if (includeVoided) {
			return getMemberships();
		}
		return getMemberships().stream().filter(m -> m.getVoided() == includeVoided).collect(Collectors.toList());
	}
	
	/**
	 * @since 2.1.0
	 */
	public Collection<CohortMembership> getMemberships() {
		if (memberships == null) {
			memberships = new TreeSet<>();
		}
		return memberships;
	}
	
	/**
	 * @since 2.1.0
	 * @param asOfDate date used to return active memberships
	 * @return Collection of cohort memberships
	 */
	public Collection<CohortMembership> getActiveMemberships(Date asOfDate) {
		return getMemberships().stream().filter(m -> m.isActive(asOfDate)).collect(Collectors.toList());
	}
	
	public Collection<CohortMembership> getActiveMemberships() {
		return getActiveMemberships(new Date());
	}
	
	/**
	 * @since 2.1.0
	 */
	public CohortMembership getActiveMembership(Patient patient) {
		return getMemberships().stream().filter(m -> m.isActive() && m.getPatientId().equals(patient.getPatientId())).findFirst().get();
	}
	
	public int size() {
		return getMemberships().stream().filter(m -> !m.getVoided()).collect(Collectors.toList())
		        .size();
	}
	
	/**
	 * @deprecated use {@link #size()}
	 */
	@Deprecated
	public int getSize() {
		return size();
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	// static utility methods
	
	/**
	 * Returns the union of two cohorts
	 *
	 * @param a The first Cohort
	 * @param b The second Cohort
	 * @return Cohort
	 */
	public static Cohort union(Cohort a, Cohort b) {
		Cohort ret = new Cohort();
		if (a != null) {
			ret.getMemberships().addAll(a.getMemberships());
		}
		if (b != null) {
			ret.getMemberships().addAll(b.getMemberships());
		}
		if (a != null && b != null) {
			ret.setName("(" + a.getName() + " + " + b.getName() + ")");
		}
		return ret;
	}
	
	/**
	 * Returns the intersection of two cohorts, treating null as an empty cohort
	 *
	 * @param a The first Cohort
	 * @param b The second Cohort
	 * @return Cohort
	 */
	public static Cohort intersect(Cohort a, Cohort b) {
		Cohort ret = new Cohort();
		ret.setName("(" + (a == null ? "NULL" : a.getName()) + " * " + (b == null ? "NULL" : b.getName()) + ")");
		if (a != null && b != null) {
			ret.getMemberships().addAll(a.getMemberships());
			ret.getMemberships().retainAll(b.getMemberships());
		}
		return ret;
	}
	
	/**
	 * Subtracts a cohort from a cohort
	 *
	 * @param a the original Cohort
	 * @param b the Cohort to subtract
	 * @return Cohort
	 */
	public static Cohort subtract(Cohort a, Cohort b) {
		Cohort ret = new Cohort();
		if (a != null) {
			ret.getMemberships().addAll(a.getMemberships());
			if (b != null) {
				ret.getMemberships().removeAll(b.getMemberships());
				ret.setName("(" + a.getName() + " - " + b.getName() + ")");
			}
		}
		return ret;
	}
	
	// getters and setters
	
	public Integer getCohortId() {
		return cohortId;
	}
	
	public void setCohortId(Integer cohortId) {
		this.cohortId = cohortId;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @deprecated since 2.1.0 cohorts are more complex than just a set of patient ids, so there is no one-line replacement
	 */
	@Deprecated
	public Set<Integer> getMemberIds() {
		Set<Integer> memberIds = new TreeSet<>();
		for (CohortMembership member : getMemberships()) {
			memberIds.add(member.getPatientId());
		}
		return memberIds;
	}
	
	/**
	 * @deprecated since 2.1.0 cohorts are more complex than just a set of patient ids, so there is no one-line replacement
	 * @param memberIds
	 */
	@Deprecated
	public void setMemberIds(Set<Integer> memberIds) {
		if (getMemberships().isEmpty()) {
			for (Integer id : memberIds) {
				addMembership(new CohortMembership(id));
			}
		}
		else {
			throw new IllegalArgumentException("since 2.1.0 cohorts are more complex than just a set of patient ids");
		}
	}
	
	public void setMemberships(Collection<CohortMembership> members) {
		this.memberships = members;
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		
		return getCohortId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer id) {
		setCohortId(id);
		
	}
	
	/**
	 * @since 2.3
	 * 
	 * This function checks if there exists any active CohortMembership for a given patientId
	 * 
	 * @param patientId is the patientid that should be checked for activity in cohort
	 * @return true if cohort has active membership for the requested patient             
	 */
	public boolean hasActiveMembership(int patientId) {
		return getMemberships().stream().anyMatch(m  -> m.getPatientId() == patientId && m.isActive());
	}
	
	/**
	 * 
	 * @since  2.3
	 * This method returns the number of active members in the cohort
	 * 
	 * @return  number of active memberships in the cohort
	 */
	public int activeMembershipSize() {
		return getActiveMemberships().size();
	}
	
	/**
	 *
	 * @since  2.3
	 * This method returns true if cohort has no active memberships
	 *
	 * @return true if no active cohort exists
	 **/
	public boolean hasNoActiveMemberships() {
		return getActiveMemberships().isEmpty();
	}
}

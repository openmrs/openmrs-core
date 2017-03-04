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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * This class represents a list of patientIds.
 */
public class Cohort extends BaseOpenmrsData  {
	
	public static final long serialVersionUID = 0L;
	
	private Integer cohortId;
	
	private String name;
	
	private String description;
	
	private Collection<CohortMembership> memberships;

	public Cohort() {
		memberships = new TreeSet<CohortMembership>();
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
	 * but
	 * {@link org.openmrs.api.CohortService#saveCohort(Cohort)} will.
	 * @param name
	 * @param description optional description
	 * @param ids option array of Integer ids
	 */
	public Cohort(String name, String description, Integer[] ids) {
		this();
		this.name = name;
		this.description = description;
		if (ids != null) {
			Arrays.stream(ids).forEach(id -> addMember(id));
		}
	}
	
	/**
	 * This constructor does not check whether the database contains patients with the given ids,
	 * but
	 * {@link org.openmrs.api.CohortService#saveCohort(Cohort)} will.
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
	 * but
	 * {@link org.openmrs.api.CohortService#saveCohort(Cohort)} will.
	 * @param patientsOrIds optional collection which may contain Patients, or patientIds which may
	 *            be Integers, Strings, or anything whose toString() can be parsed to an Integer.
	 */
	public Cohort(Collection<?> patientsOrIds) {
		this(null, null, patientsOrIds);
	}
	
	/**
	 * This constructor does not check whether the database contains patients with the given ids,
	 * but
	 * {@link org.openmrs.api.CohortService#saveCohort(Cohort)} will.
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
	 * @param commaSeparatedIds
	 */
	public Cohort(String commaSeparatedIds) {
		this();
		String[] ids = StringUtils.split(commaSeparatedIds, ',');
		Arrays.stream(ids).forEach(id -> addMembership(Integer.valueOf(id.trim())));
	}
	
	/**
	 * @return Returns a comma-separated list of patient ids in the cohort.
	 */
	public String getCommaSeparatedPatientIds() {
		return StringUtils.join(getMemberIds(), ',');
	}

	public boolean contains(Integer patientId) {
		return getMemberships() != null && getMemberships().stream()
				.anyMatch(m -> m.getPatientId().equals(patientId) && m.isActive());
	}
	
	/**
	 * @since 2.1.0
	 */
	public boolean contains(Patient patient) {
		return contains(patient.getPatientId());
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

	public void removeMember(Integer memberId) {
		removeMember(new Patient(memberId));
	}
	
	/**
	 * @since 2.1.0
	 */
	public boolean removeMember(Patient patient) {
		CohortMembership memberToRemove = this.getActiveMembership(patient);
		if (memberToRemove != null && memberToRemove.getPatientId().equals(patient.getPatientId())) {
			memberToRemove.setEndDate(new Date());
			return true;
		}
		return false;
	}
	
	/**
	 * @since 2.1.0
	 */
	public boolean addMembership(CohortMembership cohortMembership) {
		if (cohortMembership != null && !this.contains(cohortMembership.getPatientId())) {
			cohortMembership.setCohort(this);
			return getMemberships().add(cohortMembership);
		}
		return false;
	}
	
	public boolean addMembership(Integer patientId) {
		return addMembership(new CohortMembership(patientId));
	}

	/**
	 * @since 2.1.0
	 */
	public void removeMembership(CohortMembership cohortMembership) {
		cohortMembership.setEndDate(new Date());
	}

	/**
	 * @since 2.1.0
	 * @param includeVoided boolean true/false to include/exclude voided memberships
	 * @return ArrayList of cohort memberships
	 */
	public List<CohortMembership> getMemberships(boolean includeVoided) {
		return getMemberships().stream()
				.filter(m -> m.getVoided() == includeVoided)
				.collect(Collectors.toList());
	}

	/**
	 * @since 2.1.0
	 */
	public Collection<CohortMembership> getMemberships() {
		if (memberships == null) {
			memberships = new TreeSet<CohortMembership>();
		}
		return memberships;
	}

	/**
	 * @since 2.1.0
	 * @param asOfDate date used to return active memberships
	 * @return ArrayList of cohort memberships
	 */
	public List<CohortMembership> getActiveMemberships(Date asOfDate) {
		return getMemberships().stream()
				.filter(m -> m.isActive(asOfDate))
				.collect(Collectors.toList());
	}

	public List<CohortMembership> getActiveMemberships() {
		return getActiveMemberships(new Date());
	}

	/**
	 * @since 2.1.0
	 */
	public CohortMembership getActiveMembership(Patient patient) {
		return getMemberships().stream()
				.filter(m -> m.isActive() && m.getPatientId().equals(patient.getPatientId()))
				.collect(Collectors.toList())
				.get(0);
	}
	
	/**
	 * @since 2.1.0
	 */
	public boolean purgeMemberships(List<CohortMembership> cohortMembershipList) {
		List<CohortMembership> membershipsToPurge = getMemberships().stream()
				.filter(m -> cohortMembershipList.stream()
						.anyMatch(c -> m.getPatientId().equals(c.getPatientId())))
				.collect(Collectors.toList());
		if (membershipsToPurge != null) {
			membershipsToPurge.forEach(m -> getMemberships().remove(m));
			return true;
		}
		return false;
	}


	public int size() {
		return getMemberships().stream()
				.filter(m -> !m.getVoided() && m.getEndDate() == null).collect(Collectors.toList()).size();
	}
	
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

	public Set<Integer> getMemberIds() {
		Set<Integer> memberIds = new TreeSet<Integer>();
		for (CohortMembership member : getMemberships()) {
			memberIds.add(member.getPatientId());
		}
		return memberIds;
	}

	public void setMemberIds(Set<Integer> memberIds) {
		Set<Integer> ids = new TreeSet<Integer>(memberIds);
		for (Integer id : ids) {
			addMembership(new CohortMembership(id));
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
}

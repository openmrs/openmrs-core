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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * This class represents a list of patientIds.
 */
public class Cohort extends BaseOpenmrsData  {
	
	public static final long serialVersionUID = 0L;
	
	private static final Log log = LogFactory.getLog(Cohort.class);
	
	private Integer cohortId;
	
	private String name;
	
	private String description;

	private Set<Integer> memberIds;

	private Collection<CohortMembership> members;

	public Cohort() {
		members = new TreeSet<CohortMembership>();
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
			for (int id : ids) {
				addMember(id);
			}
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
			for (Patient p : patients) {
				addMembership(new CohortMembership(p));
			}
		}
	}
	
	/**
	 * This constructor does not check whether the database contains patients with the given ids,
	 * but
	 * {@link org.openmrs.api.CohortService#saveCohort(Cohort)} will.
	 * @param patientsOrIds optional collection which may contain Patients, or patientIds which may
	 *            be Integers, Strings, or anything whose toString() can be parsed to an Integer.
	 */
	@SuppressWarnings("unchecked")
	public Cohort(Collection patientsOrIds) {
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
	@SuppressWarnings("unchecked")
	public Cohort(String name, String description, Collection patientsOrIds) {
		this(name, description, (Integer[]) null);
		if (patientsOrIds != null) {
			for (Object o : patientsOrIds) {
				if (o instanceof Patient) {
					addMembership(new CohortMembership((Patient) o));
				} else if (o instanceof Integer) {
					addMembership(new CohortMembership(new Patient((Integer) o)));
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
		for (StringTokenizer st = new StringTokenizer(commaSeparatedIds, ","); st.hasMoreTokens();) {
			String id = st.nextToken();
			Patient pid = new Patient(Integer.valueOf(id.trim()));
			addMembership(new CohortMembership(pid));
		}
	}
	
	/**
	 * @return Returns a comma-separated list of patient ids in the cohort.
	 */
	public String getCommaSeparatedPatientIds() {
		StringBuilder sb = new StringBuilder();
		for (CohortMembership member : getMembers()) {
			sb.append(member.getPatient().getPatientId());
			sb.append(",");
		}
		return sb.toString();
	}

	public boolean contains(Integer patientId) {
		return getMembers() != null && getMembers().stream()
				.anyMatch(m -> m.getPatient().getPatientId().equals(patientId) && m.isMemberActive());
	}
	
	public boolean contains(Patient patient) {
		return contains(patient.getPatientId());
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("Cohort id=" + getCohortId());
		if (getName() != null) {
			sb.append(" name=").append(getName());
		}
		if (getMembers() != null) {
			sb.append(" size=").append(getMembers().size());
		}
		return sb.toString();
	}

	public void addMember(Integer memberId) {
		this.addMembership(new CohortMembership(new Patient(memberId)));
	}

	public void removeMember(Integer memberId) {
		removeMember(new Patient(memberId));
	}
	
	public void removeMember(Patient patient) {
		List<CohortMembership> memberToRemoveList = getMembers().stream()
				.filter(m -> m.getPatient().getPatientId().equals(patient.getPatientId())).collect(Collectors.toList());
		memberToRemoveList.forEach(m -> m.setEndDate(new Date()));
	}

	public void addMembership(CohortMembership cohortMembership) {
		if (!this.contains(cohortMembership.getPatient().getPatientId())) {
			cohortMembership.setCohort(this);
			getMembers().add(cohortMembership);
		}
	}

	public void removeMembership(CohortMembership cohortMembership) {
		List<CohortMembership> memberToRemoveList = getMembers().stream()
				.filter(m -> m.equals(cohortMembership) ||
						m.getPatient().getPatientId().equals(cohortMembership.getPatient().getPatientId()))
				.collect(Collectors.toList());
		memberToRemoveList.forEach(m -> m.setEndDate(new Date()));
	}

	public List<CohortMembership> getMemberships(Date asOf) {
		return getMembers().stream()
				.filter(m -> !m.getStartDate().before(asOf)).collect(Collectors.toList());
	}

	public void purgeMemberships(List<CohortMembership> cohortMembershipList) {
		List<CohortMembership> membersToPurge = getMembers().stream()
				.filter(m -> cohortMembershipList.stream()
						.anyMatch(c -> m.getPatient().getPatientId().equals(c.getPatient().getPatientId())))
				.collect(Collectors.toList());
		membersToPurge.forEach(m -> getMembers().remove(m));
	}


	public int size() {
		return getMembers() == null ? 0 : getMembers().size();
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
			ret.getMembers().addAll(a.getMembers());
		}
		if (b != null) {
			ret.getMembers().addAll(b.getMembers());
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
			ret.getMembers().addAll(a.getMembers());
			ret.getMembers().retainAll(b.getMembers());
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
			ret.getMembers().addAll(a.getMembers());
			if (b != null) {
				ret.getMembers().removeAll(b.getMembers());
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
		memberIds = new TreeSet<Integer>();
		for (CohortMembership member : getMembers()) {
			memberIds.add(member.getPatient().getPatientId());
		}
		return memberIds;
	}

	public void setMemberIds(Set<Integer> memberIds) {
		this.memberIds = new TreeSet<Integer>(memberIds);
		for (Integer id : memberIds) {
			addMembership(new CohortMembership(new Patient(id)));
		}
	}

	public Collection<CohortMembership> getMembers() {
		if (members == null) {
			members = new TreeSet<CohortMembership>();
		}
		return members;
	}

	public void setMembers(Collection<CohortMembership> members) {
		this.members = members;
	}

	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	public Integer getId() {
		
		return getCohortId();
	}
	
	/**
	 * @since 1.5
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	public void setId(Integer id) {
		setCohortId(id);

	}
}

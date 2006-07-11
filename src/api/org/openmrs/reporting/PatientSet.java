package org.openmrs.reporting;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.openmrs.Patient;

public class PatientSet {

	private Integer patientSetId;
	private String name;
	private Set<Integer> patientIds;
	
	public PatientSet() {
		patientIds = new HashSet<Integer>();
	}
	
	public PatientSet(Set<Patient> patients) {
		patientIds = new HashSet<Integer>();
		for (Patient patient : patients) {
			patientIds.add(patient.getPatientId());
		}
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Returns the patientIds.
	 */
	public Set<Integer> getPatientIds() {
		return patientIds;
	}

	/**
	 * @param patientIds The patientIds to set.
	 */
	public void setPatientIds(Set<Integer> patientIds) {
		this.patientIds = patientIds;
	}

	/**
	 * @return Returns the patientSetId.
	 */
	public Integer getPatientSetId() {
		return patientSetId;
	}

	/**
	 * @param patientSetId The patientSetId to set.
	 */
	public void setPatientSetId(Integer patientSetId) {
		this.patientSetId = patientSetId;
	}

	public void add(Integer patientId) {
		patientIds.add(patientId);
	}
	
	public void add(Patient patient) {
		patientIds.add(patient.getPatientId());
	}
	
	public boolean remove(Integer patientId) {
		return patientIds.remove(patientId);
	}
	
	public boolean remove(Patient patient) {
		return patientIds.remove(patient.getPatientId());
	}
	
	public boolean contains(Integer patientId) {
		return patientIds.contains(patientId);
	}
	
	public boolean contains(Patient patient) {
		return patientIds.contains(patient.getPatientId());
	}
	
	public PatientSet copy() {
		PatientSet ret = new PatientSet();
		ret.patientIds.addAll(patientIds);
		return ret;
	}
	
	/**
	 * Does not change this PatientSet object
	 * @return the intersection between this and other 
	 */
	public PatientSet intersect(PatientSet other) {
		PatientSet ret = copy();
		ret.patientIds.retainAll(other.patientIds);
		return ret;
	}

	/**
	 * Does not change this PatientSet object
	 * @return this set *minus* all members of the other set
	 */
	public PatientSet subtract(PatientSet other) {
		PatientSet ret = copy();
		ret.patientIds.removeAll(other.patientIds);
		return ret;
	}

	public String toString() {
		StringBuffer ret = new StringBuffer();
		int soFar = 0; 
		for (Integer patientId : patientIds) {
			ret.append(patientId).append("\n");
			if (++soFar > 20) {
				ret.append("...");
				break;
			}
		}
		return ret.toString();
	}
	
	public static PatientSet parseCommaSeparatedPatientIds(String s) {
		PatientSet ret = new PatientSet();
		for (StringTokenizer st = new StringTokenizer(s, ","); st.hasMoreTokens(); ) {
			ret.add(new Integer(st.nextToken()));
		}
		return ret;
	}
	
	public String toCommaSeparatedPatientIds() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<Integer> i = patientIds.iterator(); i.hasNext(); ) {
			sb.append(i.next());
			if (i.hasNext()) {
				sb.append(",");
			}
		}
		return sb.toString();
	}
	
	public int size() {
		return patientIds.size();
	}
	
	public int getSize() {
		return patientIds.size();
	}
}

package org.openmrs.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

public class PatientSet {

	private Integer patientSetId;
	private String name;
	private List<Integer> patientIds;
	
	public PatientSet() {
		patientIds = new ArrayList<Integer>();
	}
		
	public PatientSet(Collection<Patient> patients) {
		patientIds = new ArrayList<Integer>();
		for (Patient patient : patients) {
			Integer ptId = patient.getPatientId();
			if (!patientIds.contains(ptId)) {
				patientIds.add(ptId);
			}
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
	public List<Integer> getPatientIds() {
		return patientIds;
	}

	/**
	 * @param patientIds The patientIds to set.
	 */
	public void setPatientIds(List<Integer> patientIds) {
		this.patientIds = patientIds;
	}

	/**
	 * @return Returns the patientSetId.
	 */
	public Integer getPatientSetId() {
		return patientSetId;
	}
	
	/**
	 * @return the current PatientSet
	 */
	public PatientSet copyPatientIds(Collection<Integer> patientIds) {
		this.patientIds = new ArrayList<Integer>(patientIds);
		return this;
	}

	/**
	 * @param patientSetId The patientSetId to set.
	 */
	public void setPatientSetId(Integer patientSetId) {
		this.patientSetId = patientSetId;
	}

	public void add(Integer patientId) {
		if (!patientIds.contains(patientId)) {
			patientIds.add(patientId);
		}
	}
	
	public void add(Patient patient) {
		if (!patientIds.contains(patient.getPatientId())) {
			patientIds.add(patient.getPatientId());
		}
	}
	
	public void add(PatientSet patientSet) {
		this.patientIds.addAll(patientSet.getPatientIds());
	}
	
	public boolean remove(Integer patientId) {
		return patientIds.remove(patientId);
	}
	
	public boolean remove(Patient patient) {
		return patientIds.remove(patient.getPatientId());
	}
	
	public boolean removeAllIds(Collection<Integer> ptIds) {
		return patientIds.removeAll(ptIds);
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
			String id = st.nextToken();
			ret.add(new Integer(id.trim()));
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
	
	// For bean-style calls
	public String getCommaSeparatedPatientIds() {
		return toCommaSeparatedPatientIds();
	}
	
	public int size() {
		return patientIds.size();
	}
	
	public int getSize() {
		return size();
	}
	
	/**
	 * This method can be resource-intensive for large patient sets
	 * @return An alphabetically-sorted list of the patients in this set
	 */
	public List<Patient> getPatients() {
		List<Patient> ret = Context.getPatientSetService().getPatients(getPatientIds());
		Collections.sort(ret, new Comparator<Patient>() {
				public int compare(Patient left, Patient right) {
					return OpenmrsUtil.compareWithNullAsGreatest(left.getPersonName(), right.getPersonName());
				}
			});
		return ret;
	}
}

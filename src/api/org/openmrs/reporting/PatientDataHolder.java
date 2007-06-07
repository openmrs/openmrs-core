package org.openmrs.reporting;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.Patient;

public class PatientDataHolder {

	private Patient patient;
	private Map<String, Object> map;
	
	public PatientDataHolder(Patient patient) {
		this.patient = patient;
		map = new HashMap<String, Object>();
	}

	/**
	 * @return Returns the map.
	 */
	public Map<String, Object> getMap() {
		return map;
	}

	/**
	 * @param map The map to set.
	 */
	public void setMap(Map<String, Object> map) {
		this.map = map;
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
	
	public Object getValue(String key) {
		return map.get(key);
	}
	
	public void putValue(String key, Object value) {
		map.put(key, value);
	}
	
	public String toString() {
		StringBuffer ret = new StringBuffer();
		for (Object o : map.values()) {
			ret.append(o + " ");
		}
		return ret.toString();
	}
	
}

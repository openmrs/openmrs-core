package org.openmrs.reporting;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import org.openmrs.Patient;

/**
 * Holds a mapping of Patient to patient data
 * @author djazayeri
 */
public class PatientDataSet {

	private Map<Patient, Map<String, Object>> data;

	/**
	 * Supplement this PatientDataSets data with the the data from another PatientDataSet   
	 * @param other
	 */
	public void combine(PatientDataSet other) {
		data.putAll(other.data);
	}
	
	/**
	 * example: pds.groupDataItems("all_positive_smears", new EarliestObservationGrouper(), "earliest_positive_smear")
	 */
	public void groupDataItems(String key, DataItemGrouper how, String newKey) {
		for (Iterator<Map.Entry<Patient, Map<String, Object>>> i = data.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<Patient, Map<String, Object>> e = i.next();
			Patient patient = e.getKey();
			Map<String, Object> patientData = e.getValue();
			Collection<Object> patientDataItem = (Collection<Object>) patientData.get(key);
			patientData.put(newKey, how.group(patient, patientDataItem));
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Patient key) {
		return data.containsKey(key);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#equals(java.lang.Object)
	 */
	public boolean equals(Map<Patient, Map<String, Object>> o) {
		return data.equals(o);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Map<String, Object> get(Patient key) {
		return data.get(key);
	}

	/* (non-Javadoc)
	 * @see java.util.Map#hashCode()
	 */
	public int hashCode() {
		return data.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		return data.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Map#size()
	 */
	public int size() {
		return data.size();
	}

}

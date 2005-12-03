package org.openmrs.reporting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmrs.Patient;

/**
 * Holds a mapping of Patient to patient data
 * @author djazayeri
 */
public class PatientDataSet {

	private Map<Patient, Map<String, Object>> data;
	private Set<String> allColumnNames; //TODO: Usage of the delegate methods here can cause this to get out of sync.
	
	public PatientDataSet(PatientSet ps) {
		data = new HashMap<Patient, Map<String, Object>>();
		allColumnNames = new java.util.HashSet<String>();
		for (Iterator<Patient> i = ps.iterator(); i.hasNext(); ) {
			data.put(i.next(), new HashMap<String, Object>());
		}
	}

	/**
	 * Supplement this PatientDataSets data with the the data from another PatientDataSet   
	 * @param other
	 */
	public void combine(PatientDataSet other) {
		data.putAll(other.data);
		allColumnNames.addAll(other.allColumnNames);
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
	
	/**
	 * Puts a patient-level data item in the data set, replacing any previously-existing value for that Patient and Key 
	 * @param who	which patient to add the data item to 
	 * @param name	the name of the data item
	 * @param value	the value of the data item
	 * @throws IllegalArgumentException if who is not in this PatientDataSet
	 */
	public void putDataItem(Patient who, String name, Object value) {
		allColumnNames.add(name);
		Map<String, Object> map = data.get(who);
		if (map == null) {
			throw new IllegalArgumentException("Patient not in PatientDataSet");
		}
		map.put(name, value);
	}
	
	public Object getDataItem(Patient who, String name) {
		Map<String, Object> map = data.get(who);
		if (who == null) {
			throw new IllegalArgumentException("Patient not in PatientDataSet");
		}
		return map.get(name);
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

	//TODO: don't let this iterator be used to add columns 
	public Iterator<Map.Entry<Patient, Map<String, Object>>> iterator() {
		return data.entrySet().iterator();
	}
	
	/* (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Map<String, Object> get(Patient key) {
		return data.get(key); //TODO: don't let this be used to add columns
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

	/**
	 * @return For debugging purposes, display this data set as an HTML table. 
	 */
	public String toHtmlTable() {
		List<String> columns = new ArrayList<String>(allColumnNames);
		Collections.sort(columns);
		StringBuffer sb = new StringBuffer();
		sb.append("<table border=1>");
		sb.append("<tr><td>PatientId</td>");
		for (Iterator<String> i = columns.iterator(); i.hasNext(); ) {
			sb.append("<td>" + i.next() + "</td>");
		}
		sb.append("</tr>");
		for (Iterator<Patient> i = data.keySet().iterator(); i.hasNext(); ) {
			Patient p = i.next();
			sb.append("<tr>");
			sb.append("<td>" + p.getPatientId() + "</td>");
			for (Iterator<String> j = columns.iterator(); j.hasNext(); ) {
				sb.append("<td>" + getDataItem(p, j.next()) + "<td>");
			}
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}
}

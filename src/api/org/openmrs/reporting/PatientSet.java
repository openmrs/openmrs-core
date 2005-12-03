package org.openmrs.reporting;

import org.openmrs.Patient;
import java.util.*;

public class PatientSet implements Set<Patient> {
	
	HashSet<Patient> patients;
	
	public PatientSet() {
		patients = new HashSet<Patient>();
	}
	
	public PatientSet(Collection<? extends Patient> p) {
		patients = new HashSet<Patient>(p);
	}

	public boolean add(Patient p) {
		return patients.add(p);
	}
	
	public boolean addAll(Collection<? extends Patient> c) {
		return patients.addAll(c);
	}

	public void clear() {
		patients.clear();
	}

	public boolean contains(Object o) {
		return patients.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return patients.containsAll(c);
	}

	public boolean equals(Object o) {
		return patients.equals(o);
	}

	public int hashCode() {
		return patients.hashCode();
	}

	public boolean isEmpty() {
		return patients.isEmpty();
	}

	public Iterator<Patient> iterator() {
		return patients.iterator();
	}

	public boolean remove(Object o) {
		return patients.remove(o);
	}

	public boolean removeAll(Collection<?> c) {
		return patients.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return patients.retainAll(c);
	}

	public int size() {
		return patients.size();
	}

	public Object[] toArray() {
		return patients.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return patients.toArray(a);
	}

	public String toString() {
		return "PatientSet{" + size() + " patients. Ids: " + getPatientIds().toString() + "}";
	}

	/**
	 * @return a shallow copy of this patient set
	 */
	public Set<Patient> getPatients() {
		return (Set<Patient>) patients.clone();
	}
	
	public Set<Integer> getPatientIds() {
		Set<Integer> ret = new HashSet<Integer>();
		for (Iterator i = patients.iterator(); i.hasNext(); ) {
			Patient p = (Patient) i.next();
			ret.add(p.getPatientId());
		}
		return ret;
	}

}
package org.openmrs.reporting;

import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;

public class PersonAttributeFilter extends AbstractPatientFilter implements PatientFilter {

	private PersonAttributeType attribute;
	private String value;
	
	/**
	 * This currently only returns patients, although it's named for persons.
	 */
	public PersonAttributeFilter() { }
	
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append("Patients with ");
		sb.append(getAttribute() != null ? getAttribute().getName() : " any attribute");
		if (getValue() != null) {
			sb.append(" equal to ");
			sb.append(getValue());
		}
		return sb.toString();
	}
	
	public PatientSet filter(PatientSet input) {
		PatientSet ps = Context.getPatientSetService().getPatientsHavingPersonAttribute(getAttribute(), getValue());
		return input == null ? ps : input.intersect(ps);
	}

	public PatientSet filterInverse(PatientSet input) {
		PatientSet ps = Context.getPatientSetService().getPatientsHavingPersonAttribute(getAttribute(), getValue());
		return input.subtract(ps);
	}

	public boolean isReadyToRun() {
		// TODO Auto-generated method stub
		return true;
	}
	
	// getters and setters

	public PersonAttributeType getAttribute() {
		return attribute;
	}

	public void setAttribute(PersonAttributeType attribute) {
		this.attribute = attribute;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}

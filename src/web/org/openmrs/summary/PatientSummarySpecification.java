package org.openmrs.summary;

import java.util.List;
import java.util.Properties;

public class PatientSummarySpecification {

	private static PatientSummarySpecification singleton;
	
	public static PatientSummarySpecification getInstance() {
		return singleton;
	}
	
	private List<Properties> specification;
	
	public PatientSummarySpecification() {
		singleton = this;
	}

	public List<Properties> getSpecification() {
		return specification;
	}

	public void setSpecification(List<Properties> specification) {
		this.specification = specification;
	}
	
}

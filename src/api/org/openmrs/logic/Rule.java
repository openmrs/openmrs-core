package org.openmrs.logic;

import org.openmrs.Patient;

public abstract class Rule {
	
	public Rule() {}
	
	public Result eval(LogicDataSource dataSource, Patient patient) {
		return eval(dataSource, patient, null);
	}
	
	public abstract Result eval(LogicDataSource dataSource, Patient patient,
			Object[] args);
	
	public Class[] getArgumentProfile() {
		return null;
	}
	
	public Rule[] getDependencies() {
		return null;
	}

}

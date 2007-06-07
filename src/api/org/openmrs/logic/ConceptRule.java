package org.openmrs.logic;

import org.openmrs.Concept;
import org.openmrs.Patient;

public class ConceptRule extends Rule {
	
	private Concept concept;
	
	public ConceptRule(Concept concept) {
		this.concept = concept;
	}
	
	public Concept getConcept() {
		return concept;
	}

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient) {
		return dataSource.eval(patient, this, null);
	}

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient, Object[] args) {
		return dataSource.eval(patient, this, null);
	}

	@Override
	public Rule[] getDependencies() {
		return super.getDependencies();
	}
	
	

}

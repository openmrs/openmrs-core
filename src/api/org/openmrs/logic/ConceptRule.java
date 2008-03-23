/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
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

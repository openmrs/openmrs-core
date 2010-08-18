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
package org.openmrs.cohort;

import java.util.List;
import java.util.Vector;

import org.openmrs.Cohort;
import org.openmrs.report.Parameter;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * This class provides access to org.openmrs.Cohort objects that are saved in the cohort table.
 * 
 * @deprecated see reportingcompatibility module
 */
@Root(strict = false)
@Deprecated
public class StaticCohortDefinition implements CohortDefinition {
	
	private static final long serialVersionUID = 1L;
	
	private Cohort cohort;
	
	public StaticCohortDefinition() {
	}
	
	public StaticCohortDefinition(Cohort cohort) {
		if (cohort == null)
			throw new NullPointerException();
		this.cohort = cohort;
	}
	
	/**
	 * @see org.openmrs.report.Parameterizable#getParameters()
	 */
	public List<Parameter> getParameters() {
		return new Vector<Parameter>();
	}
	
	/**
	 * @return the cohort
	 */
	@Element(required = false)
	public Cohort getCohort() {
		return cohort;
	}
	
	/**
	 * @param cohort the cohort to set
	 */
	@Element(required = false)
	public void setCohort(Cohort cohort) {
		this.cohort = cohort;
	}
	
}

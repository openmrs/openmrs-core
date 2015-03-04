/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
		if (cohort == null) {
			throw new NullPointerException();
		}
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

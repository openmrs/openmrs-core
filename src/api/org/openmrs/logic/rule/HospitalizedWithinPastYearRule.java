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
package org.openmrs.logic.rule;

import org.openmrs.Patient;
import org.openmrs.logic.DateConstraint;
import org.openmrs.logic.Duration;
import org.openmrs.logic.LogicDataSource;
import org.openmrs.logic.Result;
import org.openmrs.logic.Rule;

public class HospitalizedWithinPastYearRule extends Rule {

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient,
			Object[] args) {

		Result referrals = dataSource.eval(patient, null, "REFERRALS ORDERED",
				DateConstraint.withinPreceding(Duration.years(1)), args);
	
		// Concept 5485 = INPATIENT CARE OR HOSPITALIZATION		
		if (referrals.containsConcept(5485))
			return new Result("YES");
		
		Result hospitalized = dataSource.eval(patient, null,
				"PATIENT HOSPITALIZED",
				DateConstraint.withinPreceding(Duration.years(1)), args);
		if (!hospitalized.isNull())
			return new Result("YES");

		Result hospitalizedSinceLastVisit = dataSource.eval(patient, null,
				"HOSPITALIZED SINCE LAST VISIT",
				DateConstraint.withinPreceding(Duration.years(1)), null);
		if (hospitalizedSinceLastVisit.contains(true))
			return new Result("YES");
		
		Result hospitalizedPreviousYear = dataSource.eval(patient, null,
				"HOSPITALIZED PREVIOUS YEAR",
				DateConstraint.withinPreceding(Duration.months(6)), null);
		if (hospitalizedPreviousYear.contains(true))
			return new Result("YES");

		if (referrals.isNull() & hospitalized.isNull() & hospitalizedSinceLastVisit.isNull() & 
				hospitalizedPreviousYear.isNull())
			return new Result("UNKNOWN");
				
		return new Result("NO");
	}

}

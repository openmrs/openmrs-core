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

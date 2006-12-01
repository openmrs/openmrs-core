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
		boolean referredToHospital = referrals.containsConcept(5485);

		boolean hospitalized = dataSource.eval(patient, null,
				"PATIENT HOSPITALIZED",
				DateConstraint.withinPreceding(Duration.years(1)), args)
				.exists();

		boolean hospitalizedSinceLastVisit = dataSource.eval(patient, null,
				"HOSPITALIZED SINCE LAST VISIT",
				DateConstraint.withinPreceding(Duration.years(1)), null)
				.exists();

		boolean hospitalizedPreviousYear = dataSource.eval(patient, null,
				"HOSPITALIZED PREVIOUS YEAR",
				DateConstraint.withinPreceding(Duration.months(6)), null)
				.exists();

		return new Result(referredToHospital || hospitalized
				|| hospitalizedSinceLastVisit || hospitalizedPreviousYear);
	}

}

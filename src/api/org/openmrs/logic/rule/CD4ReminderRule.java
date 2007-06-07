package org.openmrs.logic.rule;

import org.openmrs.Patient;
import org.openmrs.logic.DateConstraint;
import org.openmrs.logic.Duration;
import org.openmrs.logic.LogicDataSource;
import org.openmrs.logic.Result;
import org.openmrs.logic.Rule;

public class CD4ReminderRule extends Rule {

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient,
			Object[] args) {
		Result cd4 = dataSource.eval(patient, null, "CD4, BY FACS", DateConstraint
				.withinPreceding(Duration.months(6)), args);
		return new Result(cd4.exists());
	}

}

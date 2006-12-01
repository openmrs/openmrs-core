package org.openmrs.logic.rule;

import java.util.Date;

import org.openmrs.Patient;
import org.openmrs.logic.Aggregation;
import org.openmrs.logic.LogicDataSource;
import org.openmrs.logic.Result;
import org.openmrs.logic.Rule;

public class HelloWorldRule extends Rule {

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient,
			Object[] args) {

		Result r = dataSource.eval(patient, Aggregation.minimum(), "WEIGHT (KG)", null, args);
		return new Result(null, new Date(), null, null, null, "Hello world! " + r, null);

	}

}

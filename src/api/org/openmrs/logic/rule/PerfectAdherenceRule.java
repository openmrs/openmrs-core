package org.openmrs.logic.rule;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.logic.LogicDataSource;
import org.openmrs.logic.Result;
import org.openmrs.logic.Rule;

public class PerfectAdherenceRule extends Rule {

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient, Object[] args) {
		Result lastMonth = dataSource.eval(patient, "OVERALL DRUG ADHERENCE IN LAST MONTH");
		if (lastMonth.containsConcept(1085)) // ANTIRETROVIRAL DRUGS (1085)
			return new Result(false);
		List<Result> lastWeek = dataSource.eval(patient, "ANTIRETROVIRAL ADHERENCE IN PAST WEEK").getResultList();
		for (Result r : lastWeek) {
			if (!r.containsConcept(1163)) // ALL (1163)
				return new Result(false);
		}
		return new Result(true);
	}

}

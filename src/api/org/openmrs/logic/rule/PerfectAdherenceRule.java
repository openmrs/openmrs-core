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

public class PerfectAdherenceRule extends Rule {

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient,
			Object[] args) {
		Result lastMonth = dataSource.eval(patient, null,
				"OVERALL DRUG ADHERENCE IN LAST MONTH", DateConstraint
						.withinPreceding(Duration.years(1)), args);
		if (lastMonth.containsConcept(1065)) // YES (1065)
			return new Result("NO");
		if (lastMonth.containsConcept(1085)) // ANTIRETROVIRAL DRUGS (1085)
			return new Result("NO");
		Result lastWeek = dataSource.eval(patient, null,
				"ANTIRETROVIRAL ADHERENCE IN PAST WEEK", DateConstraint
						.withinPreceding(Duration.years(1)), null);
		for (Result r : lastWeek.getResultList()) {
			if (!r.containsConcept(1163)) // ALL (1163)
				return new Result("NO");
		}
		if (lastMonth.isNull() & lastWeek.isNull())
			return new Result("UNKNOWN");
		return new Result("YES");
	}

}

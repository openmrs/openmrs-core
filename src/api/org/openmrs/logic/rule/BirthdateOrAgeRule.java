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

import java.text.DateFormat;
import java.util.Date;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicDataSource;
import org.openmrs.logic.Result;
import org.openmrs.logic.Rule;

public class BirthdateOrAgeRule extends Rule {
	
	public final long MONTH_IN_MILLISECONDS = 2629800000L;
	public final long YEAR_IN_MILLISECONDS = 31557600000L;

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient,
			Object[] args) {
		Date birthdate = patient.getBirthdate();
		Boolean birthdateEstimated = patient.getBirthdateEstimated();
		if (birthdateEstimated == null)
			birthdateEstimated = false;
		if (birthdateEstimated) {
			long ageInMilliseconds = new Date().getTime() - birthdate.getTime();
			if (ageInMilliseconds > YEAR_IN_MILLISECONDS)
				return new Result(String.valueOf(Math.round(ageInMilliseconds/YEAR_IN_MILLISECONDS)) + " years");
			else if (ageInMilliseconds > MONTH_IN_MILLISECONDS)
				return new Result(String.valueOf(Math.round(ageInMilliseconds/MONTH_IN_MILLISECONDS)) + " months");
			else
				return new Result("newborn");
		} else
			return new Result(DateFormat.getDateInstance(DateFormat.MEDIUM,
					Context.getLocale()).format(birthdate));
	}

}

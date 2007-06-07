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

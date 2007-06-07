package org.openmrs.logic.rule;

import java.text.DateFormat;
import java.util.Date;
import java.util.Set;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicDataSource;
import org.openmrs.logic.Result;
import org.openmrs.logic.Rule;

public class FirstEncounterDate extends Rule {

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient, Object[] args) {
		Set<Encounter> encounterList = Context.getEncounterService().getEncounters(patient);
		Date firstEncounterDate = null;
		if (encounterList != null) {
			for (Encounter encounter : encounterList) {
				if (firstEncounterDate == null || encounter.getEncounterDatetime().before(firstEncounterDate))
					firstEncounterDate = encounter.getEncounterDatetime();
			}
		}
		if (firstEncounterDate != null)
			return new Result(DateFormat.getDateInstance(DateFormat.MEDIUM,
					Context.getLocale()).format(firstEncounterDate));
		else
			return new Result("N/A");
	}

}

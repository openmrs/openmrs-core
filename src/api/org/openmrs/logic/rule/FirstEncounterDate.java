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

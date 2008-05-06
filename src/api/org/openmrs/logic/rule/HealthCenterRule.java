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

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.logic.LogicDataSource;
import org.openmrs.logic.Result;
import org.openmrs.logic.Rule;

public class HealthCenterRule extends Rule {

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient,
			Object[] args) {
		Location healthCenter = null; //patient.getHealthCenter();
		if (healthCenter == null)
			return new Result("UNKNOWN");
		Result result = new Result(healthCenter.getName());
		result.setValueNumeric(healthCenter.getLocationId());
		return result;
	}

}

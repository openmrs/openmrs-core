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

import java.util.List;
import java.util.Vector;

import org.openmrs.Patient;
import org.openmrs.logic.LogicDataSource;
import org.openmrs.logic.Result;
import org.openmrs.logic.Rule;

public class ProblemListRule extends Rule {

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient, Object[] args) {
		
		List<Result> newProbs = dataSource.eval(patient, "PROBLEM ADDED").unique().getResultList();
		if (newProbs.size() < 1)
			return new Result(newProbs, true);
		
		List<Result> resolvedProbs = dataSource.eval(patient, "PROBLEM RESOLVED").unique().getResultList();
		if (resolvedProbs.size() < 1)
			return new Result(newProbs, true);
		
		List<Result> probList = new Vector<Result>();
		for (Result p : newProbs) {
			int i = resolvedProbs.indexOf(p);
			if (i == -1 || resolvedProbs.get(i).getDate().before(p.getDate()))
				probList.add(p);
		}
		
		return new Result(probList, true);
	}

}

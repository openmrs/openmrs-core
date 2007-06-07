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

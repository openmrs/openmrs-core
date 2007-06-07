package org.openmrs.logic.rule;

import org.openmrs.Patient;
import org.openmrs.logic.LogicDataSource;
import org.openmrs.logic.Result;
import org.openmrs.logic.Rule;

public class CXREverRule extends Rule {

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient, Object[] args) {
		Result cxr = dataSource.eval(patient, "X-RAY, CHEST");
		return new Result(cxr.exists());
	}
	
}

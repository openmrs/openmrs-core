package org.openmrs.logic.rule;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.logic.LogicDataSource;
import org.openmrs.logic.Result;
import org.openmrs.logic.Rule;

public class PatientAlternateIdentifiersRule extends Rule {

	@Override
	public Result eval(LogicDataSource dataSource, Patient patient, Object[] args) {
		// First in list is preferred identifier (we want to skip it)
		List<PatientIdentifier> identifierList = patient.getActiveIdentifiers();
		if (identifierList.size() > 1) {
			Result result = null;
			for (int i = 1; i < identifierList.size(); i++) {
				if (result == null)
					result = new Result(identifierList.get(i).getIdentifier());
				else
					result.add(new Result(identifierList.get(i).getIdentifier()));
			}
			return result;
		} else {
			return Result.NULL_RESULT;
		}
	}

}

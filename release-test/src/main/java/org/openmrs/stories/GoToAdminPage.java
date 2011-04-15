package org.openmrs.stories;

import java.util.List;

import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.openmrs.Story;
import org.openmrs.steps.AdminSteps;
import org.openmrs.steps.LoginSteps;

public class GoToAdminPage extends Story {
	@Override
	public List<CandidateSteps> candidateSteps() {
		return new InstanceStepsFactory(configuration(),
				new LoginSteps(driver), new AdminSteps(driver))
				.createCandidateSteps();
	}
}

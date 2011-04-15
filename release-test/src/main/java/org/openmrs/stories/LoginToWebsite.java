package org.openmrs.stories;

import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.openmrs.Story;
import org.openmrs.steps.LoginSteps;

import java.util.List;

public class LoginToWebsite extends Story {

	@Override
	public List<CandidateSteps> candidateSteps() {
      	return new InstanceStepsFactory(configuration(),
				new LoginSteps(driver))
				.createCandidateSteps();
	}
}

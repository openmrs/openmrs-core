package org.openmrs.stories;

import java.util.List;

import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.openmrs.Story;
import org.openmrs.steps.FindPatientSteps;
import org.openmrs.steps.LoginSteps;


public class FindAPatient extends Story {
    @Override
    public List<CandidateSteps> candidateSteps() {
        return new InstanceStepsFactory(configuration(),
                new LoginSteps(driver),new FindPatientSteps(driver))
                .createCandidateSteps();
    }
}

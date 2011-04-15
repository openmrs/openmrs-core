package org.openmrs.stories;

import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.openmrs.Story;
import org.openmrs.steps.CreatePatientSteps;
import org.openmrs.steps.FindPatientSteps;
import org.openmrs.steps.LoginSteps;

import java.util.List;

public class CreateAPatient extends Story {
    @Override
    public List<CandidateSteps> candidateSteps()  {
        return new InstanceStepsFactory(configuration(),
                new LoginSteps(driver),new FindPatientSteps(driver),new CreatePatientSteps(driver))
                .createCandidateSteps();
    }
}

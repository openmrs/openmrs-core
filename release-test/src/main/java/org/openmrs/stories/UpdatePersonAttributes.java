package org.openmrs.stories;

import org.junit.Ignore;
import org.openmrs.Steps;
import org.openmrs.Story;
import org.openmrs.steps.AdminSteps;
import org.openmrs.steps.LoginSteps;
import org.openmrs.steps.UpdatePersonAttributesSteps;

import java.util.List;

import static java.util.Arrays.asList;

@Ignore //corresponding story definition need to be created
public class UpdatePersonAttributes extends Story {
    @Override
	public List<Steps> includeSteps() {
		return asList(new LoginSteps(driver), new AdminSteps(driver), new UpdatePersonAttributesSteps(driver));
	}
}

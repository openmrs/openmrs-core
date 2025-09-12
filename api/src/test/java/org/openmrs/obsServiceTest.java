package org.openmrs;

public class obsServiceTest {
import org.junit.jupiter.api.Test;
import org.openmrs.obs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.api.ObsService;
import static org.junit.jupiter.api.Assertions .*;

	public class ObsServiceTest {

		private ObsService obsService;

		@BeforeEach
		public void setup() {
			obsService = Context.getObsService();
		}

		@Test
		public void saveObs_shouldVoidOnlyOldObsWhenAllObsEditedAndNewObsAdded() {

			// Prepare old observations
			Obs oldObs = new Obs();
			oldObs.setVoided(false);
			obsService.saveObs(oldObs, null);

			// Simulate editing old observations
			oldObs.setDecodedValue("Updated Value");
			obsService.saveObs(oldObs, null);

			// Prepare new observations
			Obs newObs = new Obs();
			newObs.setVoided(false);
			newObs.setValue("New Value");
			obsService.saveObs(newObs, null);

			// Checking that only the old observation is voided
			assertTrue(oldObs.isVoided(), "Old observation should be voided");

			// Assert the new observation is not voided
			assertFalse(newObs.isVoided(), "New observation should not be voided");

			// Optionally, verify the state of other properties or more assertions as needed
		}
	}
}


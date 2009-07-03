package org.openmrs.web.controller.maintenance;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.Verifies;
import org.openmrs.web.test.BaseWebContextSensitiveTest;
import org.springframework.ui.ModelMap;

public class PatientsWhoAreUsersControllerTest extends BaseWebContextSensitiveTest {

	/**
     * @see {@link PatientsWhoAreUsersController#getListOfPatientsWhoAreUsers(ModelMap)}
     */
    @Test
    @Verifies(value = "should find users who are also patients", method = "getListOfPatientsWhoAreUsers(ModelMap)")
    public void getListOfPatientsWhoAreUsers_shouldFindUsersWhoAreAlsoPatients() throws Exception {
    	// sanity check to make sure this person doesn't exist yet: 
    	Assert.assertNull(Context.getPersonService().getPerson(777));
    	executeDataSet("org/openmrs/web/include/users-who-are-patients.xml");
    	
    	PatientsWhoAreUsersController controller = new PatientsWhoAreUsersController();
    	ModelMap model = new ModelMap();
    	controller.getListOfPatientsWhoAreUsers(model);
    	
    	Map<String, List<String>> map = (Map<String, List<String>>) model.get("usernameToPatientIdentifiers");
    	Assert.assertEquals(1, map.size());
    	Assert.assertTrue(map.containsKey("alsopatient"));
    	Collection<String> ids = map.values().iterator().next();
    	Assert.assertEquals(2, ids.size());
    	Assert.assertTrue(ids.contains("909"));
    	Assert.assertTrue(ids.contains("909-6"));
    }
}
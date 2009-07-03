package org.openmrs.web.controller.maintenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/admin/maintenance/patientsWhoAreUsers")
public class PatientsWhoAreUsersController {
	
	/**
	 * Find all users who are also patients. Assumes that user_id == person_id == patient_id
	 * 
	 * @param model
	 * 
	 * @should find users who are also patients
	 */
	@RequestMapping(method=RequestMethod.GET)
	public void getListOfPatientsWhoAreUsers(ModelMap model) {
		String sql =
			"select u.username, p.patient_id, p.identifier " +
			" from patient_identifier p inner join users u on p.patient_id = u.user_id " +
			" order by u.username asc ";
		List<List<Object>> data = Context.getAdministrationService().executeSQL(sql, true);
		
		// username -> list of patient identifiers
		Map<String, List<String>> usersWhoArePatients = new HashMap<String, List<String>>();
		for (List<Object> row : data) {
			String username = row.get(0).toString();
			String identifier = row.get(2).toString();
			List<String> holder = usersWhoArePatients.get(username);
			if (holder == null) {
				holder = new ArrayList<String>();
				usersWhoArePatients.put(username, holder);
			}
			holder.add(identifier);
		}
		model.put("usernameToPatientIdentifiers", usersWhoArePatients);
	}
	
}

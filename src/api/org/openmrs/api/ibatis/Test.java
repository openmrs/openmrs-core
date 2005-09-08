package org.openmrs.api.ibatis;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientName;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.context.Context;
import org.openmrs.context.ContextAuthenticationException;
import org.openmrs.context.ContextFactory;

public class Test {

	private Context context = ContextFactory.getContext();

	public void test() {

		// authenticate
		try {
			context.authenticate("3-4", "test");
		} catch (ContextAuthenticationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		System.out.println("authenticated = " + context.isAuthenticated());

		try {
			List patients = context.getPatientService()
				.getPatientByIdentifier("%1");
			for (Iterator i = patients.iterator(); i.hasNext(); ) {
				displayPatient((Patient)i.next());
			}
//			User user = context.getUserService().getUserByUsername("scooby-1");
//			createUser();
//			 deleteUser(user);
//			unvoidUser(user);
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void displayPatient(Patient patient) {
		System.out.println("Patient # " + patient.getPatientId());
		if (true) return;
		System.out.print("\tgender : " + patient.getGender());
		System.out.println("\tborn : " + patient.getBirthdate());
		List<PatientName> names = patient.getNames();
		for (Iterator i = names.iterator(); i.hasNext(); ) {
			System.out.println("\t"+(PatientName)i.next());
		}
		List<PatientIdentifier> ids = patient.getIdentifiers();
		for (Iterator i = ids.iterator(); i.hasNext(); ) {
			System.out.println("\t"+((PatientIdentifier)i.next()).getIdentifier());
		}
	}

	private void listRoles(User user) {
		System.out.println("Roles for " + user.getFirstName());
		List roles = user.getRoles();
		for (Iterator i = roles.iterator(); i.hasNext();) {
			Role r = (Role) i.next();
			System.out.println(r.getRole() + ": " + r.getDescription());
			List privileges = r.getPrivileges();
			for (Iterator j = privileges.iterator(); j.hasNext();) {
				Privilege p = (Privilege) j.next();
				System.out.println("\t" + p.getPrivilege() + ": "
						+ p.getDescription());
			}
		}
	}

	private User createUser() throws APIException {
		User user = new User();
		user.setUsername("scooby-1");
		user.setFirstName("Scooby");
		user.setMiddleName("Dooby");
		user.setLastName("Doo");
		user.setCreator(context.getAuthenticatedUser());
		user.setDateCreated(new Date());
		context.getUserService().createUser(user);
		return user;
	}
	
	private void voidUser(User user) throws APIException {
		context.getUserService().voidUser(user, "no good reason");	
	}
	
	private void unvoidUser(User user) throws APIException {
		context.getUserService().unvoidUser(user);
	}

	private void deleteUser(User user) throws APIException {
		context.getUserService().deleteUser(user);
	}

	/**
	 * @param arg
	 */
	public static void main(String[] arg) {

		new Test().test();

	}

}

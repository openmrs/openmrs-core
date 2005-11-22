package org.openmrs.util;

import java.util.List;

import org.hibernate.Session;
import org.openmrs.api.db.hibernate.HibernateUtil;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	    //final Log log = LogFactory.getLog("org.openmrs.util.Test");

		Session session = HibernateUtil.currentSession();
		List names = session.createQuery(
		"from Patient as p")
		.list();
		
		System.out.println(names.size() + "found");
		
//		Patient patient = (Patient) session.get(Patient.class, new Integer(5));
//		List names = session.createQuery(
//				"from PatientName as n where n.familyName = ?")
//				.setString(0,"Moo")
//				.list();
//		Set<PatientName> names = patient.getNames();
//		int count=0;
//		for (Iterator i = names.iterator(); i.hasNext(); ) {
//			PatientName name = (PatientName)i.next();
//			System.out.println((++count) + ": " + name.getGivenName() + " "
//					+ name.getFamilyName());
//		}
//		if (names.size() > 0) {
//			System.out.print("deleting...");
//			Transaction tx = session.beginTransaction();
//			session.delete(names.get(0));
//			tx.commit();
//			System.out.println("done");
//		}

		// Session session = HibernateUtil.currentSession();
		// Transaction tx = session.beginTransaction();
		// User user = (User) session.get(User.class, new Integer(1));
		// Patient patient = (Patient) session.get(Patient.class, new
		// Integer(5));
		// PatientName name = new PatientName();
		// name.setGivenName("Woo");
		// name.setFamiyName("Hoo");
		// name.setCreator(user);
		// name.setDateCreated(new Date());
		// patient.addName(name);
		// tx.commit();

		HibernateUtil.closeSession();

	}
}

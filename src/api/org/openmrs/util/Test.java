package org.openmrs.util;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.openmrs.api.hibernate.HibernateUtil;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ERROR);
		// Logger logger = Logger.getLogger(Test.class);
		// logger.setLevel(Level.INFO);

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
